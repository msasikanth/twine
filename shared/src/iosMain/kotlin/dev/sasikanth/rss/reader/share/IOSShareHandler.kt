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

package dev.sasikanth.rss.reader.share

import dev.sasikanth.rss.reader.di.scopes.ActivityScope
import kotlinx.cinterop.ExperimentalForeignApi
import me.tatarka.inject.annotations.Inject
import platform.CoreGraphics.CGRectGetMaxY
import platform.CoreGraphics.CGRectGetMidX
import platform.CoreGraphics.CGRectMake
import platform.UIKit.UIActivityViewController
import platform.UIKit.UIViewController
import platform.UIKit.popoverPresentationController

@Inject
@ActivityScope
class IOSShareHandler(private val viewControllerProvider: () -> UIViewController) : ShareHandler {

  @OptIn(ExperimentalForeignApi::class)
  override fun share(content: String?) {
    if (content.isNullOrBlank()) return

    val viewController = viewControllerProvider()
    val items = listOf(content)
    val activityController = UIActivityViewController(items, null)
    activityController.popoverPresentationController?.setSourceView(viewController.view)
    activityController.popoverPresentationController?.setSourceRect(
      CGRectMake(
        x = CGRectGetMidX(viewController.view.bounds),
        y = CGRectGetMaxY(viewController.view.bounds),
        width = 0.0,
        height = 0.0
      )
    )
    viewController.presentViewController(activityController, true, null)
  }
}
