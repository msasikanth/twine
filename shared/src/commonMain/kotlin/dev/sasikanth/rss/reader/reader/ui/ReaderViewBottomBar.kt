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

package dev.sasikanth.rss.reader.reader.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDp
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipAnchorPosition
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawOutline
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.layout
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import dev.sasikanth.rss.reader.app.LocalSnackbarHostState
import dev.sasikanth.rss.reader.components.image.AsyncImage
import dev.sasikanth.rss.reader.resources.icons.ArticleShortcut
import dev.sasikanth.rss.reader.resources.icons.Close
import dev.sasikanth.rss.reader.resources.icons.OpenBrowser
import dev.sasikanth.rss.reader.resources.icons.Pause
import dev.sasikanth.rss.reader.resources.icons.Platform
import dev.sasikanth.rss.reader.resources.icons.Play
import dev.sasikanth.rss.reader.resources.icons.RSS
import dev.sasikanth.rss.reader.resources.icons.Settings
import dev.sasikanth.rss.reader.resources.icons.Share
import dev.sasikanth.rss.reader.resources.icons.TwineIcons
import dev.sasikanth.rss.reader.resources.icons.platform
import dev.sasikanth.rss.reader.share.LocalShareHandler
import dev.sasikanth.rss.reader.share.ShareResult
import dev.sasikanth.rss.reader.ui.AppColorScheme
import dev.sasikanth.rss.reader.ui.AppTheme
import dev.sasikanth.rss.reader.utils.Constants
import dev.sasikanth.rss.reader.utils.LocalBlockImage
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import twine.shared.generated.resources.Res
import twine.shared.generated.resources.cdLoadFullArticle
import twine.shared.generated.resources.cdWatchVideo
import twine.shared.generated.resources.nowPlayingStop
import twine.shared.generated.resources.openWebsite
import twine.shared.generated.resources.pause
import twine.shared.generated.resources.play
import twine.shared.generated.resources.readerCollapseEpisodePage
import twine.shared.generated.resources.readerExpandPlayer
import twine.shared.generated.resources.readerLoadEpisodePage
import twine.shared.generated.resources.readerNowPlaying
import twine.shared.generated.resources.readerOpenCurrentEpisode
import twine.shared.generated.resources.readerSettings
import twine.shared.generated.resources.readerWatchVideo
import twine.shared.generated.resources.share
import twine.shared.generated.resources.shareLinkCopied

private val PillShape = RoundedCornerShape(50)
private val TooltipShape = RoundedCornerShape(4.dp)
private val BottomBarHeight = 64.dp
private val IconSlotWidth = 52.dp
private val MiniArtworkSize = 36.dp
private val MiniArtworkSlotSize = 42.dp
private val DockedArtworkSize = 28.dp
private val DockedArtworkSlotSize = 34.dp
private val ToggleSpringSpec =
  spring<Dp>(stiffness = Spring.StiffnessMedium, dampingRatio = Spring.DampingRatioMediumBouncy)
private val PillModeSizeTransform =
  spring<IntSize>(
    dampingRatio = Spring.DampingRatioLowBouncy,
    stiffness = Spring.StiffnessMediumLow,
  )

@Immutable
internal sealed interface ReaderPillMode {
  data class Article(val loadFullArticle: Boolean) : ReaderPillMode

  data class PodcastUtility(val showFullArticle: Boolean) : ReaderPillMode

  data class PodcastMiniPlayer(
    val coverUrl: String?,
    val isPlaying: Boolean,
    val isBuffering: Boolean,
    val progress: Float,
  ) : ReaderPillMode

  data object Video : ReaderPillMode
}

private fun ReaderPillMode.pillModeKey(): Int =
  when (this) {
    is ReaderPillMode.Article -> 0
    is ReaderPillMode.PodcastUtility -> 1
    is ReaderPillMode.PodcastMiniPlayer -> 2
    ReaderPillMode.Video -> 3
  }

@Composable
internal fun ReaderActionPill(
  mode: ReaderPillMode,
  postLink: String,
  selectedAppColorScheme: AppColorScheme?,
  openInBrowserClick: () -> Unit,
  loadFullArticleClick: () -> Unit,
  watchVideoClick: () -> Unit,
  openReaderViewSettings: () -> Unit,
  onMiniPlayPauseClick: () -> Unit,
  onExpandPlayerClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val colorScheme = selectedAppColorScheme ?: AppTheme.colorScheme
  val shareHandler = LocalShareHandler.current
  val snackbarHostState = LocalSnackbarHostState.current
  val coroutineScope = rememberCoroutineScope()
  val linkCopiedLabel = stringResource(Res.string.shareLinkCopied)

  val onShareClick =
    remember(postLink, shareHandler, snackbarHostState, linkCopiedLabel) {
      {
        coroutineScope.launch {
          if (shareHandler.share(postLink) == ShareResult.CopiedToClipboard) {
            snackbarHostState.showSnackbar(linkCopiedLabel)
          }
        }
        Unit
      }
    }

  AnimatedContent(
    modifier = modifier,
    targetState = mode,
    contentKey = { it.pillModeKey() },
    transitionSpec = {
      fadeIn(tween(durationMillis = 180, delayMillis = 60)) togetherWith
        fadeOut(tween(durationMillis = 80)) using
        SizeTransform { _, _ -> PillModeSizeTransform }
    },
    contentAlignment = Alignment.Center,
  ) { targetMode ->
    when (targetMode) {
      is ReaderPillMode.Article -> {
        ArticleShortcutActionPill(
          isFullArticleLoaded = targetMode.loadFullArticle,
          toggleLabel = stringResource(Res.string.cdLoadFullArticle),
          colorScheme = colorScheme,
          openInBrowserClick = openInBrowserClick,
          loadFullArticleClick = loadFullArticleClick,
          openReaderViewSettings = openReaderViewSettings,
        )
      }
      is ReaderPillMode.PodcastUtility -> {
        ArticleShortcutActionPill(
          isFullArticleLoaded = targetMode.showFullArticle,
          toggleLabel =
            stringResource(
              if (targetMode.showFullArticle) {
                Res.string.readerCollapseEpisodePage
              } else {
                Res.string.readerLoadEpisodePage
              }
            ),
          colorScheme = colorScheme,
          openInBrowserClick = openInBrowserClick,
          loadFullArticleClick = loadFullArticleClick,
          openReaderViewSettings = openReaderViewSettings,
        )
      }
      is ReaderPillMode.PodcastMiniPlayer -> {
        PodcastMiniPlayerActionPill(
          mode = targetMode,
          colorScheme = colorScheme,
          onMiniPlayPauseClick = onMiniPlayPauseClick,
          onExpandPlayerClick = onExpandPlayerClick,
          openReaderViewSettings = openReaderViewSettings,
        )
      }
      ReaderPillMode.Video -> {
        VideoActionPill(
          colorScheme = colorScheme,
          onShareClick = onShareClick,
          watchVideoClick = watchVideoClick,
          openReaderViewSettings = openReaderViewSettings,
        )
      }
    }
  }
}

@Composable
private fun ArticleShortcutActionPill(
  isFullArticleLoaded: Boolean,
  toggleLabel: String,
  colorScheme: AppColorScheme,
  openInBrowserClick: () -> Unit,
  loadFullArticleClick: () -> Unit,
  openReaderViewSettings: () -> Unit,
) {
  Row(
    modifier = Modifier.wrapContentWidth().height(BottomBarHeight).padding(horizontal = 12.dp),
    horizontalArrangement = Arrangement.spacedBy(8.dp),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    val transition = updateTransition(isFullArticleLoaded)
    val readerViewToggleWidthState =
      transition.animateDp(transitionSpec = { ToggleSpringSpec }) { if (it) 88.dp else 72.dp }
    val readerViewToggleVerticalPaddingState =
      transition.animateDp(transitionSpec = { ToggleSpringSpec }) { if (it) 8.dp else 12.dp }
    val toggleProgressState = transition.animateFloat { if (it) 1f else 0f }

    PillIconButton(
      label = stringResource(Res.string.openWebsite),
      icon = TwineIcons.OpenBrowser,
      onClick = openInBrowserClick,
      contentColor = { colorScheme.onSurfaceVariant },
    )

    ReaderActionTooltip(label = toggleLabel) {
      BottomBarToggleIconButton(
        label = toggleLabel,
        icon = TwineIcons.ArticleShortcut,
        onClick = loadFullArticleClick,
        verticalPadding = { readerViewToggleVerticalPaddingState.value },
        backgroundColor = {
          lerpColor(
            colorScheme.surfaceContainerHighest,
            colorScheme.primaryContainer,
            toggleProgressState.value,
          )
        },
        contentColor = {
          lerpColor(
            colorScheme.onSurface,
            colorScheme.onPrimaryContainer,
            toggleProgressState.value,
          )
        },
        minWidth = { readerViewToggleWidthState.value },
      )
    }

    PillIconButton(
      label = stringResource(Res.string.readerSettings),
      icon = TwineIcons.Settings,
      onClick = openReaderViewSettings,
      contentColor = { colorScheme.onSurfaceVariant },
    )
  }
}

@Composable
private fun PodcastMiniPlayerActionPill(
  mode: ReaderPillMode.PodcastMiniPlayer,
  colorScheme: AppColorScheme,
  onMiniPlayPauseClick: () -> Unit,
  onExpandPlayerClick: () -> Unit,
  openReaderViewSettings: () -> Unit,
) {
  Row(
    modifier = Modifier.wrapContentWidth().height(BottomBarHeight).padding(horizontal = 12.dp),
    horizontalArrangement = Arrangement.spacedBy(8.dp),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    val transition = updateTransition(mode.isPlaying)
    val playPauseWidthState =
      transition.animateDp(transitionSpec = { ToggleSpringSpec }) { if (it) 88.dp else 72.dp }
    val playPauseVerticalPaddingState =
      transition.animateDp(transitionSpec = { ToggleSpringSpec }) { if (it) 8.dp else 12.dp }
    val toggleProgressState = transition.animateFloat { if (it) 1f else 0f }

    val expandLabel = stringResource(Res.string.readerExpandPlayer)
    Box(modifier = Modifier.width(IconSlotWidth), contentAlignment = Alignment.Center) {
      ReaderActionTooltip(label = expandLabel) {
        CircularProgressArtwork(
          coverUrl = mode.coverUrl,
          progress = mode.progress,
          isBuffering = mode.isBuffering,
          slotSize = MiniArtworkSlotSize,
          artworkSize = MiniArtworkSize,
          fallbackIconSize = 16.dp,
          ringColor = colorScheme.primary,
          trackColor = colorScheme.onSurfaceVariant.copy(alpha = 0.25f),
          modifier =
            Modifier.clip(CircleShape)
              .semantics {
                role = Role.Button
                contentDescription = expandLabel
              }
              .clickable(onClick = onExpandPlayerClick),
        )
      }
    }

    val playPauseLabel = stringResource(if (mode.isPlaying) Res.string.pause else Res.string.play)
    ReaderActionTooltip(label = playPauseLabel) {
      BottomBarToggleIconButton(
        label = playPauseLabel,
        icon = if (mode.isPlaying) TwineIcons.Pause else TwineIcons.Play,
        onClick = onMiniPlayPauseClick,
        verticalPadding = { playPauseVerticalPaddingState.value },
        backgroundColor = {
          lerpColor(
            colorScheme.surfaceContainerHighest,
            colorScheme.primaryContainer,
            toggleProgressState.value,
          )
        },
        contentColor = {
          lerpColor(
            colorScheme.onSurface,
            colorScheme.onPrimaryContainer,
            toggleProgressState.value,
          )
        },
        minWidth = { playPauseWidthState.value },
        showProgress = mode.isBuffering,
      )
    }

    PillIconButton(
      label = stringResource(Res.string.readerSettings),
      icon = TwineIcons.Settings,
      onClick = openReaderViewSettings,
      contentColor = { colorScheme.onSurfaceVariant },
    )
  }
}

@Composable
private fun VideoActionPill(
  colorScheme: AppColorScheme,
  onShareClick: () -> Unit,
  watchVideoClick: () -> Unit,
  openReaderViewSettings: () -> Unit,
) {
  Row(
    modifier = Modifier.wrapContentWidth().height(BottomBarHeight).padding(horizontal = 12.dp),
    horizontalArrangement = Arrangement.spacedBy(8.dp),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    PillIconButton(
      label = stringResource(Res.string.share),
      icon = TwineIcons.Share,
      onClick = onShareClick,
      contentColor = { colorScheme.onSurfaceVariant },
    )

    val watchTooltipLabel = stringResource(Res.string.cdWatchVideo)
    val watchLabel = stringResource(Res.string.readerWatchVideo)
    ReaderActionTooltip(label = watchTooltipLabel) {
      Row(
        modifier =
          Modifier.padding(vertical = 8.dp)
            .fillMaxHeight()
            .clip(PillShape)
            .background(colorScheme.primaryContainer)
            .semantics {
              role = Role.Button
              contentDescription = watchTooltipLabel
            }
            .clickable(onClick = watchVideoClick)
            .padding(horizontal = 20.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
      ) {
        Icon(
          modifier = Modifier.requiredSize(18.dp),
          imageVector = TwineIcons.Play,
          contentDescription = null,
          tint = colorScheme.onPrimaryContainer,
        )

        Text(
          text = watchLabel,
          style = MaterialTheme.typography.labelLarge,
          color = colorScheme.onPrimaryContainer,
          maxLines = 1,
        )
      }
    }

    PillIconButton(
      label = stringResource(Res.string.readerSettings),
      icon = TwineIcons.Settings,
      onClick = openReaderViewSettings,
      contentColor = { colorScheme.onSurfaceVariant },
    )
  }
}

@Composable
private fun PillIconButton(
  label: String,
  icon: ImageVector,
  onClick: () -> Unit,
  contentColor: () -> Color,
) {
  ReaderActionTooltip(label = label) {
    BottomBarIconButton(
      modifier = Modifier.padding(vertical = 12.dp),
      label = label,
      icon = icon,
      onClick = onClick,
      contentColor = contentColor,
      minWidth = { IconSlotWidth },
    )
  }
}

@Composable
internal fun DockedNowPlayingStrip(
  coverUrl: String?,
  title: String?,
  artist: String?,
  isPlaying: Boolean,
  isBuffering: Boolean,
  progress: Float,
  onOpenClick: () -> Unit,
  onPlayPauseClick: () -> Unit,
  onStopClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val colorScheme = AppTheme.colorScheme
  val openLabel = stringResource(Res.string.readerOpenCurrentEpisode)
  val playPauseLabel = stringResource(if (isPlaying) Res.string.pause else Res.string.play)
  val stopLabel = stringResource(Res.string.nowPlayingStop)

  val nowPlayingFallback = stringResource(Res.string.readerNowPlaying)

  val displayLabel =
    remember(title, artist, nowPlayingFallback) {
      when {
        !title.isNullOrBlank() && !artist.isNullOrBlank() ->
          "$title ${Constants.BULLET_POINT} $artist"
        !title.isNullOrBlank() -> title
        !artist.isNullOrBlank() -> artist
        else -> nowPlayingFallback
      }
    }

  Column(modifier = modifier.widthIn(max = 332.dp).fillMaxWidth()) {
    Row(
      modifier =
        Modifier.fillMaxWidth()
          .semantics {
            role = Role.Button
            contentDescription = openLabel
          }
          .clickable(onClick = onOpenClick)
          .padding(start = 18.dp, end = 12.dp, top = 12.dp, bottom = 10.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
      CircularProgressArtwork(
        coverUrl = coverUrl,
        progress = progress,
        isBuffering = isBuffering,
        slotSize = DockedArtworkSlotSize,
        artworkSize = DockedArtworkSize,
        fallbackIconSize = 14.dp,
        ringColor = colorScheme.primary,
        trackColor = colorScheme.onSurfaceVariant.copy(alpha = 0.25f),
      )

      Text(
        modifier =
          Modifier.weight(1f)
            .then(
              if (isPlaying) {
                Modifier.basicMarquee(iterations = Int.MAX_VALUE)
              } else {
                Modifier
              }
            ),
        text = displayLabel,
        style = MaterialTheme.typography.labelMedium,
        color = colorScheme.onSurface,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
      )

      ReaderActionTooltip(label = playPauseLabel) {
        Box(
          modifier =
            Modifier.size(36.dp)
              .clip(CircleShape)
              .semantics {
                role = Role.Button
                contentDescription = playPauseLabel
              }
              .clickable(onClick = onPlayPauseClick),
          contentAlignment = Alignment.Center,
        ) {
          Icon(
            modifier = Modifier.size(18.dp),
            imageVector = if (isPlaying) TwineIcons.Pause else TwineIcons.Play,
            contentDescription = null,
            tint = colorScheme.onSurface,
          )
        }
      }

      ReaderActionTooltip(label = stopLabel) {
        Box(
          modifier =
            Modifier.size(36.dp)
              .clip(CircleShape)
              .semantics {
                role = Role.Button
                contentDescription = stopLabel
              }
              .clickable(onClick = onStopClick),
          contentAlignment = Alignment.Center,
        ) {
          Icon(
            modifier = Modifier.size(18.dp),
            imageVector = TwineIcons.Close,
            contentDescription = null,
            tint = colorScheme.onSurfaceVariant,
          )
        }
      }
    }

    Box(
      modifier =
        Modifier.fillMaxWidth()
          .padding(horizontal = 20.dp)
          .height(1.dp)
          .background(colorScheme.bottomSheetBorder)
    )
  }
}

@Composable
private fun CircularProgressArtwork(
  coverUrl: String?,
  progress: Float,
  isBuffering: Boolean,
  slotSize: Dp,
  artworkSize: Dp,
  fallbackIconSize: Dp,
  ringColor: Color,
  trackColor: Color,
  modifier: Modifier = Modifier,
) {
  val colorScheme = AppTheme.colorScheme
  val shouldBlockImage = LocalBlockImage.current

  Box(
    modifier =
      modifier.size(slotSize).drawWithCache {
        val strokeWidth = 2.dp.toPx()
        val stroke = Stroke(width = strokeWidth, cap = StrokeCap.Round)
        val topLeft = Offset(strokeWidth / 2f, strokeWidth / 2f)
        val arcSize = Size(size.width - strokeWidth, size.height - strokeWidth)

        onDrawBehind {
          drawArc(
            color = trackColor,
            startAngle = 0f,
            sweepAngle = 360f,
            useCenter = false,
            topLeft = topLeft,
            size = arcSize,
            style = stroke,
          )

          val sweep = progress.coerceIn(0f, 1f) * 360f
          if (sweep > 0f) {
            drawArc(
              color = ringColor,
              startAngle = -90f,
              sweepAngle = sweep,
              useCenter = false,
              topLeft = topLeft,
              size = arcSize,
              style = stroke,
            )
          }
        }
      },
    contentAlignment = Alignment.Center,
  ) {
    Box(
      modifier = Modifier.requiredSize(artworkSize).clip(CircleShape),
      contentAlignment = Alignment.Center,
    ) {
      if (!coverUrl.isNullOrBlank() && !shouldBlockImage) {
        AsyncImage(
          url = coverUrl,
          contentDescription = null,
          contentScale = ContentScale.Crop,
          modifier = Modifier.requiredSize(artworkSize),
        )
      } else {
        Box(
          modifier =
            Modifier.requiredSize(artworkSize).background(colorScheme.surfaceContainerHigh),
          contentAlignment = Alignment.Center,
        ) {
          Icon(
            modifier = Modifier.requiredSize(fallbackIconSize),
            imageVector = TwineIcons.RSS,
            contentDescription = null,
            tint = colorScheme.onSurfaceVariant,
          )
        }
      }

      if (isBuffering) {
        Box(
          modifier = Modifier.requiredSize(artworkSize).background(Color.Black.copy(alpha = 0.45f))
        )
        CircularProgressIndicator(
          modifier = Modifier.requiredSize(16.dp),
          color = Color.White,
          strokeWidth = 2.dp,
        )
      }
    }
  }
}

@Composable
private fun ReaderActionTooltip(label: String, content: @Composable () -> Unit) {
  if (platform !is Platform.Desktop) {
    content()
    return
  }

  val colorScheme = AppTheme.colorScheme
  TooltipBox(
    state = rememberTooltipState(),
    positionProvider =
      TooltipDefaults.rememberTooltipPositionProvider(positioning = TooltipAnchorPosition.Above),
    tooltip = {
      Box(
        modifier =
          Modifier.drawWithCache {
              val outline = TooltipShape.createOutline(size, layoutDirection, this)
              onDrawBehind { drawOutline(outline, colorScheme.surface) }
            }
            .padding(8.dp)
      ) {
        Text(
          text = label,
          style = MaterialTheme.typography.labelMedium,
          color = colorScheme.onSurface,
        )
      }
    },
    content = content,
  )
}

@Composable
private fun BottomBarIconButton(
  label: String,
  icon: ImageVector,
  onClick: () -> Unit,
  contentColor: () -> Color,
  minWidth: () -> Dp,
  modifier: Modifier = Modifier,
) {
  val painter = rememberVectorPainter(icon)
  Box(
    modifier =
      Modifier.then(modifier)
        .layout { measurable, constraints ->
          val minWidthPx = minWidth().roundToPx().coerceAtLeast(constraints.minWidth)
          val placeable =
            measurable.measure(
              constraints.copy(
                minWidth = minWidthPx,
                maxWidth = maxOf(constraints.maxWidth, minWidthPx),
              )
            )
          layout(placeable.width, placeable.height) { placeable.placeRelative(0, 0) }
        }
        .clip(PillShape)
        .semantics {
          role = Role.Button
          contentDescription = label
        }
        .clickable { onClick() },
    contentAlignment = Alignment.Center,
  ) {
    Spacer(
      modifier =
        Modifier.padding(vertical = 10.dp).requiredSize(20.dp).drawBehind {
          with(painter) { draw(size = size, colorFilter = ColorFilter.tint(contentColor())) }
        }
    )
  }
}

@Composable
private fun BottomBarToggleIconButton(
  label: String,
  icon: ImageVector,
  onClick: () -> Unit,
  verticalPadding: () -> Dp,
  backgroundColor: () -> Color,
  contentColor: () -> Color,
  minWidth: () -> Dp,
  modifier: Modifier = Modifier,
  showProgress: Boolean = false,
) {
  val painter = rememberVectorPainter(icon)
  Box(
    modifier =
      modifier
        .fillMaxHeight()
        .layout { measurable, constraints ->
          val vPadPx = verticalPadding().roundToPx()
          val minHeightPx = 40.dp.roundToPx()
          val targetHeight = (constraints.maxHeight - vPadPx * 2).coerceAtLeast(minHeightPx)
          val minWidthPx = minWidth().roundToPx()
          val placeable =
            measurable.measure(
              constraints.copy(
                minWidth = minWidthPx,
                maxWidth = maxOf(constraints.maxWidth, minWidthPx),
                minHeight = targetHeight,
                maxHeight = targetHeight,
              )
            )
          val layoutHeight = maxOf(constraints.maxHeight, placeable.height + vPadPx * 2)
          layout(placeable.width, layoutHeight) {
            placeable.placeRelative(0, (layoutHeight - placeable.height) / 2)
          }
        }
        .clip(PillShape)
        .drawBehind { drawRect(backgroundColor()) }
        .semantics {
          role = Role.Button
          contentDescription = label
        }
        .clickable { onClick() },
    contentAlignment = Alignment.Center,
  ) {
    if (showProgress) {
      ToggleProgressIndicator(color = contentColor)
    } else {
      Spacer(
        modifier =
          Modifier.requiredSize(20.dp).drawBehind {
            with(painter) { draw(size = size, colorFilter = ColorFilter.tint(contentColor())) }
          }
      )
    }
  }
}

@Composable
private fun ToggleProgressIndicator(color: () -> Color) {
  CircularProgressIndicator(
    modifier = Modifier.requiredSize(20.dp),
    color = color(),
    strokeWidth = 2.dp,
  )
}

private fun lerpColor(start: Color, stop: Color, fraction: Float): Color =
  when {
    fraction <= 0f -> start
    fraction >= 1f -> stop
    else -> lerp(start, stop, fraction)
  }
