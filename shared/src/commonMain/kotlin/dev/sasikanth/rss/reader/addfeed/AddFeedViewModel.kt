/*
 * Copyright 2024 Sasikanth Miriyampalli
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package dev.sasikanth.rss.reader.addfeed

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.touchlab.crashkios.bugsnag.BugsnagKotlin
import dev.sasikanth.rss.reader.core.model.local.FeedGroup
import dev.sasikanth.rss.reader.data.repository.FeedAddResult
import dev.sasikanth.rss.reader.data.repository.RssRepository
import dev.sasikanth.rss.reader.exceptions.XmlParsingError
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
    }
  }

  private fun onRemoveSelectedGroup(group: FeedGroup) {
    viewModelScope.launch {
      val selectedGroups = _state.value.selectedFeedGroups
      _state.update { it.copy(selectedFeedGroups = selectedGroups - group) }
    }
  }

  private fun onGroupsSelected(selectedGroupIds: Set<String>) {
    viewModelScope.launch {
      val feedGroups = rssRepository.groupByIds(selectedGroupIds)
      _state.update {
        it.copy(selectedFeedGroups = _state.value.selectedFeedGroups + feedGroups.toSet())
      }
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
        when (val feedAddResult = rssRepository.fetchAndAddFeed(feedLink, title)) {
          is FeedAddResult.DatabaseError -> handleDatabaseErrors(feedAddResult, feedLink)
          is FeedAddResult.HttpStatusError -> handleHttpStatusErrors(feedAddResult)
          is FeedAddResult.NetworkError -> handleNetworkErrors(feedAddResult, feedLink)
          FeedAddResult.TooManyRedirects -> {
            _state.update { it.copy(error = AddFeedErrorType.TooManyRedirects) }
          }
          is FeedAddResult.Success -> {
            rssRepository.addFeedIdsToGroups(
              groupIds = groups.map { it.id }.toSet(),
              feedIds = listOf(feedAddResult.feedId)
            )

            _state.update { it.copy(goBack = true) }
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

  private suspend fun handleNetworkErrors(
    feedAddResult: FeedAddResult.NetworkError,
    feedLink: String,
  ) {
    when (feedAddResult.exception) {
      is UnsupportedOperationException -> {
        _state.update { it.copy(error = AddFeedErrorType.UnknownFeedType) }
      }
      is XmlParsingError -> {
        BugsnagKotlin.setCustomValue("AddingFeed", key = "feed_url", value = feedLink)
        BugsnagKotlin.sendHandledException(feedAddResult.exception)
        _state.update { it.copy(error = AddFeedErrorType.FailedToParseXML) }
      }
      is ConnectTimeoutException,
      is SocketTimeoutException, -> {
        _state.update { it.copy(error = AddFeedErrorType.Timeout) }
      }
      else -> {
        BugsnagKotlin.setCustomValue("AddingFeed", key = "feed_url", value = feedLink)
        BugsnagKotlin.sendHandledException(feedAddResult.exception)
        _state.update { it.copy(error = AddFeedErrorType.Unknown(feedAddResult.exception)) }
      }
    }
  }

  private suspend fun handleHttpStatusErrors(httpStatusError: FeedAddResult.HttpStatusError) {
    when (val statusCode = httpStatusError.statusCode) {
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

  private fun handleDatabaseErrors(databaseError: FeedAddResult.DatabaseError, feedLink: String) {
    BugsnagKotlin.setCustomValue("AddingFeed", key = "feed_url", value = feedLink)
    BugsnagKotlin.sendHandledException(databaseError.exception)
  }
}
