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

import dev.sasikanth.rss.reader.core.model.local.ReadabilityResult
import dev.sasikanth.rss.reader.utils.asJSString
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.htmlunit.BrowserVersion
import org.htmlunit.WebClient
import org.htmlunit.corejs.javascript.Undefined
import org.htmlunit.html.HtmlPage

/**
 * Exercises `stripAriaHidden` from main.es5.js through the same HtmlUnit/Rhino engine the desktop
 * app uses. The unit-level cases call the function directly; the pipeline-level cases run the full
 * `parseReaderContent` (including the real Readability library) to prove the actual reported bug -
 * a visible aria-hidden="true" image being silently dropped - is fixed, and that genuinely hidden
 * content is still excluded.
 */
class StripAriaHiddenTest {

  private val json = Json { ignoreUnknownKeys = true }

  @Test
  fun stripAriaHidden_withHiddenElement_removesAttribute() = runTest {
    val result = runStripAriaHidden("<div aria-hidden=\"true\"><p>Hello</p></div>")

    assertEquals(1, result.beforeCount)
    assertEquals(0, result.afterCount)
  }

  @Test
  fun stripAriaHidden_withNestedHiddenElements_removesAllAttributes() = runTest {
    val result =
      runStripAriaHidden(
        """
        <div aria-hidden="true">
          <a aria-hidden="true" href="#"><img src="a.jpg"></a>
        </div>
        <span aria-hidden="true">icon</span>
        """
          .trimIndent()
      )

    assertEquals(3, result.beforeCount)
    assertEquals(0, result.afterCount)
  }

  @Test
  fun stripAriaHidden_leavesOtherAttributesUntouched() = runTest {
    val result =
      runStripAriaHidden(
        "<a aria-hidden=\"true\" href=\"https://example.com\" tabindex=\"-1\">link</a>"
      )

    assertEquals(0, result.afterCount)
    assertEquals(listOf("https://example.com"), result.hrefs)
  }

  @Test
  fun stripAriaHidden_withNoHiddenElements_isNoop() = runTest {
    val result = runStripAriaHidden("<p>Nothing hidden here.</p>")

    assertEquals(0, result.beforeCount)
    assertEquals(0, result.afterCount)
  }

  @Test
  fun parseReaderContent_withAriaHiddenImageLink_keepsImageInOutput() = runTest {
    val html =
      """
      <div>
        <p>Choosing the best walking pad for a home office setup takes real research, and
        we spent six weeks testing more than a dozen models before settling on our top
        picks for quiet operation, foldability, and price.</p>
        <h2>Best Overall</h2>
        <p>The Urevo CyberPad impressed us with how quiet it ran even at higher speeds, and
        its low profile means it slides easily under a standing desk when it's not in use.</p>
        <figure>
          <a aria-hidden="true" tabindex="-1" href="https://example.com/buy/cyberpad">
            <img src="https://example.com/photos/cyberpad.jpg" alt="Urevo CyberPad treadmill">
          </a>
        </figure>
        <p><a href="https://example.com/buy/cyberpad">Jump to review</a></p>
        <p>We also liked the companion app, which tracks steps and distance without needing
        a separate fitness tracker strapped to your wrist during the day.</p>
      </div>
      """
        .trimIndent()

    val result = runFullPipeline(html)

    assertTrue(
      result.content.orEmpty().contains("example.com/photos/cyberpad.jpg"),
      "expected the aria-hidden image to survive extraction, got: ${result.content}",
    )
  }

  @Test
  fun parseReaderContent_withGenuinelyHiddenElement_excludesItFromOutput() = runTest {
    val html =
      """
      <div>
        <p>Choosing the best walking pad for a home office setup takes real research, and
        we spent six weeks testing more than a dozen models before settling on our top
        picks for quiet operation, foldability, and price.</p>
        <div style="display:none">
          <img src="https://example.com/photos/should-not-appear.jpg" alt="Hidden decorative image">
        </div>
        <p>The Urevo CyberPad impressed us with how quiet it ran even at higher speeds, and
        its low profile means it slides easily under a standing desk when it's not in use.</p>
      </div>
      """
        .trimIndent()

    val result = runFullPipeline(html)

    assertFalse(
      result.content.orEmpty().contains("should-not-appear.jpg"),
      "expected genuinely hidden content to stay excluded, got: ${result.content}",
    )
  }

  private suspend fun runStripAriaHidden(html: String): AriaHiddenProbe {
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
        var beforeCount = doc.querySelectorAll("[aria-hidden]").length;
        stripAriaHidden(doc);
        var afterCount = doc.querySelectorAll("[aria-hidden]").length;
        var hrefs = Array.prototype.slice.call(doc.querySelectorAll("a[href]")).map(
          function(el) { return el.getAttribute("href"); }
        );
        JSON.stringify({ beforeCount: beforeCount, afterCount: afterCount, hrefs: hrefs });
        """
          .trimIndent()

      val result = page.executeJavaScript(script).javaScriptResult.toString()
      return json.decodeFromString<AriaHiddenProbe>(result)
    }
  }

  private suspend fun runFullPipeline(html: String): ReadabilityResult {
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
        var parsingResult = null;
        var parsingError = null;
        try {
          parseReaderContent(null, null, ${html.asJSString}).then(function(res) {
            parsingResult = JSON.stringify(res);
          }).catch(function(err) {
            parsingError = "Promise Error: " + err.toString();
          });
        } catch (e) {
          parsingError = "Sync Error: " + e.toString();
        }
        """
          .trimIndent()

      page.executeJavaScript(script)

      var result: String? = null
      for (i in 0 until 100) {
        webClient.waitForBackgroundJavaScript(100)

        val errorObj = page.executeJavaScript("parsingError").javaScriptResult
        if (errorObj != null && errorObj != Undefined.instance) {
          throw RuntimeException("JS parsing failed: $errorObj")
        }

        val resultObj = page.executeJavaScript("parsingResult").javaScriptResult
        if (resultObj != null && resultObj != Undefined.instance) {
          result = resultObj.toString()
          break
        }
      }

      checkNotNull(result) { "Timeout waiting for readability parsing" }
      return json.decodeFromString<ReadabilityResult>(result)
    }
  }
}

@Serializable
private data class AriaHiddenProbe(
  val beforeCount: Int,
  val afterCount: Int,
  val hrefs: List<String>,
)
