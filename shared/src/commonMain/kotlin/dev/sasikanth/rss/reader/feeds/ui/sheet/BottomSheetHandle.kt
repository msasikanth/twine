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

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.layout
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.lerp
import androidx.compose.ui.unit.offset
import dev.sasikanth.rss.reader.ui.AppTheme

private val COLLAPSED_HANDLE_SIZE = 24.dp
private val EXPANDED_HANDLE_SIZE = 64.dp

@Composable
internal fun BottomSheetHandle(
  progress: () -> Float,
  modifier: Modifier = Modifier,
) {
  val statusBarPadding = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()

  Box(modifier = modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
    Box(
      Modifier.layout { measurable, constraints ->
          val progress = progress.invoke()
          val collapsedTopPadding = 12.dp
          val targetTopPadding = statusBarPadding + collapsedTopPadding
          val topPadding =
            lerp(start = collapsedTopPadding, stop = targetTopPadding, fraction = progress)
              .roundToPx()
          val targetHandleSize =
            lerp(
                start = COLLAPSED_HANDLE_SIZE,
                stop = EXPANDED_HANDLE_SIZE,
                fraction = progress,
              )
              .roundToPx()

          val minHeight = 3.dp.roundToPx()
          val placeable =
            measurable.measure(
              constraints
                .offset(vertical = topPadding)
                .copy(minWidth = targetHandleSize, minHeight = minHeight)
            )

          layout(width = placeable.width, height = placeable.height + topPadding) {
            placeable.place(x = 0, y = topPadding)
          }
        }
        .background(AppTheme.colorScheme.onSurfaceVariant, shape = RoundedCornerShape(50))
    )
  }
}
