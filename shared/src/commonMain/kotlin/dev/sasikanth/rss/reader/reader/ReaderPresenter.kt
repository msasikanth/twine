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

package dev.sasikanth.rss.reader.reader

import app.cash.paging.cachedIn
import app.cash.paging.createPager
import app.cash.paging.createPagingConfig
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.essenty.backhandler.BackCallback
import com.arkivanov.essenty.instancekeeper.InstanceKeeper
import com.arkivanov.essenty.instancekeeper.getOrCreate
import com.arkivanov.essenty.lifecycle.doOnCreate
import dev.sasikanth.rss.reader.billing.BillingHandler
import dev.sasikanth.rss.reader.billing.BillingHandler.SubscriptionResult
import dev.sasikanth.rss.reader.core.model.local.PostWithMetadata
import dev.sasikanth.rss.reader.core.model.local.SearchSortOrder
import dev.sasikanth.rss.reader.data.repository.ReaderFont
import dev.sasikanth.rss.reader.data.repository.RssRepository
import dev.sasikanth.rss.reader.data.repository.SettingsRepository
import dev.sasikanth.rss.reader.posts.AllPostsPager
import dev.sasikanth.rss.reader.reader.ReaderScreenArgs.FromScreen.Bookmarks
import dev.sasikanth.rss.reader.reader.ReaderScreenArgs.FromScreen.Home
import dev.sasikanth.rss.reader.reader.ReaderScreenArgs.FromScreen.Search
import dev.sasikanth.rss.reader.util.DispatchersProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import me.tatarka.inject.annotations.Assisted
import me.tatarka.inject.annotations.Inject

@Inject
class ReaderPresenter(
  dispatchersProvider: DispatchersProvider,
  private val rssRepository: RssRepository,
  private val allPostsPager: AllPostsPager,
  private val settingsRepository: SettingsRepository,
  private val billingHandler: BillingHandler,
  @Assisted private val readerScreenArgs: ReaderScreenArgs,
  @Assisted componentContext: ComponentContext,
  @Assisted private val goBack: (activePostIndex: Int) -> Unit,
  @Assisted private val openPaywall: () -> Unit,
) : ComponentContext by componentContext {

  private val coroutineScope = CoroutineScope(SupervisorJob() + dispatchersProvider.main)
  private val presenterInstance =
    instanceKeeper.getOrCreate {
      PresenterInstance(
        dispatchersProvider = dispatchersProvider,
        readerScreenArgs = readerScreenArgs,
        rssRepository = rssRepository,
        allPostsPager = allPostsPager,
        settingsRepository = settingsRepository,
      )
    }

  internal val state = presenterInstance.state

  private val backCallback = BackCallback {
    if (state.value.showReaderCustomisations) {
      dispatch(ReaderEvent.HideReaderCustomisations)
    } else {
      dispatch(ReaderEvent.BackClicked)
    }
  }

  init {
    lifecycle.doOnCreate {
      backHandler.register(backCallback)
      backCallback.isEnabled = true
    }
  }

  fun dispatch(event: ReaderEvent) {
    val canForwardDispatch =
      when (event) {
        ReaderEvent.BackClicked -> {
          goBack(state.value.activePostIndex)
          false
        }
        ReaderEvent.ShowReaderCustomisations -> {
          coroutineScope.launch {
            val isSubscribed = billingHandler.customerResult() is SubscriptionResult.Subscribed
            if (!isSubscribed) {
              openPaywall()
            } else {
              presenterInstance.dispatch(event)
            }
          }
          false
        }
        else -> {
          true
        }
      }

    if (canForwardDispatch) {
      presenterInstance.dispatch(event)
    }
  }

  private class PresenterInstance(
    dispatchersProvider: DispatchersProvider,
    private val rssRepository: RssRepository,
    private val readerScreenArgs: ReaderScreenArgs,
    private val allPostsPager: AllPostsPager,
    private val settingsRepository: SettingsRepository,
  ) : InstanceKeeper.Instance {

    private val coroutineScope = CoroutineScope(SupervisorJob() + dispatchersProvider.main)
    private val openedPostItems = mutableSetOf<String>()

    private val defaultReaderState =
      ReaderState.default(
        initialPostIndex = readerScreenArgs.postIndex,
        initialPostId = readerScreenArgs.postId
      )
    private val _state = MutableStateFlow(defaultReaderState)
    val state: StateFlow<ReaderState> =
      _state.stateIn(
        scope = coroutineScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = defaultReaderState
      )

    init {
      init()
    }

    fun dispatch(event: ReaderEvent) {
      when (event) {
        ReaderEvent.BackClicked -> {
          /* no-op */
        }
        is ReaderEvent.TogglePostBookmark ->
          togglePostBookmark(event.postId, event.currentBookmarkStatus)
        is ReaderEvent.PostPageChanged -> postPageChange(event.postIndex, event.post)
        is ReaderEvent.LoadFullArticleClicked -> loadFullArticleClicked(event.postId)
        is ReaderEvent.PostLoaded -> postLoaded(event.post)
        is ReaderEvent.ShowReaderCustomisations -> toggleReaderCustomisations(show = true)
        is ReaderEvent.HideReaderCustomisations -> toggleReaderCustomisations(show = false)
        is ReaderEvent.UpdateReaderFont -> updateReaderFont(event.font)
        is ReaderEvent.UpdateFontScaleFactor -> updateFontScaleFactor(event.fontScaleFactor)
        is ReaderEvent.UpdateFontLineHeightFactor ->
          updateFontLineHeightFactor(event.fontLineHeightFactor)
      }
    }

    private fun updateFontLineHeightFactor(fontLineHeightFactor: Float) {
      coroutineScope.launch {
        settingsRepository.updateReaderLineHeightScaleFactor(fontLineHeightFactor)
      }
    }

    private fun updateFontScaleFactor(fontScaleFactor: Float) {
      coroutineScope.launch { settingsRepository.updateReaderFontScaleFactor(fontScaleFactor) }
    }

    private fun updateReaderFont(font: ReaderFont) {
      coroutineScope.launch { settingsRepository.updateReaderFont(font) }
    }

    private fun toggleReaderCustomisations(show: Boolean) {
      _state.update { it.copy(showReaderCustomisations = show) }
    }

    private fun postLoaded(post: PostWithMetadata) {
      coroutineScope.launch {
        _state.update {
          val newLoadFullArticleMap =
            if (it.loadFullArticleMap.containsKey(post.id)) {
              it.loadFullArticleMap
            } else {
              it.loadFullArticleMap + Pair(post.id, post.alwaysFetchFullArticle)
            }

          it.copy(loadFullArticleMap = newLoadFullArticleMap)
        }
      }
    }

    private fun postPageChange(postIndex: Int, post: PostWithMetadata) {
      openedPostItems += post.id
      _state.update { it.copy(activePostIndex = postIndex, activePostId = post.id) }
    }

    private fun loadFullArticleClicked(postId: String) {
      coroutineScope.launch {
        _state.update {
          val newLoadFullArticleMap =
            if (it.loadFullArticleMap[postId] == true) {
              it.loadFullArticleMap - postId
            } else {
              it.loadFullArticleMap + Pair(postId, true)
            }

          it.copy(loadFullArticleMap = newLoadFullArticleMap)
        }
      }
    }

    private fun markPostsAsRead(): Job {
      return coroutineScope.launch { rssRepository.markPostsAsRead(openedPostItems) }
    }

    private fun init() {
      coroutineScope.launch {
        combine(
            settingsRepository.readerFontStyle,
            settingsRepository.readerFontScaleFactor,
            settingsRepository.readerLineHeightScaleFactor,
            { fontStyle, fontScaleFactor, lineHeightScaleFactor ->
              Triple(fontStyle, fontScaleFactor, lineHeightScaleFactor)
            }
          )
          .onEach { (fontStyle, fontScaleFactor, lineHeightScaleFactor) ->
            _state.update {
              it.copy(
                selectedReaderFont = fontStyle,
                readerFontScaleFactor = fontScaleFactor,
                readerLineHeightScaleFactor = lineHeightScaleFactor
              )
            }
          }
          .launchIn(coroutineScope)

        if (readerScreenArgs.fromScreen == Home) {
          allPostsPager.allPostsPagingData
            .onEach { postsPagingData -> _state.update { it.copy(posts = postsPagingData) } }
            .launchIn(coroutineScope)
        } else {
          val posts =
            createPager(
                config =
                  createPagingConfig(
                    pageSize = 4,
                    enablePlaceholders = true,
                  ),
                initialKey = readerScreenArgs.postIndex,
              ) {
                when (readerScreenArgs.fromScreen) {
                  is Search -> {
                    rssRepository.search(
                      searchQuery = readerScreenArgs.fromScreen.searchQuery,
                      sortOrder = readerScreenArgs.fromScreen.sortOrder,
                    )
                  }
                  Bookmarks -> {
                    rssRepository.bookmarks()
                  }
                  else -> {
                    throw IllegalArgumentException(
                      "Unknown from screen: ${readerScreenArgs.fromScreen}"
                    )
                  }
                }
              }
              .flow
              .cachedIn(coroutineScope)

          _state.update { it.copy(posts = posts) }
        }
      }
    }

    private fun togglePostBookmark(postId: String, currentBookmarkStatus: Boolean) {
      coroutineScope.launch {
        rssRepository.updateBookmarkStatus(bookmarked = !currentBookmarkStatus, id = postId)
      }
    }

    override fun onDestroy() {
      markPostsAsRead().invokeOnCompletion { coroutineScope.cancel() }
    }
  }
}

internal typealias ReaderPresenterFactory =
  (
    args: ReaderScreenArgs,
    ComponentContext,
    goBack: (activePostIndex: Int) -> Unit,
    openPaywall: () -> Unit,
  ) -> ReaderPresenter

@Serializable
data class ReaderScreenArgs(
  val postIndex: Int,
  val postId: String,
  val fromScreen: FromScreen,
) {

  @Serializable
  sealed interface FromScreen {

    @Serializable data object Home : FromScreen

    @Serializable
    data class Search(val searchQuery: String, val sortOrder: SearchSortOrder) : FromScreen

    @Serializable data object Bookmarks : FromScreen
  }
}
