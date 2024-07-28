/*
 * Copyright 2024 Sasikanth Miriyampalli
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

import androidx.compose.ui.unit.LayoutDirection
import twine.shared.generated.resources.Res

object ReaderHTML {

  internal suspend fun create(
    title: String,
    feedName: String,
    feedHomePageLink: String,
    publishedAt: String,
    locale: String,
    direction: LayoutDirection
  ): String {
    val readabilityJS = readFile("readability.js")
    val readerJS = readFile("main.js")
    val readerStyles = readFile("styles.css")

    val postMetadata =
      postMetadata(
        feedName = feedName,
        feedHomePageLink = feedHomePageLink,
        publishedAt = publishedAt,
        hasTitle = title.isNotBlank()
      )

    // language=HTML
    return """
    <html lang="$locale" dir="${direction.name}">
    <head>
      <link rel="preconnect" href="https://fonts.googleapis.com">
      <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
      <link rel="preload" as='style' href="https://fonts.googleapis.com/css2?family=Golos+Text:wght@400;500&display=swap">
      <link rel="stylesheet" href="https://fonts.googleapis.com/css2?family=Golos+Text:wght@400;500&display=swap">
      <link rel="preload" as='style' href="https://fonts.googleapis.com/css2?family=Source+Code+Pro&display=swap">
      <link rel="stylesheet" href="https://fonts.googleapis.com/css2?family=Source+Code+Pro&display=swap">
      <style>$readerStyles</style>
      <script>$readabilityJS</script>
      <script>$readerJS</script>
      <title>$title</title>
    </head>
    <body>
      <h1 id='title'>$title</h1>
      <div>$postMetadata</div>
      <div id='content'></div>
    </body>
    </html>
        """
      .trimIndent()
  }

  private fun postMetadata(
    feedName: String,
    feedHomePageLink: String,
    publishedAt: String,
    hasTitle: Boolean,
  ): String {
    return buildString {
      if (hasTitle) {
        appendLine("<hr class=\"top-divider\">")
      }

      appendLine(
        """
      <div class ="feedName"><a href='$feedHomePageLink'>$feedName</a></div>
      <div class="caption">$publishedAt</div>
      <hr class="top-divider">
    """
          .trimIndent()
      )
    }
  }

  private suspend fun readFile(fileName: String): String {
    return Res.readBytes("files/reader/$fileName").decodeToString()
  }
}
