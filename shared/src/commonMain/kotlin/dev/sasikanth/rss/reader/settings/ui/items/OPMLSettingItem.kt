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

package dev.sasikanth.rss.reader.settings.ui.items

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import dev.sasikanth.rss.reader.components.Button
import dev.sasikanth.rss.reader.components.InverseButton
import dev.sasikanth.rss.reader.components.OutlinedButton
import dev.sasikanth.rss.reader.components.TranslucentButton
import dev.sasikanth.rss.reader.data.opml.OpmlFeed
import dev.sasikanth.rss.reader.data.opml.OpmlResult
import dev.sasikanth.rss.reader.feeds.ui.SelectedCheckIndicator
import dev.sasikanth.rss.reader.ui.AppTheme
import dev.sasikanth.rss.reader.utils.Constants
import org.jetbrains.compose.resources.stringResource
import twine.shared.generated.resources.Res
import twine.shared.generated.resources.buttonGoBack
import twine.shared.generated.resources.settingsHeaderOpml
import twine.shared.generated.resources.settingsOpmlCancel
import twine.shared.generated.resources.settingsOpmlExport
import twine.shared.generated.resources.settingsOpmlExporting
import twine.shared.generated.resources.settingsOpmlImport
import twine.shared.generated.resources.settingsOpmlImporting
import twine.shared.generated.resources.settingsOpmlSelectionSubtitle
import twine.shared.generated.resources.settingsOpmlSelectionTitle

@Composable
internal fun OPMLSettingItem(
  opmlResult: OpmlResult?,
  onImportClicked: () -> Unit,
  onExportClicked: () -> Unit,
  onCancelClicked: () -> Unit,
) {
  Row(
    modifier = Modifier.padding(vertical = 16.dp).padding(start = 16.dp, end = 24.dp),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Text(
      modifier = Modifier.weight(1f).padding(horizontal = 8.dp),
      text = stringResource(Res.string.settingsHeaderOpml),
      style = MaterialTheme.typography.titleSmall,
      color = AppTheme.colorScheme.onSurface,
    )

    when (opmlResult) {
      is OpmlResult.InProgress.Importing,
      is OpmlResult.InProgress.Exporting -> {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
          val string =
            when (opmlResult) {
              is OpmlResult.InProgress.Importing -> {
                stringResource(Res.string.settingsOpmlImporting, opmlResult.progress)
              }

              is OpmlResult.InProgress.Exporting -> {
                stringResource(Res.string.settingsOpmlExporting, opmlResult.progress)
              }
            }

          TranslucentButton(
            text = string,
            onClick = {
              // no-op
            },
          )

          InverseButton(
            text = stringResource(Res.string.settingsOpmlCancel),
            onClick = onCancelClicked,
          )
        }
      }

      // TODO: Handle error states
      OpmlResult.Idle,
      OpmlResult.Error.NoContentInOpmlFile,
      is OpmlResult.Error.UnknownFailure -> {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
          TranslucentButton(
            text = stringResource(Res.string.settingsOpmlImport),
            onClick = onImportClicked,
          )

          TranslucentButton(
            text = stringResource(Res.string.settingsOpmlExport),
            onClick = onExportClicked,
          )
        }
      }

      null -> {
        Box(Modifier.requiredHeight(40.dp))
      }
    }
  }
}

@Composable
internal fun OpmlFeedSelectionSheet(
  feeds: List<OpmlFeed>,
  onFeedsSelected: (List<OpmlFeed>) -> Unit,
  onDismiss: () -> Unit,
) {
  ModalBottomSheet(
    onDismissRequest = onDismiss,
    containerColor = AppTheme.colorScheme.surfaceContainerLowest,
    contentColor = AppTheme.colorScheme.onSurface,
    sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
  ) {
    val selectedFeeds = remember { mutableStateListOf<OpmlFeed>() }
    val canSelectMore by remember {
      derivedStateOf { selectedFeeds.size < Constants.MAX_FREE_FEEDS }
    }

    Column(modifier = Modifier.fillMaxWidth()) {
      LazyColumn(
        modifier = Modifier.weight(1f, fill = false),
        contentPadding = PaddingValues(bottom = 16.dp),
      ) {
        stickyHeader {
          Column(modifier = Modifier.background(AppTheme.colorScheme.surfaceContainerLowest)) {
            Text(
              modifier = Modifier.padding(horizontal = 24.dp).padding(top = 16.dp),
              text = stringResource(Res.string.settingsOpmlSelectionTitle),
              color = AppTheme.colorScheme.onSurface,
              style = MaterialTheme.typography.titleLarge,
            )

            Spacer(Modifier.height(4.dp))

            Text(
              modifier = Modifier.padding(horizontal = 24.dp).padding(bottom = 16.dp),
              text =
                stringResource(Res.string.settingsOpmlSelectionSubtitle, Constants.MAX_FREE_FEEDS),
              style = MaterialTheme.typography.labelLarge,
              color = AppTheme.colorScheme.onSurfaceVariant,
            )

            HorizontalDivider(color = AppTheme.colorScheme.outlineVariant)
          }
        }

        items(feeds) { feed ->
          val isSelected = selectedFeeds.contains(feed)
          OpmlFeedItem(
            feed = feed,
            isSelected = isSelected,
            canSelectMore = canSelectMore,
            onClick = {
              if (isSelected) {
                selectedFeeds.remove(feed)
              } else if (canSelectMore) {
                selectedFeeds.add(feed)
              }
            },
          )
        }
      }

      Row(
        modifier =
          Modifier.fillMaxWidth().padding(start = 24.dp, top = 40.dp, end = 24.dp, bottom = 24.dp)
      ) {
        OutlinedButton(
          modifier = Modifier.weight(1f),
          colors =
            ButtonDefaults.outlinedButtonColors(
              containerColor = Color.Transparent,
              contentColor = AppTheme.colorScheme.primary,
            ),
          border = BorderStroke(1.dp, AppTheme.colorScheme.primary),
          onClick = onDismiss,
        ) {
          Text(text = stringResource(Res.string.buttonGoBack))
        }

        Spacer(Modifier.requiredWidth(16.dp))

        Button(
          modifier = Modifier.weight(1f),
          enabled = selectedFeeds.isNotEmpty(),
          colors =
            ButtonDefaults.buttonColors(
              containerColor = AppTheme.colorScheme.primary,
              contentColor = AppTheme.colorScheme.onPrimary,
            ),
          onClick = { onFeedsSelected(selectedFeeds.toList()) },
        ) {
          Text(text = stringResource(Res.string.settingsOpmlImport))
        }
      }
    }
  }
}

@Composable
private fun OpmlFeedItem(
  feed: OpmlFeed,
  isSelected: Boolean,
  canSelectMore: Boolean,
  onClick: () -> Unit,
) {
  val enabled = isSelected || canSelectMore
  Row(
    modifier =
      Modifier.fillMaxWidth()
        .clickable(enabled = enabled, onClick = onClick)
        .padding(horizontal = 24.dp, vertical = 12.dp)
        .alpha(if (enabled) 1f else 0.38f),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Column(modifier = Modifier.weight(1f)) {
      Text(
        text = feed.title ?: feed.link,
        style = MaterialTheme.typography.titleMedium,
        color = AppTheme.colorScheme.onSurface,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
      )
      Text(
        text = feed.link,
        style = MaterialTheme.typography.labelLarge,
        color = AppTheme.colorScheme.onSurfaceVariant,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
      )
    }

    Spacer(Modifier.requiredWidth(16.dp))

    SelectedCheckIndicator(selected = isSelected, enabled = enabled)
  }
}
