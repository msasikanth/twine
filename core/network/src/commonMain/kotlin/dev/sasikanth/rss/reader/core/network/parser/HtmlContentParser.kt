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
package dev.sasikanth.rss.reader.core.network.parser

import com.mohamedrejeb.ksoup.html.parser.KsoupHtmlHandler

internal class HtmlContentParser(private val onEnd: (HtmlContent) -> Unit) : KsoupHtmlHandler {

  private val contentStringBuilder = StringBuilder()
  private var imageUrl: String? = null

  private var currentTagsStack: ArrayDeque<String> = ArrayDeque()

  private val allowedContentTags = setOf("p", "a", "span", "em", "u", "b", "i", "strong")

  override fun onText(text: String) {
    val tag = currentTagsStack.firstOrNull()
    if (tag in allowedContentTags || tag.isNullOrBlank()) {
      contentStringBuilder.append(text.cleanWhitespaces())
    }
  }

  override fun onOpenTag(name: String, attributes: Map<String, String>, isImplied: Boolean) {
    currentTagsStack.addFirst(name)

    if (name == "p" || name == "br") {
      contentStringBuilder.appendLine()
    }

    val srcAttr = attributes["src"].orEmpty()
    if (
      name == "img" && imageUrl.isNullOrBlank() && srcAttr.isNotBlank() && !srcAttr.endsWith(".gif")
    ) {
      this.imageUrl = srcAttr
    }
  }

  override fun onCloseTag(name: String, isImplied: Boolean) {
    currentTagsStack.removeFirst()
  }

  override fun onEnd() {
    onEnd(HtmlContent(imageUrl = imageUrl, content = contentStringBuilder.toString()))
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

  data class HtmlContent(val imageUrl: String?, val content: String)
}
