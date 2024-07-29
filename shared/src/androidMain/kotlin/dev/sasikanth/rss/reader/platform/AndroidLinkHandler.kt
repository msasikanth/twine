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

package dev.sasikanth.rss.reader.platform

import android.content.Intent
import android.net.Uri
import androidx.activity.ComponentActivity
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.net.toUri
import dev.sasikanth.rss.reader.data.repository.BrowserType
import dev.sasikanth.rss.reader.data.repository.SettingsRepository
import dev.sasikanth.rss.reader.di.scopes.ActivityScope
import kotlinx.coroutines.flow.first
import me.tatarka.inject.annotations.Inject

@Inject
@ActivityScope
class AndroidLinkHandler(
  private val activity: ComponentActivity,
  private val settingsRepository: SettingsRepository
) : LinkHandler {

  override suspend fun openLink(link: String?) {
    if (link.isNullOrBlank()) return

    val browserType = settingsRepository.browserType.first()
    when (browserType) {
      BrowserType.Default -> {
        openDefaultBrowserIfExists(link)
      }
      BrowserType.InApp -> {
        openCustomTab(link)
      }
    }
  }

  private fun openDefaultBrowserIfExists(link: String) {
    val packageManager = activity.packageManager
    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(link))
    if (intent.resolveActivity(packageManager) != null) {
      activity.startActivity(intent)
    } else {
      openCustomTab(link)
    }
  }

  private fun openCustomTab(url: String) {
    val intent = CustomTabsIntent.Builder().build()
    intent.launchUrl(activity, url.toUri())
  }
}
