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

package dev.sasikanth.rss.reader.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Surface
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.window.core.layout.WindowSizeClass
import dev.sasikanth.rss.reader.utils.LocalRootWindowSizeClass

/**
 * Renders [content] as a [ModalBottomSheet] on compact windows, and as a centered dialog on
 * expanded windows, so a short, fixed-height set of choices feels native to the window it's shown
 * in rather than always sliding up from the bottom.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun AdaptiveSheet(
  onDismissRequest: () -> Unit,
  modifier: Modifier = Modifier,
  containerColor: Color = MaterialTheme.colorScheme.surface,
  contentColor: Color = MaterialTheme.colorScheme.onSurface,
  sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
  content: @Composable ColumnScope.() -> Unit,
) {
  val isExpandedWidth =
    LocalRootWindowSizeClass.current.isWidthAtLeastBreakpoint(
      WindowSizeClass.WIDTH_DP_EXPANDED_LOWER_BOUND
    )

  if (isExpandedWidth) {
    Dialog(
      onDismissRequest = onDismissRequest,
      properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
      Surface(
        modifier = modifier.widthIn(max = 480.dp),
        shape = MaterialTheme.shapes.extraLarge,
        color = containerColor,
        contentColor = contentColor,
      ) {
        Column(content = content)
      }
    }
  } else {
    ModalBottomSheet(
      modifier = modifier,
      onDismissRequest = onDismissRequest,
      containerColor = containerColor,
      contentColor = contentColor,
      sheetState = sheetState,
      content = content,
    )
  }
}
