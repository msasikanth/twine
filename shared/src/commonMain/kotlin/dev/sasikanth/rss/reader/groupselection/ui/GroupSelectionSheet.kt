/*
 * Copyright 2024 Sasikanth Miriyampalli
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import app.cash.paging.compose.collectAsLazyPagingItems
import dev.sasikanth.rss.reader.components.Button
import dev.sasikanth.rss.reader.components.OutlinedButton
import dev.sasikanth.rss.reader.feeds.ui.CreateGroupDialog
import dev.sasikanth.rss.reader.feeds.ui.FeedGroupItem
import dev.sasikanth.rss.reader.groupselection.GroupSelectionEvent
import dev.sasikanth.rss.reader.groupselection.GroupSelectionPresenter
import dev.sasikanth.rss.reader.resources.icons.Add
import dev.sasikanth.rss.reader.resources.icons.TwineIcons
import dev.sasikanth.rss.reader.resources.strings.LocalStrings
import dev.sasikanth.rss.reader.ui.AppTheme
import dev.sasikanth.rss.reader.ui.DynamicContentTheme
import dev.sasikanth.rss.reader.ui.SYSTEM_SCRIM

@Composable
fun GroupSelectionSheet(presenter: GroupSelectionPresenter, modifier: Modifier = Modifier) {
  DynamicContentTheme(useDarkTheme = true) {
    ModalBottomSheet(
      modifier = Modifier.then(modifier),
      onDismissRequest = { presenter.dispatch(GroupSelectionEvent.BackClicked) },
      containerColor = AppTheme.colorScheme.tintedBackground,
      contentColor = Color.Unspecified,
      windowInsets =
        WindowInsets.systemBars
          .only(WindowInsetsSides.Bottom)
          .union(WindowInsets.ime.only(WindowInsetsSides.Bottom)),
      sheetState = SheetState(skipPartiallyExpanded = true, density = LocalDensity.current),
      scrimColor = SYSTEM_SCRIM
    ) {
      val state by presenter.state.collectAsState()
      val groups = state.groups.collectAsLazyPagingItems()

      var showCreateGroupDialog by remember { mutableStateOf(false) }

      if (showCreateGroupDialog) {
        CreateGroupDialog(
          onCreateGroup = { presenter.dispatch(GroupSelectionEvent.OnCreateGroup(it)) },
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
                text = LocalStrings.current.groupAddNew,
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
                presenter.dispatch(GroupSelectionEvent.OnToggleGroupSelection(group))
              },
              onFeedGroupClick = {
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
              containerColor = AppTheme.colorScheme.tintedBackground,
              contentColor = AppTheme.colorScheme.tintedForeground
            ),
          border = BorderStroke(1.dp, AppTheme.colorScheme.tintedHighlight),
          onClick = { presenter.dispatch(GroupSelectionEvent.BackClicked) }
        ) {
          Text(text = LocalStrings.current.buttonGoBack)
        }

        Spacer(Modifier.requiredWidth(16.dp))

        Button(
          modifier = Modifier.weight(1f),
          enabled = state.areGroupsSelected,
          onClick = { presenter.dispatch(GroupSelectionEvent.OnConfirmGroupSelectionClicked) }
        ) {
          Text(text = LocalStrings.current.buttonConfirm)
        }
      }
    }
  }
}
