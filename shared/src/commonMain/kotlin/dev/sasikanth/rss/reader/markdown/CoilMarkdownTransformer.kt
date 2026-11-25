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

package dev.sasikanth.rss.reader.markdown

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImagePainter
import coil3.compose.LocalPlatformContext
import coil3.compose.rememberAsyncImagePainter
import coil3.request.ImageRequest
import coil3.size.Dimension
import com.mikepenz.markdown.model.ImageData
import com.mikepenz.markdown.model.ImageTransformer
import dev.sasikanth.rss.reader.reader.ui.LocalOnImageClick

internal val MarkdownImageSize = coil3.size.Size(2048, Dimension.Undefined)
private const val INLINE_IMAGE_MEMORY_CACHE_SUFFIX = "#reader_inline"

internal fun inlineImageMemoryCacheKey(link: String): String =
  "$link$INLINE_IMAGE_MEMORY_CACHE_SUFFIX"

object CoilMarkdownTransformer : ImageTransformer {

  @Composable
  override fun transform(link: String): ImageData? {
    val context = LocalPlatformContext.current
    val onImageClick = LocalOnImageClick.current
    val containerWidthPx = LocalWindowInfo.current.containerSize.width
    val requestSize =
      remember(containerWidthPx) {
        if (containerWidthPx > 0 && containerWidthPx < 2048) {
          coil3.size.Size(containerWidthPx, Dimension.Undefined)
        } else {
          MarkdownImageSize
        }
      }
    val model =
      remember(context, link, requestSize) {
        ImageRequest.Builder(context)
          .data(link)
          .size(requestSize)
          .diskCacheKey(link)
          .memoryCacheKey(inlineImageMemoryCacheKey(link))
          .build()
      }

    val shape = MaterialTheme.shapes.large
    return rememberAsyncImagePainter(model = model).let { painter ->
      ImageData(
        painter = painter,
        modifier = Modifier.padding(vertical = 8.dp).clip(shape).clickable { onImageClick(link) },
        alignment = Alignment.Center,
      )
    }
  }

  @Composable
  override fun intrinsicSize(painter: Painter): Size {
    if (painter is AsyncImagePainter) {
      val state by painter.state.collectAsState()
      return state.painter?.intrinsicSize ?: painter.intrinsicSize
    }
    return painter.intrinsicSize
  }
}
