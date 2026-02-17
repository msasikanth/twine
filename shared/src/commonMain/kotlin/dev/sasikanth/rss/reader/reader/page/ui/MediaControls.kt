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

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandIn
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.graphics.shapes.Morph
import dev.sasikanth.rss.reader.media.PlaybackState
import dev.sasikanth.rss.reader.media.SleepTimerOption
import dev.sasikanth.rss.reader.resources.icons.Forward30
import dev.sasikanth.rss.reader.resources.icons.Pause
import dev.sasikanth.rss.reader.resources.icons.Play
import dev.sasikanth.rss.reader.resources.icons.Replay30
import dev.sasikanth.rss.reader.resources.icons.Timer
import dev.sasikanth.rss.reader.resources.icons.TwineIcons
import dev.sasikanth.rss.reader.ui.AppTheme
import dev.sasikanth.rss.reader.utils.toShape
import org.jetbrains.compose.resources.stringResource
import twine.shared.generated.resources.Res
import twine.shared.generated.resources.pause
import twine.shared.generated.resources.play
import twine.shared.generated.resources.playback_speed
import twine.shared.generated.resources.seek_backward
import twine.shared.generated.resources.seek_forward
import twine.shared.generated.resources.sleep_timer
import twine.shared.generated.resources.sleep_timer_end_of_track
import twine.shared.generated.resources.sleep_timer_minutes
import twine.shared.generated.resources.sleep_timer_off

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
internal fun MediaControls(
  playbackState: PlaybackState,
  onPlayClick: () -> Unit,
  onPauseClick: () -> Unit,
  onSeek: (Long) -> Unit,
  onSeekForward: () -> Unit,
  onSeekBackward: () -> Unit,
  onPlaybackSpeedChange: (Float) -> Unit,
  onSleepTimerClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val haptic = LocalHapticFeedback.current
  val isPlaying = playbackState.isPlaying || playbackState.buffering
  val progress =
    if (playbackState.duration > 0) {
      (playbackState.currentPosition.toFloat() / playbackState.duration.toFloat()).coerceIn(0f, 1f)
    } else {
      0f
    }
  val showExtendedControls = playbackState.duration > 0
  val extendedControlsAnimationSpec =
    spring<IntSize>(stiffness = Spring.StiffnessLow, dampingRatio = Spring.DampingRatioMediumBouncy)

  Column(
    modifier =
      modifier
        .fillMaxWidth()
        .background(AppTheme.colorScheme.surface, RoundedCornerShape(16.dp))
        .padding(16.dp),
    verticalArrangement = Arrangement.spacedBy(if (showExtendedControls) 16.dp else 0.dp),
  ) {
    AnimatedVisibility(
      visible = showExtendedControls,
      enter = fadeIn() + expandVertically(animationSpec = extendedControlsAnimationSpec),
      exit = fadeOut() + shrinkVertically(animationSpec = extendedControlsAnimationSpec),
    ) {
      Column {
        val sliderColors =
          SliderDefaults.colors(
            activeTrackColor = AppTheme.colorScheme.primaryContainer,
            inactiveTrackColor = AppTheme.colorScheme.surfaceContainerHigh,
          )
        Slider(
          modifier = Modifier.padding(top = 8.dp),
          value = progress,
          onValueChange = {
            val newPosition = (it * playbackState.duration).toLong()
            val currentPosition = (progress * playbackState.duration).toLong()
            if (newPosition / 1000 != currentPosition / 1000) {
              haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            }
            onSeek(newPosition)
          },
          thumb = {
            Box(
              modifier =
                Modifier.requiredSize(24.dp)
                  .shadow(elevation = 2.dp, shape = CircleShape)
                  .background(AppTheme.colorScheme.inverseSurface, CircleShape)
                  .border(1.dp, AppTheme.colorScheme.secondary, CircleShape)
            )
          },
          track = {
            SliderDefaults.Track(sliderState = it, thumbTrackGapSize = 0.dp, colors = sliderColors)
          },
        )

        Row(modifier = Modifier.fillMaxWidth()) {
          Text(
            text = formatDuration(playbackState.currentPosition),
            style = MaterialTheme.typography.labelSmall,
            color = AppTheme.colorScheme.onSurfaceVariant,
          )
          Spacer(Modifier.weight(1f))
          Text(
            text = formatDuration(playbackState.duration),
            style = MaterialTheme.typography.labelSmall,
            color = AppTheme.colorScheme.onSurfaceVariant,
          )
        }
      }
    }

    Row(
      modifier = Modifier.fillMaxWidth(),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.SpaceEvenly,
    ) {
      AnimatedVisibility(
        visible = showExtendedControls,
        enter =
          fadeIn() +
            expandIn(
              animationSpec = extendedControlsAnimationSpec,
              expandFrom = Alignment.Center,
              clip = false,
            ),
        exit =
          fadeOut() +
            shrinkOut(
              animationSpec = extendedControlsAnimationSpec,
              shrinkTowards = Alignment.Center,
              clip = false,
            ),
      ) {
        TextButton(
          onClick = {
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            onPlaybackSpeedChange(playbackState.playbackSpeed)
          }
        ) {
          AnimatedContent(
            targetState = playbackState.playbackSpeed,
            transitionSpec = {
              (fadeIn() + scaleIn() + slideInVertically()).togetherWith(
                (fadeOut() + scaleOut() + slideOutVertically { it / 2 })
              )
            },
          ) {
            Text(
              text = stringResource(Res.string.playback_speed, it),
              style = MaterialTheme.typography.labelLarge,
              color = AppTheme.colorScheme.onSurfaceVariant,
            )
          }
        }
      }

      AnimatedVisibility(
        visible = showExtendedControls,
        enter =
          fadeIn() +
            expandIn(
              animationSpec = extendedControlsAnimationSpec,
              expandFrom = Alignment.Center,
              clip = false,
            ),
        exit =
          fadeOut() +
            shrinkOut(
              animationSpec = extendedControlsAnimationSpec,
              shrinkTowards = Alignment.Center,
              clip = false,
            ),
      ) {
        IconButton(
          onClick = {
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            onSeekBackward()
          }
        ) {
          Icon(
            imageVector = TwineIcons.Replay30,
            contentDescription = stringResource(Res.string.seek_backward),
            tint = AppTheme.colorScheme.onSurfaceVariant,
          )
        }
      }

      val progress by animateFloatAsState(if (isPlaying) 1f else 0f)
      val buttonSize by
        animateDpAsState(
          if (isPlaying) {
            64.dp
          } else {
            56.dp
          },
          animationSpec =
            spring(
              stiffness = Spring.StiffnessMedium,
              dampingRatio = Spring.DampingRatioMediumBouncy,
            ),
        )
      val fabMorph by remember {
        derivedStateOf { Morph(start = MaterialShapes.Circle, end = MaterialShapes.Cookie9Sided) }
      }
      val infiniteTransition = rememberInfiniteTransition(label = "playButtonRotation")
      val rotationAngle by
        infiniteTransition.animateFloat(
          initialValue = 0f,
          targetValue = 360f,
          animationSpec =
            infiniteRepeatable(animation = tween(durationMillis = 6000, easing = LinearEasing)),
          label = "playButtonRotatingAngle",
        )

      Box(contentAlignment = Alignment.Center, modifier = Modifier.size(64.dp)) {
        // Rotating shape background
        Box(
          modifier =
            Modifier.size(buttonSize)
              .graphicsLayer { rotationZ = if (isPlaying) rotationAngle else 0f }
              .background(AppTheme.colorScheme.primaryContainer, fabMorph.toShape(progress))
        )

        // Non-rotating icon
        Box(
          modifier =
            Modifier.matchParentSize()
              .clip(fabMorph.toShape(progress))
              .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = {
                  haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                  if (isPlaying) onPauseClick() else onPlayClick()
                },
              ),
          contentAlignment = Alignment.Center,
        ) {
          if (playbackState.buffering) {
            CircularProgressIndicator(
              modifier = Modifier.size(32.dp),
              color = AppTheme.colorScheme.onPrimaryContainer,
              strokeWidth = 2.dp,
            )
          } else {
            Icon(
              modifier = Modifier.size(32.dp),
              imageVector = if (isPlaying) TwineIcons.Pause else TwineIcons.Play,
              contentDescription =
                if (isPlaying) stringResource(Res.string.pause)
                else stringResource(Res.string.play),
              tint = AppTheme.colorScheme.onPrimaryContainer,
            )
          }
        }
      }

      AnimatedVisibility(
        visible = showExtendedControls,
        enter =
          fadeIn() +
            expandIn(
              animationSpec = extendedControlsAnimationSpec,
              expandFrom = Alignment.Center,
              clip = false,
            ),
        exit =
          fadeOut() +
            shrinkOut(
              animationSpec = extendedControlsAnimationSpec,
              shrinkTowards = Alignment.Center,
              clip = false,
            ),
      ) {
        IconButton(
          onClick = {
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            onSeekForward()
          }
        ) {
          Icon(
            imageVector = TwineIcons.Forward30,
            contentDescription = stringResource(Res.string.seek_forward),
            tint = AppTheme.colorScheme.onSurfaceVariant,
          )
        }
      }

      AnimatedVisibility(
        visible = showExtendedControls,
        enter =
          fadeIn() +
            expandIn(
              animationSpec = extendedControlsAnimationSpec,
              expandFrom = Alignment.Center,
              clip = false,
            ),
        exit =
          fadeOut() +
            shrinkOut(
              animationSpec = extendedControlsAnimationSpec,
              shrinkTowards = Alignment.Center,
              clip = false,
            ),
      ) {
        IconButton(
          onClick = {
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            onSleepTimerClick()
          }
        ) {
          val sleepTimerRemaining = playbackState.sleepTimerRemaining
          val text =
            if (sleepTimerRemaining != null && sleepTimerRemaining > 0) {
              formatDuration(sleepTimerRemaining)
            } else if (sleepTimerRemaining == -1L) {
              stringResource(Res.string.sleep_timer_end_of_track)
            } else {
              stringResource(Res.string.sleep_timer)
            }

          Icon(
            imageVector = TwineIcons.Timer,
            contentDescription = text,
            tint = AppTheme.colorScheme.onSurfaceVariant,
          )
        }
      }
    }
  }
}

internal fun formatDuration(duration: Long): String {
  val seconds = (duration / 1000) % 60
  val minutes = (duration / (1000 * 60)) % 60
  val hours = (duration / (1000 * 60 * 60))

  return if (hours > 0) {
    "${hours.toString().padStart(2, '0')}:${minutes.toString().padStart(2, '0')}:${seconds.toString().padStart(2, '0')}"
  } else {
    "${minutes.toString().padStart(2, '0')}:${seconds.toString().padStart(2, '0')}"
  }
}

@Composable
internal fun SleepTimerBottomSheet(
  playbackState: PlaybackState,
  onOptionSelected: (SleepTimerOption) -> Unit,
  onDismiss: () -> Unit,
) {
  ModalBottomSheet(
    onDismissRequest = onDismiss,
    containerColor = AppTheme.colorScheme.surfaceContainerLowest,
    contentColor = AppTheme.colorScheme.onSurface,
    sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
  ) {
    Column(
      modifier =
        Modifier.fillMaxWidth().padding(horizontal = 24.dp).padding(top = 8.dp, bottom = 48.dp),
      verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
      Text(
        text = stringResource(Res.string.sleep_timer),
        style = MaterialTheme.typography.titleLarge,
        modifier = Modifier.padding(bottom = 16.dp),
      )

      val currentOption = playbackState.selectedSleepTimerOption

      SleepTimerOptionItem(
        label = stringResource(Res.string.sleep_timer_off),
        isSelected = currentOption is SleepTimerOption.None,
        onClick = { onOptionSelected(SleepTimerOption.None) },
      )

      SleepTimerOptionItem(
        label = stringResource(Res.string.sleep_timer_end_of_track),
        isSelected = currentOption is SleepTimerOption.EndOfTrack,
        onClick = { onOptionSelected(SleepTimerOption.EndOfTrack) },
      )

      listOf(15, 30, 45, 60).forEach { minutes ->
        val option = SleepTimerOption.Minutes(minutes)
        SleepTimerOptionItem(
          label = stringResource(Res.string.sleep_timer_minutes, minutes),
          isSelected = currentOption == option,
          onClick = { onOptionSelected(option) },
        )
      }
    }
  }
}

@Composable
internal fun SleepTimerOptionItem(label: String, isSelected: Boolean, onClick: () -> Unit) {
  Row(
    modifier =
      Modifier.fillMaxWidth()
        .clip(RoundedCornerShape(12.dp))
        .clickable(onClick = onClick)
        .padding(vertical = 12.dp),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(16.dp),
  ) {
    RadioButton(
      selected = isSelected,
      onClick = null,
      colors =
        RadioButtonDefaults.colors(
          selectedColor = AppTheme.colorScheme.primary,
          unselectedColor = AppTheme.colorScheme.outline,
        ),
    )

    Text(
      text = label,
      style = MaterialTheme.typography.bodyLarge,
      color = if (isSelected) AppTheme.colorScheme.primary else AppTheme.colorScheme.onSurface,
    )
  }
}
