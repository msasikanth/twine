/*
 * Copyright 2023 Sasikanth Miriyampalli
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package dev.sasikanth.rss.reader.feeds.ui.sheet

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
private val BOTTOM_SHEET_CORNER_SIZE = 32.dp
private val BOTTOM_SHEET_HORIZONTAL_PADDING = 32.dp

@OptIn(ExperimentalComposeUiApi::class)
@Composable
internal fun FeedsBottomSheet(
  feedsViewModel: FeedsViewModel,
  darkTheme: Boolean,
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
  val shadowContext = LocalGraphicsContext.current.shadowContext
  val state by feedsViewModel.state.collectAsStateWithLifecycle()

  BackHandler(enabled = state.isInMultiSelectMode) {
    feedsViewModel.dispatch(FeedsEvent.CancelSourcesSelection)
  }

  val collapsedSheetBackgroundColor = AppTheme.colorScheme.bottomSheet
  val collapsedSheetBorderColor = AppTheme.colorScheme.bottomSheetBorder
  val (shadowColor1, shadowColor2) =
    remember {
      if (darkTheme) {
        Pair(Color.Black.copy(alpha = 0.6f), Color.Black.copy(alpha = 0.24f))
      } else {
        Pair(Color.Black.copy(alpha = 0.4f), Color.Black.copy(alpha = 0.16f))
      }
    }

  val isExpanding by remember { derivedStateOf { bottomSheetProgress() > 0f } }
  val isCollapsing by remember { derivedStateOf { bottomSheetProgress() < 1f } }

  LaunchedEffect(isCollapsing) { focusManager.clearFocus() }

  AppTheme(useDarkTheme = true) {
    Column(
      modifier =
        modifier.fillMaxSize().drawBehind {
          val bottomSheetProgress = bottomSheetProgress()

          val collapsedSheetHeight = BOTTOM_SHEET_PEEK_HEIGHT.toPx()
          val targetSheetHeight = size.height
          val sheetHeight =
            lerp(
              start = collapsedSheetHeight,
              stop = targetSheetHeight,
              fraction = (bottomSheetProgress * 2f).coerceAtMost(1f)
            )
          val sheetHorizontalPadding =
            lerp(
              start = BOTTOM_SHEET_HORIZONTAL_PADDING,
              stop = 0.dp,
              fraction = (bottomSheetProgress * 4f).coerceAtMost(1f),
            )
          val offset =
            Offset(x = sheetHorizontalPadding.toPx(), 1.dp.toPx() * bottomSheetProgress.inverse())
          val sheetSize = Size(size.width - (offset.x * 2), sheetHeight)

          val cornerRadiusDp = BOTTOM_SHEET_CORNER_SIZE * bottomSheetProgress.inverse()
          val cornerRadius =
            CornerRadius(
              x = cornerRadiusDp.toPx(),
              y = cornerRadiusDp.toPx(),
            )
          val backgroundColor =
            lerp(
              collapsedSheetBackgroundColor,
              Color.Black,
              bottomSheetProgress,
            )
          val borderColor =
            lerp(
              start = collapsedSheetBorderColor,
              stop = backgroundColor,
              fraction = bottomSheetProgress,
            )

          val shadow1Painter =
            shadowContext.createDropShadowPainter(
              shape = RoundedCornerShape(cornerRadiusDp),
              shadow =
                Shadow(
                  offset = DpOffset(x = offset.x.toDp(), y = offset.y.toDp() + 16.dp),
                  radius = 32.dp,
                  color = shadowColor1,
                )
            )

          val shadow2Painter =
            shadowContext.createDropShadowPainter(
              shape = RoundedCornerShape(cornerRadiusDp),
              shadow =
                Shadow(
                  offset = DpOffset(x = offset.x.toDp(), y = offset.y.toDp() + 4.dp),
                  radius = 8.dp,
                  color = shadowColor2,
                )
            )

          with(shadow1Painter) { draw(sheetSize) }

          with(shadow2Painter) { draw(sheetSize) }

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
        },
    ) {
      Box(modifier = Modifier.fillMaxSize()) {
        if (isExpanding) {
          val statusBarPadding = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
          val paddingTop =
            lerp(start = 16.dp, stop = statusBarPadding + 16.dp, fraction = bottomSheetProgress())

          BottomSheetExpandedContent(
            modifier =
              Modifier.fillMaxSize().padding(top = paddingTop).graphicsLayer {
                alpha = bottomSheetProgress()
              },
            viewModel = feedsViewModel,
            openFeedInfoSheet = openFeedInfoSheet,
            openGroupScreen = openGroupScreen,
            openGroupSelectionSheet = openGroupSelectionSheet,
            openAddFeedScreen = openAddFeedScreen,
            openPaywall = openPaywall,
            closeFeeds = closeFeeds
          )
        }

        if (isCollapsing) {
          BottomSheetCollapsedContent(
            modifier =
              Modifier.padding(horizontal = BOTTOM_SHEET_HORIZONTAL_PADDING)
                .clip(RoundedCornerShape(BOTTOM_SHEET_CORNER_SIZE))
                .graphicsLayer { alpha = (bottomSheetProgress() * 5f).inverse() },
            pinnedSources = state.pinnedSources,
            activeSource = state.activeSource,
            canShowUnreadPostsCount = state.canShowUnreadPostsCount,
            onSourceClick = { feed -> feedsViewModel.dispatch(FeedsEvent.OnSourceClick(feed)) },
            onHomeSelected = { feedsViewModel.dispatch(FeedsEvent.OnHomeSelected) },
            openFeeds = openFeeds
          )
        }
      }
    }
  }
}
