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

package dev.sasikanth.rss.reader.share

import dev.sasikanth.rss.reader.di.scopes.ActivityScope
import java.awt.Toolkit
import java.awt.datatransfer.StringSelection
import me.tatarka.inject.annotations.Inject

@Inject
@ActivityScope
class JvmShareHandler : ShareHandler {
  override fun share(content: String?): ShareResult {
    if (content.isNullOrBlank()) return ShareResult.Unavailable
    val selection = StringSelection(content)
    val clipboard = Toolkit.getDefaultToolkit().systemClipboard
    return runCatching {
        clipboard.setContents(selection, selection)
        ShareResult.CopiedToClipboard
      }
      .getOrDefault(ShareResult.Unavailable)
  }
}
