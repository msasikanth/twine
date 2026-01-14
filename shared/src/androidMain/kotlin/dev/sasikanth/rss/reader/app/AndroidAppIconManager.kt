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

import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager
import me.tatarka.inject.annotations.Inject

@Inject
class AndroidAppIconManager(private val context: Context) : AppIconManager {

  override fun setIcon(icon: AppIcon) {
    val packageManager = context.packageManager
    val packageName = context.packageName

    AppIcon.entries.forEach { appIcon ->
      val componentName =
        ComponentName(packageName, "dev.sasikanth.rss.reader.MainActivity${appIcon.name}")
      val newState =
        if (appIcon == icon) {
          PackageManager.COMPONENT_ENABLED_STATE_ENABLED
        } else {
          PackageManager.COMPONENT_ENABLED_STATE_DISABLED
        }

      packageManager.setComponentEnabledSetting(
        componentName,
        newState,
        PackageManager.DONT_KILL_APP
      )
    }
  }
}
