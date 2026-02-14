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
package dev.sasikanth.rss.reader.app

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.sasikanth.rss.reader.core.model.local.Feed
import dev.sasikanth.rss.reader.core.model.local.FeedGroup
import dev.sasikanth.rss.reader.core.model.local.PostsType
import dev.sasikanth.rss.reader.data.refreshpolicy.RefreshPolicy
import dev.sasikanth.rss.reader.data.repository.ObservableActiveSource
import dev.sasikanth.rss.reader.data.repository.RssRepository
import dev.sasikanth.rss.reader.data.repository.SettingsRepository
import dev.sasikanth.rss.reader.data.sync.SyncCoordinator
import dev.sasikanth.rss.reader.data.sync.auth.OAuthManager
import dev.sasikanth.rss.reader.data.utils.PostsFilterUtils
import dev.sasikanth.rss.reader.di.scopes.ActivityScope
import dev.sasikanth.rss.reader.platform.LinkHandler
import dev.sasikanth.rss.reader.reader.ReaderScreenArgs
import dev.sasikanth.rss.reader.reader.ReaderScreenArgs.FromScreen
import dev.sasikanth.rss.reader.utils.NTuple9
import dev.sasikanth.rss.reader.utils.combine
import kotlin.time.Clock
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import me.tatarka.inject.annotations.Inject

@Inject
@ActivityScope
class AppViewModel(
  private val refreshPolicy: RefreshPolicy,
  private val rssRepository: RssRepository,
  private val settingsRepository: SettingsRepository,
  private val observableActiveSource: ObservableActiveSource,
  private val syncCoordinator: SyncCoordinator,
  private val oAuthManager: OAuthManager,
  private val linkHandler: LinkHandler,
) : ViewModel() {

  private val _state = MutableStateFlow(AppState.DEFAULT)
  val state: StateFlow<AppState>
    get() = _state

  private val _navigateToReader = MutableSharedFlow<ReaderScreenArgs>()
  val navigateToReader: SharedFlow<ReaderScreenArgs> = _navigateToReader.asSharedFlow()

  init {
    refreshFeedsIfExpired()
    setupSessionTracking()

    combine(
        settingsRepository.appThemeMode,
        settingsRepository.useAmoled,
        settingsRepository.dynamicColorEnabled,
        settingsRepository.showFeedFavIcon,
        settingsRepository.homeViewMode,
        settingsRepository.showReaderView,
        settingsRepository.blockImages,
        observableActiveSource.activeSource,
        settingsRepository.postsType,
      ) {
        appThemeMode,
        useAmoled,
        dynamicColorEnabled,
        showFeedFavIcon,
        homeViewMode,
        showReaderView,
        blockImages,
        activeSource,
        postsType ->
        NTuple9(
          appThemeMode,
          useAmoled,
          dynamicColorEnabled,
          showFeedFavIcon,
          homeViewMode,
          showReaderView,
          blockImages,
          activeSource,
          postsType,
        )
      }
      .onEach {
        (
          appThemeMode,
          useAmoled,
          dynamicColorEnabled,
          showFeedFavIcon,
          homeViewMode,
          showReaderView,
          blockImages,
          activeSource,
          postsType) ->
        _state.update {
          it.copy(
            appThemeMode = appThemeMode,
            useAmoled = useAmoled,
            dynamicColorEnabled = dynamicColorEnabled,
            showFeedFavIcon = showFeedFavIcon,
            homeViewMode = homeViewMode,
            showReaderView = showReaderView,
            blockImages = blockImages,
            activeSource = activeSource,
            postsType = postsType,
          )
        }
      }
      .launchIn(viewModelScope)
  }

  fun onPostOpened(postId: String, index: Int) {
    updateActivePostIndex(index, postId)
    markPostAsRead(postId)
  }

  fun markPostAsRead(id: String) {
    viewModelScope.launch { rssRepository.updatePostReadStatus(read = true, id = id) }
  }

  fun updateActivePostIndex(index: Int, postId: String? = null) {
    _state.update { it.copy(activePostIndex = index, activePostId = postId) }
  }

  fun onCurrentlyPlayingDeepLink(playingPostId: String) {
    viewModelScope.launch {
      val post = rssRepository.resolvedPostById(playingPostId) ?: return@launch

      val currentState = _state.value
      val activeSource = currentState.activeSource
      val postsType = currentState.postsType

      val isInActiveSource =
        when (activeSource) {
          is Feed -> activeSource.id == post.sourceId
          is FeedGroup -> activeSource.feedIds.contains(post.sourceId)
          null -> true
          else -> false
        }

      val unreadOnly = PostsFilterUtils.shouldGetUnreadPostsOnly(postsType)
      val isReadStatusCompatible = unreadOnly == null || (unreadOnly == !post.read)

      val lastRefreshedAt = refreshPolicy.fetchLastRefreshedAt()
      val postsAfter =
        if (lastRefreshedAt != null) {
          PostsFilterUtils.postsThresholdTime(
            postsType,
            lastRefreshedAt.toLocalDateTime(TimeZone.currentSystemDefault()),
          )
        } else {
          Instant.DISTANT_PAST
        }
      val isDateCompatible = post.date > postsAfter

      if (!isInActiveSource || !isReadStatusCompatible || !isDateCompatible) {
        observableActiveSource.clearSelection()
        settingsRepository.updatePostsType(PostsType.ALL)
        _state.filter { it.activeSource == null && it.postsType == PostsType.ALL }.first()
      }

      val finalState = _state.value
      val finalPostsType = finalState.postsType
      val finalLastRefreshedAt = refreshPolicy.fetchLastRefreshedAt()
      val finalPostsAfter =
        if (finalLastRefreshedAt != null) {
          PostsFilterUtils.postsThresholdTime(
            finalPostsType,
            finalLastRefreshedAt.toLocalDateTime(TimeZone.currentSystemDefault()),
          )
        } else {
          Instant.DISTANT_PAST
        }

      val finalActiveSourceIds =
        when (val source = finalState.activeSource) {
          is Feed -> listOf(source.id)
          is FeedGroup -> source.feedIds
          else -> emptyList()
        }
      val finalUnreadOnly = PostsFilterUtils.shouldGetUnreadPostsOnly(finalPostsType)
      val postsUpperBound = finalLastRefreshedAt ?: Clock.System.now()

      val position =
        rssRepository.postPosition(
          postId = playingPostId,
          sourceId = post.sourceId,
          activeSourceIds = finalActiveSourceIds,
          unreadOnly = finalUnreadOnly,
          after = finalPostsAfter,
          postsUpperBound = postsUpperBound,
        )

      if (position != null) {
        _navigateToReader.emit(
          ReaderScreenArgs(
            postIndex = position,
            postId = playingPostId,
            fromScreen = FromScreen.AudioPlayer,
          )
        )
      }
    }
  }

  fun onOAuthRedirect(uri: String) {
    viewModelScope.launch {
      val signInSucceeded = oAuthManager.handleRedirect(uri)
      if (signInSucceeded) {
        syncCoordinator.push()
      }
      linkHandler.close()
    }
  }

  private fun refreshFeedsIfExpired() {
    viewModelScope.launch {
      if (refreshPolicy.hasExpired()) {
        syncCoordinator.pull()
      }
    }
  }

  private fun setupSessionTracking() {
    viewModelScope.launch {
      val currentTime = Clock.System.now()
      val installDate = settingsRepository.installDate.first()

      if (installDate == null) {
        settingsRepository.updateInstallDate(currentTime)
      }

      settingsRepository.incrementUserSessionCount()
    }
  }
}
