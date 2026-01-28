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

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.touchlab.crashkios.bugsnag.BugsnagKotlin
import dev.sasikanth.rss.reader.core.model.local.FeedGroup
import dev.sasikanth.rss.reader.core.network.fetcher.FeedFetchResult
import dev.sasikanth.rss.reader.core.network.fetcher.FeedFetcher
import dev.sasikanth.rss.reader.data.repository.FeedAddResult
import dev.sasikanth.rss.reader.data.repository.RssRepository
import dev.sasikanth.rss.reader.exceptions.XmlParsingError
import dev.sasikanth.rss.reader.utils.InAppRating
import io.ktor.client.network.sockets.ConnectTimeoutException
import io.ktor.client.network.sockets.SocketTimeoutException
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import me.tatarka.inject.annotations.Inject

@Inject
class AddFeedViewModel(
  private val rssRepository: RssRepository,
  private val feedFetcher: FeedFetcher,
  private val inAppRating: InAppRating,
) : ViewModel() {

  private val _state = MutableStateFlow(AddFeedState.DEFAULT)
  val state: StateFlow<AddFeedState>
    get() = _state

  fun dispatch(event: AddFeedEvent) {
    when (event) {
      is AddFeedEvent.AddFeedClicked ->
        addFeed(
          feedLink = event.feedLink,
          title = event.name,
          groups = _state.value.selectedFeedGroups
        )
      is AddFeedEvent.OnGroupsSelected -> onGroupsSelected(event.selectedGroupIds)
      is AddFeedEvent.OnRemoveGroupClicked -> onRemoveSelectedGroup(event.group)
      AddFeedEvent.MarkErrorAsShown -> {
        _state.update { it.copy(error = null) }
      }
      AddFeedEvent.MarkGoBackAsDone -> {
        _state.update { it.copy(goBack = false) }
      }
      is AddFeedEvent.OnAlwaysFetchSourceArticleChanged -> {
        _state.update { it.copy(alwaysFetchSourceArticle = event.newValue) }
      }
      is AddFeedEvent.OnShowFeedFavIconChanged -> {
        _state.update { it.copy(showFeedFavIcon = event.newValue) }
      }
    }
  }

  private fun onRemoveSelectedGroup(group: FeedGroup) {
    viewModelScope.launch { _state.update { it.copy(selectedFeedGroups = emptySet()) } }
  }

  private fun onGroupsSelected(selectedGroupIds: Set<String>) {
    viewModelScope.launch {
      val feedGroups = rssRepository.groupByIds(selectedGroupIds)
      _state.update { it.copy(selectedFeedGroups = feedGroups.toSet()) }
    }
  }

  private fun addFeed(
    feedLink: String,
    title: String?,
    groups: Set<FeedGroup>,
  ) {
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
              val feedId =
                rssRepository.upsertFeedWithPosts(
                  feedPayload = feedPayload,
                  title = title,
                  alwaysFetchSourceArticle = _state.value.alwaysFetchSourceArticle,
                  showWebsiteFavIcon = _state.value.showFeedFavIcon,
                )
              rssRepository.addFeedIdsToGroups(
                groupIds = groups.map { it.id }.toSet(),
                feedIds = listOf(feedId)
              )
            } catch (e: Exception) {
              handleDatabaseErrors(FeedAddResult.DatabaseError(e), feedLink)
            }

            _state.update { it.copy(goBack = true) }
            inAppRating.request()
          }
        }
      } catch (e: Exception) {
        BugsnagKotlin.setCustomValue(section = "AddingFeed", key = "feed_url", value = feedLink)
        BugsnagKotlin.sendHandledException(e)
        _state.update { it.copy(error = AddFeedErrorType.Unknown(e)) }
      } finally {
        _state.update { it.copy(feedFetchingState = FeedFetchingState.Idle) }
      }
    }
  }

  private fun handleNetworkErrors(
    error: FeedFetchResult.Error,
    feedLink: String,
  ) {
    when (error.exception) {
      is UnsupportedOperationException -> {
        _state.update { it.copy(error = AddFeedErrorType.UnknownFeedType) }
      }
      is XmlParsingError -> {
        BugsnagKotlin.setCustomValue("AddingFeed", key = "feed_url", value = feedLink)
        BugsnagKotlin.sendHandledException(error.exception)
        _state.update { it.copy(error = AddFeedErrorType.FailedToParseXML) }
      }
      is ConnectTimeoutException,
      is SocketTimeoutException, -> {
        _state.update { it.copy(error = AddFeedErrorType.Timeout) }
      }
      else -> {
        BugsnagKotlin.setCustomValue("AddingFeed", key = "feed_url", value = feedLink)
        BugsnagKotlin.sendHandledException(error.exception)
        _state.update { it.copy(error = AddFeedErrorType.Unknown(error.exception)) }
      }
    }
  }

  private fun handleHttpStatusErrors(error: FeedFetchResult.HttpStatusError) {
    when (val statusCode = error.statusCode) {
      HttpStatusCode.BadRequest,
      HttpStatusCode.Unauthorized,
      HttpStatusCode.PaymentRequired,
      HttpStatusCode.Forbidden, -> {
        _state.update { it.copy(error = AddFeedErrorType.UnAuthorized(statusCode)) }
      }
      HttpStatusCode.NotFound -> {
        _state.update { it.copy(error = AddFeedErrorType.FeedNotFound(statusCode)) }
      }
      HttpStatusCode.InternalServerError,
      HttpStatusCode.NotImplemented,
      HttpStatusCode.BadGateway,
      HttpStatusCode.ServiceUnavailable,
      HttpStatusCode.GatewayTimeout, -> {
        _state.update { it.copy(error = AddFeedErrorType.ServerError(statusCode)) }
      }
      else -> {
        _state.update { it.copy(error = AddFeedErrorType.UnknownHttpStatusError(statusCode)) }
      }
    }
  }

  private fun handleDatabaseErrors(error: FeedAddResult.DatabaseError, feedLink: String) {
    BugsnagKotlin.setCustomValue("AddingFeed", key = "feed_url", value = feedLink)
    BugsnagKotlin.sendHandledException(error.exception)
  }
}
