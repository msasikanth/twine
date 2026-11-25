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

import androidx.collection.lruCache
import co.touchlab.kermit.Logger
import dev.sasikanth.rss.reader.core.model.local.ReadabilityResult
import dev.sasikanth.rss.reader.di.scopes.AppScope
import dev.sasikanth.rss.reader.reader.redability.ReadabilityRunner
import dev.sasikanth.rss.reader.reader.redability.ReaderHTML
import dev.sasikanth.rss.reader.reader.redability.fallbackReadabilityResult
import dev.sasikanth.rss.reader.util.DispatchersProvider
import kotlin.concurrent.AtomicInt
import kotlin.coroutines.Continuation
import kotlin.coroutines.cancellation.CancellationException
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.time.Duration.Companion.seconds
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.ObjCSignatureOverride
import kotlinx.cinterop.readValue
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
import platform.CoreGraphics.CGRectZero
import platform.Foundation.NSError
import platform.WebKit.WKNavigation
import platform.WebKit.WKNavigationDelegateProtocol
import platform.WebKit.WKScriptMessage
import platform.WebKit.WKScriptMessageHandlerProtocol
import platform.WebKit.WKUserContentController
import platform.WebKit.WKWebView
import platform.WebKit.WKWebViewConfiguration
import platform.WebKit.WKWebsiteDataStore
import platform.darwin.NSObject

private val PARSE_TIMEOUT = 15.seconds
private const val RESULT_CACHE_MAX_SIZE = 16
private const val MESSAGE_HANDLER_NAME = "readabilityMessageHandler"

@Inject
@AppScope
class IosReadabilityRunner(private val dispatchersProvider: DispatchersProvider) :
  ReadabilityRunner {

  private val semaphore = Semaphore(permits = 1)
  private val pool = mutableListOf<PooledWebView>()
  private val poolMutex = Mutex()
  private val shellMutex = Mutex()
  private var cachedShell: String? = null
  private val nextRequestId = AtomicInt(0)
  private val resultCache = lruCache<ReadabilityCacheKey, ReadabilityResult>(RESULT_CACHE_MAX_SIZE)

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

    val cacheKey =
      withContext(dispatchersProvider.default) {
        buildCacheKey(link = link, image = image, content = content)
      }
    resultCache[cacheKey]?.let {
      return it
    }

    return semaphore.withPermit {
      resultCache[cacheKey]?.let {
        return@withPermit it
      }
      val requestId = nextRequestId.addAndGet(1)
      val okPrefix = "$requestId:OK:"
      val errorPrefix = "$requestId:ERROR:"

      val (htmlShell, executionScript) =
        withContext(dispatchersProvider.default) {
          val shell =
            shellMutex.withLock {
              cachedShell ?: ReaderHTML.createOrGet().also { cachedShell = it }
            }
          val linkJs = Json.encodeToString(link.orEmpty())
          val imageJs = Json.encodeToString(image.orEmpty())
          val contentJs = Json.encodeToString(content)

          val script =
            """
            (function() {
                try {
                    if (typeof parseReaderContent === 'undefined') {
                        window.webkit.messageHandlers.readabilityMessageHandler.postMessage("$errorPrefix" + "parseReaderContent undefined");
                        return;
                    }
                    parseReaderContent($linkJs, $imageJs, $contentJs)
                        .then(res => {
                            window.webkit.messageHandlers.readabilityMessageHandler.postMessage("$okPrefix" + JSON.stringify(res));
                        })
                        .catch(err => {
                            window.webkit.messageHandlers.readabilityMessageHandler.postMessage("$errorPrefix" + err);
                        });
                } catch (e) {
                    window.webkit.messageHandlers.readabilityMessageHandler.postMessage("$errorPrefix" + e.message);
                }
            })();
            """
              .trimIndent()

          shell to script
        }

      val rawResult =
        try {
          withContext(dispatchersProvider.main) {
            val pooled = getOrCreateWebView()
            var completedCleanly = false
            try {
              val payload =
                withTimeoutOrNull(PARSE_TIMEOUT) {
                  var result: String? = null
                  for (attempt in 0..1) {
                    try {
                      result =
                        awaitParsedJson(
                          pooled = pooled,
                          htmlShell = htmlShell,
                          okPrefix = okPrefix,
                          errorPrefix = errorPrefix,
                          executionScript = executionScript,
                        )
                      break
                    } catch (e: CancellationException) {
                      throw e
                    } catch (e: RetryableWebViewException) {
                      pooled.loadGeneration++
                      pooled.isShellLoaded = false
                      pooled.webView.stopLoading()
                      if (attempt == 1) throw e
                    }
                  }
                  result
                }

              if (payload == null) {
                Logger.e { "Timed out running readability pipeline, falling back to raw content" }
              } else {
                completedCleanly = true
              }

              payload
            } finally {
              withContext(NonCancellable) {
                pooled.clearActiveCallbacks()
                if (completedCleanly && pooled.isShellLoaded) {
                  returnWebViewToPool(pooled)
                } else {
                  pooled.dispose()
                }
              }
            }
          }
        } catch (e: CancellationException) {
          throw e
        } catch (e: Exception) {
          Logger.e(e) { "Failed to run readability pipeline, falling back to raw content" }
          null
        }

      withContext(dispatchersProvider.default) {
        if (rawResult != null) {
          try {
            json.decodeFromString<ReadabilityResult>(rawResult).also {
              resultCache.put(cacheKey, it)
            }
          } catch (e: Exception) {
            Logger.e(e) { "Failed to decode readability payload, falling back to raw content" }
            fallbackReadabilityResult(content)
          }
        } else {
          fallbackReadabilityResult(content)
        }
      }
    }
  }

  private suspend fun awaitParsedJson(
    pooled: PooledWebView,
    htmlShell: String,
    okPrefix: String,
    errorPrefix: String,
    executionScript: String,
  ): String = suspendCancellableCoroutine { continuation ->
    val generation = ++pooled.loadGeneration
    val isFinished = AtomicInt(0)
    var scriptStarted = false
    var expectedNavigation: WKNavigation? = null

    fun triggerEvaluationOnce() {
      if (generation != pooled.loadGeneration || scriptStarted || isFinished.value != 0) return
      scriptStarted = true
      pooled.isShellLoaded = true
      evaluateExecutionScript(
        pooled = pooled,
        generation = generation,
        executionScript = executionScript,
        isFinished = isFinished,
        continuation = continuation,
      )
    }

    pooled.activeAbort = { cause ->
      if (generation == pooled.loadGeneration && isFinished.compareAndSet(0, 1)) {
        continuation.resumeWithException(cause)
      }
    }

    pooled.navigationDelegate.onFinishNavigation = { navigation ->
      if (
        generation == pooled.loadGeneration &&
          !pooled.isShellLoaded &&
          expectedNavigation != null &&
          navigation == expectedNavigation
      ) {
        triggerEvaluationOnce()
      }
    }

    pooled.navigationDelegate.onFailNavigation = { navigation, error ->
      if (
        generation == pooled.loadGeneration &&
          !pooled.isShellLoaded &&
          expectedNavigation != null &&
          navigation == expectedNavigation
      ) {
        pooled.isShellLoaded = false
        if (isFinished.compareAndSet(0, 1)) {
          continuation.resumeWithException(
            Exception("Shell navigation failed: ${error.localizedDescription}")
          )
        }
      }
    }

    pooled.handler.onMessage = { message ->
      if (generation == pooled.loadGeneration) {
        when {
          message == "READY" && !pooled.isShellLoaded && expectedNavigation != null -> {
            triggerEvaluationOnce()
          }
          message.startsWith(okPrefix) -> {
            if (isFinished.compareAndSet(0, 1)) {
              continuation.resume(message.substring(okPrefix.length))
            }
          }
          message.startsWith(errorPrefix) -> {
            val errorDetail = message.substring(errorPrefix.length)
            val undefinedShell = errorDetail.contains("parseReaderContent undefined")
            if (undefinedShell) {
              pooled.isShellLoaded = false
            }
            if (isFinished.compareAndSet(0, 1)) {
              val exception =
                if (undefinedShell) {
                  RetryableWebViewException(errorDetail)
                } else {
                  Exception(errorDetail)
                }
              continuation.resumeWithException(exception)
            }
          }
        }
      }
    }

    continuation.invokeOnCancellation { isFinished.value = 1 }

    if (pooled.isShellLoaded) {
      scriptStarted = true
      evaluateExecutionScript(
        pooled = pooled,
        generation = generation,
        executionScript = executionScript,
        isFinished = isFinished,
        continuation = continuation,
      )
    } else {
      expectedNavigation = pooled.webView.loadHTMLString(htmlShell, baseURL = null)
    }
  }

  private fun evaluateExecutionScript(
    pooled: PooledWebView,
    generation: Long,
    executionScript: String,
    isFinished: AtomicInt,
    continuation: Continuation<String>,
  ) {
    pooled.webView.evaluateJavaScript(executionScript) { _, error ->
      if (error != null && generation == pooled.loadGeneration) {
        pooled.isShellLoaded = false
        if (isFinished.compareAndSet(0, 1)) {
          continuation.resumeWithException(
            RetryableWebViewException("JS Execution Error: ${error.localizedDescription}")
          )
        }
      }
    }
  }

  @OptIn(ExperimentalForeignApi::class)
  private suspend fun getOrCreateWebView(): PooledWebView =
    poolMutex.withLock {
      if (pool.isNotEmpty()) {
        pool.removeAt(0)
      } else {
        val handler = ReaderJSHandler()
        val navigationDelegate = ReaderNavigationDelegate()
        val userContentController =
          WKUserContentController().apply { addScriptMessageHandler(handler, MESSAGE_HANDLER_NAME) }
        val config =
          WKWebViewConfiguration().apply {
            websiteDataStore = WKWebsiteDataStore.nonPersistentDataStore()
            this.userContentController = userContentController
          }
        val webView = WKWebView(frame = CGRectZero.readValue(), configuration = config)
        PooledWebView(
          webView = webView,
          handler = handler,
          navigationDelegate = navigationDelegate,
          userContentController = userContentController,
        )
      }
    }

  private suspend fun returnWebViewToPool(pooled: PooledWebView) =
    poolMutex.withLock { pool.add(pooled) }

  private class PooledWebView(
    val webView: WKWebView,
    val handler: ReaderJSHandler,
    val navigationDelegate: ReaderNavigationDelegate,
    val userContentController: WKUserContentController,
    var isShellLoaded: Boolean = false,
    var loadGeneration: Long = 0L,
  ) {
    var activeAbort: ((Throwable) -> Unit)? = null

    init {
      webView.navigationDelegate = navigationDelegate
      navigationDelegate.onProcessTerminated = {
        isShellLoaded = false
        val abort = activeAbort
        activeAbort = null
        abort?.invoke(RetryableWebViewException("WKWebView WebContent process terminated"))
        loadGeneration++
      }
    }

    fun clearActiveCallbacks() {
      handler.onMessage = null
      activeAbort = null
      navigationDelegate.onFinishNavigation = null
      navigationDelegate.onFailNavigation = null
    }

    fun dispose() {
      loadGeneration++
      isShellLoaded = false
      clearActiveCallbacks()
      navigationDelegate.onProcessTerminated = null
      webView.navigationDelegate = null
      webView.stopLoading()
      userContentController.removeScriptMessageHandlerForName(MESSAGE_HANDLER_NAME)
    }
  }
}

private class RetryableWebViewException(message: String) : Exception(message)

internal class ReaderJSHandler : NSObject(), WKScriptMessageHandlerProtocol {
  var onMessage: ((String) -> Unit)? = null

  override fun userContentController(
    userContentController: WKUserContentController,
    didReceiveScriptMessage: WKScriptMessage,
  ) {
    val body = didReceiveScriptMessage.body as? String ?: return
    onMessage?.invoke(body)
  }
}

internal class ReaderNavigationDelegate : NSObject(), WKNavigationDelegateProtocol {
  var onFinishNavigation: ((WKNavigation?) -> Unit)? = null
  var onFailNavigation: ((WKNavigation?, NSError) -> Unit)? = null
  var onProcessTerminated: (() -> Unit)? = null

  @ObjCSignatureOverride
  override fun webView(webView: WKWebView, didFinishNavigation: WKNavigation?) {
    onFinishNavigation?.invoke(didFinishNavigation)
  }

  @ObjCSignatureOverride
  override fun webView(webView: WKWebView, didFailNavigation: WKNavigation?, withError: NSError) {
    onFailNavigation?.invoke(didFailNavigation, withError)
  }

  @ObjCSignatureOverride
  override fun webView(
    webView: WKWebView,
    didFailProvisionalNavigation: WKNavigation?,
    withError: NSError,
  ) {
    onFailNavigation?.invoke(didFailProvisionalNavigation, withError)
  }

  override fun webViewWebContentProcessDidTerminate(webView: WKWebView) {
    onProcessTerminated?.invoke()
  }
}

private data class ReadabilityCacheKey(
  val link: String?,
  val image: String?,
  val contentLength: Int,
  val primaryHash: Int,
  val secondaryHash: Long,
)

private fun buildCacheKey(link: String?, image: String?, content: String): ReadabilityCacheKey {
  var fnv = -3750763034362895579L
  for (i in 0 until content.length) {
    fnv = (fnv xor content[i].code.toLong()) * 1099511628211L
  }
  return ReadabilityCacheKey(
    link = link,
    image = image,
    contentLength = content.length,
    primaryHash = content.hashCode(),
    secondaryHash = fnv,
  )
}
