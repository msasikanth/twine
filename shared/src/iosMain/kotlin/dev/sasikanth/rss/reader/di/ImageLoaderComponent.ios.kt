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

package dev.sasikanth.rss.reader.di

import coil3.PlatformContext
import kotlinx.cinterop.ExperimentalForeignApi
import me.tatarka.inject.annotations.Provides
import okio.Path
import okio.Path.Companion.toPath
import platform.Foundation.NSCachesDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSUserDomainMask

actual interface ImageLoaderPlatformComponent {

  @Provides fun providePlatformContext(): PlatformContext = PlatformContext.INSTANCE

  @Provides
  fun diskCache(): Path =
    NSFileManager.defaultManager.cacheDir.toPath().resolve("dev_sasikanth_rss_reader_images_cache")

  @OptIn(ExperimentalForeignApi::class)
  private val NSFileManager.cacheDir: String
    get() =
      URLForDirectory(
          directory = NSCachesDirectory,
          inDomain = NSUserDomainMask,
          appropriateForURL = null,
          create = true,
          error = null,
        )
        ?.path
        .orEmpty()
}
