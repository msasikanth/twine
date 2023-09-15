/*
 * Copyright 2023 Sasikanth Miriyampalli
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
@file:OptIn(ExperimentalMaterialApi::class)

package dev.sasikanth.rss.reader.home

import androidx.compose.material.ExperimentalMaterialApi
import app.cash.paging.Pager
import app.cash.paging.PagingConfig
import app.cash.paging.cachedIn
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.childContext
import com.arkivanov.essenty.backhandler.BackCallback
import com.arkivanov.essenty.instancekeeper.InstanceKeeper
import com.arkivanov.essenty.instancekeeper.getOrCreate
import com.arkivanov.essenty.lifecycle.doOnCreate
import dev.sasikanth.rss.reader.components.bottomsheet.BottomSheetValue
import dev.sasikanth.rss.reader.feeds.FeedsPresenter
import dev.sasikanth.rss.reader.feeds.ui.FeedsSheetMode.Default
import dev.sasikanth.rss.reader.feeds.ui.FeedsSheetMode.Edit
import dev.sasikanth.rss.reader.feeds.ui.FeedsSheetMode.LinkEntry
import dev.sasikanth.rss.reader.models.local.PostWithMetadata
import dev.sasikanth.rss.reader.repository.FeedAddResult
import dev.sasikanth.rss.reader.repository.RssRepository
import dev.sasikanth.rss.reader.utils.DispatchersProvider
import dev.sasikanth.rss.reader.utils.ObservableSelectedFeed
import dev.sasikanth.rss.reader.utils.XmlParsingError
import io.ktor.client.network.sockets.ConnectTimeoutException
import io.ktor.client.network.sockets.SocketTimeoutException
import io.ktor.http.HttpStatusCode
import io.sentry.kotlin.multiplatform.Sentry
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import me.tatarka.inject.annotations.Assisted
import me.tatarka.inject.annotations.Inject

@Inject
@OptIn(ExperimentalCoroutinesApi::class)
class HomePresenter(
  dispatchersProvider: DispatchersProvider,
  feedsPresenterFactory: (ComponentContext) -> FeedsPresenter,
  private val rssRepository: RssRepository,
  private val observableSelectedFeed: ObservableSelectedFeed,
  @Assisted componentContext: ComponentContext,
  @Assisted private val openSearch: () -> Unit,
  @Assisted private val openBookmarks: () -> Unit,
  @Assisted private val openSettings: () -> Unit,
) : ComponentContext by componentContext {

  private val presenterInstance =
    instanceKeeper.getOrCreate {
      PresenterInstance(
        dispatchersProvider = dispatchersProvider,
        rssRepository = rssRepository,
        observableSelectedFeed = observableSelectedFeed,
      )
    }

  val state = presenterInstance.state
  val effects = presenterInstance.effects.asSharedFlow()

  private val backCallback = BackCallback { dispatch(HomeEvent.BackClicked) }

  internal val feedsPresenter = feedsPresenterFactory(childContext("feeds_presenter"))

  init {
    lifecycle.doOnCreate {
      presenterInstance.dispatch(HomeEvent.Init)
      backHandler.register(backCallback)
      backCallback.isEnabled = state.value.feedsSheetState == BottomSheetValue.Expanded
    }
  }

  fun dispatch(event: HomeEvent) {
    when (event) {
      is HomeEvent.FeedsSheetStateChanged -> {
        backCallback.isEnabled = event.feedsSheetState == BottomSheetValue.Expanded
      }
      is HomeEvent.SearchClicked -> openSearch()
      is HomeEvent.BookmarksClicked -> openBookmarks()
      is HomeEvent.SettingsClicked -> openSettings()
      else -> {
        // no-op
      }
    }
    presenterInstance.dispatch(event)
  }

  private class PresenterInstance(
    dispatchersProvider: DispatchersProvider,
    private val rssRepository: RssRepository,
    private val observableSelectedFeed: ObservableSelectedFeed,
  ) : InstanceKeeper.Instance {

    private val coroutineScope = CoroutineScope(SupervisorJob() + dispatchersProvider.main)

    private val _state = MutableStateFlow(HomeState.DEFAULT)
    val state: StateFlow<HomeState> =
      _state.stateIn(
        scope = coroutineScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = HomeState.DEFAULT
      )

    val effects = MutableSharedFlow<HomeEffect>(extraBufferCapacity = 10)

    fun dispatch(event: HomeEvent) {
      when (event) {
        HomeEvent.Init -> init()
        HomeEvent.OnSwipeToRefresh -> refreshContent()
        is HomeEvent.OnPostClicked -> onPostClicked(event.post)
        is HomeEvent.FeedsSheetStateChanged -> feedsSheetStateChanged(event.feedsSheetState)
        HomeEvent.OnHomeSelected -> onHomeSelected()
        HomeEvent.OnAddFeedClicked -> onAddFeedClicked()
        HomeEvent.OnCancelAddFeedClicked -> onCancelAddFeedClicked()
        is HomeEvent.AddFeed -> addFeed(event.feedLink)
        HomeEvent.OnPrimaryActionClicked -> onPrimaryActionClicked()
        HomeEvent.BackClicked -> backClicked()
        HomeEvent.SearchClicked -> {
          /* no-op */
        }
        is HomeEvent.OnPostBookmarkClick -> onPostBookmarkClicked(event.post)
        HomeEvent.BookmarksClicked -> {
          /* no-op */
        }
        HomeEvent.SettingsClicked -> {
          /* no-op */
        }
        HomeEvent.EditFeedsClicked -> editFeedsClicked()
        HomeEvent.ExitFeedsEdit -> exitFeedsEdit()
      }
    }

    private fun exitFeedsEdit() {
      _state.update { it.copy(feedsSheetMode = Default) }
    }

    private fun editFeedsClicked() {
      _state.update { it.copy(feedsSheetMode = Edit) }
    }

    private fun onPostBookmarkClicked(post: PostWithMetadata) {
      coroutineScope.launch {
        rssRepository.updateBookmarkStatus(bookmarked = !post.bookmarked, link = post.link)
      }
    }

    private fun backClicked() {
      coroutineScope.launch {
        when (state.value.feedsSheetMode) {
          Default,
          LinkEntry -> {
            effects.emit(HomeEffect.MinimizeSheet)
          }
          Edit -> {
            _state.update { it.copy(feedsSheetMode = Default) }
          }
        }
      }
    }

    private fun init() {
      observableSelectedFeed.selectedFeed
        .onEach { selectedFeed -> _state.update { it.copy(selectedFeed = selectedFeed) } }
        .onEach { selectedFeed ->
          val posts =
            Pager<Int, PostWithMetadata>(PagingConfig(pageSize = 20)) {
                rssRepository.posts(selectedFeedLink = selectedFeed?.link)
              }
              .flow
              .cachedIn(coroutineScope)

          _state.update { it.copy(posts = posts) }
        }
        .flatMapLatest { selectedFeed ->
          rssRepository.featuredPosts(selectedFeedLink = selectedFeed?.link)
        }
        .onEach { featuredPosts ->
          _state.update { it.copy(featuredPosts = featuredPosts.toImmutableList()) }
        }
        .launchIn(coroutineScope)
    }

    private fun onPrimaryActionClicked() {
      if (_state.value.feedsSheetState == BottomSheetValue.Collapsed) {
        dispatch(HomeEvent.OnHomeSelected)
      } else {
        dispatch(HomeEvent.OnAddFeedClicked)
      }
    }

    private fun addFeed(feedLink: String) {
      coroutineScope.launch {
        _state.update { it.copy(feedFetchingState = FeedFetchingState.Loading) }
        try {
          when (val feedAddResult = rssRepository.addFeed(feedLink)) {
            is FeedAddResult.DatabaseError -> handleDatabaseErrors(feedAddResult, feedLink)
            is FeedAddResult.HttpStatusError -> handleHttpStatusErrors(feedAddResult)
            is FeedAddResult.NetworkError -> handleNetworkErrors(feedAddResult, feedLink)
            FeedAddResult.TooManyRedirects -> {
              effects.emit(HomeEffect.ShowError(HomeErrorType.TooManyRedirects))
            }
            FeedAddResult.Success -> {
              // no-op
            }
          }
        } catch (e: Exception) {
          Sentry.captureException(e) { scope -> scope.setContext("feed_url", feedLink) }
          effects.emit(HomeEffect.ShowError(HomeErrorType.Unknown(e)))
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
          effects.emit(HomeEffect.ShowError(HomeErrorType.UnknownFeedType))
        }
        is XmlParsingError -> {
          Sentry.captureException(feedAddResult.exception) { scope ->
            scope.setContext("feed_url", feedLink)
          }
          effects.emit(HomeEffect.ShowError(HomeErrorType.FailedToParseXML))
        }
        is ConnectTimeoutException,
        is SocketTimeoutException -> {
          effects.emit(HomeEffect.ShowError(HomeErrorType.Timeout))
        }
        else -> {
          Sentry.captureException(feedAddResult.exception) { scope ->
            scope.setContext("feed_url", feedLink)
          }
          effects.emit(HomeEffect.ShowError(HomeErrorType.Unknown(feedAddResult.exception)))
        }
      }
    }

    private suspend fun handleHttpStatusErrors(httpStatusError: FeedAddResult.HttpStatusError) {
      when (val statusCode = httpStatusError.statusCode) {
        HttpStatusCode.BadRequest,
        HttpStatusCode.Unauthorized,
        HttpStatusCode.PaymentRequired,
        HttpStatusCode.Forbidden -> {
          effects.emit(HomeEffect.ShowError(HomeErrorType.UnAuthorized(statusCode)))
        }
        HttpStatusCode.NotFound -> {
          effects.emit(HomeEffect.ShowError(HomeErrorType.FeedNotFound(statusCode)))
        }
        HttpStatusCode.InternalServerError,
        HttpStatusCode.NotImplemented,
        HttpStatusCode.BadGateway,
        HttpStatusCode.ServiceUnavailable,
        HttpStatusCode.GatewayTimeout -> {
          effects.emit(HomeEffect.ShowError(HomeErrorType.ServerError(statusCode)))
        }
        else -> {
          effects.emit(HomeEffect.ShowError(HomeErrorType.UnknownHttpStatusError(statusCode)))
        }
      }
    }

    private fun handleDatabaseErrors(databaseError: FeedAddResult.DatabaseError, feedLink: String) {
      Sentry.captureException(databaseError.exception) { scope ->
        scope.setContext("feed_url", feedLink)
      }
    }

    private fun feedsSheetStateChanged(feedsSheetState: BottomSheetValue) {
      _state.update { it.copy(feedsSheetState = feedsSheetState) }
    }

    private fun onCancelAddFeedClicked() {
      _state.update { it.copy(feedsSheetMode = Default) }
    }

    private fun onAddFeedClicked() {
      _state.update { it.copy(feedsSheetMode = LinkEntry) }
    }

    private fun onHomeSelected() {
      coroutineScope.launch { observableSelectedFeed.clearSelection() }
    }

    private fun onPostClicked(post: PostWithMetadata) {
      coroutineScope.launch { effects.emit(HomeEffect.OpenPost(post)) }
    }

    private fun refreshContent() {
      coroutineScope.launch {
        _state.update { it.copy(loadingState = HomeLoadingState.Loading) }
        try {
          rssRepository.updateFeeds()
        } catch (e: Exception) {
          Sentry.captureException(e)
        } finally {
          _state.update { it.copy(loadingState = HomeLoadingState.Idle) }
        }
      }
    }

    override fun onDestroy() {
      coroutineScope.cancel()
    }
  }
}
