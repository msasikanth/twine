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
import com.fleeksoft.ksoup.nodes.Element
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
    private const val ATTR_WIDTH = "width"
    private const val ATTR_HEIGHT = "height"
    private const val TAG_AUDIO = "audio"
    private const val TAG_SOURCE = "source"
    private const val ATTR_TYPE = "type"

    private const val MAX_CONTENT_SIZE = 5 * 1024 * 1024 // 5MB
    private const val MIN_HERO_IMAGE_DIMENSION = 16

    private val gifRegex = Regex("\\.gif(\\?.*)?$", RegexOption.IGNORE_CASE)
    private val trackingFilenameRegex = Regex("(?:^|[._-])1x1(?:[._-]|$)", RegexOption.IGNORE_CASE)
  }

  private val allowedContentTags by lazy {
    Safelist()
      .addTags(TAG_FIGCAPTION, TAG_IMG, TAG_AUDIO, TAG_SOURCE)
      .addAttributes(TAG_IMG, ATTR_SRC)
      .addAttributes(TAG_AUDIO, ATTR_SRC)
      .addAttributes(TAG_SOURCE, ATTR_SRC, ATTR_TYPE)
  }

  fun parse(htmlContent: String): Result? {
    if (htmlContent.isBlank() || htmlContent.length > MAX_CONTENT_SIZE) return null

    return try {
      val originalHtmlDocument =
        Ksoup.parse(htmlContent).also {
          it.head().remove()
          it.select("script, style, noscript").remove()
        }
      val cleanedHtmlDocument = Cleaner(allowedContentTags).clean(originalHtmlDocument)
      val body = cleanedHtmlDocument.body().first()
      var firstGifImage: String? = null
      val heroImage =
        originalHtmlDocument.body().select(TAG_IMG).firstNotNullOfOrNull { element ->
          val imageUrl =
            element.attr(ATTR_SRC).trim().removeSurrounding("\"").ifBlank {
              return@firstNotNullOfOrNull null
            }
          if (isTrackingOrTinyImage(element, imageUrl)) return@firstNotNullOfOrNull null

          if (gifRegex.containsMatchIn(imageUrl)) {
            if (firstGifImage == null) {
              firstGifImage = imageUrl
            }
            null
          } else {
            imageUrl
          }
        } ?: firstGifImage

      val audioUrl =
        body.select(TAG_AUDIO).firstOrNull()?.let { audio ->
          val src = audio.attr(ATTR_SRC)
          src.ifBlank { audio.select(TAG_SOURCE).attr(ATTR_SRC) }
        }

      Result(
        heroImage = heroImage,
        textContent = body.ownText(),
        cleanedHtml = originalHtmlDocument.html(),
        audioUrl = audioUrl,
      )
    } catch (e: Exception) {
      null
    } catch (e: MalformedInputException) {
      CrashReporter.log(e)
      null
    }
  }

  private fun isTrackingOrTinyImage(element: Element, imageUrl: String): Boolean {
    if (imageUrl.startsWith("data:", ignoreCase = true)) return true

    val width = element.attr(ATTR_WIDTH).trim().removeSuffix("px").trim().toIntOrNull()
    if (width != null && width <= MIN_HERO_IMAGE_DIMENSION) return true

    val height = element.attr(ATTR_HEIGHT).trim().removeSuffix("px").trim().toIntOrNull()
    if (height != null && height <= MIN_HERO_IMAGE_DIMENSION) return true

    val path = imageUrl.substringBefore('?').substringBefore('#')
    val filename = path.substringAfterLast('/')
    return filename.equals("pixel.gif", ignoreCase = true) ||
      path.contains("/1x1/") ||
      trackingFilenameRegex.containsMatchIn(filename)
  }

  data class Result(
    val heroImage: String?,
    val textContent: String,
    val cleanedHtml: String,
    val audioUrl: String? = null,
  )
}
