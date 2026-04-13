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
      // Standard DPI is 96 on most systems. Scale the density based on system DPI.
      val scaleFactor = systemDpi / 96f
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
