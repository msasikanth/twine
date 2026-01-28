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

package dev.sasikanth.rss.reader.di

import coil3.ImageLoader
import coil3.PlatformContext
import coil3.disk.DiskCache
import coil3.memory.MemoryCache
import coil3.svg.SvgDecoder
import dev.sasikanth.rss.reader.app.AppInfo
import me.tatarka.inject.annotations.Provides
import okio.Path.Companion.toPath

expect interface ImageLoaderPlatformComponent

interface ImageLoaderComponent : ImageLoaderPlatformComponent {

  val imageLoader: ImageLoader

  @Provides
  fun imageLoader(
    platformContext: PlatformContext,
    appInfo: AppInfo,
  ): ImageLoader {
    return ImageLoader.Builder(platformContext)
      .components { add(SvgDecoder.Factory()) }
      .memoryCache { MemoryCache.Builder().maxSizePercent(platformContext, percent = 0.25).build() }
      .diskCache {
        DiskCache.Builder()
          .directory(appInfo.cachePath().toPath().resolve("dev_sasikanth_rss_reader_images_cache"))
          .maxSizePercent(0.05)
          .build()
      }
      .build()
  }
}
