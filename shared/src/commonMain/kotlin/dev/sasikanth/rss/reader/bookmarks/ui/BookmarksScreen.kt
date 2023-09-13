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
package dev.sasikanth.rss.reader.bookmarks.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.dp
import app.cash.paging.compose.collectAsLazyPagingItems
import dev.sasikanth.rss.reader.bookmarks.BookmarksEvent
import dev.sasikanth.rss.reader.bookmarks.BookmarksPresenter
import dev.sasikanth.rss.reader.components.ScrollToTopButton
import dev.sasikanth.rss.reader.home.ui.PostListItem
import dev.sasikanth.rss.reader.resources.strings.LocalStrings
import dev.sasikanth.rss.reader.ui.AppTheme

@Composable
internal fun BookmarksScreen(
  bookmarksPresenter: BookmarksPresenter,
  openLink: (String) -> Unit,
  modifier: Modifier = Modifier
) {
  val state by bookmarksPresenter.state.collectAsState()
  val bookmarks = state.bookmarks.collectAsLazyPagingItems()
  val listState = rememberLazyListState()
  val showScrollToTop by remember { derivedStateOf { listState.firstVisibleItemIndex > 0 } }
  val layoutDirection = LocalLayoutDirection.current

  Scaffold(
    modifier = modifier,
    topBar = {
      Box {
        CenterAlignedTopAppBar(
          title = { Text(LocalStrings.current.bookmarks) },
          navigationIcon = {
            IconButton(onClick = { bookmarksPresenter.dispatch(BookmarksEvent.BackClicked) }) {
              Icon(Icons.Rounded.ArrowBack, contentDescription = null)
            }
          },
          colors =
            TopAppBarDefaults.topAppBarColors(
              containerColor = AppTheme.colorScheme.surface,
              navigationIconContentColor = AppTheme.colorScheme.onSurface,
              titleContentColor = AppTheme.colorScheme.onSurface,
              actionIconContentColor = AppTheme.colorScheme.onSurface
            ),
        )

        Divider(
          modifier = Modifier.fillMaxWidth().align(Alignment.BottomStart),
          color = AppTheme.colorScheme.surfaceContainer
        )
      }
    },
    content = { padding ->
      Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
          contentPadding =
            PaddingValues(
              bottom = padding.calculateBottomPadding() + 80.dp,
              top = padding.calculateTopPadding()
            ),
          state = listState
        ) {
          items(count = bookmarks.itemCount) { index ->
            val post = bookmarks[index]
            if (post != null) {
              PostListItem(
                item = post,
                onClick = { openLink(post.link) },
                onPostBookmarkClick = {
                  bookmarksPresenter.dispatch(BookmarksEvent.OnPostBookmarkClick(post))
                }
              )
              if (index != bookmarks.itemCount - 1) {
                Divider(
                  modifier = Modifier.fillParentMaxWidth().padding(horizontal = 24.dp),
                  color = AppTheme.colorScheme.surfaceContainer
                )
              }
            }
          }
        }

        ScrollToTopButton(
          visible = showScrollToTop,
          modifier =
            Modifier.padding(
              end = padding.calculateEndPadding(layoutDirection) + 24.dp,
              bottom = padding.calculateBottomPadding() + 24.dp
            )
        ) {
          listState.animateScrollToItem(0)
        }
      }
    },
    containerColor = AppTheme.colorScheme.surfaceContainerLowest,
    contentColor = Color.Unspecified
  )
}
