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
import android.os.Handler
import android.os.Looper
import android.webkit.JavascriptInterface
import android.webkit.RenderProcessGoneDetail
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.collection.lruCache
import co.touchlab.kermit.Logger
import dev.sasikanth.rss.reader.core.model.local.ReadabilityResult
import dev.sasikanth.rss.reader.di.scopes.AppScope
import dev.sasikanth.rss.reader.reader.redability.ReadabilityRunner
import dev.sasikanth.rss.reader.reader.redability.ReaderHTML
import dev.sasikanth.rss.reader.reader.redability.fallbackReadabilityResult
import dev.sasikanth.rss.reader.util.DispatchersProvider
import dev.sasikanth.rss.reader.utils.asJSString
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger
import kotlin.coroutines.cancellation.CancellationException
import kotlin.coroutines.resume
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
private const val RESULT_CACHE_MAX_SIZE = 16
private const val JS_INTERFACE_NAME = "ReaderJSInterface"

@Inject
@AppScope
class AndroidReadabilityRunner(
  private val context: Context,
  private val dispatchersProvider: DispatchersProvider,
) : ReadabilityRunner {
  private val semaphore = Semaphore(permits = 2)
  private val pool = ArrayDeque<PooledWebView>()
  private val poolMutex = Mutex()
  private val shellMutex = Mutex()
  private var cachedShell: String? = null
  private val nextRequestId = AtomicInteger(0)
  private val mainHandler = Handler(Looper.getMainLooper())
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

      val requestId = nextRequestId.incrementAndGet()
      val (htmlShell, executionScript) =
        withContext(dispatchersProvider.default) {
          val shell =
            shellMutex.withLock {
              cachedShell ?: ReaderHTML.createOrGet().also { cachedShell = it }
            }
          val script =
            """
            (function() {
                try {
                    if (typeof parseReaderContent === 'undefined') {
                        $JS_INTERFACE_NAME.onContentParseFailed($requestId, 'parseReaderContent is undefined');
                    } else {
                        parseReaderContent(
                            ${link.asJSString},
                            ${image.asJSString},
                            ${content.asJSString}
                        ).then(function (res) {
                            $JS_INTERFACE_NAME.onContentParsed($requestId, JSON.stringify(res));
                        }).catch(function (err) {
                            $JS_INTERFACE_NAME.onContentParseFailed($requestId, 'Promise error: ' + err);
                        });
                    }
                } catch (e) {
                    $JS_INTERFACE_NAME.onContentParseFailed($requestId, 'Script error: ' + e);
                }
            })();
            """
              .trimIndent()

          shell to script
        }

      val rawResult =
        try {
          withContext(dispatchersProvider.main) {
            var pooled = getOrCreateWebView()
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
                          requestId = requestId,
                          htmlShell = htmlShell,
                          executionScript = executionScript,
                        )
                      break
                    } catch (e: CancellationException) {
                      throw e
                    } catch (e: RetryableWebViewException) {
                      if (attempt == 1) throw e
                      if (pooled.isDead) {
                        pooled.dispose()
                        pooled = getOrCreateWebView()
                      } else {
                        pooled.loadGeneration++
                        pooled.isShellLoaded = false
                        pooled.webView.stopLoading()
                      }
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
                if (completedCleanly && pooled.isShellLoaded && !pooled.isDead) {
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
    requestId: Int,
    htmlShell: String,
    executionScript: String,
  ): String = suspendCancellableCoroutine { continuation ->
    val generation = ++pooled.loadGeneration
    val isFinished = AtomicBoolean(false)
    var scriptStarted = false

    fun triggerEvaluationOnce() {
      if (generation != pooled.loadGeneration || scriptStarted || isFinished.get()) return
      scriptStarted = true
      pooled.isShellLoaded = true
      pooled.webView.evaluateJavascript(executionScript, null)
    }

    pooled.activeAbort = { cause ->
      if (generation == pooled.loadGeneration && isFinished.compareAndSet(false, true)) {
        continuation.resumeWithException(cause)
      }
    }

    pooled.client.onPageFinished = {
      if (generation == pooled.loadGeneration && !pooled.isShellLoaded) {
        triggerEvaluationOnce()
      }
    }

    pooled.client.onReceivedMainFrameError = { description ->
      if (generation == pooled.loadGeneration && !pooled.isShellLoaded) {
        pooled.isShellLoaded = false
        if (isFinished.compareAndSet(false, true)) {
          continuation.resumeWithException(
            RetryableWebViewException("Failed to load reader shell: $description")
          )
        }
      }
    }

    pooled.bridge.onReady = {
      if (generation == pooled.loadGeneration && !pooled.isShellLoaded && !isFinished.get()) {
        mainHandler.post { triggerEvaluationOnce() }
      }
    }

    pooled.bridge.onResult = { id, result ->
      if (
        id == requestId &&
          generation == pooled.loadGeneration &&
          isFinished.compareAndSet(false, true)
      ) {
        continuation.resume(result)
      }
    }

    pooled.bridge.onError = { id, error ->
      if (id == requestId && generation == pooled.loadGeneration) {
        val undefinedShell = error.contains("parseReaderContent is undefined")
        if (undefinedShell) {
          pooled.isShellLoaded = false
        }
        if (isFinished.compareAndSet(false, true)) {
          val exception =
            if (undefinedShell) {
              RetryableWebViewException(error)
            } else {
              IllegalStateException(error)
            }
          continuation.resumeWithException(exception)
        }
      }
    }

    continuation.invokeOnCancellation { isFinished.set(true) }

    if (pooled.isShellLoaded) {
      scriptStarted = true
      pooled.webView.evaluateJavascript(executionScript, null)
    } else {
      pooled.webView.loadDataWithBaseURL(null, htmlShell, "text/html", "UTF-8", null)
    }
  }

  @SuppressLint("SetJavaScriptEnabled")
  private suspend fun getOrCreateWebView(): PooledWebView =
    poolMutex.withLock {
      pool.removeFirstOrNull()
        ?: run {
          val bridge = ReaderJSBridge()
          val client = ReaderWebViewClient()
          val webView =
            WebView(context).apply {
              settings.javaScriptEnabled = true
              addJavascriptInterface(bridge, JS_INTERFACE_NAME)
              webViewClient = client
            }
          PooledWebView(webView = webView, bridge = bridge, client = client)
        }
    }

  private suspend fun returnWebViewToPool(pooled: PooledWebView) =
    poolMutex.withLock { pool.addLast(pooled) }

  private class PooledWebView(
    val webView: WebView,
    val bridge: ReaderJSBridge,
    val client: ReaderWebViewClient,
    @Volatile var isShellLoaded: Boolean = false,
    @Volatile var loadGeneration: Long = 0L,
    @Volatile var isDead: Boolean = false,
  ) {
    @Volatile var activeAbort: ((Throwable) -> Unit)? = null

    init {
      client.onRenderProcessGone = {
        isDead = true
        isShellLoaded = false
        val abort = activeAbort
        activeAbort = null
        abort?.invoke(RetryableWebViewException("WebView render process terminated"))
        loadGeneration++
      }
    }

    fun clearActiveCallbacks() {
      bridge.onReady = null
      bridge.onResult = null
      bridge.onError = null
      activeAbort = null
      client.onPageFinished = null
      client.onReceivedMainFrameError = null
    }

    fun dispose() {
      loadGeneration++
      isShellLoaded = false
      clearActiveCallbacks()
      client.onRenderProcessGone = null
      webView.stopLoading()
      webView.removeJavascriptInterface(JS_INTERFACE_NAME)
      webView.webViewClient = WebViewClient()
      webView.destroy()
    }
  }
}

private class RetryableWebViewException(message: String) : Exception(message)

private class ReaderJSBridge {
  @Volatile var onReady: (() -> Unit)? = null
  @Volatile var onResult: ((Int, String) -> Unit)? = null
  @Volatile var onError: ((Int, String) -> Unit)? = null

  @JavascriptInterface
  fun onPageReady() {
    onReady?.invoke()
  }

  @JavascriptInterface
  fun onContentParsed(requestId: Int, result: String) {
    onResult?.invoke(requestId, result)
  }

  @JavascriptInterface
  fun onContentParseFailed(requestId: Int, error: String) {
    onError?.invoke(requestId, error)
  }
}

private class ReaderWebViewClient : WebViewClient() {
  @Volatile var onPageFinished: (() -> Unit)? = null
  @Volatile var onReceivedMainFrameError: ((String) -> Unit)? = null
  @Volatile var onRenderProcessGone: (() -> Unit)? = null

  override fun onPageFinished(view: WebView, url: String?) {
    onPageFinished?.invoke()
  }

  override fun onReceivedError(
    view: WebView,
    request: WebResourceRequest,
    error: WebResourceError,
  ) {
    if (!request.isForMainFrame) return
    onReceivedMainFrameError?.invoke(error.description?.toString().orEmpty())
  }

  override fun onRenderProcessGone(view: WebView, detail: RenderProcessGoneDetail): Boolean {
    onRenderProcessGone?.invoke()
    return true
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
