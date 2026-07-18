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

import dev.sasikanth.rss.reader.utils.asJSString
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.htmlunit.BrowserVersion
import org.htmlunit.WebClient
import org.htmlunit.html.HtmlPage

/**
 * Exercises `dedupeAdjacentHeadings` from main.es5.js through the same HtmlUnit/Rhino engine the
 * desktop app uses, so the test runs the real bundled JS rather than a re-implementation of its
 * logic.
 */
class DedupeAdjacentHeadingsTest {

  @Test
  fun dedupeAdjacentHeadings_withImmediatelyAdjacentDuplicateHeading_removesDuplicate() = runTest {
    val result =
      runDedupe(
        """
        <h2>The Drop</h2>
        <h2>The Drop</h2>
        <p>Some article body text.</p>
        """
          .trimIndent()
      )

    assertEquals(listOf("The Drop"), result.headingTexts)
  }

  @Test
  fun dedupeAdjacentHeadings_withThreeConsecutiveDuplicates_collapsesToOne() = runTest {
    val result =
      runDedupe(
        """
        <h2>The Drop</h2>
        <h2>The Drop</h2>
        <h2>The Drop</h2>
        <p>Some article body text.</p>
        """
          .trimIndent()
      )

    assertEquals(listOf("The Drop"), result.headingTexts)
  }

  @Test
  fun dedupeAdjacentHeadings_withOnlyWhitespaceBetweenDuplicates_removesDuplicate() = runTest {
    val result =
      runDedupe(
        """
        <h2>The Drop</h2>

        <h2>The Drop</h2>
        <p>Some article body text.</p>
        """
          .trimIndent()
      )

    assertEquals(listOf("The Drop"), result.headingTexts)
  }

  @Test
  fun dedupeAdjacentHeadings_withEmptyAnchorBetweenDuplicates_removesDuplicate() = runTest {
    val result =
      runDedupe(
        """
        <h2>The Drop</h2>
        <a name="the-drop"></a>
        <h2>The Drop</h2>
        <p>Some article body text.</p>
        """
          .trimIndent()
      )

    assertEquals(listOf("The Drop"), result.headingTexts)
  }

  @Test
  fun dedupeAdjacentHeadings_withCaseAndWhitespaceVariant_stillDeduped() = runTest {
    val result =
      runDedupe(
        """
        <h2>  The Drop </h2>
        <h2>the drop</h2>
        <p>Some article body text.</p>
        """
          .trimIndent()
      )

    assertEquals(1, result.headingTexts.size)
  }

  @Test
  fun dedupeAdjacentHeadings_withParagraphBetweenDuplicates_keepsBothHeadings() = runTest {
    val result =
      runDedupe(
        """
        <h2>The Drop</h2>
        <p>First section's content.</p>
        <h2>The Drop</h2>
        <p>Second section reusing the same heading title.</p>
        """
          .trimIndent()
      )

    assertEquals(listOf("The Drop", "The Drop"), result.headingTexts)
  }

  @Test
  fun dedupeAdjacentHeadings_withImageBetweenDuplicates_keepsBothHeadings() = runTest {
    val result =
      runDedupe(
        """
        <h2>The Drop</h2>
        <img src="https://example.com/photo.jpg" alt="photo">
        <h2>The Drop</h2>
        <p>Some article body text.</p>
        """
          .trimIndent()
      )

    assertEquals(listOf("The Drop", "The Drop"), result.headingTexts)
  }

  @Test
  fun dedupeAdjacentHeadings_withDifferentText_keepsBothHeadings() = runTest {
    val result =
      runDedupe(
        """
        <h2>The Drop</h2>
        <h2>Bear 2.9</h2>
        <p>Some article body text.</p>
        """
          .trimIndent()
      )

    assertEquals(listOf("The Drop", "Bear 2.9"), result.headingTexts)
  }

  @Test
  fun dedupeAdjacentHeadings_withDuplicateUnderDifferentParents_keepsBothHeadings() = runTest {
    val result =
      runDedupe(
        """
        <div><h2>The Drop</h2></div>
        <div><h2>The Drop</h2></div>
        <p>Some article body text.</p>
        """
          .trimIndent()
      )

    assertEquals(listOf("The Drop", "The Drop"), result.headingTexts)
  }

  @Test
  fun dedupeAdjacentHeadings_withNoHeadings_doesNothing() = runTest {
    val result = runDedupe("<p>No headings here.</p>")

    assertEquals(emptyList(), result.headingTexts)
  }

  private suspend fun runDedupe(html: String): DedupeResult {
    WebClient(BrowserVersion.CHROME).use { webClient ->
      webClient.options.isCssEnabled = false
      webClient.options.isDownloadImages = false
      webClient.options.isGeolocationEnabled = false
      webClient.options.isThrowExceptionOnScriptError = true
      webClient.options.isThrowExceptionOnFailingStatusCode = false

      val htmlShell = ReaderHTML.createOrGet()
      val page: HtmlPage = webClient.loadHtmlCodeIntoCurrentWindow(htmlShell)

      val script =
        """
        var doc = new DOMParser().parseFromString(${html.asJSString}, "text/html");
        dedupeAdjacentHeadings(doc);
        var headings = Array.prototype.slice.call(
          doc.querySelectorAll("h1, h2, h3, h4, h5, h6")
        ).map(function(h) { return h.textContent; });
        JSON.stringify({ headingTexts: headings });
        """
          .trimIndent()

      val result = page.executeJavaScript(script).javaScriptResult.toString()
      return Json.decodeFromString<DedupeResult>(result)
    }
  }
}

@Serializable private data class DedupeResult(val headingTexts: List<String>)
