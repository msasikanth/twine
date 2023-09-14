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
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import androidx.compose.ui.composed
import androidx.compose.ui.draw.BlurredEdgeTreatment
import androidx.compose.ui.draw.blur
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.dp
import dev.sasikanth.rss.reader.components.AsyncImage
import dev.sasikanth.rss.reader.models.local.PostWithMetadata
import dev.sasikanth.rss.reader.resources.icons.Bookmarks
import dev.sasikanth.rss.reader.resources.icons.RSS
import dev.sasikanth.rss.reader.resources.icons.TwineIcons
import dev.sasikanth.rss.reader.resources.strings.LocalStrings
import dev.sasikanth.rss.reader.ui.AppTheme
import dev.sasikanth.rss.reader.utils.LocalWindowSizeClass
import dev.sasikanth.rss.reader.utils.canBlurImage
import kotlinx.collections.immutable.ImmutableList
import kotlinx.coroutines.flow.collectLatest

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
  onPostCommentsClick: (String) -> Unit,
  onFeaturedItemChange: (imageUrl: String?) -> Unit,
  onSearchClicked: () -> Unit,
  onBookmarksClicked: () -> Unit,
  onSettingsClicked: () -> Unit
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

    Column {
      AppBar(
        onSearchClicked = onSearchClicked,
        onBookmarksClicked = onBookmarksClicked,
        onSettingsClicked = onSettingsClicked
      )

      if (featuredPosts.isNotEmpty()) {
        val layoutDirection = LocalLayoutDirection.current

        val systemBarsPaddingValues =
          WindowInsets.systemBars.only(WindowInsetsSides.Horizontal).asPaddingValues()
        val startPadding = systemBarsPaddingValues.calculateStartPadding(layoutDirection)
        val endPadding = systemBarsPaddingValues.calculateEndPadding(layoutDirection)

        val horizontalPadding =
          if (startPadding > endPadding) {
            startPadding
          } else {
            endPadding
          }

        val pagerContentPadding =
          PaddingValues(
            start = horizontalPadding + 24.dp,
            top = 24.dp,
            end = horizontalPadding + 24.dp,
            bottom = 24.dp
          )

        HorizontalPager(
          state = pagerState,
          contentPadding = pagerContentPadding,
          pageSpacing = 16.dp,
          verticalAlignment = Alignment.Top
        ) {
          val featuredPost = featuredPosts[it]
          FeaturedPostItem(
            item = featuredPost,
            onClick = { onItemClick(featuredPost) },
            onBookmarkClick = { onPostBookmarkClick(featuredPost) },
            onCommentsClick = { onPostCommentsClick(featuredPost.commentsLink!!) }
          )
        }
      }
    }
  }
}

@Composable
private fun AppBar(
  onSearchClicked: () -> Unit,
  onBookmarksClicked: () -> Unit,
  onSettingsClicked: () -> Unit
) {
  Row(
    modifier =
      Modifier.fillMaxWidth()
        .windowInsetsPadding(
          WindowInsets.systemBars.only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal)
        )
        .padding(start = 24.dp, end = 12.dp, top = 16.dp),
    verticalAlignment = Alignment.CenterVertically
  ) {
    Row(verticalAlignment = Alignment.CenterVertically) {
      Text(
        text = LocalStrings.current.appName,
        color = Color.White,
        style = MaterialTheme.typography.headlineSmall
      )

      Spacer(Modifier.width(4.dp))

      Icon(imageVector = TwineIcons.RSS, contentDescription = null, tint = Color.White)
    }

    Spacer(Modifier.weight(1f))

    IconButton(
      onClick = onSearchClicked,
    ) {
      Icon(
        imageVector = Icons.Rounded.Search,
        contentDescription = LocalStrings.current.searchHint,
        tint = AppTheme.colorScheme.tintedForeground
      )
    }

    IconButton(
      onClick = onBookmarksClicked,
    ) {
      Icon(
        imageVector = TwineIcons.Bookmarks,
        contentDescription = LocalStrings.current.bookmarks,
        tint = AppTheme.colorScheme.tintedForeground
      )
    }

    OverflowMenu(onSettingsClicked)
  }
}

@Composable
private fun OverflowMenu(onSettingsClicked: () -> Unit) {
  Box {
    var dropdownExpanded by remember { mutableStateOf(false) }

    IconButton(
      onClick = { dropdownExpanded = true },
    ) {
      Icon(
        imageVector = Icons.Rounded.MoreVert,
        contentDescription = LocalStrings.current.moreMenuOptions,
        tint = AppTheme.colorScheme.tintedForeground
      )
    }

    if (dropdownExpanded) {
      DropdownMenu(expanded = dropdownExpanded, onDismissRequest = { dropdownExpanded = false }) {
        DropdownMenuItem(
          text = {
            Text(
              text = LocalStrings.current.settings,
              color = AppTheme.colorScheme.textEmphasisHigh
            )
          },
          leadingIcon = {
            Icon(
              imageVector = Icons.Rounded.Settings,
              contentDescription = LocalStrings.current.settings,
              tint = AppTheme.colorScheme.textEmphasisHigh
            )
          },
          onClick = {
            dropdownExpanded = false
            onSettingsClicked()
          }
        )
      }
    }
  }
}

@Composable
private fun FeaturedSectionBlurredBackground(modifier: Modifier = Modifier, imageUrl: String?) {
  BoxWithConstraints(modifier = modifier) {
    if (canBlurImage) {
      AsyncImage(
        url = imageUrl!!,
        modifier =
          Modifier.aspectRatio(featuredImageBackgroundAspectRatio)
            .blur(100.dp, BlurredEdgeTreatment.Unbounded),
        contentDescription = null,
        contentScale = ContentScale.Crop
      )
    } else {
      Box(
        modifier =
          Modifier.aspectRatio(0.8f).composed {
            val colorStops =
              listOf(
                AppTheme.colorScheme.tintedHighlight.copy(alpha = 0.0f),
                AppTheme.colorScheme.tintedHighlight.copy(alpha = 0.33f),
                AppTheme.colorScheme.tintedHighlight.copy(alpha = 0.50f),
                AppTheme.colorScheme.tintedHighlight.copy(alpha = 0.70f),
                AppTheme.colorScheme.tintedHighlight.copy(alpha = 0.60f),
                AppTheme.colorScheme.tintedHighlight.copy(alpha = 0.33f),
                AppTheme.colorScheme.tintedHighlight.copy(alpha = 0.10f),
                AppTheme.colorScheme.tintedHighlight.copy(alpha = 0.0f),
              )

            background(Brush.verticalGradient(colorStops))
          }
      )
    }

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
