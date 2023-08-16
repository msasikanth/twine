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
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ripple.LocalRippleTheme
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.BlurredEdgeTreatment
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.moriatsushi.insetsx.statusBarsPadding
import dev.sasikanth.rss.reader.components.AsyncImage
import dev.sasikanth.rss.reader.components.DropdownMenuShareItem
import dev.sasikanth.rss.reader.database.PostWithMetadata
import dev.sasikanth.rss.reader.ui.AppTheme
import dev.sasikanth.rss.reader.ui.ListItemRippleTheme
import kotlinx.collections.immutable.ImmutableList

@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun FeaturedPostItems(
  pagerState: PagerState,
  featuredPosts: ImmutableList<PostWithMetadata>,
  modifier: Modifier = Modifier,
  onItemClick: (PostWithMetadata) -> Unit,
  onFeaturedItemChange: (imageUrl: String?) -> Unit
) {
  Box(modifier = modifier) {
    var selectedImage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(pagerState.settledPage, featuredPosts) {
      val selectedFeaturedPost = featuredPosts.getOrNull(pagerState.settledPage)
      selectedImage = selectedFeaturedPost?.imageUrl
      onFeaturedItemChange(selectedImage)
    }

    selectedImage?.let { FeaturedPostItemBackground(imageUrl = it) }

    HorizontalPager(
      modifier = Modifier.statusBarsPadding(),
      state = pagerState,
      contentPadding = PaddingValues(horizontal = 24.dp, vertical = 24.dp),
      pageSpacing = 16.dp,
      verticalAlignment = Alignment.Top
    ) {
      val featuredPost = featuredPosts[it]
      FeaturedPostItem(item = featuredPost) { onItemClick(featuredPost) }
    }
  }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun FeaturedPostItem(item: PostWithMetadata, onClick: () -> Unit) {
  CompositionLocalProvider(LocalRippleTheme provides ListItemRippleTheme) {
    val hapticFeedback = LocalHapticFeedback.current
    var dropdownMenuExpanded by remember { mutableStateOf(false) }

    Column(
      modifier =
        Modifier.clip(MaterialTheme.shapes.extraLarge)
          .combinedClickable(
            onClick = onClick,
            onLongClick = {
              hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
              dropdownMenuExpanded = true
            }
          )
    ) {
      Box {
        AsyncImage(
          url = item.imageUrl!!,
          modifier =
            Modifier.clip(MaterialTheme.shapes.extraLarge)
              .aspectRatio(1.77f)
              .background(AppTheme.colorScheme.surfaceContainerLowest),
          contentDescription = null,
          contentScale = ContentScale.Crop
        )

        PostMetadata(post = item, modifier = Modifier.align(Alignment.BottomStart))
      }

      Spacer(modifier = Modifier.height(16.dp))

      Box {
        Text(
          modifier = Modifier.padding(horizontal = 16.dp),
          text = item.title,
          style = MaterialTheme.typography.headlineSmall,
          color = AppTheme.colorScheme.textEmphasisHigh,
          maxLines = 2,
          overflow = TextOverflow.Ellipsis
        )

        MaterialTheme(colorScheme = lightColorScheme()) {
          DropdownMenu(
            expanded = dropdownMenuExpanded,
            onDismissRequest = { dropdownMenuExpanded = false }
          ) {
            DropdownMenuShareItem(contentToShare = item.link)
          }
        }
      }

      if (item.description.isNotBlank()) {
        Spacer(modifier = Modifier.height(8.dp))

        Text(
          modifier = Modifier.padding(horizontal = 16.dp),
          text = item.description,
          style = MaterialTheme.typography.bodySmall,
          color = AppTheme.colorScheme.textEmphasisHigh,
          maxLines = 3,
          overflow = TextOverflow.Ellipsis,
        )
      }

      Spacer(modifier = Modifier.height(16.dp))
    }
  }
}

@Composable
internal fun FeaturedPostItemBackground(modifier: Modifier = Modifier, imageUrl: String?) {
  BoxWithConstraints(modifier = modifier) {
    AsyncImage(
      url = imageUrl!!,
      modifier = Modifier.aspectRatio(1.1f).blur(100.dp, BlurredEdgeTreatment.Unbounded),
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
private fun PostMetadata(post: PostWithMetadata, modifier: Modifier = Modifier) {
  val feedName = post.feedName ?: "Unknown"
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
    post.feedIcon?.let { url ->
      Box(modifier = Modifier.clip(CircleShape).background(Color.White)) {
        AsyncImage(
          url = url,
          contentDescription = null,
          contentScale = ContentScale.Crop,
          modifier = Modifier.requiredSize(16.dp)
        )
      }
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
