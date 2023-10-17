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
package dev.sasikanth.rss.reader

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.interop.LocalUIViewController
import androidx.compose.ui.uikit.OnFocusBehavior
import androidx.compose.ui.window.ComposeUIViewController
import com.arkivanov.decompose.ExperimentalDecomposeApi
import com.arkivanov.decompose.extensions.compose.jetbrains.PredictiveBackGestureOverlay
import com.arkivanov.essenty.backhandler.BackDispatcher
import dev.sasikanth.rss.reader.app.App
import dev.sasikanth.rss.reader.repository.BrowserType
import dev.sasikanth.rss.reader.repository.BrowserType.*
import me.tatarka.inject.annotations.Assisted
import me.tatarka.inject.annotations.Inject
import platform.Foundation.NSURL
import platform.SafariServices.SFSafariViewController
import platform.UIKit.UIApplication
import platform.UIKit.UIModalPresentationPageSheet
import platform.UIKit.UIViewController

typealias HomeViewController = (backDispatcher: BackDispatcher) -> UIViewController

@OptIn(ExperimentalDecomposeApi::class)
@Inject
fun HomeViewController(app: App, @Assisted backDispatcher: BackDispatcher) =
  ComposeUIViewController(configure = { onFocusBehavior = OnFocusBehavior.DoNothing }) {
    val uiViewController = LocalUIViewController.current

    PredictiveBackGestureOverlay(
      backDispatcher = backDispatcher,
      backIcon = null,
      modifier = Modifier.fillMaxSize()
    ) {
      app(
        { link, browserType -> openLink(link, browserType, uiViewController) },
        { reportIssueLink -> openLink(reportIssueLink, Default, uiViewController) }
      )
    }
  }

private fun openLink(link: String, browserType: BrowserType, uiViewController: UIViewController) {
  when (browserType) {
    Default -> {
      UIApplication.sharedApplication().openURL(NSURL(string = link))
    }
    InApp -> {
      val safari = SFSafariViewController(NSURL(string = link))
      safari.modalPresentationStyle = UIModalPresentationPageSheet

      uiViewController.presentViewController(safari, animated = true, completion = null)
    }
  }
}
