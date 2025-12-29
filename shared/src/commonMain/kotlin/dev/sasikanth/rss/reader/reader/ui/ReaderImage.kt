/*
 * Copyright 2025 Sasikanth Miriyampalli
 *
 * Licensed under the GPL, Version 3.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.gnu.org/licenses/gpl-3.0.en.html
 *
 */

package dev.sasikanth.rss.reader.reader.ui

import androidx.compose.runtime.Composable
import com.mikepenz.markdown.compose.components.MarkdownComponentModel
import com.mikepenz.markdown.compose.elements.MarkdownImage
import dev.sasikanth.rss.reader.utils.LocalBlockImage

@Composable
fun ReaderImage(markdownComponentModel: MarkdownComponentModel) {
  val shouldBlockImage = LocalBlockImage.current
  if (shouldBlockImage) {
    // no-op
  } else {
    MarkdownImage(markdownComponentModel.content, markdownComponentModel.node)
  }
}
