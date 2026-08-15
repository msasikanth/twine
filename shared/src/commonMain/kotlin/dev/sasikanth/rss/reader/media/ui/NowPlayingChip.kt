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
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
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
)

@Composable
internal fun NowPlayingChip(
  playbackState: StateFlow<PlaybackState>,
  onPlayPauseClick: () -> Unit,
  onOpenClick: () -> Unit,
  onStopClick: () -> Unit,
  modifier: Modifier = Modifier,
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
            )
          }
          .distinctUntilChanged()
      }
      .collectAsStateWithLifecycle(NowPlayingChipState())

  AnimatedVisibility(
    visible = chipState.hasTrack,
    enter = fadeIn() + expandHorizontally(expandFrom = Alignment.Start),
    exit = fadeOut() + shrinkHorizontally(shrinkTowards = Alignment.Start),
    modifier = modifier,
  ) {
    val shape = CircleShape
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

    Row(
      modifier =
        Modifier.padding(start = 8.dp, end = 8.dp)
          .clip(CircleShape)
          .background(containerColor)
          .padding(CONTAINER_PADDING),
      horizontalArrangement = Arrangement.spacedBy(6.dp),
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Box(modifier = Modifier.size(48.dp), contentAlignment = Alignment.Center) {
        Box(
          modifier =
            Modifier.requiredSize(48.dp)
              .clip(shape)
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
              },
          contentAlignment = Alignment.Center,
        ) {
          val coverUrl = chipState.coverUrl
          if (!coverUrl.isNullOrBlank()) {
            AsyncImage(
              url = coverUrl,
              contentDescription = null,
              contentScale = ContentScale.Crop,
              modifier = Modifier.requiredSize(48.dp),
            )
          }

          if (chipState.isBuffering) {
            Box(modifier = Modifier.requiredSize(48.dp).background(SCRIM))
            CircularProgressIndicator(
              modifier = Modifier.requiredSize(20.dp),
              color = Color.White,
              strokeWidth = 2.dp,
            )
          }
        }
      }

      Box(
        modifier =
          Modifier.requiredSize(44.dp)
            .clip(CircleShape)
            .clickable(onClick = onPlayPauseClick)
            .semantics {
              role = Role.Button
              contentDescription = controlLabel
            },
        contentAlignment = Alignment.Center,
      ) {
        Icon(
          modifier = Modifier.requiredSize(if (chipState.isPlaying) 22.dp else 26.dp),
          imageVector = if (chipState.isPlaying) TwineIcons.Pause else TwineIcons.Play,
          contentDescription = null,
          tint = contentColor,
        )
      }
    }
  }
}

private val SCRIM = Color.Black.copy(alpha = 0.45f)
private val CONTAINER_PADDING = 4.dp
