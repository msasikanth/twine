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
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.arkivanov.decompose.extensions.compose.jetbrains.subscribeAsState
import com.moriatsushi.insetsx.statusBarsPadding
import dev.sasikanth.rss.reader.database.PostWithMetadata
import dev.sasikanth.rss.reader.home.HomeComponent
import dev.sasikanth.rss.reader.home.HomeEffect
import dev.sasikanth.rss.reader.home.HomeEvent
import dev.sasikanth.rss.reader.utils.openBrowser
import kotlinx.coroutines.channels.consumeEach

private const val NUMBER_OF_FEATURED_POSTS = 6

@Composable
fun HomeScreen(component: HomeComponent) {
  val viewModel = component.viewModel
  val state by viewModel.state.subscribeAsState()

  val posts by state.posts.collectAsState(initial = emptyList())

  val postsWithImages = posts.filter { !it.imageUrl.isNullOrBlank() }
  val featuredPosts =
    if (postsWithImages.size > NUMBER_OF_FEATURED_POSTS) {
      postsWithImages.take(NUMBER_OF_FEATURED_POSTS)
    } else {
      postsWithImages
    }
  val postsList = posts.filter { !featuredPosts.contains(it) }

  LaunchedEffect(Unit) {
    viewModel.effects.consumeEach { effect ->
      when (effect) {
        HomeEffect.NavigateToAddScreen -> TODO()
        is HomeEffect.OpenPost -> {
          openBrowser(effect.post.link)
        }
      }
    }
  }

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
