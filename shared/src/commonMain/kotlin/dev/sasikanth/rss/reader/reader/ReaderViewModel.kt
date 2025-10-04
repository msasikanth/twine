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

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.navigation.toRoute
import app.cash.paging.cachedIn
import app.cash.paging.createPager
import app.cash.paging.createPagingConfig
import dev.sasikanth.rss.reader.app.Screen
import dev.sasikanth.rss.reader.billing.BillingHandler
import dev.sasikanth.rss.reader.core.model.local.PostWithMetadata
import dev.sasikanth.rss.reader.data.repository.ReaderFont
import dev.sasikanth.rss.reader.data.repository.RssRepository
import dev.sasikanth.rss.reader.data.repository.SettingsRepository
import dev.sasikanth.rss.reader.data.repository.WidgetDataRepository
import dev.sasikanth.rss.reader.posts.AllPostsPager
import dev.sasikanth.rss.reader.reader.ReaderScreenArgs.FromScreen.Bookmarks
import dev.sasikanth.rss.reader.reader.ReaderScreenArgs.FromScreen.Home
import dev.sasikanth.rss.reader.reader.ReaderScreenArgs.FromScreen.Search
import dev.sasikanth.rss.reader.reader.ReaderScreenArgs.FromScreen.UnreadWidget
import dev.sasikanth.rss.reader.util.DispatchersProvider
import kotlin.reflect.typeOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import me.tatarka.inject.annotations.Assisted
import me.tatarka.inject.annotations.Inject

@Inject
class ReaderViewModel(
  dispatchersProvider: DispatchersProvider,
  private val rssRepository: RssRepository,
  private val widgetDataRepository: WidgetDataRepository,
  private val allPostsPager: AllPostsPager,
  private val settingsRepository: SettingsRepository,
  private val billingHandler: BillingHandler,
  @Assisted private val readerScreenArgs: ReaderScreenArgs,
) : ViewModel() {

  private val coroutineScope = CoroutineScope(SupervisorJob() + dispatchersProvider.main)
  private val openedPostItems = mutableSetOf<String>()

  private val defaultReaderState =
    ReaderState.default(
      initialPostIndex = readerScreenArgs.postIndex,
      initialPostId = readerScreenArgs.postId
    )
  private val _state = MutableStateFlow(defaultReaderState)
  val state: StateFlow<ReaderState>
    get() = _state

  private val _exitScreen = MutableSharedFlow<Boolean>(replay = 0)
  val exitScreen: SharedFlow<Boolean>
    get() = _exitScreen

  init {
    init()
  }

  fun dispatch(event: ReaderEvent) {
    when (event) {
      is ReaderEvent.TogglePostBookmark ->
        togglePostBookmark(event.postId, event.currentBookmarkStatus)
      is ReaderEvent.PostPageChanged -> postPageChange(event.postIndex, event.post)
      is ReaderEvent.ShowReaderCustomisations -> toggleReaderCustomisations(show = true)
      is ReaderEvent.HideReaderCustomisations -> toggleReaderCustomisations(show = false)
      is ReaderEvent.UpdateReaderFont -> updateReaderFont(event.font)
      is ReaderEvent.UpdateFontScaleFactor -> updateFontScaleFactor(event.fontScaleFactor)
      is ReaderEvent.UpdateFontLineHeightFactor ->
        updateFontLineHeightFactor(event.fontLineHeightFactor)
      ReaderEvent.MarkOpenPaywallDone -> {
        _state.update { it.copy(openPaywall = false) }
      }
      is ReaderEvent.OnMarkAsUnread -> onMarkAsUnreadAndExit(postId = event.postId)
    }
  }

  private fun onMarkAsUnreadAndExit(postId: String) {
    coroutineScope.launch {
      openedPostItems.remove(postId)
      rssRepository.updatePostReadStatus(read = false, id = postId)
      _exitScreen.emit(true)
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
    coroutineScope.launch {
      if (!billingHandler.isSubscribed()) {
        _state.update { it.copy(openPaywall = true) }
      } else {
        _state.update { it.copy(showReaderCustomisations = show) }
      }
    }
  }

  private fun postPageChange(postIndex: Int, post: PostWithMetadata) {
    openedPostItems += post.id
    _state.update { it.copy(activePostIndex = postIndex, activePostId = post.id) }
  }

  private fun markPostsAsRead(): Job {
    return coroutineScope.launch { rssRepository.markPostsAsRead(openedPostItems) }
  }

  private fun init() {
    coroutineScope.launch {
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
                UnreadWidget -> {
                  widgetDataRepository.unreadPostsPager()
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

      if (billingHandler.isSubscribed()) {
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
      }
    }
  }

  private fun togglePostBookmark(postId: String, currentBookmarkStatus: Boolean) {
    coroutineScope.launch {
      rssRepository.updateBookmarkStatus(bookmarked = !currentBookmarkStatus, id = postId)
    }
  }

  override fun onCleared() {
    markPostsAsRead().invokeOnCompletion { super.onCleared() }
  }
}
