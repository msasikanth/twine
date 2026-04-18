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

package dev.sasikanth.rss.reader

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import dev.sasikanth.rss.reader.di.ApplicationComponent
import dev.sasikanth.rss.reader.di.DesktopComponent
import dev.sasikanth.rss.reader.di.create
import dev.sasikanth.rss.reader.utils.ExternalUriHandler
import java.awt.Desktop
import java.awt.Toolkit

fun main() {
  val applicationComponent = ApplicationComponent::class.create()
  val desktopComponent = DesktopComponent::class.create(applicationComponent)

  applicationComponent.initializers.forEach { it.initialize() }

  try {
    Desktop.getDesktop().setOpenURIHandler { event ->
      ExternalUriHandler.onNewUri(event.uri.toString())
    }
  } catch (e: UnsupportedOperationException) {
    println("setOpenURIHandler is unsupported")
  }

  application {
    Window(onCloseRequest = ::exitApplication, title = "") {
      val systemDpi = Toolkit.getDefaultToolkit().screenResolution
      val screenSize = Toolkit.getDefaultToolkit().screenSize

      val multiplier =
        when {
          systemDpi > 96 && screenSize.width >= 1728 -> 1.45f // High-density large screens
          systemDpi > 96 && screenSize.width >= 1512 -> 1.3f // High-density smaller screens
          systemDpi > 96 -> 1.25f // Other HiDPI screens
          screenSize.width >= 1920 -> 1.2f // Large standard-density screens (e.g. 1080p/1440p)
          else -> 1.15f // Smaller standard-density screens (e.g. 720p)
        }
      val scaleFactor = (systemDpi / 96f) * multiplier
      val density = Density(density = scaleFactor, fontScale = 1f)

      CompositionLocalProvider(LocalDensity provides density) {
        desktopComponent.app(
          { /* Handle theme change if needed */ },
          { /* No-op on desktop */ },
          { /* No-op on desktop */ },
        )
      }
    }
  }
}
