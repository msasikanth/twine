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
package dev.sasikanth.rss.reader.feeds.ui.sheet

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.backhandler.BackHandler
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.shadow.Shadow
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalGraphicsContext
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.lerp
import androidx.compose.ui.util.lerp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.sasikanth.rss.reader.feeds.FeedsEvent
import dev.sasikanth.rss.reader.feeds.FeedsViewModel
import dev.sasikanth.rss.reader.feeds.ui.sheet.collapsed.BottomSheetCollapsedContent
import dev.sasikanth.rss.reader.feeds.ui.sheet.expanded.BottomSheetExpandedContent
import dev.sasikanth.rss.reader.ui.AppTheme
import dev.sasikanth.rss.reader.utils.inverse

internal val BOTTOM_SHEET_PEEK_HEIGHT = 80.dp
internal val BOTTOM_SHEET_CORNER_SIZE = 32.dp
private val BOTTOM_SHEET_HORIZONTAL_PADDING = 32.dp

@OptIn(ExperimentalComposeUiApi::class)
@Composable
internal fun FeedsBottomSheet(
  feedsViewModel: FeedsViewModel,
  bottomSheetProgress: () -> Float,
  openFeedInfoSheet: (id: String) -> Unit,
  openGroupScreen: (id: String) -> Unit,
  openGroupSelectionSheet: () -> Unit,
  openAddFeedScreen: () -> Unit,
  openPaywall: () -> Unit,
  openFeeds: () -> Unit,
  closeFeeds: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val focusManager = LocalFocusManager.current
  val state by feedsViewModel.state.collectAsStateWithLifecycle()

  BackHandler(enabled = state.isInMultiSelectMode) {
    feedsViewModel.dispatch(FeedsEvent.CancelSourcesSelection)
  }

  val isParentThemeDark = AppTheme.isDark
  val collapsedSheetBackgroundColor = AppTheme.colorScheme.bottomSheet
  val collapsedSheetBorderColor = AppTheme.colorScheme.bottomSheetBorder
  val (shadowColor1, shadowColor2) =
    remember {
      if (isParentThemeDark) {
        Pair(Color.Black.copy(alpha = 0.6f), Color.Black.copy(alpha = 0.24f))
      } else {
        Pair(Color.Black.copy(alpha = 0.4f), Color.Black.copy(alpha = 0.16f))
      }
    }

  val animatedProgress by
    animateFloatAsState(
      targetValue = bottomSheetProgress(),
      animationSpec =
        spring(
          dampingRatio = Spring.DampingRatioMediumBouncy,
          stiffness = Spring.StiffnessMediumLow,
        ),
      label = "Bouncy Progress",
    )

  val isExpanding by remember {
    derivedStateOf { bottomSheetProgress() > 0f || animatedProgress > 0f }
  }
  val isCollapsing by remember {
    derivedStateOf { bottomSheetProgress() < 1f || animatedProgress < 1f }
  }

  LaunchedEffect(isCollapsing) { focusManager.clearFocus() }

  val bouncyProgressLambda = remember(animatedProgress) { { animatedProgress } }

  AppTheme(useDarkTheme = true) {
    Box(modifier = modifier.fillMaxSize()) {
      BottomSheetBackground(
        bottomSheetProgress = bouncyProgressLambda,
        shadowColor1 = shadowColor1,
        shadowColor2 = shadowColor2,
        collapsedSheetBackgroundColor = collapsedSheetBackgroundColor,
        collapsedSheetBorderColor = collapsedSheetBorderColor,
      )

      Column(
        modifier =
          Modifier.fillMaxSize().graphicsLayer {
            if (animatedProgress < 0f) {
              translationY = -animatedProgress * 40.dp.toPx()
            }
          }
      ) {
        Box(modifier = Modifier.fillMaxSize()) {
          if (isExpanding) {
            val statusBarPadding = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
            val paddingTop =
              lerp(start = 16.dp, stop = statusBarPadding + 16.dp, fraction = animatedProgress)

            BottomSheetExpandedContent(
              modifier =
                Modifier.fillMaxSize().padding(top = paddingTop).graphicsLayer {
                  alpha =
                    if (animatedProgress <= 0.25f) {
                      0f
                    } else {
                      animatedProgress.coerceIn(0f, 1f)
                    }
                },
              viewModel = feedsViewModel,
              openFeedInfoSheet = openFeedInfoSheet,
              openGroupScreen = openGroupScreen,
              openGroupSelectionSheet = openGroupSelectionSheet,
              openAddFeedScreen = openAddFeedScreen,
              openPaywall = openPaywall,
              closeFeeds = closeFeeds,
            )
          }

          if (isCollapsing) {
            BottomSheetCollapsedContent(
              modifier =
                Modifier.padding(horizontal = BOTTOM_SHEET_HORIZONTAL_PADDING)
                  .clip(RoundedCornerShape(BOTTOM_SHEET_CORNER_SIZE))
                  .graphicsLayer { alpha = (animatedProgress * 5f).coerceIn(0f, 1f).inverse() },
              pinnedSources = state.pinnedSources,
              activeSource = state.activeSource,
              isParentThemeDark = isParentThemeDark,
              canShowUnreadPostsCount = state.canShowUnreadPostsCount,
              onSourceClick = { feed -> feedsViewModel.dispatch(FeedsEvent.OnSourceClick(feed)) },
              onHomeSelected = { feedsViewModel.dispatch(FeedsEvent.OnHomeSelected) },
              openFeeds = openFeeds,
            )
          }
        }
      }
    }
  }
}

@Composable
private fun BottomSheetBackground(
  bottomSheetProgress: () -> Float,
  shadowColor1: Color,
  shadowColor2: Color,
  collapsedSheetBackgroundColor: Color,
  collapsedSheetBorderColor: Color,
  modifier: Modifier = Modifier,
) {
  val shadowContext = LocalGraphicsContext.current.shadowContext

  val quantizedProgress by
    remember(bottomSheetProgress) {
      derivedStateOf { (bottomSheetProgress().coerceIn(0f, 1f) * 100).toInt() / 100f }
    }

  val shadow1Painter =
    remember(quantizedProgress, shadowColor1) {
      val cornerRadiusDp = BOTTOM_SHEET_CORNER_SIZE * quantizedProgress.inverse()
      shadowContext.createDropShadowPainter(
        shape = RoundedCornerShape(cornerRadiusDp),
        shadow =
          Shadow(offset = DpOffset(x = 0.dp, y = 16.dp), radius = 32.dp, color = shadowColor1),
      )
    }

  val shadow2Painter =
    remember(quantizedProgress, shadowColor2) {
      val cornerRadiusDp = BOTTOM_SHEET_CORNER_SIZE * quantizedProgress.inverse()
      shadowContext.createDropShadowPainter(
        shape = RoundedCornerShape(cornerRadiusDp),
        shadow = Shadow(offset = DpOffset(x = 0.dp, y = 4.dp), radius = 8.dp, color = shadowColor2),
      )
    }

  Spacer(
    modifier =
      modifier.fillMaxSize().drawBehind {
        val progress = bottomSheetProgress()
        val coercedProgress = progress.coerceIn(0f, 1f)
        val collapsedSheetHeight = BOTTOM_SHEET_PEEK_HEIGHT.toPx()
        val targetSheetHeight = size.height
        val sheetHeight =
          if (progress > 0f) {
            lerp(
              start = collapsedSheetHeight,
              stop = targetSheetHeight,
              fraction = (progress * 2f).coerceAtMost(1f),
            )
          } else {
            collapsedSheetHeight
          }

        val sheetHorizontalPadding =
          lerp(
            start = BOTTOM_SHEET_HORIZONTAL_PADDING,
            stop = 0.dp,
            fraction = (coercedProgress * 4f).coerceAtMost(1f),
          )

        val offset =
          Offset(
            x = sheetHorizontalPadding.toPx(),
            y =
              (1.dp.toPx() * coercedProgress.inverse()) +
                (if (progress < 0f) -progress * 40.dp.toPx() else 0f),
          )
        val sheetSize = Size(size.width - (offset.x * 2), sheetHeight)

        val cornerRadiusDp = BOTTOM_SHEET_CORNER_SIZE * coercedProgress.inverse()
        val cornerRadius = CornerRadius(x = cornerRadiusDp.toPx(), y = cornerRadiusDp.toPx())
        val backgroundColor = lerp(collapsedSheetBackgroundColor, Color.Black, coercedProgress)
        val borderColor =
          lerp(
            start = collapsedSheetBorderColor,
            stop = backgroundColor,
            fraction = coercedProgress,
          )

        translate(left = offset.x, top = offset.y) {
          with(shadow1Painter) { draw(sheetSize) }
          with(shadow2Painter) { draw(sheetSize) }
        }

        drawRoundRect(
          color = backgroundColor,
          cornerRadius = cornerRadius,
          size = sheetSize,
          topLeft = offset,
        )

        drawRoundRect(
          color = borderColor,
          style = Stroke(width = 1.dp.toPx()),
          cornerRadius = cornerRadius,
          size = sheetSize,
          topLeft = offset,
        )
      }
  )
}
