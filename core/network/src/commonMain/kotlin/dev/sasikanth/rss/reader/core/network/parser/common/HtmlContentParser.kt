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
package dev.sasikanth.rss.reader.core.network.parser.common

import co.touchlab.crashkios.bugsnag.BugsnagKotlin
import com.fleeksoft.ksoup.Ksoup
import com.fleeksoft.ksoup.safety.Safelist
import io.ktor.utils.io.charsets.MalformedInputException
import me.tatarka.inject.annotations.Inject

@Inject
class HtmlContentParser {

  companion object {
    private const val TAG_BODY = "body"
    private const val TAG_IMG = "img"
    private const val TAG_FIGCAPTION = "figcaption"
    private const val ATTR_SRC = "src"
  }

  private val allowedContentTags by lazy {
    Safelist().addTags(TAG_FIGCAPTION, TAG_IMG).addAttributes(TAG_IMG, ATTR_SRC)
  }
  private val gifRegex by lazy {
    Regex("/\\.gif(\\?.*)?\\$/i")
  }

  fun parse(htmlContent: String): Result? {
    if (htmlContent.isBlank()) return null

    return try {
      val cleanedHtml = Ksoup.clean(htmlContent, allowedContentTags)
      val document = Ksoup.parse(cleanedHtml)
      val body = document.getElementsByTag(TAG_BODY).first() ?: return null

      val leadImage =
        body.firstNotNullOfOrNull {
          val imageUrl = it.attr(ATTR_SRC)
          if (it.tagName() == TAG_IMG && !gifRegex.containsMatchIn(imageUrl)) {
            imageUrl.removeSurrounding("\"")
          } else {
            null
          }
        }

      Result(leadImage = leadImage, content = body.ownText())
    } catch (e: Exception) {
      null
    } catch (e: MalformedInputException) {
      BugsnagKotlin.sendHandledException(e)
      null
    }
  }

  data class Result(val leadImage: String?, val content: String)
}
