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

package dev.sasikanth.rss.reader.favicons

import coil3.ImageLoader
import coil3.PlatformContext
import coil3.annotation.ExperimentalCoilApi
import coil3.network.CacheStrategy
import coil3.network.ktor2.asNetworkClient
import io.ktor.client.HttpClient
import kotlinx.atomicfu.atomic
import kotlinx.atomicfu.updateAndGet

object FavIconImageLoader {

  private val reference = atomic<ImageLoader?>(null)

  fun get(context: PlatformContext): ImageLoader {
    return reference.value ?: newImageLoader(context)
  }

  @OptIn(ExperimentalCoilApi::class)
  private fun newImageLoader(context: PlatformContext): ImageLoader {
    var imageLoader: ImageLoader? = null

    return reference.updateAndGet { value ->
      when {
        value is ImageLoader -> value
        imageLoader != null -> imageLoader
        else -> {
          ImageLoader.Builder(context)
            .components {
              add(
                FavIconFetcher.Factory(
                  networkClient = { HttpClient().asNetworkClient() },
                  cacheStrategy = { CacheStrategy() }
                )
              )
            }
            .build()
            .also { imageLoader = it }
        }
      }
    } as ImageLoader
  }
}
