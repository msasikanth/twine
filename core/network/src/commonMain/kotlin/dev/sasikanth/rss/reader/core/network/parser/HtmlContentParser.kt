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

import co.touchlab.crashkios.bugsnag.BugsnagKotlin
import com.fleeksoft.ksoup.Ksoup
import io.ktor.utils.io.charsets.MalformedInputException

internal object HtmlContentParser {

  private val allowedContentTags = setOf("p", "span", "em", "u", "b", "i", "strong")

  fun parse(htmlContent: String): HtmlContent? {
    if (htmlContent.isBlank()) return null

    return try {
      val document = Ksoup.parse(htmlContent)

      val imageUrl =
        document
          .getElementsByTag("img")
          .firstOrNull { it.hasAttr("src") && !it.attr("src").endsWith(".gif") }
          ?.attr("src")

      val contentStringBuilder = StringBuilder()
      document.getAllElements().forEach { element ->
        if (allowedContentTags.contains(element.tagName())) {
          contentStringBuilder.append(element.text().cleanWhitespaces())
        }

        if (element.tagName() == "p" || element.tagName() == "br") {
          contentStringBuilder.appendLine()
        }
      }

      HtmlContent(imageUrl = imageUrl, content = contentStringBuilder.toString())
    } catch (e: Exception) {
      null
    } catch (e: MalformedInputException) {
      BugsnagKotlin.sendHandledException(e)
      null
    }
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
