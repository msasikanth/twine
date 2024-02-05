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

import dev.sasikanth.rss.reader.di.scopes.ActivityScope
import dev.sasikanth.rss.reader.repository.BrowserType
import dev.sasikanth.rss.reader.repository.SettingsRepository
import kotlinx.coroutines.flow.first
import me.tatarka.inject.annotations.Inject
import platform.Foundation.NSURL
import platform.SafariServices.SFSafariViewController
import platform.UIKit.UIApplication
import platform.UIKit.UIModalPresentationPageSheet
import platform.UIKit.UIViewController
import platform.darwin.dispatch_async
import platform.darwin.dispatch_get_main_queue

@Inject
@ActivityScope
class IOSLinkHandler(
  private val uiViewControllerProvider: () -> UIViewController,
  private val settingsRepository: SettingsRepository,
) : LinkHandler {

  override suspend fun openLink(link: String) {
    val browserType = settingsRepository.browserType.first()
    val url = NSURL(string = link)

    when (browserType) {
      BrowserType.Default -> {
        val canOpenUrl = UIApplication.sharedApplication().canOpenURL(url)
        if (canOpenUrl) {
          openBrowser(url)
        } else {
          inAppBrowser(url)
        }
      }
      BrowserType.InApp -> {
        inAppBrowser(url)
      }
    }
  }

  private fun openBrowser(url: NSURL) {
    dispatch_async(dispatch_get_main_queue()) { UIApplication.sharedApplication().openURL(url) }
  }

  private fun inAppBrowser(url: NSURL) {
    try {
      val safari = SFSafariViewController(url)
      safari.modalPresentationStyle = UIModalPresentationPageSheet

      uiViewControllerProvider().presentViewController(safari, animated = true, completion = null)
    } catch (e: Exception) {
      openBrowser(url)
    }
  }
}
