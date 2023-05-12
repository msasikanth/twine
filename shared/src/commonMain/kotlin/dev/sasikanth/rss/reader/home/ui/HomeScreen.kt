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
package dev.sasikanth.rss.reader.home.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.moriatsushi.insetsx.statusBarsPadding
import dev.sasikanth.rss.reader.database.PostWithMetadata
import dev.sasikanth.rss.reader.home.HomeEffect
import dev.sasikanth.rss.reader.home.HomeEvent
import dev.sasikanth.rss.reader.home.HomeViewModelFactory
import dev.sasikanth.rss.reader.home.isLoading
import dev.sasikanth.rss.reader.utils.openBrowser

private const val NUMBER_OF_FEATURED_POSTS = 6

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun HomeScreen(homeViewModelFactory: HomeViewModelFactory) {
  val viewModel = homeViewModelFactory.viewModel
  val state by viewModel.state.collectAsState()
  val posts by state.posts.collectAsState(initial = emptyList())

  // TODO: Move this transformation to data layer in background thread
  val postsWithImages = posts.filter { !it.imageUrl.isNullOrBlank() }
  val featuredPosts =
    if (postsWithImages.size > NUMBER_OF_FEATURED_POSTS) {
      postsWithImages.take(NUMBER_OF_FEATURED_POSTS)
    } else {
      postsWithImages
    }
  val postsList = posts.filter { !featuredPosts.contains(it) }

  val isRefreshing = state.loadingState.isLoading
  val swipeRefreshState =
    rememberPullRefreshState(
      isRefreshing,
      onRefresh = { viewModel.dispatch(HomeEvent.OnSwipeToRefresh) }
    )

  LaunchedEffect(Unit) {
    viewModel.effects.collect { effect ->
      when (effect) {
        HomeEffect.NavigateToAddScreen -> TODO()
        is HomeEffect.OpenPost -> {
          openBrowser(effect.post.link)
        }
      }
    }
  }

  Box(Modifier.pullRefresh(swipeRefreshState)) {
    LazyColumn(contentPadding = PaddingValues(bottom = 136.dp)) {
      if (featuredPosts.isNotEmpty()) {
        item {
          FeaturedPostItems(featuredPosts = featuredPosts) { post ->
            viewModel.dispatch(HomeEvent.OnPostClicked(post))
          }
        }
      }

      itemsIndexed(postsList) { i, post ->
        PostListItem(post) { viewModel.dispatch(HomeEvent.OnPostClicked(post)) }
        if (i != posts.size - 1) {
          Divider(
            modifier = Modifier.fillParentMaxWidth().padding(horizontal = 24.dp),
            color = MaterialTheme.colorScheme.outlineVariant
          )
        }
      }
    }

    PullRefreshIndicator(
      refreshing = isRefreshing,
      state = swipeRefreshState,
      modifier = Modifier.statusBarsPadding().align(Alignment.TopCenter)
    )
  }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun FeaturedPostItems(
  modifier: Modifier = Modifier,
  featuredPosts: List<PostWithMetadata>,
  onItemClick: (PostWithMetadata) -> Unit
) {
  Box(modifier = modifier) {
    val pagerState = rememberPagerState()
    val selectedFeaturedPost = featuredPosts[pagerState.settledPage]

    FeaturedPostItemBackground(imageUrl = selectedFeaturedPost.imageUrl)

    HorizontalPager(
      modifier = Modifier.statusBarsPadding(),
      state = pagerState,
      pageCount = featuredPosts.size,
      contentPadding = PaddingValues(horizontal = 24.dp, vertical = 24.dp),
      pageSpacing = 16.dp,
      verticalAlignment = Alignment.Top
    ) {
      val featuredPost = featuredPosts[it]
      FeaturedPostItem(item = featuredPost) { onItemClick(featuredPost) }
    }
  }
}
