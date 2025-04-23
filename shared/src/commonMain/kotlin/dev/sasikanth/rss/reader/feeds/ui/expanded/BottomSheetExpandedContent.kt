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

package dev.sasikanth.rss.reader.feeds.ui.expanded

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeightIn
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import app.cash.paging.compose.collectAsLazyPagingItems
import dev.sasikanth.rss.reader.core.model.local.Feed
import dev.sasikanth.rss.reader.core.model.local.FeedGroup
import dev.sasikanth.rss.reader.feeds.FeedsEvent
import dev.sasikanth.rss.reader.feeds.FeedsPresenter
import dev.sasikanth.rss.reader.feeds.SourceListItem
import dev.sasikanth.rss.reader.feeds.ui.FeedListItem
import dev.sasikanth.rss.reader.resources.strings.LocalStrings
import dev.sasikanth.rss.reader.ui.AppTheme
import dev.sasikanth.rss.reader.utils.KeyboardState
import dev.sasikanth.rss.reader.utils.keyboardVisibilityAsState

@Composable
internal fun BottomSheetExpandedContent(
  feedsPresenter: FeedsPresenter,
  modifier: Modifier = Modifier
) {
  val state by feedsPresenter.state.collectAsState()

  Column(modifier = modifier) {
    val sources = state.sources.collectAsLazyPagingItems()
    SearchBar(
      query = feedsPresenter.searchQuery,
      onQueryChange = { feedsPresenter.dispatch(FeedsEvent.SearchQueryChanged(it)) },
      onClearClick = { feedsPresenter.dispatch(FeedsEvent.ClearSearchQuery) },
    )

    LazyColumn(
      modifier =
        Modifier.fillMaxWidth()
          .weight(1f)
          .clip(RoundedCornerShape(28.dp))
          .background(AppTheme.colorScheme.surfaceContainerLowest),
    ) {
      items(sources.itemCount) { index ->
        val sourceListItem = sources[index]
        if (sourceListItem != null && sourceListItem is SourceListItem.SourceItem) {
          when (val source = sourceListItem.source) {
            is Feed -> {
              FeedListItem(
                modifier = Modifier.fillMaxWidth(),
                feed = source,
                canShowUnreadPostsCount = state.canShowUnreadPostsCount,
                isInMultiSelectMode = state.isInMultiSelectMode,
                isFeedSelected = state.selectedSources.contains(source),
                onFeedClick = { feedsPresenter.dispatch(FeedsEvent.OnSourceClick(source)) },
                onFeedSelected = {
                  feedsPresenter.dispatch(FeedsEvent.OnToggleFeedSelection(source))
                },
                toggleFeedPin = { feedsPresenter.dispatch(FeedsEvent.OnFeedPinClicked(source)) },
              )
            }
            is FeedGroup -> {}
          }
        }
      }
    }

    Box(modifier = Modifier.fillMaxWidth().requiredHeightIn(min = 104.dp).background(Color.Black))
  }
}

@Composable
private fun SearchBar(
  query: TextFieldValue,
  onQueryChange: (TextFieldValue) -> Unit,
  onClearClick: () -> Unit,
) {
  val keyboardState by keyboardVisibilityAsState()
  val focusManager = LocalFocusManager.current

  LaunchedEffect(keyboardState) {
    if (keyboardState == KeyboardState.Closed) {
      focusManager.clearFocus()
    }
  }

  Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
    MaterialTheme(
      colorScheme = MaterialTheme.colorScheme.copy(primary = AppTheme.colorScheme.tintedForeground)
    ) {
      OutlinedTextField(
        modifier =
          Modifier.weight(1f).padding(vertical = 16.dp).padding(start = 24.dp, end = 12.dp),
        value = query.copy(selection = TextRange(query.text.length)),
        onValueChange = onQueryChange,
        placeholder = {
          Text(
            text = LocalStrings.current.feedsSearchHint,
            color = AppTheme.colorScheme.tintedForeground,
            style = MaterialTheme.typography.bodyLarge
          )
        },
        leadingIcon = {
          Icon(
            imageVector = Icons.Rounded.Search,
            contentDescription = null,
            tint = AppTheme.colorScheme.tintedForeground
          )
        },
        trailingIcon = {
          if (query.text.isNotBlank()) {
            IconButton(onClick = onClearClick) {
              Icon(
                Icons.Rounded.Close,
                contentDescription = null,
                tint = AppTheme.colorScheme.tintedForeground
              )
            }
          }
        },
        shape = RoundedCornerShape(50),
        singleLine = true,
        textStyle = MaterialTheme.typography.bodyLarge,
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

    Spacer(Modifier.requiredWidth(20.dp))
  }
}
