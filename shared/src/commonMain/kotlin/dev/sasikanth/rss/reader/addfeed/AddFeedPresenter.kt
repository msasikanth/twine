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

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.essenty.instancekeeper.InstanceKeeper
import com.arkivanov.essenty.instancekeeper.getOrCreate
import dev.sasikanth.rss.reader.core.model.local.FeedGroup
import dev.sasikanth.rss.reader.data.repository.FeedAddResult
import dev.sasikanth.rss.reader.data.repository.RssRepository
import dev.sasikanth.rss.reader.exceptions.XmlParsingError
import dev.sasikanth.rss.reader.util.DispatchersProvider
import io.ktor.client.network.sockets.ConnectTimeoutException
import io.ktor.client.network.sockets.SocketTimeoutException
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import me.tatarka.inject.annotations.Assisted
import me.tatarka.inject.annotations.Inject

internal typealias AddFeedPresenterFactory =
  (ComponentContext, goBack: () -> Unit, openGroupSelection: () -> Unit) -> AddFeedPresenter

@Inject
class AddFeedPresenter(
  dispatchersProvider: DispatchersProvider,
  rssRepository: RssRepository,
  @Assisted componentContext: ComponentContext,
  @Assisted private val goBack: () -> Unit,
  @Assisted private val openGroupSelection: () -> Unit,
) : ComponentContext by componentContext {

  private val presenterInstance =
    instanceKeeper.getOrCreate {
      PresenterInstance(
        dispatchersProvider = dispatchersProvider,
        rssRepository = rssRepository,
      )
    }

  internal val state = presenterInstance.state
  internal val effects = presenterInstance.effects.asSharedFlow()

  fun dispatch(event: AddFeedEvent) {
    when (event) {
      AddFeedEvent.BackClicked -> goBack()
      AddFeedEvent.OnGroupDropdownClicked -> openGroupSelection()
      else -> {
        // no-op
      }
    }

    presenterInstance.dispatch(event)
  }

  private class PresenterInstance(
    dispatchersProvider: DispatchersProvider,
    private val rssRepository: RssRepository,
  ) : InstanceKeeper.Instance {

    private val coroutineScope = CoroutineScope(SupervisorJob() + dispatchersProvider.main)

    private val _state = MutableStateFlow(AddFeedState.DEFAULT)
    val state: StateFlow<AddFeedState> =
      _state.stateIn(
        scope = coroutineScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = AddFeedState.DEFAULT
      )

    val effects = MutableSharedFlow<AddFeedEffect>()

    fun dispatch(event: AddFeedEvent) {
      when (event) {
        is AddFeedEvent.AddFeedClicked ->
          addFeed(
            feedLink = event.feedLink,
            title = event.name,
            groups = _state.value.selectedFeedGroups
          )
        AddFeedEvent.BackClicked -> {
          // no-op
        }
        AddFeedEvent.OnGroupDropdownClicked -> {
          // no-op
        }
        is AddFeedEvent.OnGroupsSelected -> onGroupsSelected(event.selectedGroupIds)
        is AddFeedEvent.OnRemoveGroupClicked -> onRemoveSelectedGroup(event.group)
      }
    }

    private fun onRemoveSelectedGroup(group: FeedGroup) {
      coroutineScope.launch {
        val selectedGroups = _state.value.selectedFeedGroups
        _state.update { it.copy(selectedFeedGroups = selectedGroups - group) }
      }
    }

    private fun onGroupsSelected(selectedGroupIds: Set<String>) {
      coroutineScope.launch {
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

      coroutineScope.launch {
        _state.update { it.copy(feedFetchingState = FeedFetchingState.Loading) }
        try {
          when (val feedAddResult = rssRepository.fetchAndAddFeed(feedLink, title)) {
            is FeedAddResult.DatabaseError -> handleDatabaseErrors(feedAddResult, feedLink)
            is FeedAddResult.HttpStatusError -> handleHttpStatusErrors(feedAddResult)
            is FeedAddResult.NetworkError -> handleNetworkErrors(feedAddResult, feedLink)
            FeedAddResult.TooManyRedirects -> {
              effects.emit(AddFeedEffect.ShowError(AddFeedErrorType.TooManyRedirects))
            }
            is FeedAddResult.Success -> {
              rssRepository.addFeedIdsToGroups(
                groupIds = groups.map { it.id }.toSet(),
                feedIds = listOf(feedAddResult.feedId)
              )

              effects.emit(AddFeedEffect.GoBack)
            }
          }
        } catch (e: Exception) {
          effects.emit(AddFeedEffect.ShowError(AddFeedErrorType.Unknown(e)))
        } finally {
          _state.update { it.copy(feedFetchingState = FeedFetchingState.Idle) }
        }
      }
    }

    private suspend fun handleNetworkErrors(
      feedAddResult: FeedAddResult.NetworkError,
      feedLink: String
    ) {
      when (feedAddResult.exception) {
        is UnsupportedOperationException -> {
          effects.emit(AddFeedEffect.ShowError(AddFeedErrorType.UnknownFeedType))
        }
        is XmlParsingError -> {
          effects.emit(AddFeedEffect.ShowError(AddFeedErrorType.FailedToParseXML))
        }
        is ConnectTimeoutException,
        is SocketTimeoutException -> {
          effects.emit(AddFeedEffect.ShowError(AddFeedErrorType.Timeout))
        }
        else -> {
          effects.emit(AddFeedEffect.ShowError(AddFeedErrorType.Unknown(feedAddResult.exception)))
        }
      }
    }

    private suspend fun handleHttpStatusErrors(httpStatusError: FeedAddResult.HttpStatusError) {
      when (val statusCode = httpStatusError.statusCode) {
        HttpStatusCode.BadRequest,
        HttpStatusCode.Unauthorized,
        HttpStatusCode.PaymentRequired,
        HttpStatusCode.Forbidden -> {
          effects.emit(AddFeedEffect.ShowError(AddFeedErrorType.UnAuthorized(statusCode)))
        }
        HttpStatusCode.NotFound -> {
          effects.emit(AddFeedEffect.ShowError(AddFeedErrorType.FeedNotFound(statusCode)))
        }
        HttpStatusCode.InternalServerError,
        HttpStatusCode.NotImplemented,
        HttpStatusCode.BadGateway,
        HttpStatusCode.ServiceUnavailable,
        HttpStatusCode.GatewayTimeout -> {
          effects.emit(AddFeedEffect.ShowError(AddFeedErrorType.ServerError(statusCode)))
        }
        else -> {
          effects.emit(AddFeedEffect.ShowError(AddFeedErrorType.UnknownHttpStatusError(statusCode)))
        }
      }
    }

    private fun handleDatabaseErrors(databaseError: FeedAddResult.DatabaseError, feedLink: String) {

    }

    override fun onDestroy() {
      coroutineScope.cancel()
    }
  }
}
