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

package dev.sasikanth.rss.reader.ui

import androidx.collection.LruCache
import androidx.collection.lruCache
import androidx.compose.runtime.Stable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import coil3.ImageLoader
import coil3.PlatformContext
import coil3.request.ImageRequest
import coil3.request.SuccessResult
import com.materialkolor.ktx.themeColorOrNull
import dev.sasikanth.rss.reader.util.DispatchersProvider
import dev.sasikanth.rss.reader.utils.toComposeImageBitmap
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import me.tatarka.inject.annotations.Inject

@Inject
@Stable
class SeedColorExtractor(
  dispatchersProvider: DispatchersProvider,
  private val imageLoader: Lazy<ImageLoader>,
  private val platformContext: Lazy<PlatformContext>,
) {
  private val lruCache: LruCache<String, Color> = lruCache(maxSize = MAX_CACHE_SIZE)
  private val mutex = Mutex()
  private val inFlightRequests = mutableMapOf<String, Deferred<Color?>>()
  private val scope = CoroutineScope(SupervisorJob() + dispatchersProvider.io)

  suspend fun calculateSeedColor(url: String?): Color? {
    if (url.isNullOrBlank()) return null

    val cachedColor = lruCache[url]
    if (cachedColor != null) return cachedColor

    val deferred =
      mutex.withLock {
        inFlightRequests.getOrPut(url) {
          scope.async {
            try {
              val color = extractSeedColor(url)
              if (color != null) {
                lruCache.put(url, color)
              }
              color
            } finally {
              mutex.withLock { inFlightRequests.remove(url) }
            }
          }
        }
      }

    return try {
      deferred.await()
    } catch (e: Exception) {
      null
    }
  }

  fun cachedSeedColor(url: String?) =
    if (url.isNullOrBlank()) {
      null
    } else {
      lruCache[url]
    }

  private suspend fun extractSeedColor(url: String): Color? {
    val request =
      ImageRequest.Builder(platformContext.value).data(url).size(DEFAULT_REQUEST_SIZE).build()

    val result = imageLoader.value.execute(request)
    return if (result is SuccessResult) {
      val bitmap = result.image.toComposeImageBitmap(platformContext.value)
      bitmap.themeColorOrNull()
    } else {
      null
    }
  }

  private companion object {
    const val DEFAULT_REQUEST_SIZE = 64
    const val MAX_CACHE_SIZE = 1000
  }
}

internal val LocalSeedColorExtractor =
  staticCompositionLocalOf<SeedColorExtractor> {
    throw IllegalStateException("Provide a seed color extractor")
  }
