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

import android.content.Context
import android.content.pm.ApplicationInfo.FLAG_DEBUGGABLE
import android.os.Build
import dev.sasikanth.rss.reader.app.AppInfo
import dev.sasikanth.rss.reader.app.isFoss
import dev.sasikanth.rss.reader.billing.BillingHandler
import dev.sasikanth.rss.reader.data.repository.RssRepository
import dev.sasikanth.rss.reader.data.repository.SettingsRepository
import dev.sasikanth.rss.reader.data.repository.WidgetDataRepository
import dev.sasikanth.rss.reader.data.sync.SyncCoordinator
import dev.sasikanth.rss.reader.data.sync.utils.NewArticleNotifier
import dev.sasikanth.rss.reader.di.scopes.AppScope
import dev.sasikanth.rss.reader.reader.readability.AndroidReadabilityRunner
import dev.sasikanth.rss.reader.reader.redability.ReadabilityRunner
import me.tatarka.inject.annotations.Component
import me.tatarka.inject.annotations.Provides

@AppScope
@Component
abstract class ApplicationComponent(@get:Provides val context: Context) :
  SharedApplicationComponent() {

  abstract val rssRepository: RssRepository

  abstract val settingsRepository: SettingsRepository

  abstract val syncCoordinator: SyncCoordinator

  abstract val newArticleNotifier: NewArticleNotifier

  abstract val widgetDataRepository: WidgetDataRepository

  abstract val billingHandler: BillingHandler

  @Provides
  @AppScope
  fun providesAppInfo(context: Context): AppInfo {
    val packageManager = context.packageManager
    val packageInfo = packageManager.getPackageInfo(context.packageName, 0)
    val applicationInfo = packageManager.getApplicationInfo(context.packageName, 0)

    val versionCode =
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
        (packageInfo.longVersionCode and 0xffffffffL).toInt()
      } else {
        @Suppress("DEPRECATION") packageInfo.versionCode
      }

    return AppInfo(
      versionName = packageInfo.versionName ?: "0.0.1",
      versionCode = versionCode,
      isDebugBuild = (applicationInfo.flags and FLAG_DEBUGGABLE) != 0,
      isFoss = isFoss,
      cachePath = { context.cacheDir.absolutePath }
    )
  }

  @Provides
  @AppScope
  fun providesReadabilityRunner(readabilityRunner: AndroidReadabilityRunner): ReadabilityRunner =
    readabilityRunner

  companion object
}
