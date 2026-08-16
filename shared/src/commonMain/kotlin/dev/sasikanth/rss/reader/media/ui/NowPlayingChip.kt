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

package dev.sasikanth.rss.reader.media.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColor
import androidx.compose.animation.core.updateTransition
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.sasikanth.rss.reader.components.image.AsyncImage
import dev.sasikanth.rss.reader.media.PlaybackState
import dev.sasikanth.rss.reader.resources.icons.Pause
import dev.sasikanth.rss.reader.resources.icons.Play
import dev.sasikanth.rss.reader.resources.icons.TwineIcons
import dev.sasikanth.rss.reader.ui.AppTheme
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import org.jetbrains.compose.resources.stringResource
import twine.shared.generated.resources.Res
import twine.shared.generated.resources.nowPlayingOpenEpisode
import twine.shared.generated.resources.nowPlayingStop
import twine.shared.generated.resources.pause
import twine.shared.generated.resources.play

@Immutable
private data class NowPlayingChipState(
  val hasTrack: Boolean = false,
  val isPlaying: Boolean = false,
  val isBuffering: Boolean = false,
  val coverUrl: String? = null,
  val title: String? = null,
  val artist: String? = null,
)

@Composable
internal fun NowPlayingChip(
  playbackState: StateFlow<PlaybackState>,
  onPlayPauseClick: () -> Unit,
  onOpenClick: () -> Unit,
  onStopClick: () -> Unit,
  modifier: Modifier = Modifier,
  expanded: Boolean = false,
) {
  val chipState by
    remember(playbackState) {
        playbackState
          .map {
            NowPlayingChipState(
              hasTrack = it.playingPostId != null,
              isPlaying = it.isPlaying,
              isBuffering = it.buffering,
              coverUrl = it.coverUrl,
              title = it.title,
              artist = it.artist,
            )
          }
          .distinctUntilChanged()
      }
      .collectAsStateWithLifecycle(NowPlayingChipState())

  val progress =
    remember(playbackState) {
        playbackState
          .map {
            if (it.duration > 0L) {
              (it.currentPosition.toFloat() / it.duration).coerceIn(0f, 1f)
            } else {
              0f
            }
          }
          .distinctUntilChanged()
      }
      .collectAsStateWithLifecycle(0f)

  AnimatedVisibility(
    visible = chipState.hasTrack,
    enter = fadeIn() + expandHorizontally(expandFrom = Alignment.Start),
    exit = fadeOut() + shrinkHorizontally(shrinkTowards = Alignment.Start),
    modifier = modifier,
  ) {
    val haptic = LocalHapticFeedback.current
    val controlLabel =
      if (chipState.isPlaying) stringResource(Res.string.pause) else stringResource(Res.string.play)
    val openLabel = stringResource(Res.string.nowPlayingOpenEpisode)
    val stopLabel = stringResource(Res.string.nowPlayingStop)

    val transition = updateTransition(chipState.isPlaying)
    val containerColor by
      transition.animateColor {
        if (it) {
          AppTheme.colorScheme.primaryContainer
        } else {
          AppTheme.colorScheme.surfaceContainerHighest
        }
      }
    val contentColor by
      transition.animateColor {
        if (it) {
          AppTheme.colorScheme.onPrimaryContainer
        } else {
          AppTheme.colorScheme.onSurface
        }
      }

    val artwork =
      @Composable {
        Artwork(
          coverUrl = chipState.coverUrl,
          isBuffering = chipState.isBuffering,
          progress = progress,
          interactive = !expanded,
          ringColor = if (expanded) containerColor else contentColor,
          trackColor =
            if (expanded) {
              AppTheme.colorScheme.onSurfaceVariant.copy(alpha = RING_TRACK_ALPHA)
            } else {
              contentColor.copy(alpha = RING_TRACK_ALPHA)
            },
          openLabel = openLabel,
          stopLabel = stopLabel,
          onOpenClick = onOpenClick,
          onStopClick = {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            onStopClick()
          },
        )
      }

    if (expanded) {
      Row(
        modifier =
          Modifier.fillMaxSize()
            .combinedClickable(
              onClick = onOpenClick,
              onLongClick = {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                onStopClick()
              },
              onLongClickLabel = stopLabel,
            )
            .semantics {
              role = Role.Button
              contentDescription = openLabel
            }
            .padding(horizontal = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
      ) {
        artwork()

        Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
          val title = chipState.title
          if (!title.isNullOrBlank()) {
            Text(
              modifier =
                if (chipState.isPlaying) {
                  Modifier.basicMarquee(iterations = Int.MAX_VALUE)
                } else {
                  Modifier
                },
              text = title,
              style = MaterialTheme.typography.labelLarge,
              color = AppTheme.colorScheme.onSurface,
              textAlign = TextAlign.Center,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis,
            )
          }

          val artist = chipState.artist
          if (!artist.isNullOrBlank()) {
            Text(
              text = artist,
              style = MaterialTheme.typography.labelSmall,
              color = AppTheme.colorScheme.onSurfaceVariant,
              textAlign = TextAlign.Center,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis,
            )
          }
        }

        Box(modifier = Modifier.size(ARTWORK_SLOT_SIZE), contentAlignment = Alignment.Center) {
          PlayPauseButton(
            isPlaying = chipState.isPlaying,
            contentColor = contentColor,
            backgroundColor = containerColor,
            label = controlLabel,
            onClick = onPlayPauseClick,
          )
        }
      }
    } else {
      Row(
        modifier =
          Modifier.padding(horizontal = 8.dp)
            .clip(CircleShape)
            .background(containerColor)
            .padding(CONTAINER_PADDING),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
      ) {
        artwork()

        PlayPauseButton(
          isPlaying = chipState.isPlaying,
          contentColor = contentColor,
          backgroundColor = null,
          label = controlLabel,
          onClick = onPlayPauseClick,
        )
      }
    }
  }
}

@Composable
private fun Artwork(
  coverUrl: String?,
  isBuffering: Boolean,
  progress: State<Float>,
  interactive: Boolean,
  ringColor: Color,
  trackColor: Color,
  openLabel: String,
  stopLabel: String,
  onOpenClick: () -> Unit,
  onStopClick: () -> Unit,
) {
  Box(
    modifier =
      Modifier.size(ARTWORK_SLOT_SIZE).drawWithCache {
        val strokeWidth = RING_STROKE_WIDTH.toPx()
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

          val sweep = progress.value * 360f
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
      modifier =
        Modifier.requiredSize(ARTWORK_SIZE)
          .clip(CircleShape)
          .then(
            if (interactive) {
              Modifier.combinedClickable(
                  onClick = onOpenClick,
                  onLongClick = onStopClick,
                  onLongClickLabel = stopLabel,
                )
                .semantics {
                  role = Role.Button
                  contentDescription = openLabel
                }
            } else {
              Modifier
            }
          ),
      contentAlignment = Alignment.Center,
    ) {
      if (!coverUrl.isNullOrBlank()) {
        AsyncImage(
          url = coverUrl,
          contentDescription = null,
          contentScale = ContentScale.Crop,
          modifier = Modifier.requiredSize(ARTWORK_SIZE),
        )
      }

      if (isBuffering) {
        Box(modifier = Modifier.requiredSize(ARTWORK_SIZE).background(SCRIM))
        CircularProgressIndicator(
          modifier = Modifier.requiredSize(20.dp),
          color = Color.White,
          strokeWidth = 2.dp,
        )
      }
    }
  }
}

@Composable
private fun PlayPauseButton(
  isPlaying: Boolean,
  contentColor: Color,
  backgroundColor: Color?,
  label: String,
  onClick: () -> Unit,
) {
  Box(
    modifier =
      Modifier.requiredSize(44.dp)
        .clip(CircleShape)
        .then(if (backgroundColor != null) Modifier.background(backgroundColor) else Modifier)
        .clickable(onClick = onClick)
        .semantics {
          role = Role.Button
          contentDescription = label
        },
    contentAlignment = Alignment.Center,
  ) {
    Icon(
      modifier = Modifier.requiredSize(if (isPlaying) 22.dp else 26.dp),
      imageVector = if (isPlaying) TwineIcons.Pause else TwineIcons.Play,
      contentDescription = null,
      tint = contentColor,
    )
  }
}

private val SCRIM = Color.Black.copy(alpha = 0.45f)
private val CONTAINER_PADDING = 4.dp
private val ARTWORK_SIZE = 48.dp
private val ARTWORK_SLOT_SIZE = 54.dp
private val RING_STROKE_WIDTH = 2.dp
private const val RING_TRACK_ALPHA = 0.3f
