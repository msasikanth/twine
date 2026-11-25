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

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDp
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.layout
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.sasikanth.rss.reader.resources.icons.ArticleShortcut
import dev.sasikanth.rss.reader.resources.icons.OpenBrowser
import dev.sasikanth.rss.reader.resources.icons.Pause
import dev.sasikanth.rss.reader.resources.icons.Play
import dev.sasikanth.rss.reader.resources.icons.Settings
import dev.sasikanth.rss.reader.resources.icons.TwineIcons
import dev.sasikanth.rss.reader.ui.AppColorScheme
import dev.sasikanth.rss.reader.ui.AppTheme
import org.jetbrains.compose.resources.stringResource
import twine.shared.generated.resources.Res
import twine.shared.generated.resources.cdLoadFullArticle
import twine.shared.generated.resources.cdWatchVideo
import twine.shared.generated.resources.openWebsite
import twine.shared.generated.resources.pause
import twine.shared.generated.resources.play
import twine.shared.generated.resources.readerSettings

private val PillShape = RoundedCornerShape(50)
private val BottomBarHeight = 64.dp
private val ToggleSpringSpec =
  spring<Dp>(stiffness = Spring.StiffnessMedium, dampingRatio = Spring.DampingRatioMediumBouncy)

@Composable
internal fun ReaderViewBottomBar(
  selectedAppColorScheme: AppColorScheme?,
  loadFullArticle: Boolean,
  openInBrowserClick: () -> Unit,
  loadFullArticleClick: () -> Unit,
  openReaderViewSettings: () -> Unit,
) {
  Row(
    modifier = Modifier.wrapContentWidth().height(BottomBarHeight).padding(horizontal = 12.dp),
    horizontalArrangement = Arrangement.spacedBy(8.dp),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    val colorScheme = selectedAppColorScheme ?: AppTheme.colorScheme
    val transition = updateTransition(loadFullArticle)
    val buttonMinWidthState =
      transition.animateDp {
        if (it) {
          52.dp
        } else {
          56.dp
        }
      }
    val readerViewToggleWidthState =
      transition.animateDp(transitionSpec = { ToggleSpringSpec }) {
        if (it) {
          88.dp
        } else {
          72.dp
        }
      }
    val readerViewToggleVerticalPaddingState =
      transition.animateDp(transitionSpec = { ToggleSpringSpec }) {
        if (it) {
          8.dp
        } else {
          12.dp
        }
      }
    val toggleProgressState = transition.animateFloat { if (it) 1f else 0f }

    BottomBarIconButton(
      modifier = Modifier.padding(vertical = 12.dp),
      label = stringResource(Res.string.openWebsite),
      icon = TwineIcons.OpenBrowser,
      onClick = openInBrowserClick,
      contentColor = { colorScheme.onSurfaceVariant },
      minWidth = { buttonMinWidthState.value },
    )

    BottomBarToggleIconButton(
      label = stringResource(Res.string.cdLoadFullArticle),
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
        lerpColor(colorScheme.onSurface, colorScheme.onPrimaryContainer, toggleProgressState.value)
      },
      minWidth = { readerViewToggleWidthState.value },
    )

    BottomBarIconButton(
      modifier = Modifier.padding(vertical = 12.dp),
      label = stringResource(Res.string.readerSettings),
      icon = TwineIcons.Settings,
      onClick = openReaderViewSettings,
      contentColor = { colorScheme.onSurfaceVariant },
      minWidth = { buttonMinWidthState.value },
    )
  }
}

@Composable
internal fun ReaderVideoBottomBar(
  selectedAppColorScheme: AppColorScheme?,
  openInBrowserClick: () -> Unit,
  watchVideoClick: () -> Unit,
  openReaderViewSettings: () -> Unit,
) {
  Row(
    modifier = Modifier.wrapContentWidth().height(BottomBarHeight).padding(horizontal = 12.dp),
    horizontalArrangement = Arrangement.spacedBy(8.dp),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    val colorScheme = selectedAppColorScheme ?: AppTheme.colorScheme

    BottomBarIconButton(
      modifier = Modifier.padding(vertical = 12.dp),
      label = stringResource(Res.string.openWebsite),
      icon = TwineIcons.OpenBrowser,
      onClick = openInBrowserClick,
      contentColor = { colorScheme.onSurfaceVariant },
      minWidth = { 52.dp },
    )

    BottomBarToggleIconButton(
      label = stringResource(Res.string.cdWatchVideo),
      icon = TwineIcons.Play,
      onClick = watchVideoClick,
      verticalPadding = { 8.dp },
      backgroundColor = { colorScheme.primaryContainer },
      contentColor = { colorScheme.onPrimaryContainer },
      minWidth = { 88.dp },
    )

    BottomBarIconButton(
      modifier = Modifier.padding(vertical = 12.dp),
      label = stringResource(Res.string.readerSettings),
      icon = TwineIcons.Settings,
      onClick = openReaderViewSettings,
      contentColor = { colorScheme.onSurfaceVariant },
      minWidth = { 52.dp },
    )
  }
}

@Composable
internal fun ReaderAudioBottomBar(
  selectedAppColorScheme: AppColorScheme?,
  isPlaying: Boolean,
  isBuffering: Boolean,
  openInBrowserClick: () -> Unit,
  playPauseClick: () -> Unit,
  openReaderViewSettings: () -> Unit,
) {
  Row(
    modifier = Modifier.wrapContentWidth().height(BottomBarHeight).padding(horizontal = 12.dp),
    horizontalArrangement = Arrangement.spacedBy(8.dp),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    val colorScheme = selectedAppColorScheme ?: AppTheme.colorScheme
    val transition = updateTransition(isPlaying)
    val playPauseWidthState =
      transition.animateDp(transitionSpec = { ToggleSpringSpec }) {
        if (it) {
          88.dp
        } else {
          72.dp
        }
      }
    val playPauseVerticalPaddingState =
      transition.animateDp(transitionSpec = { ToggleSpringSpec }) {
        if (it) {
          8.dp
        } else {
          12.dp
        }
      }
    val toggleProgressState = transition.animateFloat { if (it) 1f else 0f }

    BottomBarIconButton(
      modifier = Modifier.padding(vertical = 12.dp),
      label = stringResource(Res.string.openWebsite),
      icon = TwineIcons.OpenBrowser,
      onClick = openInBrowserClick,
      contentColor = { colorScheme.onSurfaceVariant },
      minWidth = { 52.dp },
    )

    BottomBarToggleIconButton(
      label = stringResource(if (isPlaying) Res.string.pause else Res.string.play),
      icon = if (isPlaying) TwineIcons.Pause else TwineIcons.Play,
      onClick = playPauseClick,
      verticalPadding = { playPauseVerticalPaddingState.value },
      backgroundColor = {
        lerpColor(
          colorScheme.surfaceContainerHighest,
          colorScheme.primaryContainer,
          toggleProgressState.value,
        )
      },
      contentColor = {
        lerpColor(colorScheme.onSurface, colorScheme.onPrimaryContainer, toggleProgressState.value)
      },
      minWidth = { playPauseWidthState.value },
      showProgress = isBuffering,
    )

    BottomBarIconButton(
      modifier = Modifier.padding(vertical = 12.dp),
      label = stringResource(Res.string.readerSettings),
      icon = TwineIcons.Settings,
      onClick = openReaderViewSettings,
      contentColor = { colorScheme.onSurfaceVariant },
      minWidth = { 52.dp },
    )
  }
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
