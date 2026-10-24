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
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import org.htmlunit.BrowserVersion
import org.htmlunit.WebClient
import org.htmlunit.corejs.javascript.Undefined
import org.htmlunit.html.HtmlPage

/**
 * Turndown has no table rules of its own, so without the ones in main.es5.js a table reaches the
 * reader as a run of loose lines. These run the real bundled JS and assert the GFM table syntax the
 * markdown renderer needs, including the fallback for grids GFM cannot express.
 */
class TableMarkdownTest {

  private val json = Json { ignoreUnknownKeys = true }

  @Test
  fun parseReaderContent_withHeadedTable_producesGfmTable() = runTest {
    val result =
      runFullPipeline(
        """
        <div>
          <p>Raspberry Pi has quietly raised the price of its 2GB boards again, the second
          increase this year, and the reason is the same one as last time: memory prices.</p>
          <table class="has-fixed-layout">
            <thead>
              <tr><th>Model</th><th>Launch price</th><th>Old price</th><th>New price</th></tr>
            </thead>
            <tbody>
              <tr><td>Raspberry Pi 4 (2GB)</td><td>45</td><td>55</td><td>67.50</td></tr>
              <tr><td>Raspberry Pi 5 (2GB)</td><td>50</td><td>65</td><td>77.50</td></tr>
            </tbody>
          </table>
          <p>Memory makers are prioritising datacentre customers, which leaves single board
          computer vendors paying spot prices for the parts they need.</p>
        </div>
        """
          .trimIndent()
      )

    val content = result.content.orEmpty()

    assertTrue(
      content.contains("| Model | Launch price | Old price | New price |"),
      "expected a header row, got: $content",
    )
    assertTrue(
      content.contains("| --- | --- | --- | --- |"),
      "expected a delimiter row, got: $content",
    )
    assertTrue(
      content.contains("| Raspberry Pi 4 (2GB) | 45 | 55 | 67.50 |"),
      "expected a body row, got: $content",
    )
    assertTrue(
      content.contains("| Raspberry Pi 5 (2GB) | 50 | 65 | 77.50 |"),
      "expected a body row, got: $content",
    )
  }

  @Test
  fun parseReaderContent_withHeaderlessTable_promotesFirstRowToHeader() = runTest {
    val result =
      runFullPipeline(
        """
        <div>
          <p>The comparison below lays out how the three boards differ once you account for
          the price rises, and which one still makes sense for a home server build.</p>
          <table>
            <tbody>
              <tr><td>Board</td><td>Memory</td></tr>
              <tr><td>Pi 4</td><td>2GB</td></tr>
              <tr><td>Pi 5</td><td>2GB</td></tr>
            </tbody>
          </table>
          <p>Neither board is a bad buy, but the newer one holds its value better if you
          plan to keep it running for years.</p>
        </div>
        """
          .trimIndent()
      )

    val content = result.content.orEmpty()

    assertTrue(
      content.contains("| Board | Memory |\n| --- | --- |\n| Pi 4 | 2GB |"),
      "expected the first row promoted to a header, got: $content",
    )
  }

  @Test
  fun parseReaderContent_withAlignedAndPipedCells_keepsAlignmentAndEscapesPipes() = runTest {
    val result =
      runFullPipeline(
        """
        <div>
          <p>Here is how the shell pipelines in each example differ, written out so you can
          copy them straight into a terminal without rewriting anything.</p>
          <table>
            <thead>
              <tr><th align="left">Command</th><th align="right">Lines</th></tr>
            </thead>
            <tbody>
              <tr><td>ls | wc -l</td><td>12</td></tr>
              <tr><td>ps | grep pi</td><td>3</td></tr>
            </tbody>
          </table>
          <p>Both pipelines do the same amount of work, so pick whichever one you find
          easier to remember when you are in a hurry.</p>
        </div>
        """
          .trimIndent()
      )

    val content = result.content.orEmpty()

    assertTrue(
      content.contains("| :--- | ---: |"),
      "expected alignment markers in the delimiter row, got: $content",
    )
    assertTrue(
      content.contains("""| ls \| wc -l | 12 |"""),
      "expected the cell pipe to be escaped, got: $content",
    )
  }

  @Test
  fun parseReaderContent_withRowSpans_leavesTableToDefaultHandling() = runTest {
    val result =
      runFullPipeline(
        """
        <div>
          <p>The schedule below is grouped by day, with the morning and afternoon sessions
          sharing a single cell wherever the same speaker runs both of them.</p>
          <table>
            <tbody>
              <tr><td rowspan="2">Monday</td><td>Morning</td></tr>
              <tr><td>Afternoon</td></tr>
            </tbody>
          </table>
          <p>Tickets for each day are sold separately, so check which sessions you want
          before picking the days you attend.</p>
        </div>
        """
          .trimIndent()
      )

    val content = result.content.orEmpty()

    assertFalse(
      content.contains("| --- |"),
      "expected no GFM table for a row-spanned grid, got: $content",
    )
    assertTrue(content.contains("Monday"), "expected the cell text to survive, got: $content")
    assertTrue(content.contains("Afternoon"), "expected the cell text to survive, got: $content")
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
