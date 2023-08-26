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

import androidx.compose.ui.window.ComposeUIViewController
import dev.sasikanth.rss.reader.app.App
import me.tatarka.inject.annotations.Inject
import platform.Foundation.NSURL
import platform.UIKit.UIApplication
import platform.UIKit.UIViewController

typealias HomeViewController = () -> UIViewController

@Inject
fun HomeViewController(app: App) = ComposeUIViewController {
  app { link -> UIApplication.sharedApplication().openURL(NSURL(string = link)) }
}
