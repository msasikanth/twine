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

package dev.sasikanth.rss.reader.opmlimport.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigationevent.NavigationEventInfo
import androidx.navigationevent.compose.NavigationBackHandler
import androidx.navigationevent.compose.rememberNavigationEventState
import dev.sasikanth.rss.reader.components.SimpleTopAppBar
import dev.sasikanth.rss.reader.opmlimport.OpmlImportEffect
import dev.sasikanth.rss.reader.opmlimport.OpmlImportEvent
import dev.sasikanth.rss.reader.opmlimport.OpmlImportViewModel
import dev.sasikanth.rss.reader.settings.ui.items.OpmlFeedSelectionSheet
import dev.sasikanth.rss.reader.ui.AppTheme
import dev.sasikanth.rss.reader.utils.iosBottomSafeAreaPadding
import org.jetbrains.compose.resources.stringResource
import twine.shared.generated.resources.Res
import twine.shared.generated.resources.buttonCancel
import twine.shared.generated.resources.opmlImportProgress
import twine.shared.generated.resources.opmlImportTitle

@Composable
internal fun OpmlImportScreen(
  viewModel: OpmlImportViewModel,
  goBack: () -> Unit,
  onImportComplete: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val state by viewModel.state.collectAsStateWithLifecycle()

  LaunchedEffect(Unit) {
    viewModel.effects.collect { effect ->
      when (effect) {
        OpmlImportEffect.GoBack -> goBack()
        OpmlImportEffect.ImportComplete -> onImportComplete()
      }
    }
  }

  NavigationBackHandler(
    state = rememberNavigationEventState(NavigationEventInfo.None),
    isBackEnabled = state.isImporting,
  ) {
    viewModel.dispatch(OpmlImportEvent.CancelClicked)
  }

  Scaffold(
    modifier = modifier,
    containerColor = AppTheme.colorScheme.backdrop,
    contentColor = Color.Unspecified,
    topBar = {
      SimpleTopAppBar(
        title = stringResource(Res.string.opmlImportTitle),
        onBackClick = {
          if (state.isImporting) {
            viewModel.dispatch(OpmlImportEvent.CancelClicked)
          } else {
            goBack()
          }
        },
        showDivider = false,
      )
    },
  ) { paddingValues ->
    val feedsToSelect = state.feedsToSelect
    if (feedsToSelect != null) {
      OpmlFeedSelectionSheet(
        feeds = feedsToSelect,
        selectionLimit = state.selectionLimit,
        onFeedsSelected = { viewModel.dispatch(OpmlImportEvent.FeedsSelected(it)) },
        onDismiss = { viewModel.dispatch(OpmlImportEvent.SelectionDismissed) },
      )
    }

    Column(
      modifier = Modifier.fillMaxSize().padding(paddingValues).iosBottomSafeAreaPadding(),
      verticalArrangement = Arrangement.Center,
      horizontalAlignment = Alignment.CenterHorizontally,
    ) {
      CircularProgressIndicator(color = AppTheme.colorScheme.primary)

      val progress = state.progress
      if (progress != null) {
        Spacer(Modifier.height(16.dp))

        Text(
          text = stringResource(Res.string.opmlImportProgress, progress),
          style = MaterialTheme.typography.bodyMedium,
          color = AppTheme.colorScheme.onSurfaceVariant,
        )

        Spacer(Modifier.height(8.dp))

        TextButton(onClick = { viewModel.dispatch(OpmlImportEvent.CancelClicked) }) {
          Text(
            text = stringResource(Res.string.buttonCancel),
            style = MaterialTheme.typography.labelLarge,
            color = AppTheme.colorScheme.primary,
          )
        }
      }
    }
  }
}
