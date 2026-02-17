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
package dev.sasikanth.rss.reader.components.image

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.RssFeed
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.ContentScale
import coil3.SingletonImageLoader
import coil3.compose.LocalPlatformContext
import coil3.request.ImageRequest
import coil3.size.Dimension
import coil3.size.Size
import dev.sasikanth.rss.reader.favicons.FavIconImageLoader
import dev.sasikanth.rss.reader.ui.AppTheme
import dev.sasikanth.rss.reader.utils.LocalBlockImage
import dev.sasikanth.rss.reader.utils.LocalShowFeedFavIconSetting

@Composable
internal fun FeedIcon(
  icon: String,
  homepageLink: String,
  showFeedFavIcon: Boolean,
  contentDescription: String?,
  shape: Shape,
  modifier: Modifier = Modifier,
  contentScale: ContentScale = ContentScale.Fit,
  size: Size = Size(Dimension.Undefined, 500),
) {
  val globalShowFeedFavIcon = LocalShowFeedFavIconSetting.current
  val useFavIcon = showFeedFavIcon && globalShowFeedFavIcon
  val shouldBlockImage = LocalBlockImage.current

  Box(
    modifier = Modifier.clip(shape).then(modifier).background(Color.White, shape),
    contentAlignment = Alignment.Center,
  ) {
    if (shouldBlockImage) {
      PlaceHolderIcon()
    } else {
      val context = LocalPlatformContext.current
      var useFallback by remember(icon, homepageLink, useFavIcon) { mutableStateOf(false) }
      val loadFavIcon = useFavIcon || useFallback

      val imageRequest =
        remember(icon, homepageLink, loadFavIcon, size) {
          val url = if (loadFavIcon) homepageLink else icon
          ImageRequest.Builder(context).data(url).diskCacheKey(url).size(size).build()
        }

      val imageLoader =
        remember(loadFavIcon) {
          if (loadFavIcon) {
            FavIconImageLoader.get(context)
          } else {
            SingletonImageLoader.get(context)
          }
        }

      var isSuccess by remember { mutableStateOf(false) }

      if (!isSuccess) {
        PlaceHolderIcon()
      }

      coil3.compose.AsyncImage(
        model = imageRequest,
        contentDescription = contentDescription,
        modifier = Modifier.matchParentSize(),
        contentScale = contentScale,
        imageLoader = imageLoader,
        onLoading = { isSuccess = false },
        onSuccess = { isSuccess = true },
        onError = {
          if (!loadFavIcon) {
            useFallback = true
          }
        },
      )
    }
  }
}

@Composable
private fun PlaceHolderIcon() {
  Icon(
    imageVector = Icons.Rounded.RssFeed,
    contentDescription = null,
    tint = AppTheme.colorScheme.primary,
  )
}
