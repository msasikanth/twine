/*
 * Copyright 2026 Sasikanth Miriyampalli
 *
 * Licensed under the GPL, Version 3.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.gnu.org/licenses/gpl-3.0.en.html
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package dev.sasikanth.rss.reader.addfeed

import androidx.compose.runtime.Stable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.sasikanth.rss.reader.core.model.local.FeedGroup
import dev.sasikanth.rss.reader.core.network.fetcher.FeedFetchResult
import dev.sasikanth.rss.reader.core.network.fetcher.FeedFetcher
import dev.sasikanth.rss.reader.core.network.utils.UrlUtils
import dev.sasikanth.rss.reader.data.repository.FeedAddResult
import dev.sasikanth.rss.reader.data.repository.RssRepository
import dev.sasikanth.rss.reader.data.sync.SyncCoordinator
import dev.sasikanth.rss.reader.exceptions.XmlParsingError
import dev.sasikanth.rss.reader.logging.CrashReporter
import io.ktor.client.network.sockets.ConnectTimeoutException
import io.ktor.client.network.sockets.SocketTimeoutException
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import me.tatarka.inject.annotations.Inject

@Stable
@Inject
class AddFeedViewModel(
  private val rssRepository: RssRepository,
  private val feedFetcher: FeedFetcher,
  private val syncCoordinator: SyncCoordinator,
) : ViewModel() {

  private val _state = MutableStateFlow(AddFeedState.DEFAULT)
  val state: StateFlow<AddFeedState>
    get() = _state

  private val _effects = MutableSharedFlow<AddFeedEffect>()
  val effects: SharedFlow<AddFeedEffect> = _effects.asSharedFlow()

  private val selectedGroupIds = MutableStateFlow(emptySet<String>())

  init {
    viewModelScope.launch {
      selectedGroupIds
        .flatMapLatest { groupIds ->
          if (groupIds.isEmpty()) {
            flowOf(emptyList())
          } else {
            rssRepository.groupsByIdsFlow(groupIds)
          }
        }
        .collect { groups -> _state.update { it.copy(selectedFeedGroups = groups.toSet()) } }
    }
  }

  fun dispatch(event: AddFeedEvent) {
    when (event) {
      is AddFeedEvent.AddFeedClicked -> addFeed(feedLink = event.feedLink, title = event.name)
      is AddFeedEvent.OnGroupsSelected -> onGroupsSelected(event.selectedGroupIds)
      is AddFeedEvent.FeedNameChanged -> {
        _state.update { it.copy(addedFeed = it.addedFeed?.copy(name = event.name)) }
      }
      is AddFeedEvent.ShowFavIconToggled -> showFavIconToggled(event.value)
      is AddFeedEvent.AlwaysFetchSourceArticleToggled ->
        alwaysFetchSourceArticleToggled(event.value)
      is AddFeedEvent.NotificationsToggled -> notificationsToggled(event.value)
      AddFeedEvent.DoneClicked -> doneClicked()
      is AddFeedEvent.OnRemoveGroupClicked -> onRemoveSelectedGroup(event.group)
      AddFeedEvent.MarkErrorAsShown -> {
        _state.update { it.copy(error = null) }
      }
      AddFeedEvent.MarkGoBackAsDone -> {
        _state.update { it.copy(goBack = false) }
      }
    }
  }

  private fun showFavIconToggled(value: Boolean) {
    val addedFeed = _state.value.addedFeed ?: return
    _state.update { it.copy(addedFeed = addedFeed.copy(showFeedFavIcon = value)) }
    viewModelScope.launch { rssRepository.updateFeedShowFavIcon(addedFeed.id, value) }
  }

  private fun alwaysFetchSourceArticleToggled(value: Boolean) {
    val addedFeed = _state.value.addedFeed ?: return
    _state.update { it.copy(addedFeed = addedFeed.copy(alwaysFetchSourceArticle = value)) }
    viewModelScope.launch { rssRepository.updateFeedAlwaysFetchSource(addedFeed.id, value) }
  }

  private fun notificationsToggled(value: Boolean) {
    val addedFeed = _state.value.addedFeed ?: return
    _state.update { it.copy(addedFeed = addedFeed.copy(enableNotifications = value)) }
    viewModelScope.launch { rssRepository.updateFeedEnableNotifications(addedFeed.id, value) }
  }

  private fun doneClicked() {
    val addedFeed = _state.value.addedFeed ?: return
    viewModelScope.launch {
      if (addedFeed.name.isNotBlank()) {
        rssRepository.updateFeedName(newFeedName = addedFeed.name, feedId = addedFeed.id)
      }
      syncCoordinator.triggerFeedPush()
      _state.update { it.copy(goBack = true) }
    }
  }

  private fun onRemoveSelectedGroup(group: FeedGroup) {
    viewModelScope.launch {
      val feedId = _state.value.addedFeed?.id
      selectedGroupIds.value = emptySet()

      if (feedId != null) {
        rssRepository.removeFeedIdsFromGroups(groupIds = setOf(group.id), feedIds = listOf(feedId))
      }
    }
  }

  private fun onGroupsSelected(groupIds: Set<String>) {
    viewModelScope.launch {
      val feedId = _state.value.addedFeed?.id
      val previousGroupIds = selectedGroupIds.value
      selectedGroupIds.value = groupIds

      if (feedId != null) {
        val removedGroupIds = previousGroupIds - groupIds
        if (removedGroupIds.isNotEmpty()) {
          rssRepository.removeFeedIdsFromGroups(
            groupIds = removedGroupIds,
            feedIds = listOf(feedId),
          )
        }

        rssRepository.addFeedIdsToGroups(groupIds = groupIds, feedIds = listOf(feedId))
      }
    }
  }

  private fun addFeed(feedLink: String, title: String?) {
    if (feedLink.isBlank()) return

    viewModelScope.launch {
      _state.update { it.copy(feedFetchingState = FeedFetchingState.Loading) }

      try {
        when (val feedFetchResult = feedFetcher.fetch(url = feedLink)) {
          is FeedFetchResult.Error -> handleNetworkErrors(feedFetchResult, feedLink)
          is FeedFetchResult.HttpStatusError -> handleHttpStatusErrors(feedFetchResult)
          is FeedFetchResult.TooManyRedirects -> {
            _state.update { it.copy(error = AddFeedErrorType.TooManyRedirects) }
          }
          is FeedFetchResult.Success -> {
            val feedPayload = feedFetchResult.feedPayload
            try {
              val showWebsiteFavIcon = !UrlUtils.prefersFeedProvidedIcon(feedPayload.link)
              val feedId =
                rssRepository.upsertFeedWithPosts(
                  feedPayload = feedPayload,
                  title = title,
                  showWebsiteFavIcon = showWebsiteFavIcon,
                )
              _state.update {
                it.copy(
                  addedFeed =
                    AddedFeed(
                      id = feedId,
                      name = if (title.isNullOrBlank()) feedPayload.name else title,
                      icon = feedPayload.icon,
                      homepageLink = feedPayload.homepageLink,
                      showFeedFavIcon = showWebsiteFavIcon,
                      alwaysFetchSourceArticle = false,
                      enableNotifications = true,
                    )
                )
              }
              _effects.emit(AddFeedEffect.RequestInAppRating)
            } catch (e: Exception) {
              handleDatabaseErrors(FeedAddResult.DatabaseError(e), feedLink)
            }
          }
        }
      } catch (e: Exception) {
        CrashReporter.setCustomValue(section = "AddingFeed", key = "feed_url", value = feedLink)
        CrashReporter.log(e)
        _state.update { it.copy(error = AddFeedErrorType.Unknown(e)) }
      } finally {
        _state.update { it.copy(feedFetchingState = FeedFetchingState.Idle) }
      }
    }
  }

  private fun handleNetworkErrors(error: FeedFetchResult.Error, feedLink: String) {
    when (error.exception) {
      is UnsupportedOperationException -> {
        _state.update { it.copy(error = AddFeedErrorType.UnknownFeedType) }
      }
      is XmlParsingError -> {
        CrashReporter.setCustomValue("AddingFeed", key = "feed_url", value = feedLink)
        CrashReporter.log(error.exception)
        _state.update { it.copy(error = AddFeedErrorType.FailedToParseXML) }
      }
      is ConnectTimeoutException,
      is SocketTimeoutException -> {
        _state.update { it.copy(error = AddFeedErrorType.Timeout) }
      }
      else -> {
        CrashReporter.setCustomValue("AddingFeed", key = "feed_url", value = feedLink)
        CrashReporter.log(error.exception)
        _state.update { it.copy(error = AddFeedErrorType.Unknown(error.exception)) }
      }
    }
  }

  private fun handleHttpStatusErrors(error: FeedFetchResult.HttpStatusError) {
    when (val statusCode = error.statusCode) {
      HttpStatusCode.BadRequest,
      HttpStatusCode.Unauthorized,
      HttpStatusCode.PaymentRequired,
      HttpStatusCode.Forbidden -> {
        _state.update { it.copy(error = AddFeedErrorType.UnAuthorized(statusCode)) }
      }
      HttpStatusCode.NotFound -> {
        _state.update { it.copy(error = AddFeedErrorType.FeedNotFound(statusCode)) }
      }
      HttpStatusCode.InternalServerError,
      HttpStatusCode.NotImplemented,
      HttpStatusCode.BadGateway,
      HttpStatusCode.ServiceUnavailable,
      HttpStatusCode.GatewayTimeout -> {
        _state.update { it.copy(error = AddFeedErrorType.ServerError(statusCode)) }
      }
      else -> {
        _state.update { it.copy(error = AddFeedErrorType.UnknownHttpStatusError(statusCode)) }
      }
    }
  }

  private fun handleDatabaseErrors(error: FeedAddResult.DatabaseError, feedLink: String) {
    CrashReporter.setCustomValue("AddingFeed", key = "feed_url", value = feedLink)
    CrashReporter.log(error.exception)
  }
}
