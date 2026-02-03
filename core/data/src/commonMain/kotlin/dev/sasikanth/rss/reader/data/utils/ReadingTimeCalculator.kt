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

package dev.sasikanth.rss.reader.data.utils

import com.fleeksoft.ksoup.Ksoup
import dev.sasikanth.rss.reader.util.DispatchersProvider
import kotlin.math.ceil
import kotlinx.coroutines.withContext
import me.tatarka.inject.annotations.Inject

@Inject
class ReadingTimeCalculator(private val dispatchersProvider: DispatchersProvider) {

  companion object {
    private const val WORDS_PER_MINUTE = 200

    // An average word is ~5 chars; 1000 chars is roughly 200 words (1 min)
    private const val FAST_PATH_THRESHOLD = 1000

    private const val MAX_HTML_SIZE = 1_000_000 // 1MB
  }

  /**
   * Estimates reading time. Uses Fast-Path for simple text and Ksoup for complex HTML.
   *
   * @return Int in minutes
   */
  suspend fun calculate(htmlContent: String?): Int =
    withContext(dispatchersProvider.default) {
      if (htmlContent.isNullOrBlank()) return@withContext 0

      if (htmlContent.length < FAST_PATH_THRESHOLD && !htmlContent.contains("<")) {
        return@withContext calculateFromPlainText(htmlContent)
      }

      if (htmlContent.length > MAX_HTML_SIZE) {
        val fallbackText = htmlContent.take(MAX_HTML_SIZE).replace(Regex("<[^>]*>"), " ")
        return@withContext calculateFromPlainText(fallbackText)
      }

      try {
        val document = Ksoup.parseBodyFragment(htmlContent)
        val plainText = document.text()

        return@withContext calculateFromPlainText(plainText)
      } catch (e: Exception) {
        val fallbackText = htmlContent.replace(Regex("<[^>]*>"), " ")
        return@withContext calculateFromPlainText(fallbackText)
      }
    }

  private fun calculateFromPlainText(text: String): Int {
    val wordCount = text.trim().split(Regex("\\s+")).count { it.isNotEmpty() }

    return if (wordCount > 0) {
      ceil(wordCount.toDouble() / WORDS_PER_MINUTE).toInt()
    } else {
      0
    }
  }
}
