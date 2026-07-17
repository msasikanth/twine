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

import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import dev.sasikanth.rss.reader.di.ApplicationComponent
import dev.sasikanth.rss.reader.di.DesktopComponent
import dev.sasikanth.rss.reader.di.create
import dev.sasikanth.rss.reader.utils.DesktopWindowChrome
import dev.sasikanth.rss.reader.utils.ExternalUriHandler
import java.awt.Color
import java.awt.Desktop

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
    Window(
      state = rememberWindowState(size = DpSize(1200.dp, 800.dp)),
      onCloseRequest = ::exitApplication,
      title = "",
    ) {
      // macOS renders the transparent title bar with the window background; other
      // platforms ignore the client property.
      DisposableEffect(Unit) {
        window.rootPane.putClientProperty("apple.awt.transparentTitleBar", true)
        DesktopWindowChrome.listener = { argb -> window.background = Color(argb, true) }
        onDispose { DesktopWindowChrome.listener = null }
      }

      desktopComponent.app(
        { /* Handle theme change if needed */ },
        { /* No-op on desktop */ },
        { /* No-op on desktop */ },
      )
    }
  }
}
