/*
 * Copyright 2023 Sasikanth Miriyampalli
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

import dev.sasikanth.rss.reader.app.AppInfo
import dev.sasikanth.rss.reader.data.repository.RssRepository
import dev.sasikanth.rss.reader.data.repository.SettingsRepository
import dev.sasikanth.rss.reader.di.scopes.AppScope
import kotlin.experimental.ExperimentalNativeApi
import kotlinx.cinterop.ExperimentalForeignApi
import me.tatarka.inject.annotations.Component
import me.tatarka.inject.annotations.Provides
import platform.Foundation.NSBundle
import platform.Foundation.NSCachesDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSUserDomainMask
import platform.UIKit.UIViewController

@AppScope
@Component
abstract class ApplicationComponent(
  @get:Provides val uiViewControllerProvider: () -> UIViewController,
) : SharedApplicationComponent() {

  abstract val rssRepository: RssRepository

  abstract val settingsRepository: SettingsRepository

  @Provides
  @AppScope
  @OptIn(ExperimentalNativeApi::class)
  fun providesAppInfo(): AppInfo =
    AppInfo(
      versionCode =
        (NSBundle.mainBundle.infoDictionary?.get("CFBundleVersion") as? String)?.toIntOrNull() ?: 0,
      versionName = NSBundle.mainBundle.infoDictionary?.get("CFBundleShortVersionString") as? String
          ?: "",
      isDebugBuild = Platform.isDebugBinary,
      cachePath = { NSFileManager.defaultManager.cacheDir }
    )

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
