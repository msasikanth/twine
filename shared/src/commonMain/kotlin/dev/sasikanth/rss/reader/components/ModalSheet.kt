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

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.SheetValue
import androidx.compose.material3.contentColorFor
import androidx.compose.material3.rememberBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import dev.sasikanth.rss.reader.resources.icons.Close
import dev.sasikanth.rss.reader.resources.icons.Platform
import dev.sasikanth.rss.reader.resources.icons.TwineIcons
import dev.sasikanth.rss.reader.resources.icons.platform
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import twine.shared.generated.resources.Res
import twine.shared.generated.resources.buttonClose

/**
 * A [ModalBottomSheet] that swaps the drag handle for a full width row with an end aligned close
 * button on desktop, where dismissing with a pointer is expected instead of a swipe.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ModalSheet(
  onDismissRequest: () -> Unit,
  modifier: Modifier = Modifier,
  containerColor: Color = BottomSheetDefaults.ContainerColor,
  contentColor: Color = contentColorFor(containerColor),
  contentWindowInsets: @Composable () -> WindowInsets = { BottomSheetDefaults.modalWindowInsets },
  sheetState: SheetState =
    rememberBottomSheetState(
      initialValue = SheetValue.Hidden,
      enabledValues = setOf(SheetValue.Hidden, SheetValue.Expanded),
    ),
  content: @Composable ColumnScope.() -> Unit,
) {
  val isDesktop = platform is Platform.Desktop
  val coroutineScope = rememberCoroutineScope()

  ModalBottomSheet(
    modifier = modifier,
    onDismissRequest = onDismissRequest,
    containerColor = containerColor,
    contentColor = contentColor,
    contentWindowInsets = contentWindowInsets,
    sheetState = sheetState,
    dragHandle = if (isDesktop) null else ({ BottomSheetDefaults.DragHandle() }),
  ) {
    if (isDesktop) {
      Box(modifier = Modifier.fillMaxWidth().padding(24.dp)) {
        CircularIconButton(
          modifier = Modifier.align(Alignment.CenterEnd),
          icon = TwineIcons.Close,
          label = stringResource(Res.string.buttonClose),
          onClick = {
            coroutineScope.launch { sheetState.hide() }.invokeOnCompletion { onDismissRequest() }
          },
        )
      }
    }

    content()
  }
}
