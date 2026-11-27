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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Size as ComposeSize
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil3.SingletonImageLoader
import coil3.compose.LocalPlatformContext
import coil3.request.ImageRequest
import coil3.size.Size
import dev.sasikanth.rss.reader.favicons.DEFAULT_FAVICON_SIZE_PX
import dev.sasikanth.rss.reader.favicons.FavIconImageLoader
import dev.sasikanth.rss.reader.resources.icons.RSS
import dev.sasikanth.rss.reader.resources.icons.TwineIcons
import dev.sasikanth.rss.reader.ui.AppTheme
import dev.sasikanth.rss.reader.utils.LocalBlockImage

private val FeedIconShape = RoundedCornerShape(25)
private val DefaultFavIconSize = Size(DEFAULT_FAVICON_SIZE_PX, DEFAULT_FAVICON_SIZE_PX)
private val PlaceholderMaxIconSize = 24.dp

@Composable
internal fun FeedIcon(
  icon: String,
  homepageLink: String,
  showFeedFavIcon: Boolean,
  contentDescription: String?,
  modifier: Modifier = Modifier,
  contentScale: ContentScale = ContentScale.Fit,
  size: Size = DefaultFavIconSize,
) {
  val useFavIcon = showFeedFavIcon
  val shouldBlockImage = LocalBlockImage.current
  val colorScheme = AppTheme.colorScheme

  if (shouldBlockImage) {
    Box(
      modifier = Modifier.then(modifier).background(Color.White, FeedIconShape),
      contentAlignment = Alignment.Center,
    ) {
      Icon(
        imageVector = TwineIcons.RSS,
        contentDescription = contentDescription,
        tint = colorScheme.primary,
      )
    }
  } else {
    val context = LocalPlatformContext.current
    var useFallback by remember(icon, homepageLink, useFavIcon) { mutableStateOf(false) }
    val loadFavIcon = useFavIcon || useFallback

    val imageRequest =
      remember(context, icon, homepageLink, loadFavIcon, size) {
        val url = if (loadFavIcon) homepageLink else icon
        val memoryCacheKey = "$url#${size.width}x${size.height}"
        ImageRequest.Builder(context)
          .data(url)
          .diskCacheKey(url)
          .memoryCacheKey(memoryCacheKey)
          .size(size)
          .build()
      }

    val imageLoader =
      remember(context, loadFavIcon) {
        if (loadFavIcon) {
          FavIconImageLoader.get(context)
        } else {
          SingletonImageLoader.get(context)
        }
      }

    val rssPainter = rememberVectorPainter(TwineIcons.RSS)
    val placeholderPainter =
      remember(rssPainter, colorScheme) {
        TintedCenteredPainter(delegate = rssPainter, colorProvider = { colorScheme.primary })
      }

    coil3.compose.AsyncImage(
      model = imageRequest,
      contentDescription = contentDescription,
      imageLoader = imageLoader,
      modifier = Modifier.then(modifier).background(Color.White, FeedIconShape).clip(FeedIconShape),
      placeholder = placeholderPainter,
      error = placeholderPainter,
      fallback = placeholderPainter,
      onError =
        if (!loadFavIcon) {
          { useFallback = true }
        } else {
          null
        },
      contentScale = contentScale,
    )
  }
}

@Stable
private class TintedCenteredPainter(
  private val delegate: Painter,
  private val colorProvider: () -> Color,
) : Painter() {
  private var lastColor: Color = Color.Unspecified
  private var lastColorFilter: ColorFilter? = null

  override val intrinsicSize: ComposeSize
    get() = ComposeSize.Unspecified

  override fun DrawScope.onDraw() {
    val color = colorProvider()
    val filter =
      if (color == lastColor && lastColorFilter != null) {
        lastColorFilter
      } else {
        ColorFilter.tint(color).also {
          lastColor = color
          lastColorFilter = it
        }
      }
    val maxPx = PlaceholderMaxIconSize.toPx()
    val targetDim = minOf(size.width, size.height, maxPx)
    val left = (size.width - targetDim) * 0.5f
    val top = (size.height - targetDim) * 0.5f
    translate(left = left, top = top) {
      with(delegate) { draw(size = ComposeSize(targetDim, targetDim), colorFilter = filter) }
    }
  }
}
