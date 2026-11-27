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

package dev.sasikanth.rss.reader.favicons

import coil3.ImageLoader
import coil3.PlatformContext
import coil3.annotation.ExperimentalCoilApi
import coil3.disk.DiskCache
import coil3.memory.MemoryCache
import coil3.network.CacheStrategy
import coil3.network.ktor3.asNetworkClient
import coil3.svg.SvgDecoder
import io.ktor.client.HttpClient
import kotlinx.atomicfu.atomic
import kotlinx.atomicfu.updateAndGet
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import okio.Path.Companion.toPath

object FavIconImageLoader {

  private val reference = atomic<ImageLoader?>(null)
  private val configuredCachePath = atomic<String?>(null)
  private val httpClient = lazy { HttpClient() }
  private val decoderDispatcher = Dispatchers.IO.limitedParallelism(2)

  fun configure(cachePath: String) {
    configuredCachePath.value = cachePath
  }

  fun get(context: PlatformContext): ImageLoader {
    return reference.value ?: newImageLoader(context)
  }

  fun clearMemoryCache() {
    reference.value?.memoryCache?.clear()
  }

  fun clearCache(context: PlatformContext) {
    val loader = get(context)
    loader.memoryCache?.clear()
    loader.diskCache?.clear()
  }

  @OptIn(ExperimentalCoilApi::class)
  private fun newImageLoader(context: PlatformContext): ImageLoader {
    var imageLoader: ImageLoader? = null
    val sharedHttpClient = httpClient
    val cachePath = configuredCachePath.value

    return reference.updateAndGet { value ->
      when {
        value is ImageLoader -> value
        imageLoader != null -> imageLoader
        else -> {
          ImageLoader.Builder(context)
            .decoderCoroutineContext(decoderDispatcher)
            .components {
              add(FavIconFetcher.SizeInterceptor)
              add(SvgDecoder.Factory())
              add(
                FavIconFetcher.Factory(
                  networkClient = { sharedHttpClient.value.asNetworkClient() },
                  cacheStrategy = { CacheStrategy.DEFAULT },
                )
              )
            }
            .memoryCache { MemoryCache.Builder().maxSizePercent(context, percent = 0.05).build() }
            .apply {
              if (cachePath != null) {
                diskCache {
                  DiskCache.Builder()
                    .directory(
                      cachePath.toPath().resolve("dev_sasikanth_rss_reader_favicons_cache")
                    )
                    .maxSizePercent(0.02)
                    .build()
                }
              }
            }
            .build()
            .also { imageLoader = it }
        }
      }
    } as ImageLoader
  }
}
