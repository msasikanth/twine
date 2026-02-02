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

/*
 * Copyright 2026 Sasikanth Miriyampalli
 */

package dev.sasikanth.rss.reader.reader.readability

import dev.sasikanth.rss.reader.core.model.local.ReadabilityResult
import dev.sasikanth.rss.reader.di.scopes.AppScope
import dev.sasikanth.rss.reader.reader.redability.ReadabilityRunner
import dev.sasikanth.rss.reader.reader.redability.ReaderHTML
import dev.sasikanth.rss.reader.util.DispatchersProvider
import kotlin.concurrent.AtomicInt
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.readValue
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.sync.withPermit
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import me.tatarka.inject.annotations.Inject
import platform.Foundation.NSURL
import platform.WebKit.*
import platform.darwin.NSObject

@Inject
@AppScope
class IosReadabilityRunner(private val dispatchersProvider: DispatchersProvider) :
  ReadabilityRunner {

  private val semaphore = Semaphore(permits = 3)
  private val pool = mutableListOf<WKWebView>()
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

      // Using your Json.encodeToString based asJSString logic directly here
      val linkJs = Json.encodeToString(link.orEmpty())
      val imageJs = Json.encodeToString(image.orEmpty())
      val contentJs = Json.encodeToString(content)

      val executionScript =
        """
            (function() {
                try {
                    if (typeof parseReaderContent === 'undefined') {
                        window.webkit.messageHandlers.readabilityMessageHandler.postMessage("ERROR:parseReaderContent undefined");
                        return;
                    }
                    parseReaderContent($linkJs, $imageJs, $contentJs)
                        .then(res => {
                            window.webkit.messageHandlers.readabilityMessageHandler.postMessage(JSON.stringify(res));
                        })
                        .catch(err => {
                            window.webkit.messageHandlers.readabilityMessageHandler.postMessage("ERROR:" + err);
                        });
                } catch (e) {
                    window.webkit.messageHandlers.readabilityMessageHandler.postMessage("ERROR:" + e.message);
                }
            })();
        """
          .trimIndent()

      withContext(dispatchersProvider.main) {
        val webView = getOrCreateWebView()

        try {
          suspendCancellableCoroutine { continuation ->
            val isFinished = AtomicInt(0)

            // We create the handler and ensure it is held strongly during this scope
            val handler = ReaderJSHandler { result ->
              if (isFinished.compareAndSet(0, 1)) {
                if (result.startsWith("ERROR:")) {
                  continuation.resumeWithException(Exception(result))
                } else {
                  try {
                    continuation.resume(json.decodeFromString<ReadabilityResult>(result))
                  } catch (e: Exception) {
                    continuation.resumeWithException(e)
                  }
                }
              }
            }

            val userController = webView.configuration.userContentController
            userController.addScriptMessageHandler(handler, "readabilityMessageHandler")

            val userScript =
              WKUserScript(
                source = executionScript,
                injectionTime = WKUserScriptInjectionTime.WKUserScriptInjectionTimeAtDocumentEnd,
                forMainFrameOnly = true,
              )
            userController.addUserScript(userScript)

            webView.loadHTMLString(htmlShell, baseURL = link?.let { NSURL.URLWithString(it) })

            continuation.invokeOnCancellation {
              isFinished.value = 1
              webView.stopLoading()
              cleanupWebView(webView)
            }
          }
        } finally {
          cleanupWebView(webView)
          returnWebViewToPool(webView)
        }
      }
    }

  private fun cleanupWebView(webView: WKWebView) {
    webView.configuration.userContentController.apply {
      removeScriptMessageHandlerForName("readabilityMessageHandler")
      removeAllUserScripts()
    }
  }

  @OptIn(ExperimentalForeignApi::class)
  private suspend fun getOrCreateWebView(): WKWebView =
    poolMutex.withLock {
      if (pool.isNotEmpty()) {
        pool.removeAt(0)
      } else {
        val config = WKWebViewConfiguration()
        WKWebView(frame = platform.CoreGraphics.CGRectZero.readValue(), configuration = config)
      }
    }

  private suspend fun returnWebViewToPool(webView: WKWebView) =
    poolMutex.withLock {
      webView.loadHTMLString("", baseURL = null)
      pool.add(webView)
    }
}

// Ensure this remains a top-level or internal class so it isn't collected
class ReaderJSHandler(private val onResult: (String) -> Unit) :
  NSObject(), WKScriptMessageHandlerProtocol {
  override fun userContentController(
    userContentController: WKUserContentController,
    didReceiveScriptMessage: WKScriptMessage,
  ) {
    val body = didReceiveScriptMessage.body as? String ?: ""
    onResult(body)
  }
}
