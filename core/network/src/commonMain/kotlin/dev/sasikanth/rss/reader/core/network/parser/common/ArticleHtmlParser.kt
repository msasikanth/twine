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
package dev.sasikanth.rss.reader.core.network.parser.common

import com.fleeksoft.ksoup.Ksoup
import com.fleeksoft.ksoup.safety.Cleaner
import com.fleeksoft.ksoup.safety.Safelist
import dev.sasikanth.rss.reader.logging.CrashReporter
import io.ktor.utils.io.charsets.MalformedInputException
import me.tatarka.inject.annotations.Inject

@Inject
class ArticleHtmlParser {

  companion object {
    private const val TAG_BODY = "body"
    private const val TAG_IMG = "img"
    private const val TAG_FIGCAPTION = "figcaption"
    private const val ATTR_SRC = "src"
  }

  private val allowedContentTags by lazy {
    Safelist().addTags(TAG_FIGCAPTION, TAG_IMG).addAttributes(TAG_IMG, ATTR_SRC)
  }
  private val gifRegex by lazy { Regex("/\\.gif(\\?.*)?\\$/i") }

  fun parse(htmlContent: String): Result? {
    if (htmlContent.isBlank()) return null

    return try {
      val originalHtmlDocument = Ksoup.parse(htmlContent)
      val cleanedHtmlDocument = Cleaner(allowedContentTags).clean(originalHtmlDocument)
      val body = cleanedHtmlDocument.body().first()

      originalHtmlDocument.head().remove()

      val heroImage =
        body.firstNotNullOfOrNull {
          val imageUrl = it.attr(ATTR_SRC)
          if (it.tagName() == TAG_IMG && !gifRegex.containsMatchIn(imageUrl)) {
            imageUrl.removeSurrounding("\"")
          } else {
            null
          }
        }

      Result(
        heroImage = heroImage,
        textContent = body.ownText(),
        cleanedHtml = originalHtmlDocument.html(),
      )
    } catch (e: Exception) {
      null
    } catch (e: MalformedInputException) {
      CrashReporter.log(e)
      null
    }
  }

  data class Result(val heroImage: String?, val textContent: String, val cleanedHtml: String)
}
