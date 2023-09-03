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
package dev.sasikanth.rss.reader.search.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import dev.sasikanth.rss.reader.components.ScrollToTopButton
import dev.sasikanth.rss.reader.home.ui.PostListItem
import dev.sasikanth.rss.reader.resources.IconResources
import dev.sasikanth.rss.reader.resources.strings.LocalStrings
import dev.sasikanth.rss.reader.search.SearchEvent
import dev.sasikanth.rss.reader.search.SearchPresenter
import dev.sasikanth.rss.reader.search.SearchSortOrder
import dev.sasikanth.rss.reader.search.SearchSortOrder.*
import dev.sasikanth.rss.reader.ui.AppTheme
import dev.sasikanth.rss.reader.utils.KeyboardState
import dev.sasikanth.rss.reader.utils.keyboardVisibilityAsState
import org.jetbrains.compose.resources.painterResource

@Composable
internal fun SearchScreen(
  searchPresenter: SearchPresenter,
  openLink: (String) -> Unit,
  modifier: Modifier = Modifier
) {
  val state by searchPresenter.state.collectAsState()
  val listState = rememberLazyListState()
  val showScrollToTop by remember { derivedStateOf { listState.firstVisibleItemIndex > 0 } }
  val layoutDirection = LocalLayoutDirection.current

  Scaffold(
    modifier = modifier,
    topBar = {
      SearchBar(
        query = searchPresenter.searchQuery,
        sortOrder = searchPresenter.searchSortOrder,
        onQueryChange = { searchPresenter.dispatch(SearchEvent.SearchQueryChanged(it)) },
        onBackClick = { searchPresenter.dispatch(SearchEvent.BackClicked) },
        onClearClick = { searchPresenter.dispatch(SearchEvent.ClearSearchQuery) },
        onSortOrderChanged = { searchPresenter.dispatch(SearchEvent.SearchSortOrderChanged(it)) }
      )
    },
    content = {
      Box(modifier = Modifier.fillMaxSize()) {
        LaunchedEffect(searchPresenter.searchSortOrder) { listState.animateScrollToItem(0) }

        LazyColumn(
          contentPadding =
            PaddingValues(
              start = it.calculateStartPadding(layoutDirection),
              end = it.calculateEndPadding(layoutDirection),
              bottom = it.calculateBottomPadding() + 64.dp
            ),
          state = listState,
          modifier = Modifier.padding(top = it.calculateTopPadding())
        ) {
          itemsIndexed(state.searchResults) { index, post ->
            PostListItem(
              item = post,
              onClick = { openLink(post.link) },
              onPostBookmarkClick = {
                searchPresenter.dispatch(SearchEvent.OnPostBookmarkClick(post))
              }
            )
            if (index != state.searchResults.lastIndex) {
              Divider(
                modifier = Modifier.fillParentMaxWidth().padding(horizontal = 24.dp),
                color = AppTheme.colorScheme.surfaceContainer
              )
            }
          }
        }

        ScrollToTopButton(
          visible = showScrollToTop,
          modifier =
            Modifier.windowInsetsPadding(WindowInsets.navigationBars)
              .padding(end = 24.dp, bottom = 24.dp)
        ) {
          listState.animateScrollToItem(0)
        }
      }
    },
    containerColor = Color.Unspecified,
    contentColor = Color.Unspecified
  )
}

@Composable
private fun SearchBar(
  query: TextFieldValue,
  sortOrder: SearchSortOrder,
  onQueryChange: (TextFieldValue) -> Unit,
  onBackClick: () -> Unit,
  onClearClick: () -> Unit,
  onSortOrderChanged: (SearchSortOrder) -> Unit
) {
  val focusRequester = remember { FocusRequester() }
  val keyboardState by keyboardVisibilityAsState()
  val focusManager = LocalFocusManager.current
  var isSearchBarFocused by remember { mutableStateOf(false) }

  LaunchedEffect(keyboardState) {
    if (keyboardState == KeyboardState.Closed) {
      focusManager.clearFocus()
    }
  }

  LaunchedEffect(Unit) { focusRequester.requestFocus() }

  Box(
    modifier =
      Modifier.fillMaxWidth()
        .background(AppTheme.colorScheme.surface)
        .windowInsetsPadding(
          WindowInsets.statusBars.union(WindowInsets.systemBars.only(WindowInsetsSides.Horizontal))
        )
  ) {
    Box(
      modifier =
        Modifier.padding(all = 16.dp)
          .background(
            color = AppTheme.colorScheme.surfaceContainer,
            shape = RoundedCornerShape(16.dp)
          )
          .padding(horizontal = 4.dp)
    ) {
      MaterialTheme(
        colorScheme = darkColorScheme(primary = AppTheme.colorScheme.tintedForeground)
      ) {
        TextField(
          modifier =
            Modifier.fillMaxWidth().focusRequester(focusRequester).onFocusChanged {
              isSearchBarFocused = it.isFocused
            },
          value = query.copy(selection = TextRange(query.text.length)),
          onValueChange = onQueryChange,
          placeholder = {
            Text(
              text = LocalStrings.current.searchHint,
              color = AppTheme.colorScheme.textEmphasisHigh,
              style = MaterialTheme.typography.bodyLarge
            )
          },
          leadingIcon = {
            IconButton(onClick = onBackClick) {
              Icon(
                Icons.Rounded.ArrowBack,
                contentDescription = null,
                tint = AppTheme.colorScheme.onSurface
              )
            }
          },
          trailingIcon = {
            if (query.text.isNotBlank()) {
              AnimatedContent(isSearchBarFocused) {
                if (it) {
                  ClearSearchQueryButton {
                    focusRequester.requestFocus()
                    onClearClick()
                  }
                } else {
                  SearchSortButton(sortOrder, onSortOrderChanged)
                }
              }
            }
          },
          shape = RoundedCornerShape(16.dp),
          singleLine = true,
          textStyle = MaterialTheme.typography.bodyLarge,
          colors =
            TextFieldDefaults.colors(
              focusedContainerColor = Color.Unspecified,
              unfocusedContainerColor = Color.Unspecified,
              focusedTextColor = AppTheme.colorScheme.textEmphasisHigh,
              unfocusedIndicatorColor = Color.Unspecified,
              focusedIndicatorColor = Color.Unspecified,
              disabledIndicatorColor = Color.Unspecified,
              errorIndicatorColor = Color.Unspecified
            )
        )
      }
    }

    Divider(
      modifier = Modifier.fillMaxWidth().align(Alignment.BottomStart),
      color = AppTheme.colorScheme.surfaceContainer
    )
  }
}

@Composable
private fun SearchSortButton(
  sortOrder: SearchSortOrder,
  onSortOrderChanged: (SearchSortOrder) -> Unit
) {
  Box {
    var isDropdownExpanded by remember { mutableStateOf(false) }

    TextButton(
      modifier = Modifier.requiredHeight(48.dp),
      onClick = { isDropdownExpanded = true },
      shape = RoundedCornerShape(12.dp),
    ) {
      Spacer(Modifier.requiredWidth(4.dp))
      Text(
        text =
          when (sortOrder) {
            Newest -> LocalStrings.current.searchSortNewest
            Oldest -> LocalStrings.current.searchSortOldest
          },
        style = MaterialTheme.typography.labelLarge,
        color = AppTheme.colorScheme.onSurface
      )
      Spacer(Modifier.requiredWidth(8.dp))
      Icon(
        painterResource(IconResources.sort),
        contentDescription = null,
        tint = AppTheme.colorScheme.onSurface
      )
      Spacer(Modifier.requiredWidth(4.dp))
    }

    if (isDropdownExpanded) {
      SortDropdownMenu(
        isDropdownExpanded = isDropdownExpanded,
        onDismiss = { isDropdownExpanded = false },
        onSortOrderChanged = {
          onSortOrderChanged(it)
          isDropdownExpanded = false
        }
      )
    }
  }
}

@Composable
private fun SortDropdownMenu(
  isDropdownExpanded: Boolean,
  onDismiss: () -> Unit,
  onSortOrderChanged: (SearchSortOrder) -> Unit
) {
  DropdownMenu(
    expanded = isDropdownExpanded,
    onDismissRequest = onDismiss,
    offset = DpOffset(0.dp, (-48).dp),
    modifier = Modifier.background(color = AppTheme.colorScheme.surfaceContainerHigh)
  ) {
    DropdownMenuItem(onClick = { onSortOrderChanged(Newest) }) {
      Text(
        text = LocalStrings.current.searchSortNewestFirst,
        style = MaterialTheme.typography.bodyLarge,
        color = AppTheme.colorScheme.onSurface
      )
    }
    DropdownMenuItem(onClick = { onSortOrderChanged(Oldest) }) {
      Text(
        text = LocalStrings.current.searchSortOldestFirst,
        style = MaterialTheme.typography.bodyLarge,
        color = AppTheme.colorScheme.onSurface
      )
    }
  }
}

@Composable
private fun ClearSearchQueryButton(onClearClick: () -> Unit) {
  IconButton(onClick = onClearClick) {
    Icon(Icons.Rounded.Close, contentDescription = null, tint = AppTheme.colorScheme.onSurface)
  }
}
