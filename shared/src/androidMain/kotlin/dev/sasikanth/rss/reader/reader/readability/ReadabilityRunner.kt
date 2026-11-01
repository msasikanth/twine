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
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import co.touchlab.kermit.Logger
import dev.sasikanth.rss.reader.core.model.local.ReadabilityResult
import dev.sasikanth.rss.reader.di.scopes.AppScope
import dev.sasikanth.rss.reader.reader.redability.ReadabilityRunner
import dev.sasikanth.rss.reader.reader.redability.ReaderHTML
import dev.sasikanth.rss.reader.reader.redability.fallbackReadabilityResult
import dev.sasikanth.rss.reader.util.DispatchersProvider
import dev.sasikanth.rss.reader.utils.asJSString
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.coroutines.cancellation.CancellationException
import kotlin.coroutines.resumeWithException
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.sync.withPermit
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.serialization.json.Json
import me.tatarka.inject.annotations.Inject

private val PARSE_TIMEOUT = 15.seconds

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
  ): ReadabilityResult {
    if (content.isBlank()) return ReadabilityResult(content = content)

    return semaphore.withPermit {
      val htmlShell = withContext(dispatchersProvider.default) { ReaderHTML.createOrGet() }

      withContext(dispatchersProvider.main) {
        val webView = getOrCreateWebView()

        try {
          withTimeoutOrNull(PARSE_TIMEOUT) {
            awaitParsedContent(
              webView = webView,
              htmlShell = htmlShell,
              link = link,
              content = content,
              image = image,
            )
          }
            ?: run {
              Logger.e { "Timed out running readability pipeline, falling back to raw content" }
              withContext(dispatchersProvider.default) { fallbackReadabilityResult(content) }
            }
        } catch (e: CancellationException) {
          throw e
        } catch (e: Exception) {
          Logger.e(e) { "Failed to run readability pipeline, falling back to raw content" }
          withContext(dispatchersProvider.default) { fallbackReadabilityResult(content) }
        } finally {
          withContext(NonCancellable) { returnWebViewToPool(webView) }
        }
      }
    }
  }

  private suspend fun awaitParsedContent(
    webView: WebView,
    htmlShell: String,
    link: String?,
    content: String,
    image: String?,
  ): ReadabilityResult = suspendCancellableCoroutine { continuation ->
    val isFinished = AtomicBoolean(false)

    val bridge =
      object {
        @JavascriptInterface
        fun onPageReady() {
          if (isFinished.get()) return
          webView.post {
            if (isFinished.get()) return@post
            val script =
              """
                  try {
                      if (typeof parseReaderContent === 'undefined') {
                          ReaderJSInterface.onContentParseFailed('parseReaderContent is undefined');
                      } else {
                          parseReaderContent(
                              ${link.asJSString}, 
                              ${image.asJSString}, 
                              ${content.asJSString}
                          ).then(function (res) {
                              ReaderJSInterface.onContentParsed(JSON.stringify(res));
                          }).catch(function (err) {
                              ReaderJSInterface.onContentParseFailed('Promise error: ' + err);
                          });
                      }
                  } catch (e) {
                      ReaderJSInterface.onContentParseFailed('Script error: ' + e);
                  }
              """
                .trimIndent()
            webView.evaluateJavascript(script, null)
          }
        }

        @JavascriptInterface
        fun onContentParsed(result: String) {
          if (!isFinished.compareAndSet(false, true)) return
          try {
            continuation.resume(json.decodeFromString<ReadabilityResult>(result)) { _, _, _ ->
              // no-op
            }
          } catch (e: Exception) {
            continuation.resumeWithException(e)
          }
        }

        @JavascriptInterface
        fun onContentParseFailed(error: String) {
          if (!isFinished.compareAndSet(false, true)) return
          continuation.resumeWithException(IllegalStateException(error))
        }
      }

    webView.addJavascriptInterface(bridge, "ReaderJSInterface")

    webView.webViewClient =
      object : WebViewClient() {
        override fun onReceivedError(
          view: WebView,
          request: WebResourceRequest,
          error: WebResourceError,
        ) {
          if (!request.isForMainFrame) return
          if (!isFinished.compareAndSet(false, true)) return
          continuation.resumeWithException(
            IllegalStateException("Failed to load reader shell: ${error.description}")
          )
        }
      }

    webView.loadDataWithBaseURL(link, htmlShell, "text/html", "UTF-8", null)

    continuation.invokeOnCancellation { isFinished.set(true) }
  }

  @SuppressLint("SetJavaScriptEnabled")
  private suspend fun getOrCreateWebView(): WebView =
    poolMutex.withLock {
      pool.removeFirstOrNull() ?: WebView(context).apply { settings.javaScriptEnabled = true }
    }

  private suspend fun returnWebViewToPool(webView: WebView) =
    poolMutex.withLock {
      webView.stopLoading()
      webView.removeJavascriptInterface("ReaderJSInterface")
      webView.webViewClient = WebViewClient()
      pool.addLast(webView)
    }
}
