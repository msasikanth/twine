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
package dev.sasikanth.rss.reader

import androidx.compose.ui.uikit.OnFocusBehavior
import androidx.compose.ui.window.ComposeUIViewController
import dev.sasikanth.rss.reader.app.App
import me.tatarka.inject.annotations.Inject
import platform.UIKit.UIViewController

@Inject
class HomeViewController(private val app: App) {

  @Suppress("unused")
  fun viewController(): UIViewController {
    return ComposeUIViewController(configure = { onFocusBehavior = OnFocusBehavior.DoNothing }) {
      app(
        {
          // no-op
        },
        {
          // no-op
        },
        {
          // no-op
        },
      )
    }
  }
}
