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
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.graphics.shapes.Morph
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.sasikanth.rss.reader.components.AdaptiveSheet
import dev.sasikanth.rss.reader.components.IconButton
import dev.sasikanth.rss.reader.media.PlaybackState
import dev.sasikanth.rss.reader.media.SleepTimerOption
import dev.sasikanth.rss.reader.resources.icons.Forward30
import dev.sasikanth.rss.reader.resources.icons.Pause
import dev.sasikanth.rss.reader.resources.icons.Play
import dev.sasikanth.rss.reader.resources.icons.Replay30
import dev.sasikanth.rss.reader.resources.icons.Timer
import dev.sasikanth.rss.reader.resources.icons.TwineIcons
import dev.sasikanth.rss.reader.ui.AppTheme
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

internal val ExtendedControlsAnimationSpec =
  spring<IntSize>(stiffness = Spring.StiffnessLow, dampingRatio = Spring.DampingRatioMediumBouncy)
private val PlayButtonScaleAnimationSpec =
  spring<Float>(stiffness = Spring.StiffnessMedium, dampingRatio = Spring.DampingRatioMediumBouncy)
private val ScrubberThumbAnimationSpec =
  spring<Dp>(stiffness = Spring.StiffnessMedium, dampingRatio = Spring.DampingRatioMediumBouncy)
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
private data class PlaybackProgress(
  val isSettledPostActive: Boolean = false,
  val currentPosition: Long = 0L,
  val duration: Long = 0L,
)

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
    PlaybackProgress(
      isSettledPostActive = true,
      currentPosition = currentPosition,
      duration = duration,
    )
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
  val initialShowExtendedControls =
    remember(playbackStateFlow, settledPostId) {
      playbackStateFlow.value.toMediaTransportState(settledPostId).showExtendedControls
    }
  val showExtendedControls by
    remember(playbackStateFlow, settledPostId) {
        playbackStateFlow
          .map { state -> state.toMediaTransportState(settledPostId).showExtendedControls }
          .distinctUntilChanged()
      }
      .collectAsStateWithLifecycle(initialShowExtendedControls)

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

    MediaTransportControls(
      playbackStateFlow = playbackStateFlow,
      settledPostId = settledPostId,
      onPlayClick = onPlayClick,
      onPauseClick = onPauseClick,
      onSeekForward = onSeekForward,
      onSeekBackward = onSeekBackward,
      onPlaybackSpeedChange = onPlaybackSpeedChange,
      onSleepTimerClick = onSleepTimerClick,
    )
  }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
internal fun MediaTransportControls(
  playbackStateFlow: StateFlow<PlaybackState>,
  settledPostId: String,
  onPlayClick: () -> Unit,
  onPauseClick: () -> Unit,
  onSeekForward: () -> Unit,
  onSeekBackward: () -> Unit,
  onPlaybackSpeedChange: (Float) -> Unit,
  onSleepTimerClick: () -> Unit,
  modifier: Modifier = Modifier,
  alwaysShowExtendedControls: Boolean = false,
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
  val showExtendedControls = alwaysShowExtendedControls || transportState.showExtendedControls

  Row(
    modifier = modifier.fillMaxWidth(),
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
              if (isPlaying) stringResource(Res.string.pause) else stringResource(Res.string.play),
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

@Composable
internal fun MediaPlaybackScrubber(
  playbackStateFlow: StateFlow<PlaybackState>,
  settledPostId: String,
  onSeek: (Long) -> Unit,
  modifier: Modifier = Modifier,
  fallbackPosition: Long = 0L,
  fallbackDuration: Long = 0L,
  onSeekBeforePlay: ((Long) -> Unit)? = null,
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

  val isSettledPostActive = playbackProgress.isSettledPostActive
  val duration =
    when {
      isSettledPostActive && playbackProgress.duration > 0L -> playbackProgress.duration
      fallbackDuration > 0L -> fallbackDuration
      else -> 0L
    }
  val currentPosition =
    when {
      isSettledPostActive && playbackProgress.duration > 0L -> playbackProgress.currentPosition
      isSettledPostActive && playbackProgress.currentPosition > 0L ->
        playbackProgress.currentPosition
      else -> fallbackPosition
    }.coerceIn(0L, duration.coerceAtLeast(0L))

  // While the user is scrubbing, the slider follows the drag instead of the player
  // position. Seeking happens once on release; issuing a seek per drag tick fights
  // async seeks (AVPlayer reports stale positions mid-seek, snapping the thumb back).
  var scrubProgress by remember(settledPostId) { mutableStateOf<Float?>(null) }
  val playerProgress =
    if (duration > 0L) {
      (currentPosition.toFloat() / duration.toFloat()).coerceIn(0f, 1f)
    } else {
      0f
    }
  val progress = scrubProgress ?: playerProgress
  val canSeek = duration > 0L
  val seekHandler =
    if (!isSettledPostActive && onSeekBeforePlay != null) {
      onSeekBeforePlay
    } else {
      onSeek
    }

  Column(modifier = modifier) {
    val colorScheme = AppTheme.colorScheme
    val sliderColors =
      SliderDefaults.colors(
        activeTrackColor = colorScheme.primary,
        inactiveTrackColor = colorScheme.onSurface.copy(alpha = 0.14f),
        disabledActiveTrackColor = colorScheme.primary.copy(alpha = 0.38f),
        disabledInactiveTrackColor = colorScheme.onSurface.copy(alpha = 0.08f),
      )
    val isScrubbing = scrubProgress != null
    val thumbWidth by
      animateDpAsState(
        targetValue = if (isScrubbing) 6.dp else 4.dp,
        animationSpec = ScrubberThumbAnimationSpec,
      )
    val thumbHeight by
      animateDpAsState(
        targetValue = if (isScrubbing) 24.dp else 20.dp,
        animationSpec = ScrubberThumbAnimationSpec,
      )
    val sliderState = rememberSliderState(value = progress)
    sliderState.value = progress

    Slider(
      modifier = Modifier.padding(top = 8.dp),
      state = sliderState,
      enabled = canSeek,
      onValueChange = {
        val newPosition = (it * duration).toLong()
        val previousPosition = (progress * duration).toLong()
        if (newPosition / 1000 != previousPosition / 1000) {
          haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        }
        scrubProgress = it
      },
      onValueChangeFinished = {
        scrubProgress?.let { seekHandler((it * duration).toLong()) }
        scrubProgress = null
      },
      thumb = {
        Box(
          modifier =
            Modifier.size(width = thumbWidth, height = thumbHeight)
              .background(
                color =
                  if (canSeek) colorScheme.primary else colorScheme.primary.copy(alpha = 0.38f),
                shape = CircleShape,
              )
        )
      },
      track = {
        SliderDefaults.Track(
          modifier = Modifier.height(6.dp),
          sliderState = it,
          enabled = canSeek,
          thumbTrackGapSize = 4.dp,
          colors = sliderColors,
          drawStopIndicator = null,
        )
      },
    )

    val elapsedMillis = (progress * duration).toLong()
    val remainingMillis = (duration - elapsedMillis).coerceAtLeast(0L)
    val timeTextStyle = MaterialTheme.typography.labelSmall.copy(fontFeatureSettings = "tnum")

    Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp)) {
      Text(
        text = formatDuration(elapsedMillis),
        style = timeTextStyle,
        color = AppTheme.colorScheme.onSurfaceVariant,
      )
      Spacer(Modifier.weight(1f))
      Text(
        text = if (duration > 0L) "-${formatDuration(remainingMillis)}" else formatDuration(0L),
        style = timeTextStyle,
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

  val activeRemaining = sleepTimerRemaining
  val activeLabel =
    when {
      activeRemaining != null && activeRemaining > 0L -> formatDuration(activeRemaining)
      activeRemaining == -1L -> stringResource(Res.string.sleep_timer_end_of_track)
      else -> null
    }

  if (activeLabel != null) {
    Column(
      modifier =
        Modifier.heightIn(min = 40.dp)
          .clip(SleepTimerItemShape)
          .clickable {
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            onSleepTimerClick()
          }
          .semantics {
            role = Role.Button
            contentDescription = activeLabel
          }
          .padding(horizontal = 6.dp, vertical = 4.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
      Icon(
        imageVector = TwineIcons.Timer,
        contentDescription = null,
        tint = AppTheme.colorScheme.primary,
        modifier = Modifier.size(18.dp),
      )

      Box(
        modifier =
          Modifier.background(AppTheme.colorScheme.primary.copy(alpha = 0.14f), CircleShape)
            .padding(horizontal = 6.dp, vertical = 1.dp),
        contentAlignment = Alignment.Center,
      ) {
        Text(
          text = activeLabel,
          style = MaterialTheme.typography.labelSmall.copy(fontFeatureSettings = "tnum"),
          color = AppTheme.colorScheme.primary,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis,
        )
      }
    }
  } else {
    IconButton(
      icon = TwineIcons.Timer,
      contentDescription = stringResource(Res.string.sleep_timer),
      onClick = {
        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        onSleepTimerClick()
      },
    )
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
  selectedOption: SleepTimerOption,
  onOptionSelected: (SleepTimerOption) -> Unit,
  onDismiss: () -> Unit,
) {
  AdaptiveSheet(
    onDismissRequest = onDismiss,
    containerColor = AppTheme.colorScheme.surfaceContainerLowest,
    contentColor = AppTheme.colorScheme.onSurface,
  ) {
    Column(
      modifier =
        Modifier.fillMaxWidth()
          .padding(horizontal = 24.dp)
          .padding(top = 16.dp, bottom = 24.dp)
          .navigationBarsPadding(),
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
