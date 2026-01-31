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

import dev.sasikanth.rss.reader.app.AppInfo
import dev.sasikanth.rss.reader.app.isFoss
import dev.sasikanth.rss.reader.billing.BillingHandler
import dev.sasikanth.rss.reader.data.repository.RssRepository
import dev.sasikanth.rss.reader.data.repository.SettingsRepository
import dev.sasikanth.rss.reader.data.repository.WidgetDataRepository
import dev.sasikanth.rss.reader.data.sync.SyncCoordinator
import dev.sasikanth.rss.reader.data.sync.utils.NewArticleNotifier
import dev.sasikanth.rss.reader.di.scopes.AppScope
import dev.sasikanth.rss.reader.reader.readability.IosReadabilityRunner
import dev.sasikanth.rss.reader.reader.redability.ReadabilityRunner
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

  abstract val syncCoordinator: SyncCoordinator

  abstract val newArticleNotifier: NewArticleNotifier

  abstract val widgetDataRepository: WidgetDataRepository

  abstract val billingHandler: BillingHandler

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
      isFoss = isFoss,
      cachePath = { NSFileManager.defaultManager.cacheDir }
    )

  @Provides
  @AppScope
  fun providesReadabilityRunner(readabilityRunner: IosReadabilityRunner): ReadabilityRunner =
    readabilityRunner

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
