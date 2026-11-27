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

package dev.sasikanth.rss.reader.reader.page.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.DisableSelection
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.window.core.layout.WindowSizeClass
import com.mikepenz.markdown.compose.components.MarkdownComponents
import com.mikepenz.markdown.model.State
import dev.sasikanth.rss.reader.components.AmbientImageBackdrop
import dev.sasikanth.rss.reader.components.InverseButton
import dev.sasikanth.rss.reader.components.Switch
import dev.sasikanth.rss.reader.components.ToggleableButtonGroup
import dev.sasikanth.rss.reader.components.ToggleableButtonItem
import dev.sasikanth.rss.reader.components.image.FeedIcon
import dev.sasikanth.rss.reader.core.model.local.MediaChapter
import dev.sasikanth.rss.reader.core.model.local.ResolvedPost
import dev.sasikanth.rss.reader.home.ui.PostActions
import dev.sasikanth.rss.reader.media.PlaybackState
import dev.sasikanth.rss.reader.media.SleepTimerOption
import dev.sasikanth.rss.reader.reader.ReaderContentType
import dev.sasikanth.rss.reader.reader.ReaderScreenArgs
import dev.sasikanth.rss.reader.reader.page.ReaderProcessingProgress
import dev.sasikanth.rss.reader.reader.ui.ImageClickEvent
import dev.sasikanth.rss.reader.resources.icons.OpenBrowser
import dev.sasikanth.rss.reader.resources.icons.TwineIcons
import dev.sasikanth.rss.reader.ui.AppTheme
import dev.sasikanth.rss.reader.utils.Constants
import dev.sasikanth.rss.reader.utils.LocalWindowSizeClass
import dev.sasikanth.rss.reader.utils.formatRelativeTime
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import org.jetbrains.compose.resources.stringResource
import twine.shared.generated.resources.Res
import twine.shared.generated.resources.openWebsite
import twine.shared.generated.resources.readerChapterSponsor
import twine.shared.generated.resources.readerChapters
import twine.shared.generated.resources.readerShowNotes
import twine.shared.generated.resources.readerSkipSponsorChapters

private const val HERO_ARTWORK_KEY = "podcast-hero-artwork"
private const val HERO_CONTROLS_KEY = "podcast-hero-controls"
private val ChapterTimestampShape = RoundedCornerShape(8.dp)
private val ChapterRowShape = RoundedCornerShape(14.dp)

private enum class PodcastContentTab {
  ShowNotes,
  Chapters,
}

@Composable
internal fun PodcastReaderPage(
  readerPost: ResolvedPost,
  contentType: ReaderContentType.Podcast,
  resolvedCoverArtUrl: String?,
  chapters: List<MediaChapter>,
  skipSponsorChapters: Boolean,
  markdownContentState: State,
  contentParsingProgress: ReaderProcessingProgress,
  markdownComponents: MarkdownComponents,
  playbackStateFlow: StateFlow<PlaybackState>,
  audioPlayerInstallationHint: String?,
  fromScreen: ReaderScreenArgs.FromScreen,
  contentPaddingValues: PaddingValues,
  onBookmarkClick: () -> Unit,
  onCommentsClick: () -> Unit,
  onMarkAsUnread: () -> Unit,
  onImageClick: (ImageClickEvent) -> Unit,
  onPlayClick: () -> Unit,
  onPlayAtClick: (Long) -> Unit,
  onChapterClick: (MediaChapter) -> Unit,
  onToggleSkipSponsorChapters: (Boolean) -> Unit,
  onPauseClick: () -> Unit,
  onResumeClick: () -> Unit,
  onSeek: (Long) -> Unit,
  onSeekForward: () -> Unit,
  onSeekBackward: () -> Unit,
  onPlaybackSpeedChange: (Float) -> Unit,
  onSleepTimerOptionSelected: (SleepTimerOption) -> Unit,
  onOpenWebsite: () -> Unit,
  onHeroVisibilityChanged: (Boolean) -> Unit,
  onContentWidthChanged: (Dp) -> Unit,
  modifier: Modifier = Modifier,
) {
  val sizeClass = LocalWindowSizeClass.current
  val isWideLayout = sizeClass.isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_MEDIUM_LOWER_BOUND)

  var selectedTab by rememberSaveable(readerPost.id) { mutableStateOf(PodcastContentTab.ShowNotes) }
  val effectiveTab = if (chapters.isEmpty()) PodcastContentTab.ShowNotes else selectedTab

  var showSleepTimerSheet by remember { mutableStateOf(false) }

  val initialActiveDuration =
    remember(playbackStateFlow, readerPost.id, readerPost.audioDuration) {
      playbackStateFlow.value.resolveEpisodeDuration(readerPost.id, readerPost.audioDuration)
    }
  val episodeDurationMs by
    remember(playbackStateFlow, readerPost.id, readerPost.audioDuration) {
        playbackStateFlow
          .map { state -> state.resolveEpisodeDuration(readerPost.id, readerPost.audioDuration) }
          .distinctUntilChanged()
      }
      .collectAsStateWithLifecycle(initialActiveDuration)

  val initialChapterIndex =
    remember(playbackStateFlow, readerPost.id, chapters) {
      playbackStateFlow.value.resolveActiveChapterIndex(readerPost.id, chapters)
    }
  val activeChapterIndex by
    remember(playbackStateFlow, readerPost.id, chapters) {
        playbackStateFlow
          .map { state -> state.resolveActiveChapterIndex(readerPost.id, chapters) }
          .distinctUntilChanged()
      }
      .collectAsStateWithLifecycle(initialChapterIndex)

  val topPadding = contentPaddingValues.calculateTopPadding()
  val bottomPadding = contentPaddingValues.calculateBottomPadding()

  val handlePlayClick =
    remember(playbackStateFlow, readerPost.id, onResumeClick, onPlayClick) {
      {
        if (playbackStateFlow.value.playingPostId == readerPost.id) {
          onResumeClick()
        } else {
          onPlayClick()
        }
      }
    }

  val handleSpeedChange: (Float) -> Unit =
    remember(onPlaybackSpeedChange) {
      { currentSpeed -> onPlaybackSpeedChange(nextPlaybackSpeed(currentSpeed)) }
    }

  if (isWideLayout) {
    LaunchedEffect(readerPost.id) { onHeroVisibilityChanged(true) }

    val notesListState = rememberLazyListState()
    val notesPadding =
      remember(topPadding, bottomPadding) {
        PaddingValues(top = topPadding + 16.dp, bottom = bottomPadding + 24.dp)
      }

    Row(modifier = modifier.fillMaxSize()) {
      Box(modifier = Modifier.weight(0.45f).fillMaxHeight()) {
        AmbientImageBackdrop(
          imageUrl = resolvedCoverArtUrl,
          modifier = Modifier.fillMaxWidth().height(380.dp).align(Alignment.TopCenter),
        )

        Column(
          modifier =
            Modifier.fillMaxSize()
              .verticalScroll(rememberScrollState())
              .padding(
                top = topPadding + 16.dp,
                bottom = bottomPadding + 24.dp,
                start = 24.dp,
                end = 24.dp,
              ),
          horizontalAlignment = Alignment.CenterHorizontally,
        ) {
          PodcastHeroIdentity(
            readerPost = readerPost,
            resolvedCoverArtUrl = resolvedCoverArtUrl,
            episodeDurationMs = episodeDurationMs,
            artworkSize = 240.dp,
            fromScreen = fromScreen,
            onBookmarkClick = onBookmarkClick,
            onCommentsClick = onCommentsClick,
            onMarkAsUnread = onMarkAsUnread,
            onImageClick = onImageClick,
          )

          Spacer(Modifier.requiredHeight(16.dp))

          PodcastPlayerControlsBlock(
            readerPost = readerPost,
            isPlayerAvailable = contentType.isPlayerAvailable,
            audioPlayerInstallationHint = audioPlayerInstallationHint,
            playbackStateFlow = playbackStateFlow,
            chapters = chapters,
            onPlayClick = handlePlayClick,
            onPlayAtClick = onPlayAtClick,
            onPauseClick = onPauseClick,
            onSeek = onSeek,
            onSeekForward = onSeekForward,
            onSeekBackward = onSeekBackward,
            onPlaybackSpeedChange = handleSpeedChange,
            onSleepTimerClick = { showSleepTimerSheet = true },
            onOpenWebsite = onOpenWebsite,
          )
        }
      }

      LazyColumn(
        modifier =
          Modifier.weight(0.55f)
            .fillMaxHeight()
            .readerContentWidthAndScrims(
              listState = notesListState,
              topPadding = topPadding,
              onContentWidthChanged = onContentWidthChanged,
            ),
        state = notesListState,
        overscrollEffect = null,
        contentPadding = notesPadding,
      ) {
        podcastBodyItems(
          readerPost = readerPost,
          chapters = chapters,
          selectedTab = effectiveTab,
          onTabSelected = { selectedTab = it },
          activeChapterIndex = activeChapterIndex,
          isPlayerAvailable = contentType.isPlayerAvailable,
          skipSponsorChapters = skipSponsorChapters,
          onToggleSkipSponsorChapters = onToggleSkipSponsorChapters,
          onChapterClick = { chapter ->
            if (contentType.isPlayerAvailable) {
              onChapterClick(chapter)
            }
          },
          markdownContentState = markdownContentState,
          contentParsingProgress = contentParsingProgress,
          markdownComponents = markdownComponents,
        )
      }
    }
  } else {
    val listState = rememberLazyListState()
    val density = LocalDensity.current
    val topPaddingPx = with(density) { topPadding.roundToPx() }

    val isHeroVisible by
      remember(listState, topPaddingPx) {
        derivedStateOf {
          val controlsItem =
            listState.layoutInfo.visibleItemsInfo.firstOrNull { it.key == HERO_CONTROLS_KEY }
          controlsItem != null && (controlsItem.offset + controlsItem.size) > topPaddingPx
        }
      }

    LaunchedEffect(isHeroVisible) { onHeroVisibilityChanged(isHeroVisible) }

    val compactContentPadding =
      remember(bottomPadding) { PaddingValues(bottom = bottomPadding + 24.dp) }

    LazyColumn(
      modifier =
        modifier
          .fillMaxSize()
          .readerContentWidthAndScrims(
            listState = listState,
            topPadding = topPadding,
            onContentWidthChanged = onContentWidthChanged,
          ),
      state = listState,
      overscrollEffect = null,
      contentPadding = compactContentPadding,
    ) {
      item(key = HERO_ARTWORK_KEY, contentType = HERO_ARTWORK_KEY) {
        Box(modifier = Modifier.fillMaxWidth()) {
          AmbientImageBackdrop(
            imageUrl = resolvedCoverArtUrl,
            modifier = Modifier.matchParentSize(),
          )

          Column(
            modifier =
              Modifier.fillMaxWidth()
                .padding(top = topPadding + 12.dp, start = 24.dp, end = 24.dp, bottom = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
          ) {
            PodcastHeroIdentity(
              readerPost = readerPost,
              resolvedCoverArtUrl = resolvedCoverArtUrl,
              episodeDurationMs = episodeDurationMs,
              artworkSize = 224.dp,
              fromScreen = fromScreen,
              onBookmarkClick = onBookmarkClick,
              onCommentsClick = onCommentsClick,
              onMarkAsUnread = onMarkAsUnread,
              onImageClick = onImageClick,
            )
          }
        }
      }

      item(key = HERO_CONTROLS_KEY, contentType = HERO_CONTROLS_KEY) {
        PodcastPlayerControlsBlock(
          modifier = Modifier.padding(horizontal = 24.dp).padding(bottom = 24.dp),
          readerPost = readerPost,
          isPlayerAvailable = contentType.isPlayerAvailable,
          audioPlayerInstallationHint = audioPlayerInstallationHint,
          playbackStateFlow = playbackStateFlow,
          chapters = chapters,
          onPlayClick = handlePlayClick,
          onPlayAtClick = onPlayAtClick,
          onPauseClick = onPauseClick,
          onSeek = onSeek,
          onSeekForward = onSeekForward,
          onSeekBackward = onSeekBackward,
          onPlaybackSpeedChange = handleSpeedChange,
          onSleepTimerClick = { showSleepTimerSheet = true },
          onOpenWebsite = onOpenWebsite,
        )
      }

      podcastBodyItems(
        readerPost = readerPost,
        chapters = chapters,
        selectedTab = effectiveTab,
        onTabSelected = { selectedTab = it },
        activeChapterIndex = activeChapterIndex,
        isPlayerAvailable = contentType.isPlayerAvailable,
        skipSponsorChapters = skipSponsorChapters,
        onToggleSkipSponsorChapters = onToggleSkipSponsorChapters,
        onChapterClick = { chapter ->
          if (contentType.isPlayerAvailable) {
            onChapterClick(chapter)
          }
        },
        markdownContentState = markdownContentState,
        contentParsingProgress = contentParsingProgress,
        markdownComponents = markdownComponents,
      )
    }
  }

  if (showSleepTimerSheet) {
    val initialSelectedOption =
      remember(playbackStateFlow, readerPost.id) {
        playbackStateFlow.value.let {
          if (it.playingPostId == readerPost.id) it.selectedSleepTimerOption
          else SleepTimerOption.None
        }
      }
    val selectedOption by
      remember(playbackStateFlow, readerPost.id) {
          playbackStateFlow
            .map { state ->
              if (state.playingPostId == readerPost.id) {
                state.selectedSleepTimerOption
              } else {
                SleepTimerOption.None
              }
            }
            .distinctUntilChanged()
        }
        .collectAsStateWithLifecycle(initialSelectedOption)

    SleepTimerBottomSheet(
      selectedOption = selectedOption,
      onOptionSelected = {
        onSleepTimerOptionSelected(it)
        showSleepTimerSheet = false
      },
      onDismiss = { showSleepTimerSheet = false },
    )
  }
}

@Composable
private fun PodcastHeroIdentity(
  readerPost: ResolvedPost,
  resolvedCoverArtUrl: String?,
  episodeDurationMs: Long,
  artworkSize: Dp,
  fromScreen: ReaderScreenArgs.FromScreen,
  onBookmarkClick: () -> Unit,
  onCommentsClick: () -> Unit,
  onMarkAsUnread: () -> Unit,
  onImageClick: (ImageClickEvent) -> Unit,
) {
  val colorScheme = AppTheme.colorScheme

  DisableSelection {
    PodcastArtworkHero(
      postId = readerPost.id,
      artworkUrl = resolvedCoverArtUrl,
      feedIcon = readerPost.feedIcon,
      feedHomepageLink = readerPost.feedHomepageLink,
      showFeedFavIcon = readerPost.showFeedFavIcon,
      size = artworkSize,
      onImageClick = onImageClick,
    )
  }

  Spacer(Modifier.requiredHeight(20.dp))

  Text(
    text = readerPost.title.ifBlank { readerPost.description },
    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
    color = colorScheme.onSurface,
    textAlign = TextAlign.Center,
    overflow = TextOverflow.Ellipsis,
  )

  Spacer(Modifier.requiredHeight(12.dp))

  DisableSelection {
    var showDropdown by remember { mutableStateOf(false) }

    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
      Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
        FeedIcon(
          icon = readerPost.feedIcon,
          homepageLink = readerPost.feedHomepageLink,
          showFeedFavIcon = readerPost.showFeedFavIcon,
          contentDescription = null,
          modifier = Modifier.requiredSize(18.dp),
        )

        Spacer(Modifier.requiredWidth(6.dp))

        Text(
          modifier = Modifier.weight(1f, fill = false),
          text = readerPost.feedName,
          style = MaterialTheme.typography.labelMedium,
          color = colorScheme.onSurfaceVariant,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis,
        )

        Text(
          modifier = Modifier.padding(horizontal = 6.dp),
          text = Constants.BULLET_POINT,
          style = MaterialTheme.typography.labelMedium,
          color = colorScheme.outline,
          maxLines = 1,
        )

        Text(
          text = readerPost.date.formatRelativeTime(),
          style = MaterialTheme.typography.labelMedium,
          color = colorScheme.outline,
          maxLines = 1,
        )

        if (episodeDurationMs > 0L) {
          Text(
            modifier = Modifier.padding(horizontal = 6.dp),
            text = Constants.BULLET_POINT,
            style = MaterialTheme.typography.labelMedium,
            color = colorScheme.outline,
            maxLines = 1,
          )

          Text(
            text = formatDuration(episodeDurationMs),
            style = MaterialTheme.typography.labelMedium.copy(fontFeatureSettings = "tnum"),
            color = colorScheme.outline,
            maxLines = 1,
          )
        }
      }

      Spacer(Modifier.requiredWidth(8.dp))

      PostActions(
        postLink = readerPost.link,
        postBookmarked = readerPost.bookmarked,
        postRead = readerPost.read,
        config = ReaderPostMetadataConfig,
        commentsLink = readerPost.commentsLink,
        showDropdown = showDropdown,
        alwaysShowMarkAsUnread = true,
        hideMarkAsOptions = fromScreen == ReaderScreenArgs.FromScreen.Bookmarks,
        onDropdownChange = { showDropdown = it },
        onBookmarkClick = onBookmarkClick,
        onCommentsClick = onCommentsClick,
        togglePostReadClick = onMarkAsUnread,
      )
    }
  }
}

@Composable
private fun PodcastPlayerControlsBlock(
  readerPost: ResolvedPost,
  isPlayerAvailable: Boolean,
  audioPlayerInstallationHint: String?,
  playbackStateFlow: StateFlow<PlaybackState>,
  chapters: List<MediaChapter>,
  onPlayClick: () -> Unit,
  onPlayAtClick: (Long) -> Unit,
  onPauseClick: () -> Unit,
  onSeek: (Long) -> Unit,
  onSeekForward: () -> Unit,
  onSeekBackward: () -> Unit,
  onPlaybackSpeedChange: (Float) -> Unit,
  onSleepTimerClick: () -> Unit,
  onOpenWebsite: () -> Unit,
  modifier: Modifier = Modifier,
) {
  DisableSelection {
    if (isPlayerAvailable) {
      val hasSavedProgress = readerPost.audioProgress > 0L && readerPost.audioDuration > 0L
      val initialShowScrubber =
        remember(playbackStateFlow, readerPost.id, hasSavedProgress) {
          hasSavedProgress ||
            playbackStateFlow.value.let { it.playingPostId == readerPost.id && it.duration > 0L }
        }
      val showScrubber by
        remember(playbackStateFlow, readerPost.id, hasSavedProgress) {
            playbackStateFlow
              .map { state ->
                hasSavedProgress || (state.playingPostId == readerPost.id && state.duration > 0L)
              }
              .distinctUntilChanged()
          }
          .collectAsStateWithLifecycle(initialShowScrubber)

      Column(modifier = modifier.fillMaxWidth()) {
        AnimatedVisibility(
          visible = showScrubber,
          enter = fadeIn() + expandVertically(animationSpec = ExtendedControlsAnimationSpec),
          exit = fadeOut() + shrinkVertically(animationSpec = ExtendedControlsAnimationSpec),
        ) {
          MediaPlaybackScrubber(
            modifier = Modifier.padding(bottom = 16.dp),
            playbackStateFlow = playbackStateFlow,
            settledPostId = readerPost.id,
            onSeek = onSeek,
            chapters = chapters,
            fallbackPosition = readerPost.audioProgress,
            fallbackDuration = readerPost.audioDuration,
            onSeekBeforePlay = onPlayAtClick,
          )
        }

        MediaTransportControls(
          playbackStateFlow = playbackStateFlow,
          settledPostId = readerPost.id,
          onPlayClick = onPlayClick,
          onPauseClick = onPauseClick,
          onSeekForward = onSeekForward,
          onSeekBackward = onSeekBackward,
          onPlaybackSpeedChange = onPlaybackSpeedChange,
          onSleepTimerClick = onSleepTimerClick,
          alwaysShowExtendedControls = showScrubber,
        )
      }
    } else {
      Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
      ) {
        MediaUnavailableBanner(installationHint = audioPlayerInstallationHint)

        if (readerPost.link.isNotBlank()) {
          InverseButton(
            text = stringResource(Res.string.openWebsite),
            icon = TwineIcons.OpenBrowser,
            onClick = onOpenWebsite,
          )
        }
      }
    }
  }
}

private fun LazyListScope.podcastBodyItems(
  readerPost: ResolvedPost,
  chapters: List<MediaChapter>,
  selectedTab: PodcastContentTab,
  onTabSelected: (PodcastContentTab) -> Unit,
  activeChapterIndex: Int,
  isPlayerAvailable: Boolean,
  skipSponsorChapters: Boolean,
  onToggleSkipSponsorChapters: (Boolean) -> Unit,
  onChapterClick: (MediaChapter) -> Unit,
  markdownContentState: State,
  contentParsingProgress: ReaderProcessingProgress,
  markdownComponents: MarkdownComponents,
) {
  if (chapters.isNotEmpty()) {
    item(key = "podcast-content-tabs", contentType = "podcast-content-tabs") {
      DisableSelection {
        val showNotesLabel = stringResource(Res.string.readerShowNotes)
        val chaptersLabel = stringResource(Res.string.readerChapters)
        val items =
          remember(selectedTab, showNotesLabel, chaptersLabel) {
            listOf(
              ToggleableButtonItem(
                label = showNotesLabel,
                isSelected = selectedTab == PodcastContentTab.ShowNotes,
                identifier = PodcastContentTab.ShowNotes,
              ),
              ToggleableButtonItem(
                label = chaptersLabel,
                isSelected = selectedTab == PodcastContentTab.Chapters,
                identifier = PodcastContentTab.Chapters,
              ),
            )
          }

        ToggleableButtonGroup(
          modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp).padding(bottom = 16.dp),
          items = items,
          onItemSelected = { item ->
            val target = item.identifier as? PodcastContentTab ?: return@ToggleableButtonGroup
            onTabSelected(target)
          },
        )
      }
    }
  }

  when (selectedTab) {
    PodcastContentTab.Chapters if chapters.isNotEmpty() -> {
      if (chapters.any { it.isSponsor }) {
        item(key = "podcast-skip-sponsors-toggle", contentType = "podcast-skip-sponsors-toggle") {
          DisableSelection {
            Row(
              modifier =
                Modifier.fillMaxWidth()
                  .padding(horizontal = 24.dp)
                  .padding(bottom = 12.dp)
                  .clip(ChapterRowShape)
                  .background(AppTheme.colorScheme.surfaceContainerLow)
                  .clickable { onToggleSkipSponsorChapters(!skipSponsorChapters) }
                  .padding(horizontal = 16.dp, vertical = 8.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
              Text(
                modifier = Modifier.weight(1f),
                text = stringResource(Res.string.readerSkipSponsorChapters),
                style = MaterialTheme.typography.labelLarge,
                color = AppTheme.colorScheme.onSurface,
              )

              Switch(checked = skipSponsorChapters, onCheckedChange = onToggleSkipSponsorChapters)
            }
          }
        }
      }

      itemsIndexed(
        items = chapters,
        key = { index, chapter -> "chapter:$index:${chapter.timestampMs}" },
        contentType = { _, _ -> "podcast-chapter" },
      ) { index, chapter ->
        DisableSelection {
          PodcastChapterRow(
            chapter = chapter,
            isActive = index == activeChapterIndex,
            enabled = isPlayerAvailable,
            onClick = { onChapterClick(chapter) },
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 2.dp),
          )
        }
      }
    }
    else -> {
      if (contentParsingProgress == ReaderProcessingProgress.Loading) {
        item(key = "progress-indicator", contentType = "progress-indicator") {
          ReaderProgressIndicator()
        }
      }

      if (markdownContentState is State.Success) {
        readerMarkdownItems(
          postId = readerPost.id,
          markdownContentState = markdownContentState,
          markdownComponents = markdownComponents,
        )
      }
    }
  }
}

@Composable
private fun PodcastChapterRow(
  chapter: MediaChapter,
  isActive: Boolean,
  enabled: Boolean,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val colorScheme = AppTheme.colorScheme
  val rowBackground =
    if (isActive) {
      colorScheme.surfaceContainerLow
    } else {
      colorScheme.backdrop
    }
  val pillBackground =
    when {
      isActive -> colorScheme.primaryContainer
      chapter.isSponsor -> colorScheme.surfaceContainer
      else -> colorScheme.surfaceContainerHigh
    }
  val pillTextColor =
    if (isActive) {
      colorScheme.onPrimaryContainer
    } else {
      colorScheme.onSurfaceVariant
    }
  val titleColor =
    when {
      isActive -> colorScheme.primary
      chapter.isSponsor -> colorScheme.onSurfaceVariant
      else -> colorScheme.onSurface
    }

  Row(
    modifier =
      modifier
        .fillMaxWidth()
        .clip(ChapterRowShape)
        .background(rowBackground)
        .semantics { role = Role.Button }
        .clickable(enabled = enabled, onClick = onClick)
        .padding(horizontal = 12.dp, vertical = 10.dp),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(12.dp),
  ) {
    Box(
      modifier =
        Modifier.background(pillBackground, ChapterTimestampShape)
          .padding(horizontal = 10.dp, vertical = 4.dp),
      contentAlignment = Alignment.Center,
    ) {
      Text(
        text = chapter.formattedTime,
        style = MaterialTheme.typography.labelMedium.copy(fontFeatureSettings = "tnum"),
        color = pillTextColor,
        maxLines = 1,
      )
    }

    Text(
      modifier = Modifier.weight(1f),
      text = chapter.title,
      style =
        if (isActive) {
          MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
        } else {
          MaterialTheme.typography.bodyMedium
        },
      color = titleColor,
      maxLines = 2,
      overflow = TextOverflow.Ellipsis,
    )

    if (chapter.isSponsor) {
      Box(
        modifier =
          Modifier.background(colorScheme.secondary.copy(alpha = 0.16f), ChapterTimestampShape)
            .padding(horizontal = 8.dp, vertical = 2.dp),
        contentAlignment = Alignment.Center,
      ) {
        Text(
          text = stringResource(Res.string.readerChapterSponsor),
          style = MaterialTheme.typography.labelSmall,
          color = colorScheme.onSurfaceVariant,
          maxLines = 1,
        )
      }
    }
  }
}

private fun PlaybackState.resolveEpisodeDuration(postId: String, fallbackDuration: Long): Long {
  return if (playingPostId == postId && duration > 0L) {
    duration
  } else {
    fallbackDuration
  }
}

private fun PlaybackState.resolveActiveChapterIndex(
  postId: String,
  chapters: List<MediaChapter>,
): Int {
  if (playingPostId != postId || chapters.isEmpty()) return -1
  val index = chapters.indexOfLast { it.timestampMs <= currentPosition }
  if (index < 0) return -1
  val endMs = chapters[index].endTimestampMs
  return if (endMs != null && currentPosition >= endMs) -1 else index
}
