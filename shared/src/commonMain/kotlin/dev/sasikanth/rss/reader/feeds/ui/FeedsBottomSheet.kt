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
package dev.sasikanth.rss.reader.feeds.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.backhandler.BackHandler
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.lerp
import androidx.compose.ui.unit.offset
import androidx.compose.ui.util.lerp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.sasikanth.rss.reader.feeds.FeedsEvent
import dev.sasikanth.rss.reader.feeds.FeedsViewModel
import dev.sasikanth.rss.reader.feeds.ui.expanded.BottomSheetExpandedContent
import dev.sasikanth.rss.reader.ui.AppTheme
import dev.sasikanth.rss.reader.utils.inverse

private val BOTTOM_SHEET_CORNER_SIZE = 36.dp
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
  modifier: Modifier = Modifier,
) {
  val focusManager = LocalFocusManager.current
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
    Scaffold(
      modifier =
        modifier.fillMaxSize().drawBehind {
          val bottomSheetProgress = bottomSheetProgress()

          val collapsedSheetHeight = 100.dp.toPx()
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
          val offset = Offset(x = sheetHorizontalPadding.toPx(), 0f)
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

          drawRoundRect(
            color = backgroundColor,
            cornerRadius = cornerRadius,
            size = sheetSize,
            topLeft = offset,
          )

          drawRoundRect(
            color = borderColor,
            style = Stroke(width = 2.dp.toPx()),
            cornerRadius = cornerRadius,
            size = sheetSize,
            topLeft = offset,
          )
        },
      topBar = { BottomSheetHandle(progress = bottomSheetProgress()) },
      containerColor = Color.Transparent,
      contentColor = Color.Unspecified,
    ) { innerPadding ->
      Box(modifier = Modifier.fillMaxSize().padding(top = innerPadding.calculateTopPadding())) {
        Box(
          modifier =
            Modifier.align(Alignment.TopCenter)
              .padding(horizontal = BOTTOM_SHEET_HORIZONTAL_PADDING)
              .requiredHeight(100.dp)
              .fillMaxWidth()
              .dropShadow(shape = RoundedCornerShape(50)) {
                offset = Offset(x = 0f, y = 16.dp.toPx())
                radius = 32.dp.toPx()
                color = shadowColor1
              }
              .dropShadow(shape = RoundedCornerShape(50)) {
                offset = Offset(x = 0f, y = 4.dp.toPx())
                radius = 8.dp.toPx()
                color = shadowColor2
              },
        )

        if (isExpanding) {
          BottomSheetExpandedContent(
            modifier =
              Modifier.fillMaxSize().padding(top = 12.dp).graphicsLayer {
                alpha = bottomSheetProgress()
              },
            viewModel = feedsViewModel,
            openFeedInfoSheet = openFeedInfoSheet,
            openGroupScreen = openGroupScreen,
            openGroupSelectionSheet = openGroupSelectionSheet,
            openAddFeedScreen = openAddFeedScreen,
            openPaywall = openPaywall,
          )
        }

        if (isCollapsing) {
          BottomSheetCollapsedContent(
            modifier =
              Modifier.padding(horizontal = BOTTOM_SHEET_HORIZONTAL_PADDING).graphicsLayer {
                alpha = (bottomSheetProgress() * 5f).inverse()
              },
            pinnedSources = state.pinnedSources,
            numberOfFeeds = state.numberOfFeeds,
            activeSource = state.activeSource,
            canShowUnreadPostsCount = state.canShowUnreadPostsCount,
            onSourceClick = { feed -> feedsViewModel.dispatch(FeedsEvent.OnSourceClick(feed)) },
            onHomeSelected = { feedsViewModel.dispatch(FeedsEvent.OnHomeSelected) },
          )
        }
      }
    }
  }
}
