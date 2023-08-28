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

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.dp
import com.moriatsushi.insetsx.statusBarsPadding
import dev.icerock.moko.resources.compose.stringResource
import dev.sasikanth.rss.reader.CommonRes
import dev.sasikanth.rss.reader.components.ScrollToTopButton
import dev.sasikanth.rss.reader.home.ui.PostListItem
import dev.sasikanth.rss.reader.search.SearchEvent
import dev.sasikanth.rss.reader.search.SearchPresenter
import dev.sasikanth.rss.reader.ui.AppTheme
import dev.sasikanth.rss.reader.utils.KeyboardState
import dev.sasikanth.rss.reader.utils.keyboardVisibilityAsState

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
        listState = listState,
        onQueryChange = { searchPresenter.dispatch(SearchEvent.SearchQueryChanged(it)) },
        onBackClick = { searchPresenter.dispatch(SearchEvent.BackClicked) },
        onClearClick = { searchPresenter.dispatch(SearchEvent.SearchQueryChanged("")) }
      )
    },
    content = {
      Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
          contentPadding =
            PaddingValues(
              start = it.calculateStartPadding(layoutDirection),
              end = it.calculateEndPadding(layoutDirection),
              top = it.calculateTopPadding(),
              bottom = it.calculateBottomPadding() + 64.dp
            ),
          state = listState,
        ) {
          itemsIndexed(state.searchResults) { index, post ->
            PostListItem(post) { openLink(post.link) }
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
          modifier = Modifier.padding(end = 24.dp, bottom = 24.dp)
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
  query: String,
  listState: LazyListState,
  onQueryChange: (String) -> Unit,
  onBackClick: () -> Unit,
  onClearClick: () -> Unit
) {
  val keyboardState by keyboardVisibilityAsState()
  val focusRequester = remember { FocusRequester() }
  val focusManager = LocalFocusManager.current

  val searchContainerAlpha by
    remember(listState) {
      // List first visible item offset before we start changing the search container alpha.
      //
      // As content reaches the search bar container, we start increasing the
      // alpha of the container.
      val threshold = 100f
      var alpha = 0f

      derivedStateOf {
        alpha =
          if (listState.firstVisibleItemIndex == 0) {
            val normalizedValue =
              ((listState.firstVisibleItemScrollOffset - 0f) / (threshold - 0f)).coerceIn(0f, 1f)
            normalizedValue
          } else {
            1f
          }

        alpha
      }
    }

  val animatedSearchContainerAlpha by animateFloatAsState(searchContainerAlpha)

  LaunchedEffect(keyboardState) {
    if (keyboardState == KeyboardState.Closed) {
      focusManager.clearFocus()
    }
  }

  LaunchedEffect(Unit) { focusRequester.requestFocus() }

  Box(
    modifier =
      Modifier.fillMaxWidth()
        .background(
          AppTheme.colorScheme.tintedForeground.copy(alpha = animatedSearchContainerAlpha)
        )
        .statusBarsPadding()
        .windowInsetsPadding(WindowInsets.navigationBars.only(WindowInsetsSides.Horizontal))
  ) {
    OutlinedTextField(
      modifier = Modifier.fillMaxWidth().padding(16.dp).focusRequester(focusRequester),
      value = query,
      onValueChange = onQueryChange,
      placeholder = {
        Text(
          stringResource(CommonRes.strings.search_hint),
          color = AppTheme.colorScheme.textEmphasisMed
        )
      },
      leadingIcon = {
        IconButton(onClick = onBackClick) {
          Icon(
            Icons.Filled.ArrowBack,
            contentDescription = null,
            tint = AppTheme.colorScheme.tintedForeground
          )
        }
      },
      trailingIcon = {
        if (query.isNotBlank()) {
          IconButton(onClick = onClearClick) {
            Icon(
              Icons.Filled.Close,
              contentDescription = null,
              tint = AppTheme.colorScheme.tintedForeground
            )
          }
        }
      },
      shape = RoundedCornerShape(50),
      singleLine = true,
      colors =
        TextFieldDefaults.colors(
          focusedContainerColor = AppTheme.colorScheme.tintedBackground,
          unfocusedContainerColor = AppTheme.colorScheme.tintedBackground,
          focusedIndicatorColor = AppTheme.colorScheme.tintedForeground,
          unfocusedIndicatorColor = AppTheme.colorScheme.tintedForeground,
          focusedTextColor = AppTheme.colorScheme.textEmphasisHigh,
        )
    )
  }
}
