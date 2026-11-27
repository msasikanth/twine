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
import dev.sasikanth.rss.reader.di.scopes.AppScope
import dev.sasikanth.rss.reader.favicons.FavIconImageLoader
import dev.sasikanth.rss.reader.initializers.Initializer
import kotlin.concurrent.AtomicInt
import me.tatarka.inject.annotations.IntoSet
import me.tatarka.inject.annotations.Provides
import platform.Foundation.NSNotificationCenter
import platform.Foundation.NSOperationQueue
import platform.UIKit.UIApplicationDidReceiveMemoryWarningNotification
import platform.darwin.NSObjectProtocol

actual interface ImageLoaderPlatformComponent {

  @Provides @AppScope fun providePlatformContext(): PlatformContext = PlatformContext.INSTANCE

  @IntoSet
  @Provides
  @AppScope
  fun providesImageLoaderMemoryWarningInitializer(imageLoader: ImageLoader): Initializer =
    object : Initializer {
      private val registered = AtomicInt(0)
      private val observers = mutableListOf<NSObjectProtocol>()

      override fun initialize() {
        if (!registered.compareAndSet(0, 1)) return

        observers +=
          NSNotificationCenter.defaultCenter.addObserverForName(
            name = UIApplicationDidReceiveMemoryWarningNotification,
            `object` = null,
            queue = NSOperationQueue.mainQueue,
          ) { _ ->
            imageLoader.memoryCache?.clear()
            FavIconImageLoader.clearMemoryCache()
          }
      }
    }
}
