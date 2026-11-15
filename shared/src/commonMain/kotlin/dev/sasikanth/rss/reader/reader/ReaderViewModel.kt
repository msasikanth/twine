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
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.cachedIn
import dev.sasikanth.rss.reader.billing.BillingHandler
import dev.sasikanth.rss.reader.core.model.local.ResolvedPost
import dev.sasikanth.rss.reader.core.model.local.ThemeVariant
import dev.sasikanth.rss.reader.data.repository.ObservableActiveReaderPost
import dev.sasikanth.rss.reader.data.repository.ObservableActiveSource
import dev.sasikanth.rss.reader.data.repository.ObservableSelectedPost
import dev.sasikanth.rss.reader.data.repository.PostRepository
import dev.sasikanth.rss.reader.data.repository.ReaderFont
import dev.sasikanth.rss.reader.data.repository.RssRepository
import dev.sasikanth.rss.reader.data.repository.SettingsRepository
import dev.sasikanth.rss.reader.data.repository.WidgetDataRepository
import dev.sasikanth.rss.reader.data.repository.isPremium
import dev.sasikanth.rss.reader.media.AudioPlayer as MediaAudioPlayer
import dev.sasikanth.rss.reader.media.PlaybackState
import dev.sasikanth.rss.reader.media.SleepTimerOption
import dev.sasikanth.rss.reader.posts.AllPostsPager
import dev.sasikanth.rss.reader.reader.ReaderScreenArgs.FromScreen.AudioPlayer
import dev.sasikanth.rss.reader.reader.ReaderScreenArgs.FromScreen.Bookmarks
import dev.sasikanth.rss.reader.reader.ReaderScreenArgs.FromScreen.Home
import dev.sasikanth.rss.reader.reader.ReaderScreenArgs.FromScreen.Search
import dev.sasikanth.rss.reader.reader.ReaderScreenArgs.FromScreen.UnreadWidget
import dev.sasikanth.rss.reader.util.DispatchersProvider
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
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
  private val observableActiveReaderPost: ObservableActiveReaderPost,
  private val observableActiveSource: ObservableActiveSource,
  private val readerSessionRegistry: ReaderSessionRegistry,
  @Assisted private val savedStateHandle: SavedStateHandle,
) : ViewModel() {

  // A reader opened under a previous source must not publish its position, or it would
  // scroll the new source's list to a stale index.
  private val initialActiveSourceId = observableActiveSource.currentActiveSource?.id

  private val canPublishSelectedPost: Boolean
    get() =
      (readerScreenArgs.fromScreen == Home || readerScreenArgs.fromScreen == AudioPlayer) &&
        observableActiveSource.currentActiveSource?.id == initialActiveSourceId

  private val readerScreenArgs = savedStateHandle.get<ReaderScreenArgs>("readerScreenArgs")!!
  private val sessionCoordinator =
    ReaderPostSessionCoordinator(
      sessionScope = viewModelScope,
      exitDispatcher = dispatchersProvider.main,
      initialPostIndex = readerScreenArgs.postIndex,
      initialPostId = readerScreenArgs.postId,
      observableActiveReaderPost = observableActiveReaderPost,
      observableSelectedPost = observableSelectedPost,
      sessionRegistry = readerSessionRegistry,
      canPublishSelectedPost = { canPublishSelectedPost },
    )
  private val coroutineScope: CoroutineScope
    get() = sessionCoordinator.exitScope

  private val _openedPostItems: StateFlow<Set<String>>
    get() = sessionCoordinator.openedPostItems

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

  private var audioProgressAutoSaveJob: Job? = null

  init {
    init()
    observePlaybackForAutoSave()
  }

  private fun observePlaybackForAutoSave() {
    audioPlayer.playbackState
      .map { it.isPlaying }
      .distinctUntilChanged()
      .onEach { isPlaying ->
        audioProgressAutoSaveJob?.cancel()
        audioProgressAutoSaveJob =
          if (isPlaying) {
            viewModelScope.launch {
              while (true) {
                delay(10.seconds)
                saveAudioProgress()
              }
            }
          } else {
            saveAudioProgress()
            null
          }
      }
      .launchIn(viewModelScope)
  }

  val playbackState: StateFlow<PlaybackState>
    get() = audioPlayer.playbackState

  fun pauseAudio() {
    audioPlayer.pause()
    saveAudioProgress()
  }

  fun resumeAudio() {
    audioPlayer.resume()
  }

  fun seekAudio(position: Long) {
    audioPlayer.seekTo(position)
    saveAudioProgress(position = position)
  }

  fun seekForward() {
    audioPlayer.seekTo(
      audioPlayer.playbackState.value.currentPosition + 30.seconds.inWholeMilliseconds
    )
  }

  fun seekBackward() {
    audioPlayer.seekTo(
      audioPlayer.playbackState.value.currentPosition - 30.seconds.inWholeMilliseconds
    )
  }

  fun setPlaybackSpeed(speed: Float) {
    audioPlayer.setPlaybackSpeed(speed)
  }

  fun setSleepTimer(option: SleepTimerOption) {
    audioPlayer.setSleepTimer(option)
  }

  private fun saveAudioProgress(position: Long? = null) {
    val playbackState = audioPlayer.playbackState.value
    val postId = playbackState.playingPostId ?: return
    val audioProgress = position ?: playbackState.currentPosition
    if (audioProgress <= 0) return

    coroutineScope.launch {
      postRepository.updateAudioProgress(
        postId = postId,
        audioProgress = audioProgress,
        audioDuration = playbackState.duration,
      )
    }
  }

  fun dispatch(event: ReaderEvent) {
    when (event) {
      is ReaderEvent.TogglePostBookmark ->
        togglePostBookmark(event.postId, event.currentBookmarkStatus)
      is ReaderEvent.PostPageChanged -> postPageChange(event.postIndex, event.post)
      is ReaderEvent.ShowPanelContent -> updatePanelContent(event.panelContent)
      is ReaderEvent.CollapsePanel -> updatePanelContent(ReaderPanelContent.Actions)
      is ReaderEvent.UpdateReaderFont -> updateReaderFont(event.font)
      is ReaderEvent.UpdateThemeVariant -> updateThemeVariant(event.themeVariant)
      is ReaderEvent.UpdateFontScaleFactor -> updateFontScaleFactor(event.fontScaleFactor)
      is ReaderEvent.UpdateFontLineHeightFactor ->
        updateFontLineHeightFactor(event.fontLineHeightFactor)
      ReaderEvent.MarkOpenPaywallDone -> {
        _state.update { it.copy(openPaywall = false) }
      }
      ReaderEvent.OnInitialTransitionSettled -> sessionCoordinator.onInitialTransitionSettled()
      is ReaderEvent.OnMarkAsUnread -> onMarkAsUnreadAndExit(postId = event.postId)
    }
  }

  private fun onMarkAsUnreadAndExit(postId: String) {
    sessionCoordinator.onMarkAsUnread(postId) {
      rssRepository.updatePostReadStatus(read = false, id = postId)
      _exitScreen.emit(true)
    }
  }

  private fun updateFontLineHeightFactor(fontLineHeightFactor: Float) {
    viewModelScope.launch {
      settingsRepository.updateReaderLineHeightScaleFactor(fontLineHeightFactor)
    }
  }

  private fun updateFontScaleFactor(fontScaleFactor: Float) {
    viewModelScope.launch { settingsRepository.updateReaderFontScaleFactor(fontScaleFactor) }
  }

  private fun updateReaderFont(font: ReaderFont) {
    viewModelScope.launch {
      if (font.isPremium && !billingHandler.isSubscribed()) {
        _state.update { it.copy(openPaywall = true) }
      } else {
        settingsRepository.updateReaderFont(font)
      }
    }
  }

  private fun updateThemeVariant(themeVariant: ThemeVariant) {
    viewModelScope.launch {
      if (themeVariant.isPremium && !billingHandler.isSubscribed()) {
        _state.update { it.copy(openPaywall = true) }
      } else {
        settingsRepository.updateThemeVariant(themeVariant)
      }
    }
  }

  private fun updatePanelContent(panelContent: ReaderPanelContent) {
    viewModelScope.launch { _state.update { it.copy(panelContent = panelContent) } }
  }

  private fun postPageChange(postIndex: Int, post: ResolvedPost) {
    if (sessionCoordinator.shouldIgnorePageChange(post.id)) return

    _state.update { it.copy(activePostIndex = postIndex, activePostId = post.id) }

    // Only readers backed by the home posts list publish the selection; indexes from
    // Search/Bookmarks/Widget belong to a different list, and posts there may not even
    // be in home's current filters, which would scroll home somewhere surprising. Same
    // reasoning applies to which posts the home list should keep pinned while read.
    sessionCoordinator.onPostPageChanged(
      postId = post.id,
      postIndex = postIndex,
      shouldMarkReadImmediately = !post.read && post.audioUrl == null,
      markPostRead = { rssRepository.updatePostReadStatus(read = true, id = post.id) },
    )
  }

  private suspend fun markPostsAsRead(openedPostItems: Set<String>) {
    if (openedPostItems.isEmpty()) return

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
      rssRepository.updatePostReadStatus(postIds = postsToMarkAsRead, read = true)
    }
  }

  private fun init() {
    viewModelScope.launch {
      if (readerScreenArgs.fromScreen == Home || readerScreenArgs.fromScreen == AudioPlayer) {
        val allPostsPagingData =
          allPostsPager
            .allPostsPagingData(
              sessionPostIds = {
                (_openedPostItems.value +
                    observableActiveReaderPost.openedPostIds.value +
                    readerScreenArgs.postId)
                  .toList()
              }
            )
            .cachedIn(viewModelScope)
        _state.update { it.copy(posts = allPostsPagingData) }
      } else {
        val posts =
          Pager(
              config = PagingConfig(pageSize = 4, enablePlaceholders = true, jumpThreshold = 12)
            ) {
              val sessionPostIds = (_openedPostItems.value + readerScreenArgs.postId).toList()
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
            .cachedIn(viewModelScope)

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
        .launchIn(viewModelScope)
    }
  }

  private fun togglePostBookmark(postId: String, currentBookmarkStatus: Boolean) {
    coroutineScope.launch {
      rssRepository.updateBookmarkStatus(bookmarked = !currentBookmarkStatus, id = postId)
    }
  }

  override fun onCleared() {
    super.onCleared()
    sessionCoordinator.onClear { openedPostIds -> markPostsAsRead(openedPostIds) }
  }
}

internal class ReaderPostSessionCoordinator(
  private val sessionScope: CoroutineScope,
  exitDispatcher: CoroutineDispatcher,
  initialPostIndex: Int = 0,
  initialPostId: String = "",
  private val observableActiveReaderPost: ObservableActiveReaderPost = ObservableActiveReaderPost(),
  private val observableSelectedPost: ObservableSelectedPost = ObservableSelectedPost(),
  private val sessionRegistry: ReaderSessionRegistry = ReaderSessionRegistry(),
  private val canPublishSelectedPost: () -> Boolean = { false },
  initialTransitionSettled: Boolean = false,
) {
  private val exitJob = SupervisorJob()
  val exitScope = CoroutineScope(exitJob + exitDispatcher)

  private val _openedPostItems = MutableStateFlow(emptySet<String>())
  val openedPostItems: StateFlow<Set<String>> = _openedPostItems

  private val sessionToken = sessionRegistry.register()
  private var activePostIndex = initialPostIndex
  private var activePostId = initialPostId

  private val markedUnreadPostIds = mutableSetOf<String>()
  private var isExiting = false

  private val initialTransitionGate =
    CompletableDeferred<Unit>().apply {
      if (initialTransitionSettled) {
        complete(Unit)
      }
    }
  private var pageSettleJob: Job? = null

  init {
    publishActivePost(initialPostId)
  }

  fun ownsActiveReaderPost(): Boolean = sessionRegistry.isActive(sessionToken)

  fun onInitialTransitionSettled() {
    initialTransitionGate.complete(Unit)
  }

  fun shouldIgnorePageChange(postId: String): Boolean {
    return isExiting || postId in markedUnreadPostIds
  }

  fun onPostPageChanged(
    postId: String,
    shouldMarkReadImmediately: Boolean,
    postIndex: Int = activePostIndex,
    onSettled: () -> Unit = {},
    markPostRead: suspend () -> Unit,
  ) {
    if (shouldIgnorePageChange(postId)) return

    activePostIndex = postIndex
    activePostId = postId
    if (ownsActiveReaderPost()) {
      publishActivePost(postId)
    }

    _openedPostItems.update { it + postId }
    pageSettleJob?.cancel()
    pageSettleJob =
      sessionScope.launch {
        initialTransitionGate.await()
        if (shouldIgnorePageChange(postId)) return@launch
        if (canPublishSelectedPost() && ownsActiveReaderPost()) {
          observableSelectedPost.updateSelectedPost(postIndex, postId)
        }
        onSettled()
        if (shouldMarkReadImmediately && postId in _openedPostItems.value) {
          markPostRead()
        }
      }
  }

  fun onMarkAsUnread(postId: String, markUnreadAndExit: suspend () -> Unit) {
    isExiting = true
    markedUnreadPostIds.add(postId)
    pageSettleJob?.cancel()
    _openedPostItems.update { it - postId }
    exitScope.launch { markUnreadAndExit() }
  }

  fun onClear(markOpenedPostsAsRead: suspend (Set<String>) -> Unit) {
    val ownsActivePost = ownsActiveReaderPost()
    sessionRegistry.unregister(sessionToken)
    isExiting = true
    pageSettleJob?.cancel()

    if (ownsActivePost) {
      observableActiveReaderPost.clearIfActive(observableActiveReaderPost.activePostId.value)
      if (canPublishSelectedPost()) {
        observableSelectedPost.updateSelectedPost(index = activePostIndex, postId = activePostId)
      }
    }

    val openedIds = _openedPostItems.value - markedUnreadPostIds
    if (openedIds.isNotEmpty()) {
      exitScope.launch { markOpenedPostsAsRead(openedIds) }
    }
    exitJob.complete()
    exitJob.invokeOnCompletion { exitScope.cancel() }
  }

  private fun publishActivePost(postId: String) {
    if (canPublishSelectedPost()) {
      observableActiveReaderPost.addOpenedPost(postId)
    }
    observableActiveReaderPost.updateActivePost(postId)
  }
}
