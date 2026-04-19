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

package dev.sasikanth.rss.reader.reader

import androidx.compose.runtime.Stable
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.navigation.toRoute
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.cachedIn
import dev.sasikanth.rss.reader.app.Screen
import dev.sasikanth.rss.reader.billing.BillingHandler
import dev.sasikanth.rss.reader.core.model.local.ResolvedPost
import dev.sasikanth.rss.reader.core.model.local.ThemeVariant
import dev.sasikanth.rss.reader.data.repository.MarkAsReadOn
import dev.sasikanth.rss.reader.data.repository.ObservableSelectedPost
import dev.sasikanth.rss.reader.data.repository.PostRepository
import dev.sasikanth.rss.reader.data.repository.ReaderFont
import dev.sasikanth.rss.reader.data.repository.RssRepository
import dev.sasikanth.rss.reader.data.repository.SettingsRepository
import dev.sasikanth.rss.reader.data.repository.WidgetDataRepository
import dev.sasikanth.rss.reader.data.repository.isPremium
import dev.sasikanth.rss.reader.media.AudioPlayer as MediaAudioPlayer
import dev.sasikanth.rss.reader.posts.AllPostsPager
import dev.sasikanth.rss.reader.reader.ReaderScreenArgs.FromScreen.AudioPlayer
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
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import me.tatarka.inject.annotations.Assisted
import me.tatarka.inject.annotations.Inject

@Stable
@Inject
class ReaderViewModel(
  dispatchersProvider: DispatchersProvider,
  private val rssRepository: RssRepository,
  private val postRepository: PostRepository,
  private val audioPlayer: MediaAudioPlayer,
  private val widgetDataRepository: WidgetDataRepository,
  private val allPostsPager: AllPostsPager,
  private val settingsRepository: SettingsRepository,
  private val billingHandler: BillingHandler,
  private val observableSelectedPost: ObservableSelectedPost,
  @Assisted private val savedStateHandle: SavedStateHandle,
) : ViewModel() {

  private val coroutineScope = CoroutineScope(SupervisorJob() + dispatchersProvider.main)
  private val _openedPostItems = MutableStateFlow(emptySet<String>())
  private val readerScreenArgs =
    savedStateHandle
      .toRoute<Screen.Reader>(
        typeMap = mapOf(typeOf<ReaderScreenArgs>() to ReaderScreenArgs.navTypeMap)
      )
      .readerScreenArgs
  private val defaultReaderState =
    ReaderState.default(
      initialPostIndex = readerScreenArgs.postIndex,
      initialPostId = readerScreenArgs.postId,
      fromScreen = readerScreenArgs.fromScreen,
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
      is ReaderEvent.UpdateThemeVariant -> updateThemeVariant(event.themeVariant)
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
      _openedPostItems.update { it - postId }
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
    coroutineScope.launch {
      if (font.isPremium && !billingHandler.isSubscribed()) {
        _state.update { it.copy(openPaywall = true) }
      } else {
        settingsRepository.updateReaderFont(font)
      }
    }
  }

  private fun updateThemeVariant(themeVariant: ThemeVariant) {
    coroutineScope.launch {
      if (themeVariant.isPremium && !billingHandler.isSubscribed()) {
        _state.update { it.copy(openPaywall = true) }
      } else {
        settingsRepository.updateThemeVariant(themeVariant)
      }
    }
  }

  private fun toggleReaderCustomisations(show: Boolean) {
    coroutineScope.launch { _state.update { it.copy(showReaderCustomisations = show) } }
  }

  private fun postPageChange(postIndex: Int, post: ResolvedPost) {
    _openedPostItems.update { it + post.id }
    _state.update { it.copy(activePostIndex = postIndex, activePostId = post.id) }
    observableSelectedPost.updateSelectedPost(postIndex, post.id)

    coroutineScope.launch {
      val markAsReadOn = settingsRepository.markAsReadOn.first()
      if (markAsReadOn == MarkAsReadOn.Open && post.audioUrl == null) {
        rssRepository.updatePostReadStatus(read = true, id = post.id)
      }
    }
  }

  private fun markPostsAsRead(): Job {
    return coroutineScope.launch {
      val markAsReadOn = settingsRepository.markAsReadOn.first()
      val openedPostItems = _openedPostItems.value
      if (markAsReadOn != MarkAsReadOn.Open || openedPostItems.isEmpty()) return@launch

      val audioMarkAsReadThreshold = settingsRepository.audioMarkAsReadThreshold.first()
      val posts = postRepository.postsByIds(openedPostItems)
      val playbackState = audioPlayer.playbackState.value
      val thresholdValue = audioMarkAsReadThreshold.value

      val postsToMarkAsRead = buildSet {
        for (post in posts) {
          val postId = post.id
          if (post.audioUrl != null) {
            val isPlaying = playbackState.playingPostId == postId
            val progress = if (isPlaying) playbackState.currentPosition else post.audioProgress
            val duration = if (isPlaying) playbackState.duration else post.audioDuration

            if (duration > 0 && (progress.toFloat() / duration.toFloat()) >= thresholdValue) {
              add(postId)
            }
          } else {
            add(postId)
          }
        }
      }

      if (postsToMarkAsRead.isNotEmpty()) {
        rssRepository.markPostsAsRead(postsToMarkAsRead)
      }
    }
  }

  private fun init() {
    coroutineScope.launch {
      if (readerScreenArgs.fromScreen == Home || readerScreenArgs.fromScreen == AudioPlayer) {
        val allPostsPagingData =
          allPostsPager
            .allPostsPagingData(sessionPostIds = { _openedPostItems.value.toList() })
            .cachedIn(coroutineScope)
        _state.update { it.copy(posts = allPostsPagingData) }
      } else {
        val posts =
          Pager(config = PagingConfig(pageSize = 4, enablePlaceholders = true)) {
              val sessionPostIds = _openedPostItems.value.toList()
              when (readerScreenArgs.fromScreen) {
                is Search -> {
                  rssRepository.search(
                    searchQuery = readerScreenArgs.fromScreen.searchQuery,
                    sortOrder = readerScreenArgs.fromScreen.sortOrder,
                    sessionPostIds = sessionPostIds,
                  )
                }
                Bookmarks -> {
                  rssRepository.bookmarks()
                }
                UnreadWidget -> {
                  widgetDataRepository.unreadPostsPager(sessionPostIds = sessionPostIds)
                }
              }
            }
            .flow
            .cachedIn(coroutineScope)

        _state.update { it.copy(posts = posts) }
      }

      val isSubscribed = billingHandler.isSubscribed()
      _state.update { it.copy(isSubscribed = isSubscribed) }

      combine(
          settingsRepository.readerFontStyle,
          settingsRepository.themeVariant,
          settingsRepository.readerFontScaleFactor,
          settingsRepository.readerLineHeightScaleFactor,
          { fontStyle, themeVariant, fontScaleFactor, lineHeightScaleFactor ->
            val result =
              object {
                val fontStyle = fontStyle
                val themeVariant = themeVariant
                val fontScaleFactor = fontScaleFactor
                val lineHeightScaleFactor = lineHeightScaleFactor
              }
            result
          },
        )
        .onEach { result ->
          _state.update {
            it.copy(
              selectedReaderFont = result.fontStyle,
              selectedThemeVariant = result.themeVariant,
              readerFontScaleFactor = result.fontScaleFactor,
              readerLineHeightScaleFactor = result.lineHeightScaleFactor,
            )
          }
        }
        .launchIn(coroutineScope)
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
