/*
 * Copyright 2023 Sasikanth Miriyampalli
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
package dev.sasikanth.rss.reader.network

import com.mohamedrejeb.ksoup.html.parser.KsoupHtmlHandler

class HtmlContentParser(private val onEnd: (HtmlContent) -> Unit) : KsoupHtmlHandler {

  private val contentStringBuilder = StringBuilder()
  private val currentData: MutableMap<String, String> = mutableMapOf()
  private var currentTag: String? = null

  override fun onText(text: String) {
    when (currentTag) {
      "p" -> contentStringBuilder.append("\n" + text.cleanWhitespaces())
      "a",
      "span",
      "em" -> {
        contentStringBuilder.append(text.cleanWhitespaces())
      }
    }

    currentData["content"] = contentStringBuilder.toString()
  }

  override fun onOpenTag(name: String, attributes: Map<String, String>, isImplied: Boolean) {
    currentTag = name
    when {
      currentTag == "img" &&
        attributes.containsKey("src") &&
        !currentData.containsKey("imageUrl") -> {
        val imageUrl = attributes["src"].toString()
        if (
          !imageUrl.endsWith(".gif") &&
            (imageUrl.startsWith("https") || imageUrl.startsWith("http"))
        ) {
          currentData["imageUrl"] = imageUrl
        }
      }
    }
  }

  override fun onEnd() {
    onEnd(
      HtmlContent(
        imageUrl = currentData["imageUrl"],
        content = currentData["content"].orEmpty().trim()
      )
    )
    currentData.clear()
  }

  private fun String.cleanWhitespaces(): String {
    var formattedText = this.trim()
    if (formattedText.isNotBlank()) {
      if (this[0].isWhitespace()) {
        formattedText = " $formattedText"
      }
      if (this.last().isWhitespace()) {
        formattedText += " "
      }
    }
    return formattedText
  }
}

data class HtmlContent(val imageUrl: String?, val content: String)
