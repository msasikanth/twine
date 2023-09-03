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
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.BlurredEdgeTreatment
import androidx.compose.ui.draw.blur
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import dev.sasikanth.rss.reader.components.AsyncImage
import dev.sasikanth.rss.reader.database.PostWithMetadata
import dev.sasikanth.rss.reader.resources.IconResources
import dev.sasikanth.rss.reader.resources.strings.LocalStrings
import dev.sasikanth.rss.reader.ui.AppTheme
import dev.sasikanth.rss.reader.utils.LocalWindowSizeClass
import kotlinx.collections.immutable.ImmutableList
import kotlinx.coroutines.flow.collectLatest
import org.jetbrains.compose.resources.painterResource

private val featuredImageBackgroundAspectRatio: Float
  @Composable
  get() =
    when (LocalWindowSizeClass.current.widthSizeClass) {
      WindowWidthSizeClass.Compact -> 1.1f
      WindowWidthSizeClass.Medium -> 1.55f
      else -> 1.1f
    }

@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun FeaturedSection(
  pagerState: PagerState,
  featuredPosts: ImmutableList<PostWithMetadata>,
  modifier: Modifier = Modifier,
  onItemClick: (PostWithMetadata) -> Unit,
  onPostBookmarkClick: (PostWithMetadata) -> Unit,
  onFeaturedItemChange: (imageUrl: String?) -> Unit,
  onSearchClicked: () -> Unit,
  onBookmarksClicked: () -> Unit
) {
  Box(modifier = modifier) {
    var selectedImage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(pagerState, featuredPosts) {
      snapshotFlow { pagerState.settledPage }
        .collectLatest { index ->
          val selectedFeaturedPost = featuredPosts.getOrNull(index)
          selectedImage = selectedFeaturedPost?.imageUrl
          onFeaturedItemChange(selectedImage)
        }
    }

    if (featuredPosts.isNotEmpty()) {
      selectedImage?.let { FeaturedSectionBlurredBackground(imageUrl = it) }
    }

    Column(modifier = Modifier.windowInsetsPadding(WindowInsets.statusBars)) {
      AppBar(onSearchClicked, onBookmarksClicked)

      if (featuredPosts.isNotEmpty()) {
        HorizontalPager(
          state = pagerState,
          contentPadding = PaddingValues(horizontal = 24.dp, vertical = 24.dp),
          pageSpacing = 16.dp,
          verticalAlignment = Alignment.Top
        ) {
          val featuredPost = featuredPosts[it]
          FeaturedPostItem(
            item = featuredPost,
            onClick = { onItemClick(featuredPost) },
            onBookmarkClick = { onPostBookmarkClick(featuredPost) }
          )
        }
      }
    }
  }
}

@Composable
private fun AppBar(onSearchClicked: () -> Unit, onBookmarksClicked: () -> Unit) {
  Row(
    modifier = Modifier.fillMaxWidth().padding(start = 24.dp, end = 12.dp, top = 16.dp),
    verticalAlignment = Alignment.CenterVertically
  ) {
    Row(verticalAlignment = Alignment.CenterVertically) {
      Text(
        text = LocalStrings.current.appName,
        color = Color.White,
        style = MaterialTheme.typography.headlineSmall
      )

      Spacer(Modifier.width(4.dp))

      Icon(
        painter = painterResource(IconResources.rss),
        contentDescription = null,
        tint = Color.White
      )
    }

    Spacer(Modifier.weight(1f))

    IconButton(
      onClick = onBookmarksClicked,
    ) {
      Icon(
        painter = painterResource(IconResources.bookmarks),
        contentDescription = LocalStrings.current.bookmarks,
        tint = AppTheme.colorScheme.tintedForeground
      )
    }

    IconButton(
      onClick = onSearchClicked,
    ) {
      Icon(
        Icons.Rounded.Search,
        contentDescription = LocalStrings.current.searchHint,
        tint = AppTheme.colorScheme.tintedForeground
      )
    }
  }
}

@Composable
private fun FeaturedSectionBlurredBackground(modifier: Modifier = Modifier, imageUrl: String?) {
  BoxWithConstraints(modifier = modifier) {
    AsyncImage(
      url = imageUrl!!,
      modifier =
        Modifier.aspectRatio(featuredImageBackgroundAspectRatio)
          .blur(100.dp, BlurredEdgeTreatment.Unbounded),
      contentDescription = null,
      contentScale = ContentScale.Crop
    )

    Box(
      modifier =
        Modifier.matchParentSize()
          .background(
            brush =
              Brush.radialGradient(
                colors =
                  listOf(
                    Color.Black,
                    Color.Black.copy(alpha = 0.0f),
                    Color.Black.copy(alpha = 0.0f)
                  ),
                center = Offset(x = constraints.maxWidth.toFloat(), y = 40f)
              )
          )
    )

    Box(
      modifier =
        Modifier.matchParentSize()
          .background(
            brush =
              Brush.verticalGradient(
                colors = listOf(Color.Black, Color.Black.copy(alpha = 0.0f)),
              )
          )
    )
  }
}
