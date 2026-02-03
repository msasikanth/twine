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
package dev.sasikanth.rss.reader.search.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.cash.paging.compose.LazyPagingItems
import app.cash.paging.compose.collectAsLazyPagingItems
import dev.sasikanth.rss.reader.components.CircularIconButton
import dev.sasikanth.rss.reader.components.DropdownMenu
import dev.sasikanth.rss.reader.components.DropdownMenuItem
import dev.sasikanth.rss.reader.components.NewArticlesScrollToTopButton
import dev.sasikanth.rss.reader.components.SubHeader
import dev.sasikanth.rss.reader.components.image.FeedIcon
import dev.sasikanth.rss.reader.core.model.local.Feed
import dev.sasikanth.rss.reader.core.model.local.FeedGroup
import dev.sasikanth.rss.reader.core.model.local.ResolvedPost
import dev.sasikanth.rss.reader.core.model.local.SearchSortOrder
import dev.sasikanth.rss.reader.core.model.local.SearchSortOrder.Newest
import dev.sasikanth.rss.reader.core.model.local.SearchSortOrder.Oldest
import dev.sasikanth.rss.reader.core.model.local.Source
import dev.sasikanth.rss.reader.feeds.ui.FeedGroupIconGrid
import dev.sasikanth.rss.reader.home.ui.PostListItem
import dev.sasikanth.rss.reader.home.ui.PostListKey
import dev.sasikanth.rss.reader.home.ui.PostMetadataConfig
import dev.sasikanth.rss.reader.platform.LocalLinkHandler
import dev.sasikanth.rss.reader.resources.icons.All
import dev.sasikanth.rss.reader.resources.icons.ArrowBack
import dev.sasikanth.rss.reader.resources.icons.Bookmarked
import dev.sasikanth.rss.reader.resources.icons.Close
import dev.sasikanth.rss.reader.resources.icons.FilterList
import dev.sasikanth.rss.reader.resources.icons.RadioUnselected
import dev.sasikanth.rss.reader.resources.icons.Sort
import dev.sasikanth.rss.reader.resources.icons.TwineIcons
import dev.sasikanth.rss.reader.search.SearchEvent
import dev.sasikanth.rss.reader.search.SearchViewModel
import dev.sasikanth.rss.reader.ui.AppTheme
import dev.sasikanth.rss.reader.utils.Constants
import dev.sasikanth.rss.reader.utils.KeyboardState
import dev.sasikanth.rss.reader.utils.keyboardVisibilityAsState
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource
import twine.shared.generated.resources.Res
import twine.shared.generated.resources.bookmarks
import twine.shared.generated.resources.buttonAll
import twine.shared.generated.resources.buttonGoBack
import twine.shared.generated.resources.feeds
import twine.shared.generated.resources.filter
import twine.shared.generated.resources.postsSearchHint
import twine.shared.generated.resources.postsUnread
import twine.shared.generated.resources.searchResultsCount
import twine.shared.generated.resources.searchSortNewest
import twine.shared.generated.resources.searchSortNewestFirst
import twine.shared.generated.resources.searchSortOldest
import twine.shared.generated.resources.searchSortOldestFirst

@Composable
internal fun SearchScreen(
  searchViewModel: SearchViewModel,
  goBack: () -> Unit,
  openPost:
    (searchQuery: String, sortOrder: SearchSortOrder, postIndex: Int, post: ResolvedPost) -> Unit,
  modifier: Modifier = Modifier,
) {
  val state by searchViewModel.state.collectAsStateWithLifecycle()
  val listState = rememberLazyListState()
  val coroutineScope = rememberCoroutineScope()
  val showScrollToTop by remember { derivedStateOf { listState.firstVisibleItemIndex > 0 } }
  val searchResults = state.searchResults.collectAsLazyPagingItems()
  val layoutDirection = LocalLayoutDirection.current
  val linkHandler = LocalLinkHandler.current

  var showSourcePicker by remember { mutableStateOf(false) }

  Scaffold(
    modifier = modifier,
    topBar = {
      Column(
        Modifier.background(AppTheme.colorScheme.surface)
          .windowInsetsPadding(
            WindowInsets.systemBars.only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal)
          )
      ) {
        Spacer(Modifier.requiredHeight(12.dp))

        SearchBar(
          query = searchViewModel.searchQuery,
          sortOrder = searchViewModel.searchSortOrder,
          onQueryChange = { searchViewModel.dispatch(SearchEvent.SearchQueryChanged(it)) },
          onBackClick = goBack,
          onClearClick = { searchViewModel.dispatch(SearchEvent.ClearSearchQuery) },
          onSortOrderChanged = { searchViewModel.dispatch(SearchEvent.SearchSortOrderChanged(it)) },
        )

        AnimatedVisibility(
          visible =
            searchViewModel.searchQuery.text.length >= Constants.MINIMUM_REQUIRED_SEARCH_CHARACTERS,
          enter = fadeIn() + expandVertically(),
          exit = shrinkVertically() + fadeOut(),
        ) {
          SourceFilterChips(
            selectedSource = state.selectedSource,
            onlyBookmarked = state.onlyBookmarked,
            onlyUnread = state.onlyUnread,
            onSourceClick = { showSourcePicker = true },
            onClearSourceClick = { searchViewModel.dispatch(SearchEvent.OnSourceChanged(null)) },
            onOnlyBookmarkedChanged = {
              searchViewModel.dispatch(SearchEvent.OnOnlyBookmarkedChanged(it))
            },
            onOnlyUnreadChanged = { searchViewModel.dispatch(SearchEvent.OnOnlyUnreadChanged(it)) },
          )
        }

        Spacer(Modifier.requiredHeight(12.dp))

        HorizontalDivider(
          modifier = Modifier.fillMaxWidth(),
          color = AppTheme.colorScheme.outlineVariant,
        )
      }
    },
    content = { padding ->
      Box(modifier = Modifier.fillMaxSize()) {
        LaunchedEffect(searchViewModel.searchSortOrder, state.selectedSource) {
          listState.animateScrollToItem(0)
        }

        LazyColumn(
          contentPadding =
            PaddingValues(
              bottom = padding.calculateBottomPadding() + 80.dp,
              top = padding.calculateTopPadding(),
            ),
          state = listState,
        ) {
          if (searchResults.itemCount > 0) {
            item {
              SubHeader(
                text =
                  pluralStringResource(
                    Res.plurals.searchResultsCount,
                    searchResults.itemCount,
                    searchResults.itemCount,
                  ),
                modifier = Modifier.padding(top = 8.dp),
              )
            }
          }

          items(
            count = searchResults.itemCount,
            key = { index ->
              val post = searchResults[index]
              if (post != null) {
                PostListKey.from(post)
              } else {
                index
              }
            },
          ) { index ->
            val post = searchResults[index]
            if (post != null) {
              PostListItem(
                modifier = Modifier.animateItem(),
                item = post,
                onClick = {
                  openPost(
                    searchViewModel.searchQuery.text,
                    searchViewModel.searchSortOrder,
                    index,
                    post,
                  )
                },
                onPostBookmarkClick = {
                  searchViewModel.dispatch(SearchEvent.OnPostBookmarkClick(post))
                },
                onPostCommentsClick = {
                  post.commentsLink?.let { coroutineScope.launch { linkHandler.openLink(it) } }
                },
                onPostSourceClick = {
                  // no-op
                },
                updatePostReadStatus = { updatedReadStatus ->
                  searchViewModel.dispatch(
                    SearchEvent.UpdatePostReadStatus(post.id, updatedReadStatus)
                  )
                },
                reduceReadItemAlpha = true,
                postMetadataConfig = PostMetadataConfig.DEFAULT.copy(enablePostSource = false),
              )

              if (index != searchResults.itemCount - 1) {
                HorizontalDivider(
                  modifier = Modifier.fillParentMaxWidth().padding(horizontal = 24.dp),
                  color = AppTheme.colorScheme.outlineVariant,
                )
              }
            }
          }
        }

        NewArticlesScrollToTopButton(
          unreadSinceLastSync = null,
          canShowScrollToTop = showScrollToTop,
          modifier =
            Modifier.padding(
              end = padding.calculateEndPadding(layoutDirection) + 16.dp,
              bottom = padding.calculateBottomPadding() + 16.dp,
            ),
          onLoadNewArticlesClick = {},
        ) {
          listState.animateScrollToItem(0)
        }
      }
    },
    containerColor = AppTheme.colorScheme.backdrop,
    contentColor = Color.Unspecified,
  )

  if (showSourcePicker) {
    SourcePicker(
      sources = searchViewModel.sources.collectAsLazyPagingItems(),
      onSourceSelected = { source ->
        searchViewModel.dispatch(SearchEvent.OnSourceChanged(source))
        showSourcePicker = false
      },
      onDismiss = { showSourcePicker = false },
    )
  }
}

@Composable
private fun SourceFilterChips(
  selectedSource: Source?,
  onlyBookmarked: Boolean,
  onlyUnread: Boolean,
  onSourceClick: () -> Unit,
  onClearSourceClick: () -> Unit,
  onOnlyBookmarkedChanged: (Boolean) -> Unit,
  onOnlyUnreadChanged: (Boolean) -> Unit,
  modifier: Modifier = Modifier,
) {
  LazyRow(
    modifier =
      modifier
        .fillMaxWidth()
        .background(AppTheme.colorScheme.surface)
        .padding(horizontal = 16.dp, vertical = 8.dp),
    horizontalArrangement = Arrangement.spacedBy(8.dp),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    item {
      SourceChip(
        selected = selectedSource == null,
        onClick = onClearSourceClick,
        label = stringResource(Res.string.buttonAll),
        icon = {
          Icon(
            imageVector = TwineIcons.All,
            contentDescription = null,
            modifier = Modifier.requiredSize(16.dp),
          )
        },
      )
    }

    item {
      if (selectedSource != null) {
        SourceChip(
          selected = true,
          onClick = onSourceClick,
          label =
            when (selectedSource) {
              is Feed -> selectedSource.name
              is FeedGroup -> selectedSource.name
              else -> ""
            },
          onTrailingIconClick = onClearSourceClick,
        )
      } else {
        SourceChip(
          selected = false,
          onClick = onSourceClick,
          label = stringResource(Res.string.filter),
          icon = {
            Icon(
              imageVector = TwineIcons.FilterList,
              contentDescription = null,
              modifier = Modifier.requiredSize(16.dp),
            )
          },
        )
      }
    }

    item {
      SourceChip(
        selected = onlyBookmarked,
        onClick = { onOnlyBookmarkedChanged(!onlyBookmarked) },
        label = stringResource(Res.string.bookmarks),
        icon = {
          Icon(
            imageVector = TwineIcons.Bookmarked,
            contentDescription = null,
            modifier = Modifier.requiredSize(16.dp),
          )
        },
      )
    }

    item {
      SourceChip(
        selected = onlyUnread,
        onClick = { onOnlyUnreadChanged(!onlyUnread) },
        label = stringResource(Res.string.postsUnread),
        icon = {
          Icon(
            imageVector = TwineIcons.RadioUnselected,
            contentDescription = null,
            modifier = Modifier.requiredSize(16.dp),
          )
        },
      )
    }
  }
}

@Composable
private fun SourceChip(
  selected: Boolean,
  onClick: () -> Unit,
  label: String,
  modifier: Modifier = Modifier,
  icon: @Composable (() -> Unit)? = null,
  onTrailingIconClick: (() -> Unit)? = null,
) {
  val backgroundColor =
    if (selected) AppTheme.colorScheme.primary else AppTheme.colorScheme.surfaceContainer
  val contentColor =
    if (selected) AppTheme.colorScheme.onPrimary else AppTheme.colorScheme.onSurface

  Row(
    modifier =
      modifier
        .clip(RoundedCornerShape(12.dp))
        .background(backgroundColor)
        .clickable(onClick = onClick)
        .padding(horizontal = 12.dp, vertical = 6.dp),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(8.dp),
  ) {
    CompositionLocalProvider(LocalContentColor provides contentColor) { icon?.invoke() }
    Text(
      text = label,
      style = MaterialTheme.typography.labelLarge,
      color = contentColor,
      maxLines = 1,
      overflow = TextOverflow.Ellipsis,
    )
    if (onTrailingIconClick != null) {
      IconButton(onClick = onTrailingIconClick, modifier = Modifier.requiredSize(18.dp)) {
        Icon(imageVector = TwineIcons.Close, contentDescription = null, tint = contentColor)
      }
    }
  }
}

@Composable
private fun SourceIcon(source: Source, modifier: Modifier = Modifier) {
  when (source) {
    is Feed -> {
      FeedIcon(
        icon = source.icon,
        homepageLink = source.homepageLink,
        showFeedFavIcon = source.showFeedFavIcon,
        contentDescription = null,
        modifier = modifier,
        contentScale = ContentScale.Crop,
        shape = RoundedCornerShape(8.dp),
      )
    }
    is FeedGroup -> {
      FeedGroupIconGrid(
        feedIconLinks = source.feedIconLinks,
        feedShowFavIconSettings = source.feedShowFavIconSettings,
        feedHomepageLinks = source.feedHomepageLinks,
        modifier = modifier,
      )
    }
  }
}

@Composable
private fun SourcePicker(
  sources: LazyPagingItems<Source>,
  onSourceSelected: (Source) -> Unit,
  onDismiss: () -> Unit,
) {
  val sheetState = rememberModalBottomSheetState()

  ModalBottomSheet(
    onDismissRequest = onDismiss,
    sheetState = sheetState,
    containerColor = AppTheme.colorScheme.surfaceContainer,
    contentColor = AppTheme.colorScheme.onSurface,
  ) {
    Column(modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)) {
      SubHeader(text = stringResource(Res.string.feeds))

      LazyColumn(modifier = Modifier.fillMaxWidth().fillMaxHeight(0.5f)) {
        items(count = sources.itemCount) { index ->
          val source = sources[index]
          if (source != null) {
            SourcePickerItem(source = source, onClick = { onSourceSelected(source) })
          }
        }
      }
    }
  }
}

@Composable
private fun SourcePickerItem(source: Source, onClick: () -> Unit, modifier: Modifier = Modifier) {
  Row(
    modifier =
      modifier
        .fillMaxWidth()
        .clickable(onClick = onClick)
        .padding(horizontal = 24.dp, vertical = 12.dp),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(16.dp),
  ) {
    SourceIcon(source = source, modifier = Modifier.requiredSize(32.dp))
    Text(
      text =
        when (source) {
          is Feed -> source.name
          is FeedGroup -> source.name
          else -> ""
        },
      style = MaterialTheme.typography.bodyLarge,
      color = AppTheme.colorScheme.textEmphasisHigh,
      maxLines = 1,
      overflow = TextOverflow.Ellipsis,
    )
  }
}

@Composable
private fun SearchBar(
  query: TextFieldValue,
  sortOrder: SearchSortOrder,
  onQueryChange: (TextFieldValue) -> Unit,
  onBackClick: () -> Unit,
  onClearClick: () -> Unit,
  onSortOrderChanged: (SearchSortOrder) -> Unit,
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

  MaterialTheme(
    colorScheme = MaterialTheme.colorScheme.copy(primary = AppTheme.colorScheme.tintedForeground)
  ) {
    Row(
      modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
      verticalAlignment = Alignment.CenterVertically,
    ) {
      CircularIconButton(
        icon = TwineIcons.ArrowBack,
        label = stringResource(Res.string.buttonGoBack),
        onClick = onBackClick,
      )

      Spacer(modifier = Modifier.width(8.dp))

      TextField(
        modifier =
          Modifier.weight(1f).focusRequester(focusRequester).onFocusChanged {
            isSearchBarFocused = it.isFocused
          },
        value = query.copy(selection = TextRange(query.text.length)),
        onValueChange = onQueryChange,
        placeholder = {
          Text(
            text = stringResource(Res.string.postsSearchHint),
            color = AppTheme.colorScheme.textEmphasisHigh,
            style = MaterialTheme.typography.bodyLarge,
          )
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
          ),
      )
    }
  }
}

@Composable
private fun SearchSortButton(
  sortOrder: SearchSortOrder,
  onSortOrderChanged: (SearchSortOrder) -> Unit,
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
            Newest -> stringResource(Res.string.searchSortNewest)
            Oldest -> stringResource(Res.string.searchSortOldest)
          },
        style = MaterialTheme.typography.labelLarge,
        color = AppTheme.colorScheme.onSurface,
      )
      Spacer(Modifier.requiredWidth(8.dp))
      Icon(
        imageVector = TwineIcons.Sort,
        contentDescription = null,
        tint = AppTheme.colorScheme.onSurface,
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
        },
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
  DropdownMenu(expanded = isDropdownExpanded, offset = offset, onDismissRequest = onDismiss) {
    DropdownMenuItem(onClick = { onSortOrderChanged(Newest) }) {
      Text(
        text = stringResource(Res.string.searchSortNewestFirst),
        style = MaterialTheme.typography.bodyLarge,
      )
    }
    DropdownMenuItem(onClick = { onSortOrderChanged(Oldest) }) {
      Text(
        text = stringResource(Res.string.searchSortOldestFirst),
        style = MaterialTheme.typography.bodyLarge,
      )
    }
  }
}

@Composable
private fun ClearSearchQueryButton(onClearClick: () -> Unit) {
  IconButton(onClick = onClearClick) {
    Icon(TwineIcons.Close, contentDescription = null, tint = AppTheme.colorScheme.onSurface)
  }
}
