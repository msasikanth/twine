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

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.min
import dev.sasikanth.rss.reader.ui.AppTheme

private val COLLAPSED_HANDLE_SIZE = 24.dp
private val EXPANDED_HANDLE_SIZE = 64.dp

@Composable
internal fun BottomSheetHandle(
  progress: Float,
  modifier: Modifier = Modifier,
) {
  val statusBarPadding = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
  val targetHandleSize =
    min(COLLAPSED_HANDLE_SIZE * progress + COLLAPSED_HANDLE_SIZE, EXPANDED_HANDLE_SIZE)

  val collapsedTopPadding = 12.dp
  val targetTopPadding =
    (collapsedTopPadding * (progress * 2) + collapsedTopPadding) + (statusBarPadding * progress)

  Column(
    modifier = Modifier.fillMaxWidth().then(modifier),
    horizontalAlignment = Alignment.CenterHorizontally
  ) {
    Spacer(Modifier.requiredHeight(targetTopPadding))
    Box(
      Modifier.background(AppTheme.colorScheme.onSurfaceVariant, shape = RoundedCornerShape(50))
        .requiredSize(width = targetHandleSize, height = 3.dp)
    )

    Spacer(Modifier.requiredHeight(13.dp))
  }
}
