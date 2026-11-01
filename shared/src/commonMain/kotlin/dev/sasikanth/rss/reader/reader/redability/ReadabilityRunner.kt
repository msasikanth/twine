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

package dev.sasikanth.rss.reader.reader.redability

import com.fleeksoft.ksoup.Ksoup
import dev.sasikanth.rss.reader.core.model.local.ReadabilityResult

interface ReadabilityRunner {

  suspend fun parseHtml(link: String?, content: String, image: String? = null): ReadabilityResult
}

internal fun fallbackReadabilityResult(content: String?): ReadabilityResult {
  if (content.isNullOrBlank()) return ReadabilityResult(content = content)

  val cleanedContent =
    runCatching {
        val document = Ksoup.parse(content)
        document.head().remove()
        document.select("script, style, noscript").remove()
        document.select("p, div, h1, h2, h3, h4, h5, h6, li, blockquote, tr").prepend("\\n\\n")
        document
          .wholeText()
          .replace("\\n", "\n")
          .lines()
          .joinToString("\n") { it.trim() }
          .replace(Regex("\n{3,}"), "\n\n")
          .trim()
      }
      .getOrDefault(content)

  return ReadabilityResult(content = cleanedContent.ifBlank { content })
}
