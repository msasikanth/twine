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
import kotlin.test.assertTrue
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.htmlunit.BrowserVersion
import org.htmlunit.WebClient
import org.htmlunit.corejs.javascript.Undefined
import org.htmlunit.html.HtmlPage

/**
 * Covers two bugs in main.es5.js that were only ever exposed once aria-hidden images could survive
 * Readability: `getBestSrcFromSrcset` shredding URLs that contain literal commas, and the "image"
 * Turndown rule only checking the direct parent for an enclosing link, missing images wrapped in an
 * intermediate tag like <picture>.
 */
class LinkedImageMarkdownTest {

  private val json = Json { ignoreUnknownKeys = true }

  @Test
  fun getBestSrcFromSrcset_withCommasInsideUrls_returnsFullLargestUrl() = runTest {
    val srcset =
      "https://media.example.com/photos/a/w_120,c_limit/photo.jpg 120w, " +
        "https://media.example.com/photos/a/w_320,c_limit/photo.jpg 320w, " +
        "https://media.example.com/photos/a/w_640,c_limit/photo.jpg 640w"

    val result = runGetBestSrcFromSrcset(srcset)

    assertEquals("https://media.example.com/photos/a/w_640,c_limit/photo.jpg", result.value)
  }

  @Test
  fun getBestSrcFromSrcset_withoutInternalCommas_stillPicksLargest() = runTest {
    val srcset = "https://example.com/img-1x.jpg 1x, https://example.com/img-2x.jpg 2x"

    val result = runGetBestSrcFromSrcset(srcset)

    assertEquals("https://example.com/img-2x.jpg", result.value)
  }

  @Test
  fun getBestSrcFromSrcset_withSingleCandidateAndNoDescriptor_returnsIt() = runTest {
    val result = runGetBestSrcFromSrcset("https://example.com/only.jpg")

    assertEquals("https://example.com/only.jpg", result.value)
  }

  @Test
  fun parseReaderContent_withImageLinkedThroughPictureWrapper_producesCleanLinkedImage() = runTest {
    val html =
      """
      <div>
        <p>Choosing the best walking pad for a home office setup takes real research, and
        we spent six weeks testing more than a dozen models before settling on our top
        picks for quiet operation, foldability, and price.</p>
        <h2>Best Overall</h2>
        <figure>
          <a href="https://example.com/buy/cyberpad">
            <picture><img
              srcset="https://example.com/photos/cyberpad,120w.jpg 120w, https://example.com/photos/cyberpad,640w.jpg 640w"
              src="https://example.com/photos/cyberpad-fallback.jpg"
              alt="Urevo CyberPad treadmill"></picture>
          </a>
        </figure>
        <p>The Urevo CyberPad impressed us with how quiet it ran even at higher speeds, and
        its low profile means it slides easily under a standing desk when it's not in use.</p>
      </div>
      """
        .trimIndent()

    val result = runFullPipeline(html)

    assertTrue(
      result.content
        .orEmpty()
        .contains(
          "[![Urevo CyberPad treadmill](https://example.com/photos/cyberpad,640w.jpg)]" +
            "(https://example.com/buy/cyberpad)"
        ),
      "expected a clean linked image with no stray blank lines, got: ${result.content}",
    )
  }

  @Test
  fun parseReaderContent_withImageDirectlyInsideLink_producesCleanLinkedImage() = runTest {
    val html =
      """
      <div>
        <p>Choosing the best walking pad for a home office setup takes real research, and
        we spent six weeks testing more than a dozen models before settling on our top
        picks for quiet operation, foldability, and price.</p>
        <a href="https://example.com/buy"><img
          src="https://example.com/photos/direct.jpg" alt="Direct link image"></a>
        <p>The Urevo CyberPad impressed us with how quiet it ran even at higher speeds, and
        its low profile means it slides easily under a standing desk when it's not in use.</p>
      </div>
      """
        .trimIndent()

    val result = runFullPipeline(html)

    assertTrue(
      result.content
        .orEmpty()
        .contains(
          "[![Direct link image](https://example.com/photos/direct.jpg)](https://example.com/buy)"
        ),
      "expected a clean linked image with no stray blank lines, got: ${result.content}",
    )
  }

  @Test
  fun parseReaderContent_withStandaloneImage_keepsBlockSpacing() = runTest {
    val html =
      """
      <div>
        <p>Choosing the best walking pad for a home office setup takes real research, and
        we spent six weeks testing more than a dozen models before settling on our top
        picks for quiet operation, foldability, and price.</p>
        <img src="https://example.com/photos/standalone.jpg" alt="Standalone photo">
        <p>The Urevo CyberPad impressed us with how quiet it ran even at higher speeds, and
        its low profile means it slides easily under a standing desk when it's not in use.</p>
      </div>
      """
        .trimIndent()

    val result = runFullPipeline(html)

    assertTrue(
      result.content
        .orEmpty()
        .contains("\n\n![Standalone photo](https://example.com/photos/standalone.jpg)\n\n"),
      "expected a block-level standalone image, got: ${result.content}",
    )
  }

  private suspend fun runGetBestSrcFromSrcset(srcset: String): SrcsetProbe {
    WebClient(BrowserVersion.CHROME).use { webClient ->
      webClient.options.isCssEnabled = false
      webClient.options.isDownloadImages = false
      webClient.options.isGeolocationEnabled = false
      webClient.options.isThrowExceptionOnScriptError = true
      webClient.options.isThrowExceptionOnFailingStatusCode = false

      val htmlShell = ReaderHTML.createOrGet()
      val page: HtmlPage = webClient.loadHtmlCodeIntoCurrentWindow(htmlShell)

      val script = "JSON.stringify({ value: getBestSrcFromSrcset(${srcset.asJSString}) });"

      val result = page.executeJavaScript(script).javaScriptResult.toString()
      return json.decodeFromString<SrcsetProbe>(result)
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

@Serializable private data class SrcsetProbe(val value: String? = null)
