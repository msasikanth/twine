/*
 * Copyright 2026 Sasikanth Miriyampalli
 *
 * Licensed under the GPL, Version 3.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.gnu.org/licenses/gpl-3.0.en.html
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package dev.sasikanth.rss.reader.reader.webview

import twine.shared.generated.resources.Res

object ReaderHTML {

  private var readerHTML: String? = null

  internal suspend fun createOrGet(): String {
    val readabilityJS = readFile("readability.js")
    val turndownJS = readFile("turndown.js")
    val readerJS = readFile("main.js")

    if (readerHTML.isNullOrBlank()) {
      // language=HTML
      @Suppress("HtmlRequiredLangAttribute", "HtmlRequiredTitleElement")
      readerHTML =
        """
        <html dir='auto'>
        <head>
          <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0">
          <script>$readabilityJS</script>
          <script>$turndownJS</script>
          <script>$readerJS</script>
        </head>
        <body />
        </html>
        """
          .trimIndent()
    }

    return readerHTML!!
  }

  private suspend fun readFile(fileName: String): String {
    return Res.readBytes("files/reader/$fileName").decodeToString()
  }
}
