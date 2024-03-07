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
import dev.sasikanth.rss.reader.di.scopes.AppScope
import dev.sasikanth.rss.reader.filemanager.FileManager
import dev.sasikanth.rss.reader.repository.RssRepository
import dev.sasikanth.rss.reader.repository.SettingsRepository
import kotlin.experimental.ExperimentalNativeApi
import me.tatarka.inject.annotations.Component
import me.tatarka.inject.annotations.Provides
import platform.Foundation.NSBundle
import platform.UIKit.UIViewController

@AppScope
@Component
abstract class ApplicationComponent(
  @get:Provides val uiViewControllerProvider: () -> UIViewController,
) : SharedApplicationComponent() {

  abstract val fileManager: FileManager

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
    )
}
