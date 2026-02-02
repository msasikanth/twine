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

package dev.sasikanth.rss.reader.app

import co.touchlab.kermit.Logger
import me.tatarka.inject.annotations.Inject
import platform.UIKit.UIApplication
import platform.UIKit.setAlternateIconName
import platform.UIKit.supportsAlternateIcons

@Inject
class IosAppIconManager : AppIconManager {

  override fun setIcon(icon: AppIcon) {
    val application = UIApplication.sharedApplication
    if (!application.supportsAlternateIcons) return

    val iconName =
      if (icon == AppIcon.DarkJade) {
        null
      } else {
        "AppIcon${icon.name}"
      }

    application.setAlternateIconName(
      iconName,
      { Logger.e { "Failed to set alternate icon: ${it?.description}" } },
    )
  }
}
