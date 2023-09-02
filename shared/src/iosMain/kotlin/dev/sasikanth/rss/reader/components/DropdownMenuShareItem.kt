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
package dev.sasikanth.rss.reader.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.twotone.Share
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.interop.LocalUIViewController
import dev.sasikanth.rss.reader.resources.strings.LocalStrings
import platform.UIKit.UIActivityViewController

@Composable
actual fun DropdownMenuShareItem(
  contentToShare: String,
  modifier: Modifier,
  onShareMenuOpened: () -> Unit
) {
  val viewController = LocalUIViewController.current

  DropdownMenuItem(
    modifier = modifier,
    text = { Text(text = LocalStrings.current.share) },
    leadingIcon = { Icon(Icons.TwoTone.Share, null) },
    onClick = {
      val items = listOf(contentToShare)
      val activityController = UIActivityViewController(items, null)
      viewController.presentViewController(activityController, true, null)
      onShareMenuOpened()
    }
  )
}
