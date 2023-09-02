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
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import dev.sasikanth.rss.reader.components.AsyncImage
import dev.sasikanth.rss.reader.database.PostWithMetadata
import dev.sasikanth.rss.reader.resources.IconResources
import dev.sasikanth.rss.reader.resources.strings.LocalStrings
import dev.sasikanth.rss.reader.ui.AppTheme
import dev.sasikanth.rss.reader.utils.LocalWindowSizeClass
import dev.sasikanth.rss.reader.utils.relativeDurationString
import kotlinx.collections.immutable.ImmutableList
import kotlinx.coroutines.flow.collectLatest
import org.jetbrains.compose.resources.painterResource

private val featuredImageAspectRatio: Float
  @Composable
  get() =
    when (LocalWindowSizeClass.current.widthSizeClass) {
      WindowWidthSizeClass.Compact -> 1.77f
      WindowWidthSizeClass.Medium -> 2.5f
      else -> 1.77f
    }

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
internal fun FeaturedPostItems(
  pagerState: PagerState,
  featuredPosts: ImmutableList<PostWithMetadata>,
  modifier: Modifier = Modifier,
  onItemClick: (PostWithMetadata) -> Unit,
  onPostBookmarkClick: (PostWithMetadata) -> Unit,
  onFeaturedItemChange: (imageUrl: String?) -> Unit,
  onSearchClicked: () -> Unit
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
      selectedImage?.let { FeaturedPostItemBackground(imageUrl = it) }
    }

    Column(modifier = Modifier.windowInsetsPadding(WindowInsets.statusBars)) {
      AppBar(onSearchClicked)

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
private fun AppBar(onSearchClicked: () -> Unit) {
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
private fun FeaturedPostItem(
  item: PostWithMetadata,
  onClick: () -> Unit,
  onBookmarkClick: () -> Unit
) {
  Column(modifier = Modifier.clip(MaterialTheme.shapes.extraLarge).clickable(onClick = onClick)) {
    Box {
      AsyncImage(
        url = item.imageUrl!!,
        modifier =
          Modifier.clip(MaterialTheme.shapes.extraLarge)
            .aspectRatio(featuredImageAspectRatio)
            .background(AppTheme.colorScheme.surfaceContainerLowest),
        contentDescription = null,
        contentScale = ContentScale.Crop
      )

      PostSourceChip(post = item, modifier = Modifier.align(Alignment.BottomStart))
    }

    Spacer(modifier = Modifier.requiredHeight(8.dp))

    Text(
      modifier = Modifier.padding(horizontal = 16.dp),
      text = item.title,
      style = MaterialTheme.typography.headlineSmall,
      color = AppTheme.colorScheme.textEmphasisHigh,
      minLines = 2,
      maxLines = 2,
      overflow = TextOverflow.Ellipsis
    )

    if (item.description.isNotBlank()) {
      Spacer(modifier = Modifier.requiredHeight(8.dp))

      Text(
        modifier = Modifier.padding(horizontal = 16.dp),
        text = item.description,
        style = MaterialTheme.typography.bodySmall,
        color = AppTheme.colorScheme.textEmphasisHigh,
        minLines = 3,
        maxLines = 3,
        overflow = TextOverflow.Ellipsis,
      )
    }

    PostMetadata(
      modifier = Modifier.padding(start = 16.dp, end = 0.dp),
      feedName = item.feedName,
      postPublishedAt = item.date.relativeDurationString(),
      postLink = item.link,
      postBookmarked = item.bookmarked,
      onBookmarkClick = onBookmarkClick
    )

    Spacer(modifier = Modifier.height(8.dp))
  }
}

@Composable
internal fun FeaturedPostItemBackground(modifier: Modifier = Modifier, imageUrl: String?) {
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

@Composable
private fun PostSourceChip(post: PostWithMetadata, modifier: Modifier = Modifier) {
  val feedName = post.feedName
  val verticalPadding = 8.dp
  val startPadding = 8.dp
  val endPadding = 16.dp
  val margin = 12.dp

  Row(
    modifier =
      Modifier.padding(margin)
        .background(color = Color.Black, shape = RoundedCornerShape(50))
        .padding(
          start = startPadding,
          top = verticalPadding,
          end = endPadding,
          bottom = verticalPadding
        )
        .then(modifier),
    horizontalArrangement = Arrangement.spacedBy(8.dp),
    verticalAlignment = Alignment.CenterVertically
  ) {
    Box(modifier = Modifier.clip(CircleShape).background(Color.White)) {
      AsyncImage(
        url = post.feedIcon,
        contentDescription = null,
        contentScale = ContentScale.Crop,
        modifier = Modifier.requiredSize(16.dp)
      )
    }

    Text(
      style = MaterialTheme.typography.labelMedium,
      maxLines = 1,
      text = feedName.uppercase().take(12),
      color = AppTheme.colorScheme.textEmphasisHigh,
      textAlign = TextAlign.Left,
      overflow = TextOverflow.Clip
    )
  }
}
