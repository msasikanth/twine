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

package dev.sasikanth.rss.reader.group.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.cash.paging.compose.collectAsLazyPagingItems
import app.cash.paging.compose.itemContentType
import app.cash.paging.compose.itemKey
import dev.sasikanth.rss.reader.components.ContextActionItem
import dev.sasikanth.rss.reader.components.ContextActionsBottomBar
import dev.sasikanth.rss.reader.feeds.ui.FeedListItem
import dev.sasikanth.rss.reader.feeds.ui.sheet.expanded.AllFeedsHeader
import dev.sasikanth.rss.reader.group.GroupEvent
import dev.sasikanth.rss.reader.group.GroupViewModel
import dev.sasikanth.rss.reader.resources.icons.ArrowBack
import dev.sasikanth.rss.reader.resources.icons.NewGroup
import dev.sasikanth.rss.reader.resources.icons.TwineIcons
import dev.sasikanth.rss.reader.resources.icons.UnGroup
import dev.sasikanth.rss.reader.ui.AppTheme
import kotlin.time.Duration.Companion.milliseconds
import kotlinx.coroutines.delay
import org.jetbrains.compose.resources.stringResource
import twine.shared.generated.resources.Res
import twine.shared.generated.resources.actionMoveTo
import twine.shared.generated.resources.actionUngroup
import twine.shared.generated.resources.groupNameHint

@Composable
fun GroupScreen(
  viewModel: GroupViewModel,
  goBack: () -> Unit,
  openGroupSelection: () -> Unit,
  modifier: Modifier = Modifier
) {
  val layoutDirection = LocalLayoutDirection.current
  val state by viewModel.state.collectAsStateWithLifecycle()
  val feeds = state.feeds.collectAsLazyPagingItems()

  AppTheme(useDarkTheme = true) {
    Scaffold(
      modifier = modifier,
      topBar = {
        Box {
          Column {
            CenterAlignedTopAppBar(
              modifier = Modifier.padding(horizontal = 4.dp),
              title = {
                GroupNameTextField(
                  value = viewModel.groupName,
                  onValueChanged = { viewModel.dispatch(GroupEvent.OnGroupNameChanged(it.text)) },
                  modifier = Modifier.weight(1f)
                )
              },
              navigationIcon = {
                IconButton(onClick = { goBack() }) {
                  Icon(TwineIcons.ArrowBack, contentDescription = null)
                }
              },
              actions = { Spacer(Modifier.requiredSize(48.dp)) },
              colors =
                TopAppBarDefaults.topAppBarColors(
                  containerColor = AppTheme.colorScheme.tintedBackground,
                  navigationIconContentColor = AppTheme.colorScheme.onSurface,
                  titleContentColor = AppTheme.colorScheme.onSurface,
                  actionIconContentColor = AppTheme.colorScheme.onSurface
                ),
            )

            AllFeedsHeader(
              modifier = Modifier.background(AppTheme.colorScheme.tintedBackground),
              feedsCount = feeds.itemCount,
              feedsSortOrder = state.feedsOrderBy,
              onFeedsSortChanged = { viewModel.dispatch(GroupEvent.OnFeedsSortOrderChanged(it)) }
            )
          }

          HorizontalDivider(
            modifier = Modifier.fillMaxWidth().align(Alignment.BottomStart),
            color = AppTheme.colorScheme.tintedSurface
          )
        }
      },
      bottomBar = {
        if (state.isInMultiSelectMode) {
          ContextActionsBottomBar(
            tooltip = null,
            onCancel = { viewModel.dispatch(GroupEvent.OnCancelSelectionClicked) }
          ) {
            ContextActionItem(
              modifier = Modifier.weight(1f),
              icon = TwineIcons.NewGroup,
              label = stringResource(Res.string.actionMoveTo),
              onClick = { openGroupSelection() }
            )

            ContextActionItem(
              modifier = Modifier.weight(1f),
              icon = TwineIcons.UnGroup,
              label = stringResource(Res.string.actionUngroup),
              onClick = { viewModel.dispatch(GroupEvent.OnUngroupClicked) }
            )
          }
        }
      },
      containerColor = AppTheme.colorScheme.tintedBackground,
    ) { innerPadding ->
      LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding =
          PaddingValues(
            start = innerPadding.calculateStartPadding(layoutDirection),
            top = innerPadding.calculateTopPadding() + 24.dp,
            end = innerPadding.calculateEndPadding(layoutDirection),
            bottom = innerPadding.calculateBottomPadding() + 200.dp
          ),
        verticalArrangement = Arrangement.spacedBy(16.dp)
      ) {
        items(
          count = feeds.itemCount,
          key = feeds.itemKey { it.id },
          contentType = feeds.itemContentType { it.id },
        ) { index ->
          val feed = feeds[index]
          if (feed != null) {
            FeedListItem(
              modifier = Modifier.padding(horizontal = 24.dp),
              feed = feed,
              canShowUnreadPostsCount = false,
              isInMultiSelectMode = state.isInMultiSelectMode,
              isFeedSelected = state.selectedSources.any { it.id == feed.id },
              onFeedClick = { viewModel.dispatch(GroupEvent.OnFeedClicked(feed)) },
              onFeedSelected = { viewModel.dispatch(GroupEvent.OnFeedClicked(feed)) },
              onOptionsClick = { viewModel.dispatch(GroupEvent.OnFeedClicked(feed)) },
            )
          }
        }
      }
    }
  }
}

@Composable
fun GroupNameTextField(
  value: TextFieldValue,
  onValueChanged: (TextFieldValue) -> Unit,
  modifier: Modifier = Modifier
) {
  var input by remember(value) { mutableStateOf(value) }

  // Debounce input changes
  LaunchedEffect(input) {
    if (input.text.isBlank()) return@LaunchedEffect
    delay(500.milliseconds)
    onValueChanged(input)
  }

  MaterialTheme(
    colorScheme = MaterialTheme.colorScheme.copy(primary = AppTheme.colorScheme.tintedForeground)
  ) {
    OutlinedTextField(
      modifier = modifier,
      value = input.copy(selection = TextRange(input.text.length)),
      onValueChange = { input = it },
      placeholder = {
        Text(
          text = stringResource(Res.string.groupNameHint),
          color = AppTheme.colorScheme.tintedForeground,
          style = MaterialTheme.typography.bodyLarge
        )
      },
      shape = RoundedCornerShape(16.dp),
      singleLine = true,
      textStyle = MaterialTheme.typography.bodyLarge.copy(textAlign = TextAlign.Center),
      colors =
        OutlinedTextFieldDefaults.colors(
          focusedBorderColor = AppTheme.colorScheme.tintedHighlight,
          unfocusedBorderColor = AppTheme.colorScheme.tintedHighlight,
          disabledBorderColor = AppTheme.colorScheme.tintedHighlight,
          focusedTextColor = AppTheme.colorScheme.textEmphasisHigh,
          disabledTextColor = Color.Transparent,
        )
    )
  }
}
