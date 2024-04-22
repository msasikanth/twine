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
import app.cash.paging.cachedIn
import app.cash.paging.createPager
import app.cash.paging.createPagingConfig
import co.touchlab.crashkios.bugsnag.BugsnagKotlin
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.childContext
import com.arkivanov.essenty.backhandler.BackCallback
import com.arkivanov.essenty.instancekeeper.InstanceKeeper
import com.arkivanov.essenty.instancekeeper.getOrCreate
import com.arkivanov.essenty.lifecycle.doOnCreate
import dev.sasikanth.rss.reader.components.bottomsheet.BottomSheetValue
import dev.sasikanth.rss.reader.core.model.local.Feed
import dev.sasikanth.rss.reader.core.model.local.FeedGroup
import dev.sasikanth.rss.reader.core.model.local.PostWithMetadata
import dev.sasikanth.rss.reader.exceptions.XmlParsingError
import dev.sasikanth.rss.reader.feeds.FeedsEvent
import dev.sasikanth.rss.reader.feeds.FeedsPresenter
import dev.sasikanth.rss.reader.home.ui.PostsType
import dev.sasikanth.rss.reader.repository.FeedAddResult
import dev.sasikanth.rss.reader.repository.ObservableActiveSource
import dev.sasikanth.rss.reader.repository.RssRepository
import dev.sasikanth.rss.reader.repository.SettingsRepository
import dev.sasikanth.rss.reader.util.DispatchersProvider
import dev.sasikanth.rss.reader.utils.getLast24HourStart
import dev.sasikanth.rss.reader.utils.getTodayStartInstant
import io.ktor.client.network.sockets.ConnectTimeoutException
import io.ktor.client.network.sockets.SocketTimeoutException
import io.ktor.http.HttpStatusCode
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
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.Instant
import me.tatarka.inject.annotations.Assisted
import me.tatarka.inject.annotations.Inject

internal typealias HomePresenterFactory =
  (
    ComponentContext,
    openSearch: () -> Unit,
    openBookmarks: () -> Unit,
    openSettings: () -> Unit,
    openPost: (PostWithMetadata) -> Unit,
    openGroupSelectionSheet: () -> Unit,
  ) -> HomePresenter

@Inject
@OptIn(ExperimentalCoroutinesApi::class)
class HomePresenter(
  dispatchersProvider: DispatchersProvider,
  feedsPresenterFactory: (ComponentContext, openGroupSelectionSheet: () -> Unit) -> FeedsPresenter,
  private val rssRepository: RssRepository,
  private val observableActiveSource: ObservableActiveSource,
  private val settingsRepository: SettingsRepository,
  @Assisted componentContext: ComponentContext,
  @Assisted private val openSearch: () -> Unit,
  @Assisted private val openBookmarks: () -> Unit,
  @Assisted private val openSettings: () -> Unit,
  @Assisted private val openPost: (post: PostWithMetadata) -> Unit,
  @Assisted private val openGroupSelectionSheet: () -> Unit,
) : ComponentContext by componentContext {

  internal val feedsPresenter =
    feedsPresenterFactory(
      childContext("feeds_presenter"),
      openGroupSelectionSheet,
    )

  private val backCallback = BackCallback {
    if (feedsPresenter.state.value.isInMultiSelectMode) {
      feedsPresenter.dispatch(FeedsEvent.CancelSourcesSelection)
      return@BackCallback
    }

    if (state.value.feedsSheetState == BottomSheetValue.Expanded) {
      dispatch(HomeEvent.BackClicked)
      return@BackCallback
    }
  }

  private val presenterInstance =
    instanceKeeper.getOrCreate {
      PresenterInstance(
        dispatchersProvider = dispatchersProvider,
        rssRepository = rssRepository,
        observableActiveSource = observableActiveSource,
        settingsRepository = settingsRepository,
        feedsPresenter = feedsPresenter
      )
    }

  internal val state = presenterInstance.state
  internal val effects = presenterInstance.effects.asSharedFlow()

  init {
    lifecycle.doOnCreate { backHandler.register(backCallback) }
  }

  fun dispatch(event: HomeEvent) {
    when (event) {
      is HomeEvent.FeedsSheetStateChanged -> {
        backCallback.isEnabled = event.feedsSheetState == BottomSheetValue.Expanded
      }
      is HomeEvent.SearchClicked -> openSearch()
      is HomeEvent.BookmarksClicked -> openBookmarks()
      is HomeEvent.SettingsClicked -> openSettings()
      is HomeEvent.OnPostClicked -> openPost(event.post)
      else -> {
        // no-op
      }
    }
    presenterInstance.dispatch(event)
  }

  private class PresenterInstance(
    dispatchersProvider: DispatchersProvider,
    private val rssRepository: RssRepository,
    private val observableActiveSource: ObservableActiveSource,
    private val settingsRepository: SettingsRepository,
    private val feedsPresenter: FeedsPresenter,
  ) : InstanceKeeper.Instance {

    private val coroutineScope = CoroutineScope(SupervisorJob() + dispatchersProvider.main)

    private val _state = MutableStateFlow(HomeState.DEFAULT)
    val state: StateFlow<HomeState> =
      _state.stateIn(
        scope = coroutineScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = HomeState.DEFAULT
      )

    val effects = MutableSharedFlow<HomeEffect>()

    init {
      dispatch(HomeEvent.Init)
    }

    fun dispatch(event: HomeEvent) {
      when (event) {
        HomeEvent.Init -> init()
        HomeEvent.OnSwipeToRefresh -> refreshContent()
        is HomeEvent.OnPostClicked -> {
          // no-op
        }
        is HomeEvent.FeedsSheetStateChanged -> feedsSheetStateChanged(event.feedsSheetState)
        HomeEvent.OnHomeSelected -> onHomeSelected()
        is HomeEvent.AddFeed -> addFeed(event.feedLink)
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
        is HomeEvent.OnPostSourceClicked -> postSourceClicked(event.feedId)
        is HomeEvent.OnPostsTypeChanged -> onPostsTypeChanged(event.postsType)
        is HomeEvent.TogglePostReadStatus -> togglePostReadStatus(event.postId, event.postRead)
      }
    }

    private fun togglePostReadStatus(postId: String, postRead: Boolean) {
      coroutineScope.launch { rssRepository.updatePostReadStatus(read = !postRead, id = postId) }
    }

    private fun onPostsTypeChanged(postsType: PostsType) {
      coroutineScope.launch { settingsRepository.updatePostsType(postsType) }
    }

    private fun postSourceClicked(feedId: String) {
      coroutineScope.launch {
        val feed = rssRepository.feedBlocking(feedId)
        observableActiveSource.changeActiveSource(feed)
      }
    }

    private fun onPostBookmarkClicked(post: PostWithMetadata) {
      coroutineScope.launch {
        rssRepository.updateBookmarkStatus(bookmarked = !post.bookmarked, id = post.id)
      }
    }

    private fun backClicked() {
      coroutineScope.launch { effects.emit(HomeEffect.MinimizeSheet) }
    }

    private fun init() {
      combine(observableActiveSource.activeSource, settingsRepository.postsType) {
          activeSource,
          postsType ->
          Pair(activeSource, postsType)
        }
        .distinctUntilChanged()
        .onEach { (activeSource, postsType) ->
          _state.update {
            it.copy(
              activeSource = activeSource,
              postsType = postsType,
              featuredPosts = null,
              posts = null
            )
          }
        }
        .flatMapLatest {
          val postsType = _state.value.postsType
          val activeSource = _state.value.activeSource

          val unreadOnly =
            when (postsType) {
              PostsType.ALL,
              PostsType.TODAY,
              PostsType.LAST_24_HOURS -> null
              PostsType.UNREAD -> true
            }

          val postsAfter =
            when (postsType) {
              PostsType.ALL,
              PostsType.UNREAD -> Instant.DISTANT_PAST
              PostsType.TODAY -> {
                getTodayStartInstant()
              }
              PostsType.LAST_24_HOURS -> {
                getLast24HourStart()
              }
            }

          rssRepository.featuredPosts(
            selectedFeedId = activeSource?.id,
            unreadOnly = unreadOnly,
            after = postsAfter
          )
        }
        .onEach { featuredPosts ->
          val postsType = _state.value.postsType
          val activeSource = _state.value.activeSource
          val featuredPostIds = featuredPosts.map { it.id }

          val unreadOnly =
            when (postsType) {
              PostsType.ALL,
              PostsType.TODAY,
              PostsType.LAST_24_HOURS -> null
              PostsType.UNREAD -> true
            }

          val postsAfter =
            when (postsType) {
              PostsType.ALL,
              PostsType.UNREAD -> Instant.DISTANT_PAST
              PostsType.TODAY -> {
                getTodayStartInstant()
              }
              PostsType.LAST_24_HOURS -> {
                getLast24HourStart()
              }
            }

          val posts =
            createPager(config = createPagingConfig(pageSize = 20, enablePlaceholders = true)) {
                rssRepository.posts(
                  selectedFeedId = activeSource?.id,
                  featuredPostsIds = featuredPostIds,
                  unreadOnly = unreadOnly,
                  after = postsAfter,
                )
              }
              .flow
              .cachedIn(coroutineScope)

          _state.update { it.copy(featuredPosts = featuredPosts.toImmutableList(), posts = posts) }
        }
        .launchIn(coroutineScope)

      rssRepository
        .hasFeeds()
        .distinctUntilChanged()
        .onEach { hasFeeds -> _state.update { it.copy(hasFeeds = hasFeeds) } }
        .launchIn(coroutineScope)

      settingsRepository.enableFeaturedItemBlur
        .onEach { value -> _state.update { it.copy(featuredItemBlurEnabled = value) } }
        .launchIn(coroutineScope)
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
          BugsnagKotlin.setCustomValue(section = "AddingFeed", key = "feed_url", value = feedLink)
          BugsnagKotlin.sendHandledException(e)
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
          BugsnagKotlin.setCustomValue("AddingFeed", key = "feed_url", value = feedLink)
          BugsnagKotlin.sendHandledException(feedAddResult.exception)
          effects.emit(HomeEffect.ShowError(HomeErrorType.FailedToParseXML))
        }
        is ConnectTimeoutException,
        is SocketTimeoutException -> {
          effects.emit(HomeEffect.ShowError(HomeErrorType.Timeout))
        }
        else -> {
          BugsnagKotlin.setCustomValue("AddingFeed", key = "feed_url", value = feedLink)
          BugsnagKotlin.sendHandledException(feedAddResult.exception)
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
      BugsnagKotlin.setCustomValue("AddingFeed", key = "feed_url", value = feedLink)
      BugsnagKotlin.sendHandledException(databaseError.exception)
    }

    private fun feedsSheetStateChanged(feedsSheetState: BottomSheetValue) {
      _state.update {
        // Clear search query once feeds sheet is collapsed
        if (feedsSheetState == BottomSheetValue.Collapsed) {
          feedsPresenter.dispatch(FeedsEvent.ClearSearchQuery)
        }

        it.copy(feedsSheetState = feedsSheetState)
      }
    }

    private fun onHomeSelected() {
      coroutineScope.launch { observableActiveSource.clearSelection() }
    }

    private fun refreshContent() {
      coroutineScope.launch {
        _state.update { it.copy(loadingState = HomeLoadingState.Loading) }
        try {
          when (val selectedSource = _state.value.activeSource) {
            is FeedGroup -> rssRepository.updateGroup(selectedSource.feedIds)
            is Feed -> rssRepository.updateFeed(selectedSource.id)
            else -> rssRepository.updateFeeds()
          }
        } catch (e: Exception) {
          BugsnagKotlin.logMessage("RefreshContent")
          BugsnagKotlin.sendHandledException(e)
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
