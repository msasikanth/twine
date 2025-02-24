/*
 * Copyright 2024 Sasikanth Miriyampalli
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
package dev.sasikanth.rss.reader.components.image

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.RssFeed
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import coil3.compose.LocalPlatformContext
import coil3.compose.SubcomposeAsyncImage
import coil3.request.ImageRequest
import coil3.size.Dimension
import coil3.size.Size
import dev.sasikanth.rss.reader.favicons.FavIconImageLoader
import dev.sasikanth.rss.reader.ui.AppTheme
import dev.sasikanth.rss.reader.utils.LocalShowFeedFavIconSetting

@Composable
internal fun FeedIcon(
  url: String,
  contentDescription: String?,
  modifier: Modifier = Modifier,
  contentScale: ContentScale = ContentScale.Fit,
  size: Size = Size(Dimension.Undefined, 500)
) {
  val showFeedFavIcon = LocalShowFeedFavIconSetting.current
  Box(modifier.background(Color.White)) {
    if (showFeedFavIcon) {
      val context = LocalPlatformContext.current
      val imageRequest =
        ImageRequest.Builder(context).data(url).diskCacheKey(url).size(size).build()
      val imageLoader = FavIconImageLoader.get(context)

      SubcomposeAsyncImage(
        model = imageRequest,
        contentDescription = contentDescription,
        modifier = Modifier.matchParentSize(),
        contentScale = contentScale,
        imageLoader = imageLoader,
        error = { PlaceHolderIcon() },
        loading = { PlaceHolderIcon() }
      )
    } else {
      AsyncImage(
        modifier = Modifier.matchParentSize(),
        url = url,
        contentDescription = contentDescription,
        backgroundColor = null,
      )
    }
  }
}

@Composable
private fun PlaceHolderIcon() {
  Icon(
    imageVector = Icons.Rounded.RssFeed,
    contentDescription = null,
    tint = AppTheme.colorScheme.tintedBackground,
  )
}
