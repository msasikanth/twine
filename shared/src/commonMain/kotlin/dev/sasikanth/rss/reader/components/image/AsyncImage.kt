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

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import coil3.compose.LocalPlatformContext
import coil3.request.ImageRequest
import coil3.request.crossfade
import coil3.size.Dimension
import coil3.size.Size
import dev.sasikanth.rss.reader.utils.LocalBlockImage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO

// Shared across all image requests so the parallelism limit is global instead of
// allocating a new limited dispatcher per request.
private val imageDecoderDispatcher = Dispatchers.IO.limitedParallelism(4)

@Composable
internal fun AsyncImage(
  url: String,
  contentDescription: String?,
  modifier: Modifier = Modifier,
  contentScale: ContentScale = ContentScale.Fit,
  size: Size = Size(Dimension.Undefined, 500),
  colorFilter: ColorFilter? = null,
  alignment: Alignment = Alignment.Center,
) {
  val context = LocalPlatformContext.current
  val shouldBlockImage = LocalBlockImage.current

  if (shouldBlockImage) {
    // no-op
  } else {
    val model =
      remember(context, url, size) {
        ImageRequest.Builder(context)
          .data(url)
          .size(size)
          .diskCacheKey(url)
          .memoryCacheKey(url)
          .crossfade(true)
          .decoderCoroutineContext(imageDecoderDispatcher)
          .build()
      }
    coil3.compose.AsyncImage(
      model = model,
      contentDescription = contentDescription,
      modifier = modifier,
      contentScale = contentScale,
      colorFilter = colorFilter,
      alignment = alignment,
    )
  }
}
