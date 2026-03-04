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

package dev.sasikanth.rss.reader.feeds.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import dev.sasikanth.rss.reader.components.AlertDialog
import org.jetbrains.compose.resources.stringResource
import twine.shared.generated.resources.Res
import twine.shared.generated.resources.buttonCancel
import twine.shared.generated.resources.delete
import twine.shared.generated.resources.removeSources
import twine.shared.generated.resources.removeSourcesDesc

@Composable
fun DeleteConfirmationDialog(
  onDelete: () -> Unit,
  dismiss: () -> Unit,
  modifier: Modifier = Modifier,
) {
  AlertDialog(
    modifier = modifier,
    title = stringResource(Res.string.removeSources),
    text = stringResource(Res.string.removeSourcesDesc),
    confirmText = stringResource(Res.string.delete),
    dismissText = stringResource(Res.string.buttonCancel),
    onConfirm = {
      onDelete()
      dismiss()
    },
    onDismiss = dismiss,
  )
}
