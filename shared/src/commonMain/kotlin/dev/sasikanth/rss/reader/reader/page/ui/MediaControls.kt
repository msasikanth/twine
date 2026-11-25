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
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberSliderState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.graphics.shapes.Morph
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.sasikanth.rss.reader.components.IconButton
import dev.sasikanth.rss.reader.components.ModalSheet
import dev.sasikanth.rss.reader.media.PlaybackState
import dev.sasikanth.rss.reader.media.SleepTimerOption
import dev.sasikanth.rss.reader.resources.icons.Forward30
import dev.sasikanth.rss.reader.resources.icons.Pause
import dev.sasikanth.rss.reader.resources.icons.Play
import dev.sasikanth.rss.reader.resources.icons.Replay30
import dev.sasikanth.rss.reader.resources.icons.Timer
import dev.sasikanth.rss.reader.resources.icons.TwineIcons
import dev.sasikanth.rss.reader.ui.AppTheme
import dev.sasikanth.rss.reader.ui.LocalTranslucentStyles
import dev.sasikanth.rss.reader.utils.toClipEntry
import dev.sasikanth.rss.reader.utils.toComposePath
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import twine.shared.generated.resources.Res
import twine.shared.generated.resources.media_player_unavailable_copied
import twine.shared.generated.resources.media_player_unavailable_message
import twine.shared.generated.resources.media_player_unavailable_title
import twine.shared.generated.resources.pause
import twine.shared.generated.resources.play
import twine.shared.generated.resources.playback_speed
import twine.shared.generated.resources.seek_backward
import twine.shared.generated.resources.seek_forward
import twine.shared.generated.resources.sleep_timer
import twine.shared.generated.resources.sleep_timer_end_of_track
import twine.shared.generated.resources.sleep_timer_minutes
import twine.shared.generated.resources.sleep_timer_off

private val ExtendedControlsAnimationSpec =
  spring<IntSize>(stiffness = Spring.StiffnessLow, dampingRatio = Spring.DampingRatioMediumBouncy)
private val PlayButtonScaleAnimationSpec =
  spring<Float>(stiffness = Spring.StiffnessMedium, dampingRatio = Spring.DampingRatioMediumBouncy)
private val SleepTimerMinutes = listOf(15, 30, 45, 60)
private val SleepTimerItemShape = RoundedCornerShape(12.dp)
private val MediaBannerShape = RoundedCornerShape(16.dp)
private val MediaHintShape = RoundedCornerShape(8.dp)
private const val PLAY_BUTTON_IDLE_SCALE = 56f / 64f

@Immutable
private data class MediaTransportState(
  val isPlaying: Boolean = false,
  val isBuffering: Boolean = false,
  val showExtendedControls: Boolean = false,
  val playbackSpeed: Float = 1f,
)

@Immutable
private data class PlaybackProgress(val currentPosition: Long = 0L, val duration: Long = 0L)

private fun PlaybackState.toMediaTransportState(settledPostId: String): MediaTransportState =
  if (playingPostId == settledPostId) {
    MediaTransportState(
      isPlaying = isPlaying,
      isBuffering = buffering,
      showExtendedControls = duration > 0,
      playbackSpeed = playbackSpeed,
    )
  } else {
    MediaTransportState()
  }

private fun PlaybackState.toPlaybackProgress(settledPostId: String): PlaybackProgress =
  if (playingPostId == settledPostId) {
    PlaybackProgress(currentPosition = currentPosition, duration = duration)
  } else {
    PlaybackProgress()
  }

private fun PlaybackState.toSleepTimerRemaining(settledPostId: String): Long? =
  if (playingPostId == settledPostId) sleepTimerRemaining else null

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
internal fun MediaControls(
  playbackStateFlow: StateFlow<PlaybackState>,
  settledPostId: String,
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
  val initialTransportState =
    remember(playbackStateFlow, settledPostId) {
      playbackStateFlow.value.toMediaTransportState(settledPostId)
    }
  val transportState by
    remember(playbackStateFlow, settledPostId) {
        playbackStateFlow
          .map { state -> state.toMediaTransportState(settledPostId) }
          .distinctUntilChanged()
      }
      .collectAsStateWithLifecycle(initialTransportState)

  val isPlaying = transportState.isPlaying || transportState.isBuffering
  val showExtendedControls = transportState.showExtendedControls

  Column(
    modifier =
      modifier.fillMaxWidth().padding(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 24.dp),
    verticalArrangement = Arrangement.spacedBy(if (showExtendedControls) 16.dp else 0.dp),
  ) {
    AnimatedVisibility(
      visible = showExtendedControls,
      enter = fadeIn() + expandVertically(animationSpec = ExtendedControlsAnimationSpec),
      exit = fadeOut() + shrinkVertically(animationSpec = ExtendedControlsAnimationSpec),
    ) {
      MediaPlaybackScrubber(
        playbackStateFlow = playbackStateFlow,
        settledPostId = settledPostId,
        onSeek = onSeek,
      )
    }

    Row(
      modifier = Modifier.fillMaxWidth(),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.SpaceEvenly,
    ) {
      Row(
        modifier = Modifier.weight(1f),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceEvenly,
      ) {
        AnimatedVisibility(
          visible = showExtendedControls,
          enter =
            fadeIn() +
              expandIn(
                animationSpec = ExtendedControlsAnimationSpec,
                expandFrom = Alignment.Center,
                clip = false,
              ),
          exit =
            fadeOut() +
              shrinkOut(
                animationSpec = ExtendedControlsAnimationSpec,
                shrinkTowards = Alignment.Center,
                clip = false,
              ),
        ) {
          TextButton(
            onClick = {
              haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
              onPlaybackSpeedChange(transportState.playbackSpeed)
            }
          ) {
            AnimatedContent(
              targetState = transportState.playbackSpeed,
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
                animationSpec = ExtendedControlsAnimationSpec,
                expandFrom = Alignment.Center,
                clip = false,
              ),
          exit =
            fadeOut() +
              shrinkOut(
                animationSpec = ExtendedControlsAnimationSpec,
                shrinkTowards = Alignment.Center,
                clip = false,
              ),
        ) {
          IconButton(
            icon = TwineIcons.Replay30,
            contentDescription = stringResource(Res.string.seek_backward),
            onClick = {
              haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
              onSeekBackward()
            },
          )
        }
      }

      val morphProgressState = animateFloatAsState(if (isPlaying) 1f else 0f)
      val buttonScaleState =
        animateFloatAsState(
          targetValue = if (isPlaying) 1f else PLAY_BUTTON_IDLE_SCALE,
          animationSpec = PlayButtonScaleAnimationSpec,
        )
      val fabMorph = remember {
        Morph(start = MaterialShapes.Circle, end = MaterialShapes.Cookie9Sided)
      }
      val rotationAngleState =
        if (isPlaying) {
          val infiniteTransition = rememberInfiniteTransition(label = "playButtonRotation")
          infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 360f,
            animationSpec =
              infiniteRepeatable(animation = tween(durationMillis = 6000, easing = LinearEasing)),
            label = "playButtonRotatingAngle",
          )
        } else {
          null
        }
      val colorScheme = AppTheme.colorScheme

      Box(contentAlignment = Alignment.Center, modifier = Modifier.size(64.dp)) {
        Box(
          modifier =
            Modifier.matchParentSize()
              .graphicsLayer {
                val scale = buttonScaleState.value
                scaleX = scale
                scaleY = scale
                rotationZ = rotationAngleState?.value ?: 0f
              }
              .drawWithCache {
                val path = Path()
                var lastProgress = Float.NaN
                var lastScale = Float.NaN
                onDrawBehind {
                  val progress = morphProgressState.value
                  val scale = size.minDimension
                  if (progress != lastProgress || scale != lastScale) {
                    fabMorph.toComposePath(progress = progress, scale = scale, path = path)
                    lastProgress = progress
                    lastScale = scale
                  }
                  drawPath(path = path, color = colorScheme.primaryContainer)
                }
              }
        )

        Box(
          modifier =
            Modifier.matchParentSize()
              .clip(CircleShape)
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
          if (transportState.isBuffering) {
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

      Row(
        modifier = Modifier.weight(1f),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceEvenly,
      ) {
        AnimatedVisibility(
          visible = showExtendedControls,
          enter =
            fadeIn() +
              expandIn(
                animationSpec = ExtendedControlsAnimationSpec,
                expandFrom = Alignment.Center,
                clip = false,
              ),
          exit =
            fadeOut() +
              shrinkOut(
                animationSpec = ExtendedControlsAnimationSpec,
                shrinkTowards = Alignment.Center,
                clip = false,
              ),
        ) {
          IconButton(
            icon = TwineIcons.Forward30,
            contentDescription = stringResource(Res.string.seek_forward),
            onClick = {
              haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
              onSeekForward()
            },
          )
        }

        AnimatedVisibility(
          visible = showExtendedControls,
          enter =
            fadeIn() +
              expandIn(
                animationSpec = ExtendedControlsAnimationSpec,
                expandFrom = Alignment.Center,
                clip = false,
              ),
          exit =
            fadeOut() +
              shrinkOut(
                animationSpec = ExtendedControlsAnimationSpec,
                shrinkTowards = Alignment.Center,
                clip = false,
              ),
        ) {
          SleepTimerButton(
            playbackStateFlow = playbackStateFlow,
            settledPostId = settledPostId,
            onSleepTimerClick = onSleepTimerClick,
          )
        }
      }
    }
  }
}

@Composable
private fun MediaPlaybackScrubber(
  playbackStateFlow: StateFlow<PlaybackState>,
  settledPostId: String,
  onSeek: (Long) -> Unit,
) {
  val haptic = LocalHapticFeedback.current
  val initialProgress =
    remember(playbackStateFlow, settledPostId) {
      playbackStateFlow.value.toPlaybackProgress(settledPostId)
    }
  val playbackProgress by
    remember(playbackStateFlow, settledPostId) {
        playbackStateFlow
          .map { state -> state.toPlaybackProgress(settledPostId) }
          .distinctUntilChanged()
      }
      .collectAsStateWithLifecycle(initialProgress)

  // While the user is scrubbing, the slider follows the drag instead of the player
  // position. Seeking happens once on release; issuing a seek per drag tick fights
  // async seeks (AVPlayer reports stale positions mid-seek, snapping the thumb back).
  var scrubProgress by remember { mutableStateOf<Float?>(null) }
  val duration = playbackProgress.duration
  val playerProgress =
    if (duration > 0) {
      (playbackProgress.currentPosition.toFloat() / duration.toFloat()).coerceIn(0f, 1f)
    } else {
      0f
    }
  val progress = scrubProgress ?: playerProgress

  Column {
    val translucentStyle = LocalTranslucentStyles.current
    val sliderColors =
      SliderDefaults.colors(
        activeTrackColor = AppTheme.colorScheme.primaryContainer,
        inactiveTrackColor = translucentStyle.prominent.background,
      )
    val sliderState = rememberSliderState(value = progress)
    sliderState.value = progress

    Slider(
      modifier = Modifier.padding(top = 8.dp),
      state = sliderState,
      onValueChange = {
        val newPosition = (it * duration).toLong()
        val previousPosition = (progress * duration).toLong()
        if (newPosition / 1000 != previousPosition / 1000) {
          haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        }
        scrubProgress = it
      },
      onValueChangeFinished = {
        scrubProgress?.let { onSeek((it * duration).toLong()) }
        scrubProgress = null
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
        SliderDefaults.Track(
          sliderState = it,
          thumbTrackGapSize = 0.dp,
          colors = sliderColors,
          drawStopIndicator = null,
        )
      },
    )

    Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp)) {
      Text(
        text = formatDuration((progress * duration).toLong()),
        style = MaterialTheme.typography.labelSmall,
        color = AppTheme.colorScheme.onSurfaceVariant,
      )
      Spacer(Modifier.weight(1f))
      Text(
        text = formatDuration(duration),
        style = MaterialTheme.typography.labelSmall,
        color = AppTheme.colorScheme.onSurfaceVariant,
      )
    }
  }
}

@Composable
private fun SleepTimerButton(
  playbackStateFlow: StateFlow<PlaybackState>,
  settledPostId: String,
  onSleepTimerClick: () -> Unit,
) {
  val haptic = LocalHapticFeedback.current
  val initialSleepTimerRemaining =
    remember(playbackStateFlow, settledPostId) {
      playbackStateFlow.value.toSleepTimerRemaining(settledPostId)
    }
  val sleepTimerRemaining by
    remember(playbackStateFlow, settledPostId) {
        playbackStateFlow
          .map { state -> state.toSleepTimerRemaining(settledPostId) }
          .distinctUntilChanged()
      }
      .collectAsStateWithLifecycle(initialSleepTimerRemaining)

  val text =
    if (sleepTimerRemaining != null && sleepTimerRemaining!! > 0) {
      formatDuration(sleepTimerRemaining!!)
    } else if (sleepTimerRemaining == -1L) {
      stringResource(Res.string.sleep_timer_end_of_track)
    } else {
      stringResource(Res.string.sleep_timer)
    }

  IconButton(
    icon = TwineIcons.Timer,
    contentDescription = text,
    onClick = {
      haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
      onSleepTimerClick()
    },
  )
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
  selectedOption: SleepTimerOption,
  onOptionSelected: (SleepTimerOption) -> Unit,
  onDismiss: () -> Unit,
) {
  ModalSheet(
    onDismissRequest = onDismiss,
    containerColor = AppTheme.colorScheme.surfaceContainerLowest,
    contentColor = AppTheme.colorScheme.onSurface,
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

      SleepTimerOptionItem(
        label = stringResource(Res.string.sleep_timer_off),
        isSelected = selectedOption is SleepTimerOption.None,
        onClick = { onOptionSelected(SleepTimerOption.None) },
      )

      SleepTimerOptionItem(
        label = stringResource(Res.string.sleep_timer_end_of_track),
        isSelected = selectedOption is SleepTimerOption.EndOfTrack,
        onClick = { onOptionSelected(SleepTimerOption.EndOfTrack) },
      )

      SleepTimerMinutes.forEach { minutes ->
        val option = SleepTimerOption.Minutes(minutes)
        SleepTimerOptionItem(
          label = stringResource(Res.string.sleep_timer_minutes, minutes),
          isSelected = selectedOption == option,
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
        .clip(SleepTimerItemShape)
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

@Composable
internal fun MediaUnavailableBanner(installationHint: String?, modifier: Modifier = Modifier) {
  val clipboard = LocalClipboard.current
  val coroutineScope = rememberCoroutineScope()
  var copied by remember { mutableStateOf(false) }

  Column(
    modifier =
      modifier
        .fillMaxWidth()
        .background(AppTheme.colorScheme.surface, MediaBannerShape)
        .padding(16.dp),
    verticalArrangement = Arrangement.spacedBy(8.dp),
  ) {
    Text(
      text = stringResource(Res.string.media_player_unavailable_title),
      style = MaterialTheme.typography.titleMedium,
      color = AppTheme.colorScheme.onSurface,
    )

    Text(
      text = stringResource(Res.string.media_player_unavailable_message),
      style = MaterialTheme.typography.bodyMedium,
      color = AppTheme.colorScheme.onSurfaceVariant,
    )

    if (!installationHint.isNullOrBlank()) {
      Row(
        modifier =
          Modifier.fillMaxWidth()
            .clip(MediaHintShape)
            .background(AppTheme.colorScheme.surfaceContainerHigh)
            .clickable {
              coroutineScope.launch {
                clipboard.setClipEntry(installationHint.toClipEntry())
                copied = true
                delay(2000)
                copied = false
              }
            }
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
      ) {
        Text(
          modifier = Modifier.weight(1f),
          text = installationHint,
          style = MaterialTheme.typography.bodyMedium.copy(fontFamily = FontFamily.Monospace),
          color = AppTheme.colorScheme.onSurface,
        )

        if (copied) {
          Text(
            text = stringResource(Res.string.media_player_unavailable_copied),
            style = MaterialTheme.typography.labelMedium,
            color = AppTheme.colorScheme.primary,
          )
        }
      }
    }
  }
}
