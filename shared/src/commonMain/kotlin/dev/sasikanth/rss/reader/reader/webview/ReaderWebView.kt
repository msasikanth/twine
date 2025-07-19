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

package dev.sasikanth.rss.reader.reader.webview

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/** Need this to parse the HTML content of the post using a custom reader parser in JS */
@Composable
internal expect fun ReaderWebView(
  link: String?,
  content: String?,
  postImage: String?,
  fetchFullArticle: Boolean,
  contentLoaded: (String) -> Unit,
  modifier: Modifier = Modifier.Companion,
)
