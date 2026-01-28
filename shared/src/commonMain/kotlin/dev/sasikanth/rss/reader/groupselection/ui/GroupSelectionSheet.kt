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

package dev.sasikanth.rss.reader.groupselection.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.cash.paging.compose.collectAsLazyPagingItems
import dev.sasikanth.rss.reader.components.Button
import dev.sasikanth.rss.reader.components.OutlinedButton
import dev.sasikanth.rss.reader.feeds.ui.CreateGroupDialog
import dev.sasikanth.rss.reader.feeds.ui.FeedGroupItem
import dev.sasikanth.rss.reader.groupselection.GroupSelectionEvent
import dev.sasikanth.rss.reader.groupselection.GroupSelectionViewModel
import dev.sasikanth.rss.reader.resources.icons.Add
import dev.sasikanth.rss.reader.resources.icons.TwineIcons
import dev.sasikanth.rss.reader.ui.AppTheme
import dev.sasikanth.rss.reader.ui.LocalTranslucentStyles
import org.jetbrains.compose.resources.stringResource
import twine.shared.generated.resources.Res
import twine.shared.generated.resources.buttonConfirm
import twine.shared.generated.resources.buttonGoBack
import twine.shared.generated.resources.groupAddNew

const val SELECTED_GROUPS_KEY = "dev.sasikanth.twine.SELECTED_GROUPS"

@Composable
fun GroupSelectionSheet(
  viewModel: GroupSelectionViewModel,
  dismiss: () -> Unit,
  onGroupsSelected: (Set<String>) -> Unit,
  modifier: Modifier = Modifier
) {
  AppTheme(useDarkTheme = true) {
    val translucentStyle = LocalTranslucentStyles.current

    ModalBottomSheet(
      modifier = Modifier.then(modifier),
      onDismissRequest = { dismiss() },
      containerColor = translucentStyle.default.background.compositeOver(Color.Black),
      contentColor = Color.Unspecified,
      contentWindowInsets = {
        WindowInsets.systemBars
          .only(WindowInsetsSides.Bottom)
          .union(WindowInsets.ime.only(WindowInsetsSides.Bottom))
      },
      sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
      scrimColor = Color.Transparent,
    ) {
      val state by viewModel.state.collectAsStateWithLifecycle()
      val groups = state.groups.collectAsLazyPagingItems()

      var showCreateGroupDialog by remember { mutableStateOf(false) }

      if (showCreateGroupDialog) {
        CreateGroupDialog(
          onCreateGroup = { viewModel.dispatch(GroupSelectionEvent.OnCreateGroup(it)) },
          onDismiss = { showCreateGroupDialog = false }
        )
      }

      LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 24.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
      ) {
        item {
          Box(
            modifier =
              Modifier.clip(MaterialTheme.shapes.large)
                .background(AppTheme.colorScheme.tintedSurface)
                .clickable { showCreateGroupDialog = true }
                .padding(vertical = 16.dp),
            contentAlignment = Alignment.Center
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(
                imageVector = TwineIcons.Add,
                contentDescription = null,
                tint = AppTheme.colorScheme.tintedForeground
              )

              Spacer(Modifier.requiredWidth(8.dp))

              Text(
                text = stringResource(Res.string.groupAddNew),
                style = MaterialTheme.typography.labelLarge,
                color = AppTheme.colorScheme.tintedForeground,
              )
            }
          }
        }

        items(groups.itemCount) { index ->
          val group = groups[index]
          if (group != null) {
            FeedGroupItem(
              feedGroup = group,
              canShowUnreadPostsCount = false,
              isInMultiSelectMode = true,
              selected = state.selectedGroups.contains(group.id),
              onFeedGroupSelected = {
                viewModel.dispatch(GroupSelectionEvent.OnToggleGroupSelection(group))
              },
              onFeedGroupClick = {
                // no-op
              },
              onOptionsClick = {
                // no-op
              }
            )
          }
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
              contentColor = AppTheme.colorScheme.tintedForeground
            ),
          border = BorderStroke(1.dp, AppTheme.colorScheme.tintedHighlight),
          onClick = { dismiss() }
        ) {
          Text(text = stringResource(Res.string.buttonGoBack))
        }

        Spacer(Modifier.requiredWidth(16.dp))

        Button(
          modifier = Modifier.weight(1f),
          enabled = state.areGroupsSelected,
          onClick = { onGroupsSelected(state.selectedGroups) }
        ) {
          Text(text = stringResource(Res.string.buttonConfirm))
        }
      }
    }
  }
}
