/*
 * Copyright 2026 Sasikanth Miriyampalli
 *
 * Licensed under the GPL, Version 3.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.gnu.org/licenses/gpl-3.0.en.html
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
      { Logger.e { "Failed to set alternate icon: ${it?.description}" } }
    )
  }
}
