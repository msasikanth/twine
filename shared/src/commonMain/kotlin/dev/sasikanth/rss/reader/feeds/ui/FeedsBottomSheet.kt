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
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.backhandler.BackHandler
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.layout.layout
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
import kotlin.math.roundToInt

private val BOTTOM_SHEET_CORNER_SIZE = 36.dp

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

  val collapsedContentBackgroundColor = AppTheme.colorScheme.bottomSheet
  val collapsedContentBorderColor = AppTheme.colorScheme.bottomSheetBorder
  val (shadowColor1, shadowColor2) =
    if (darkTheme) {
      Pair(Color.Black.copy(alpha = 0.6f), Color.Black.copy(alpha = 0.24f))
    } else {
      Pair(Color.Black.copy(alpha = 0.4f), Color.Black.copy(alpha = 0.16f))
    }
  val homeItemShadowColors =
    arrayOf(
      0.85f to AppTheme.colorScheme.bottomSheet,
      0.9f to AppTheme.colorScheme.bottomSheet.copy(alpha = 0.4f),
      1f to Color.Transparent
    )
  val isCollapsing by remember { derivedStateOf { bottomSheetProgress() < 1f } }

  LaunchedEffect(isCollapsing) { focusManager.clearFocus() }

  AppTheme(useDarkTheme = true) {
    Box(modifier = modifier.fillMaxSize()) {
      Column(
        modifier =
          Modifier.layout { measurable, constraints ->
              val collapsedSheetHeight = 100.dp
              val targetSheetHeight = constraints.maxHeight.toDp()
              val sheetHeight =
                lerp(
                    start = collapsedSheetHeight,
                    stop = targetSheetHeight,
                    fraction = bottomSheetProgress()
                  )
                  .toPx()
              val sheetHorizontalPadding =
                lerp(
                  start = 32.dp,
                  stop = 0.dp,
                  fraction = bottomSheetProgress(),
                )
              val minTargetHeight = sheetHeight.roundToInt()

              val paddedConstraints =
                constraints
                  .offset(
                    horizontal = (sheetHorizontalPadding.roundToPx() * 2).unaryMinus(),
                  )
                  .copy(minHeight = minTargetHeight, maxHeight = minTargetHeight)

              val placeable = measurable.measure(paddedConstraints)
              val layoutWidth =
                lerp(
                  start = if (placeable.width > 0) placeable.width else constraints.maxWidth,
                  stop = constraints.maxWidth,
                  fraction = bottomSheetProgress(),
                )
              val layoutHeight = placeable.height

              layout(layoutWidth, layoutHeight) {
                placeable.placeRelative(sheetHorizontalPadding.roundToPx(), 0)
              }
            }
            .dropShadow(shape = RoundedCornerShape(50)) {
              offset = Offset(x = 0f, y = 16.dp.toPx())
              radius = 32.dp.toPx()
              color = shadowColor1
            }
            .dropShadow(shape = RoundedCornerShape(50)) {
              offset = Offset(x = 0f, y = 4.dp.toPx())
              radius = 8.dp.toPx()
              color = shadowColor2
            }
            .graphicsLayer {
              shape =
                RoundedCornerShape(
                  BOTTOM_SHEET_CORNER_SIZE * bottomSheetProgress().inverse(),
                )
              clip = true
            }
            .drawBehind {
              val cornerRadiusDp = BOTTOM_SHEET_CORNER_SIZE * bottomSheetProgress().inverse()
              val cornerRadius =
                CornerRadius(
                  x = cornerRadiusDp.toPx(),
                  y = cornerRadiusDp.toPx(),
                )
              val backgroundColor =
                lerp(
                  collapsedContentBackgroundColor,
                  Color.Black,
                  bottomSheetProgress(),
                )

              drawRoundRect(color = backgroundColor)

              val borderColor =
                lerp(
                  start = collapsedContentBorderColor,
                  stop = backgroundColor,
                  fraction = bottomSheetProgress(),
                )

              drawRoundRect(
                color = borderColor,
                style = Stroke(width = 2.dp.toPx()),
                cornerRadius = cornerRadius
              )
            },
      ) {
        BottomSheetHandle(progress = bottomSheetProgress())

        BottomSheetCollapsedContent(
          modifier =
            Modifier.layout { measurable, constraints ->
                val placeable = measurable.measure(constraints)
                val height =
                  lerp(
                      placeable.height,
                      0,
                      bottomSheetProgress() * 5f,
                    )
                    .coerceAtLeast(0)

                layout(placeable.width, height) { placeable.place(0, 0) }
              }
              .graphicsLayer { alpha = (bottomSheetProgress() * 5f).inverse() },
          pinnedSources = state.pinnedSources,
          numberOfFeeds = state.numberOfFeeds,
          activeSource = state.activeSource,
          canShowUnreadPostsCount = state.canShowUnreadPostsCount,
          homeItemBackgroundColor = AppTheme.colorScheme.surfaceContainerLow,
          homeItemShadowColors = homeItemShadowColors,
          onSourceClick = { feed -> feedsViewModel.dispatch(FeedsEvent.OnSourceClick(feed)) },
          onHomeSelected = { feedsViewModel.dispatch(FeedsEvent.OnHomeSelected) },
        )

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
    }
  }
}
