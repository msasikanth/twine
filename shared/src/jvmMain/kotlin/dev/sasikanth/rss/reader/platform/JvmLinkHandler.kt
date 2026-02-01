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

package dev.sasikanth.rss.reader.platform

import dev.sasikanth.rss.reader.di.scopes.ActivityScope
import java.awt.Desktop
import java.net.URI
import me.tatarka.inject.annotations.Inject

@Inject
@ActivityScope
class JvmLinkHandler : LinkHandler {

  override suspend fun openLink(link: String?, useInAppBrowser: Boolean) {
    if (link.isNullOrBlank()) return
    if (Desktop.isDesktopSupported()) {
      Desktop.getDesktop().browse(URI(link))
    }
  }

  override suspend fun close() {
    // no-op
  }
}
