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

import android.content.Context
import android.os.Build
import dev.sasikanth.rss.reader.app.AppInfo
import dev.sasikanth.rss.reader.core.network.post.PostSourceFetcher
import dev.sasikanth.rss.reader.di.scopes.AppScope
import dev.sasikanth.rss.reader.repository.RssRepository
import dev.sasikanth.rss.reader.repository.SettingsRepository
import me.tatarka.inject.annotations.Component
import me.tatarka.inject.annotations.Provides

@AppScope
@Component
abstract class ApplicationComponent(@get:Provides val context: Context) :
  SharedApplicationComponent() {

  abstract val rssRepository: RssRepository

  abstract val settingsRepository: SettingsRepository

  abstract val postSourceFetcher: PostSourceFetcher

  @Provides
  @AppScope
  fun providesAppInfo(context: Context): AppInfo {
    val packageManager = context.packageManager
    val packageInfo = packageManager.getPackageInfo(context.packageName, 0)
    val versionCode =
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
        (packageInfo.longVersionCode and 0xffffffffL).toInt()
      } else {
        @Suppress("DEPRECATION") packageInfo.versionCode
      }

    return AppInfo(
      versionName = packageInfo.versionName,
      versionCode = versionCode,
    )
  }

  companion object
}
