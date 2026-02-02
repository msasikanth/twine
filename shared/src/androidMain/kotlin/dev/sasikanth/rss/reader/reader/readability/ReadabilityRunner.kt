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

import android.annotation.SuppressLint
import android.content.Context
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.webkit.WebViewClient
import dev.sasikanth.rss.reader.core.model.local.ReadabilityResult
import dev.sasikanth.rss.reader.di.scopes.AppScope
import dev.sasikanth.rss.reader.reader.redability.ReadabilityRunner
import dev.sasikanth.rss.reader.reader.redability.ReaderHTML
import dev.sasikanth.rss.reader.util.DispatchersProvider
import dev.sasikanth.rss.reader.utils.asJSString
import kotlin.coroutines.resumeWithException
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.sync.withPermit
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import me.tatarka.inject.annotations.Inject

@Inject
@AppScope
class AndroidReadabilityRunner(
  private val context: Context,
  private val dispatchersProvider: DispatchersProvider,
) : ReadabilityRunner {
  private val semaphore = Semaphore(permits = 3)
  private val pool = ArrayDeque<WebView>()
  private val poolMutex = Mutex()
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
    semaphore.withPermit {
      val htmlShell = withContext(dispatchersProvider.default) { ReaderHTML.createOrGet() }

      withContext(dispatchersProvider.main) {
        val webView = getOrCreateWebView()

        try {
          suspendCancellableCoroutine { continuation ->
            val bridge =
              object {
                @JavascriptInterface
                fun onContentParsed(result: String) {
                  try {
                    val parsed = json.decodeFromString<ReadabilityResult>(result)
                    continuation.resume(parsed) { _, _, _ ->
                      // no-op
                    }
                  } catch (e: Exception) {
                    continuation.resumeWithException(e)
                  }
                }
              }

            webView.addJavascriptInterface(bridge, "ReaderJSInterface")

            webView.webViewClient =
              object : WebViewClient() {
                override fun onPageFinished(view: WebView, url: String) {
                  val script =
                    """
                                parseReaderContent(
                                    ${link.asJSString}, 
                                    ${image.asJSString}, 
                                    ${content.asJSString}
                                ).then(res => ReaderJSInterface.onContentParsed(JSON.stringify(res)))
                            """
                      .trimIndent()
                  view.evaluateJavascript(script, null)
                }
              }

            webView.loadDataWithBaseURL(link, htmlShell, "text/html", "UTF-8", null)

            continuation.invokeOnCancellation { webView.stopLoading() }
          }
        } finally {
          returnWebViewToPool(webView)
        }
      }
    }

  @SuppressLint("SetJavaScriptEnabled")
  private suspend fun getOrCreateWebView(): WebView =
    poolMutex.withLock {
      pool.removeFirstOrNull() ?: WebView(context).apply { settings.javaScriptEnabled = true }
    }

  private suspend fun returnWebViewToPool(webView: WebView) =
    poolMutex.withLock {
      webView.removeJavascriptInterface("ReaderJSInterface")
      webView.loadUrl("about:blank")
      pool.addLast(webView)
    }
}
