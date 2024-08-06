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

package dev.sasikanth.rss.reader.ui

import androidx.collection.lruCache
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import coil3.ImageLoader
import coil3.PlatformContext
import coil3.annotation.ExperimentalCoilApi
import coil3.request.ImageRequest
import dev.sasikanth.material.color.utilities.quantize.QuantizerCelebi
import dev.sasikanth.material.color.utilities.score.Score
import dev.sasikanth.rss.reader.utils.toComposeImageBitmap
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlinx.coroutines.suspendCancellableCoroutine
import me.tatarka.inject.annotations.Inject

@Inject
class SeedColorExtractor(
  private val imageLoader: Lazy<ImageLoader>,
  private val platformContext: Lazy<PlatformContext>,
) {
  private val cache = lruCache<String, Color>(100)

  @OptIn(ExperimentalCoilApi::class)
  suspend fun calculateSeedColor(url: String): Color {
    val cached = cache[url]
    if (cached != null) {
      return cached
    }

    val bitmap = suspendCancellableCoroutine { cont ->
      val request =
        ImageRequest.Builder(platformContext.value)
          .data(url)
          .size(DEFAULT_REQUEST_SIZE)
          .target(
            onSuccess = { result ->
              cont.resume(result.toComposeImageBitmap(platformContext.value))
            },
            onError = { cont.resumeWithException(IllegalArgumentException()) },
          )
          .build()

      imageLoader.value.enqueue(request)
    }

    return bitmap.seedColor().also { cache.put(url, it) }
  }

  private fun ImageBitmap.seedColor(): Color {
    val bitmapPixels = IntArray(width * height)
    readPixels(buffer = bitmapPixels)

    return Color(Score.score(QuantizerCelebi.quantize(bitmapPixels, maxColors = 128)).first())
  }

  private companion object {
    const val DEFAULT_REQUEST_SIZE = 64
  }
}
