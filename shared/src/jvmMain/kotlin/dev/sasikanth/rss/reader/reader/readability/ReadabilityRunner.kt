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

package dev.sasikanth.rss.reader.reader.readability

import dev.sasikanth.rss.reader.core.model.local.ReadabilityResult
import dev.sasikanth.rss.reader.di.scopes.AppScope
import dev.sasikanth.rss.reader.reader.redability.ReadabilityRunner
import dev.sasikanth.rss.reader.reader.redability.ReaderHTML
import dev.sasikanth.rss.reader.util.DispatchersProvider
import dev.sasikanth.rss.reader.utils.asJSString
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import me.tatarka.inject.annotations.Inject
import org.htmlunit.BrowserVersion
import org.htmlunit.WebClient
import org.htmlunit.html.HtmlPage

@Inject
@AppScope
class HtmlReadabilityRunner(private val dispatchersProvider: DispatchersProvider) :
  ReadabilityRunner {

  private val json = Json {
    ignoreUnknownKeys = true
    isLenient = true
    explicitNulls = false
  }

  override suspend fun parseHtml(
    link: String?,
    content: String,
    image: String?,
  ): ReadabilityResult =
    withContext(dispatchersProvider.io) {
      // Use CHROME as it is generally the most compatible, but Rhino is the engine.
      WebClient(BrowserVersion.CHROME).use { webClient ->
        webClient.options.isCssEnabled = false
        webClient.options.isDownloadImages = false
        webClient.options.isGeolocationEnabled = false
        // We want to catch errors to know if our ES5 transpilation failed
        webClient.options.isThrowExceptionOnScriptError = true
        webClient.options.isThrowExceptionOnFailingStatusCode = false

        val htmlShell = ReaderHTML.createOrGet()
        val page: HtmlPage = webClient.loadHtmlCodeIntoCurrentWindow(htmlShell)

        val script =
          """
        var parsingResult = null;
        var parsingError = null;
        
        if (typeof parseReaderContent === 'undefined') {
          parsingError = "parseReaderContent is undefined. Scripts not loaded?";
        } else {
          try {
            parseReaderContent(
              ${link.asJSString}, 
              ${image.asJSString}, 
              ${content.asJSString}
            ).then(function(res) {
              parsingResult = JSON.stringify(res);
            }).catch(function(err) {
              parsingError = "Promise Error: " + err.toString();
            });
          } catch (e) {
            parsingError = "Sync Error: " + e.toString();
          }
        }
      """
            .trimIndent()

        try {
          page.executeJavaScript(script)
        } catch (e: Exception) {
          throw RuntimeException("Failed to execute initial script: ${e.message}", e)
        }

        // Wait for result
        val maxRetries = 100 // 10 seconds
        var result: String? = null

        for (i in 0 until maxRetries) {
          webClient.waitForBackgroundJavaScript(100)

          val errorObj = page.executeJavaScript("parsingError").javaScriptResult
          if (errorObj != null && errorObj != org.htmlunit.corejs.javascript.Undefined.instance) {
            throw RuntimeException("JS parsing failed: ${errorObj}")
          }

          val resultObj = page.executeJavaScript("parsingResult").javaScriptResult
          if (resultObj != null && resultObj != org.htmlunit.corejs.javascript.Undefined.instance) {
            result = resultObj.toString()
            break
          }
        }

        if (result == null) {
          throw RuntimeException("Timeout waiting for readability parsing (10s)")
        }

        return@use json.decodeFromString<ReadabilityResult>(result)
      }
    }
}
