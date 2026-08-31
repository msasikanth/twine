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
import java.awt.Image
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO

fun main() {
  useBundledJnaLibrary()

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
        window.iconImages = AppIcon.images
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

private fun useBundledJnaLibrary() {
  val resourcesDir = System.getProperty("compose.application.resources.dir") ?: return
  if (!File(resourcesDir, "libjnidispatch.dylib").exists()) return

  System.setProperty("jna.boot.library.path", resourcesDir)
  System.setProperty("jna.nounpack", "true")
}

private object AppIcon {

  // Windows requests separate icons for the title bar, taskbar and alt-tab switcher.
  // Supplying each size lets AWT pick an exact match instead of downscaling one large image.
  val images: List<BufferedImage> by lazy {
    val source = ImageIO.read(AppIcon::class.java.getResourceAsStream("/icon.png"))
    listOf(16, 24, 32, 48, 64, 128, 256).map { size ->
      BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB).apply {
        createGraphics().run {
          drawImage(source.getScaledInstance(size, size, Image.SCALE_SMOOTH), 0, 0, null)
          dispose()
        }
      }
    }
  }
}
