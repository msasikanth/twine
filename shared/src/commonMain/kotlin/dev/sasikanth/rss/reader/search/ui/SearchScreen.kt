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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import app.cash.paging.compose.collectAsLazyPagingItems
import dev.sasikanth.rss.reader.components.CompactFloatingActionButton
import dev.sasikanth.rss.reader.components.DropdownMenu
import dev.sasikanth.rss.reader.components.DropdownMenuItem
import dev.sasikanth.rss.reader.components.SubHeader
import dev.sasikanth.rss.reader.core.model.local.SearchSortOrder
import dev.sasikanth.rss.reader.core.model.local.SearchSortOrder.*
import dev.sasikanth.rss.reader.home.ui.PostListItem
import dev.sasikanth.rss.reader.home.ui.PostMetadataConfig
import dev.sasikanth.rss.reader.platform.LocalLinkHandler
import dev.sasikanth.rss.reader.resources.icons.ArrowBack
import dev.sasikanth.rss.reader.resources.icons.Sort
import dev.sasikanth.rss.reader.resources.icons.TwineIcons
import dev.sasikanth.rss.reader.resources.strings.LocalStrings
import dev.sasikanth.rss.reader.search.SearchEvent
import dev.sasikanth.rss.reader.search.SearchPresenter
import dev.sasikanth.rss.reader.ui.AppTheme
import dev.sasikanth.rss.reader.utils.KeyboardState
import dev.sasikanth.rss.reader.utils.keyboardVisibilityAsState
import kotlinx.coroutines.launch

@Composable
internal fun SearchScreen(searchPresenter: SearchPresenter, modifier: Modifier = Modifier) {
  val state by searchPresenter.state.collectAsState()
  val listState = rememberLazyListState()
  val coroutineScope = rememberCoroutineScope()
  val showScrollToTop by remember { derivedStateOf { listState.firstVisibleItemIndex > 0 } }
  val searchResults = state.searchResults.collectAsLazyPagingItems()
  val layoutDirection = LocalLayoutDirection.current
  val linkHandler = LocalLinkHandler.current

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
    content = { padding ->
      Box(modifier = Modifier.fillMaxSize()) {
        LaunchedEffect(searchPresenter.searchSortOrder) { listState.animateScrollToItem(0) }

        LazyColumn(
          contentPadding =
            PaddingValues(
              bottom = padding.calculateBottomPadding() + 80.dp,
              top = padding.calculateTopPadding()
            ),
          state = listState
        ) {
          if (searchResults.itemCount > 0) {
            item {
              SubHeader(
                text = LocalStrings.current.searchResultsCount(searchResults.itemCount),
                modifier = Modifier.padding(top = 8.dp)
              )
            }
          }

          items(count = searchResults.itemCount) { index ->
            val post = searchResults[index]
            if (post != null) {
              PostListItem(
                item = post,
                postMetadataConfig = PostMetadataConfig.DEFAULT.copy(enablePostSource = false),
                reduceReadItemAlpha = true,
                onClick = { searchPresenter.dispatch(SearchEvent.OnPostClicked(index, post)) },
                onPostBookmarkClick = {
                  searchPresenter.dispatch(SearchEvent.OnPostBookmarkClick(post))
                },
                onPostCommentsClick = {
                  post.commentsLink?.let { coroutineScope.launch { linkHandler.openLink(it) } }
                },
                onPostSourceClick = {
                  // no-op
                },
                togglePostReadClick = {
                  searchPresenter.dispatch(SearchEvent.TogglePostReadStatus(post.id, post.read))
                }
              )

              if (index != searchResults.itemCount - 1) {
                HorizontalDivider(
                  modifier = Modifier.fillParentMaxWidth().padding(horizontal = 24.dp),
                  color = AppTheme.colorScheme.surfaceContainer
                )
              }
            }
          }
        }

        CompactFloatingActionButton(
          label = LocalStrings.current.scrollToTop,
          visible = showScrollToTop,
          modifier =
            Modifier.padding(
              end = padding.calculateEndPadding(layoutDirection) + 16.dp,
              bottom = padding.calculateBottomPadding() + 16.dp
            )
        ) {
          listState.animateScrollToItem(0)
        }
      }
    },
    containerColor = AppTheme.colorScheme.surfaceContainerLowest,
    contentColor = Color.Unspecified,
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
          WindowInsets.systemBars.only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal)
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
        colorScheme =
          MaterialTheme.colorScheme.copy(primary = AppTheme.colorScheme.tintedForeground)
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
              text = LocalStrings.current.postsSearchHint,
              color = AppTheme.colorScheme.textEmphasisHigh,
              style = MaterialTheme.typography.bodyLarge
            )
          },
          leadingIcon = {
            IconButton(onClick = onBackClick) {
              Icon(
                TwineIcons.ArrowBack,
                contentDescription = LocalStrings.current.buttonGoBack,
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
              focusedContainerColor = Color.Transparent,
              unfocusedContainerColor = Color.Transparent,
              focusedTextColor = AppTheme.colorScheme.textEmphasisHigh,
              unfocusedIndicatorColor = Color.Transparent,
              focusedIndicatorColor = Color.Transparent,
              disabledIndicatorColor = Color.Transparent,
              errorIndicatorColor = Color.Transparent,
            )
        )
      }
    }

    HorizontalDivider(
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
    val density = LocalDensity.current
    var buttonHeight by remember { mutableStateOf(Dp.Unspecified) }
    var isDropdownExpanded by remember { mutableStateOf(false) }

    TextButton(
      modifier =
        Modifier.onGloballyPositioned { coordinates ->
            buttonHeight = with(density) { coordinates.size.height.toDp() }
          }
          .requiredHeight(48.dp),
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
        imageVector = TwineIcons.Sort,
        contentDescription = null,
        tint = AppTheme.colorScheme.onSurface
      )
      Spacer(Modifier.requiredWidth(4.dp))
    }

    if (isDropdownExpanded) {
      SortDropdownMenu(
        isDropdownExpanded = isDropdownExpanded,
        offset = DpOffset(0.dp, buttonHeight.unaryMinus()),
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
  onSortOrderChanged: (SearchSortOrder) -> Unit,
  offset: DpOffset = DpOffset.Zero,
) {
  DropdownMenu(
    expanded = isDropdownExpanded,
    offset = offset,
    onDismissRequest = onDismiss,
  ) {
    DropdownMenuItem(onClick = { onSortOrderChanged(Newest) }) {
      Text(
        text = LocalStrings.current.searchSortNewestFirst,
        style = MaterialTheme.typography.bodyLarge
      )
    }
    DropdownMenuItem(onClick = { onSortOrderChanged(Oldest) }) {
      Text(
        text = LocalStrings.current.searchSortOldestFirst,
        style = MaterialTheme.typography.bodyLarge
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
