/*
 * Copyright 2025 Sasikanth Miriyampalli
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package dev.sasikanth.rss.reader.reader.ui

import twine.shared.generated.resources.Res

object ReaderHTML {

  internal suspend fun create(): String {
    val mercuryJS = readFile("mercury.web.js")
    val readabilityJS = readFile("readability.bundle.min.js")
    val turndownJS = readFile("turndown.js")
    val readerJS = readFile("main.js")

    // language=HTML
    @Suppress("HtmlRequiredLangAttribute", "HtmlRequiredTitleElement")
    return """
    <html dir='auto'>
    <head>
      <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0">
      <script>$turndownJS</script>
      <script>$readabilityJS</script>
      <script>$readerJS</script>
    </head>
    <body />
    </html>
        """
      .trimIndent()
  }

  private suspend fun readFile(fileName: String): String {
    return Res.readBytes("files/reader/$fileName").decodeToString()
  }
}
