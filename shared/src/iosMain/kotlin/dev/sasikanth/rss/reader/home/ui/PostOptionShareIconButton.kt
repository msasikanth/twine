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
package dev.sasikanth.rss.reader.home.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.interop.LocalUIViewController
import dev.sasikanth.rss.reader.resources.icons.Share
import dev.sasikanth.rss.reader.resources.icons.TwineIcons
import dev.sasikanth.rss.reader.resources.strings.LocalStrings
import platform.UIKit.UIActivityViewController

@Composable
internal actual fun PostOptionShareIconButton(postLink: String) {
  val viewController = LocalUIViewController.current
  PostOptionIconButton(
    icon = TwineIcons.Share,
    contentDescription = LocalStrings.current.share,
    onClick = {
      val items = listOf(postLink)
      val activityController = UIActivityViewController(items, null)
      viewController.presentViewController(activityController, true, null)
    }
  )
}
