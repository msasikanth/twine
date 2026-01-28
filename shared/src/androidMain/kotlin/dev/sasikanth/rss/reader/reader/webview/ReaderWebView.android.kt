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

package dev.sasikanth.rss.reader.reader.webview

import android.annotation.SuppressLint
import android.graphics.Color
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import dev.sasikanth.rss.reader.utils.asJSString
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@SuppressLint("SetJavaScriptEnabled")
@Composable
internal actual fun ReaderWebView(
  link: String?,
  content: String?,
  postImage: String?,
  contentLoaded: (String) -> Unit,
  modifier: Modifier,
) {
  val coroutineScope = rememberCoroutineScope()

  val webViewClient by
    rememberUpdatedState(
      object : WebViewClient() {
        override fun onPageFinished(view: WebView, url: String) {
          if (content.isNullOrBlank()) return

          val script =
            """
          parseReaderContent(
              ${link.asJSString},
              ${postImage.orEmpty().asJSString},
              ${content.asJSString},
          ).then(result => window.ReaderJSInterface.onContentParsed(result))
        """
              .trimIndent()

          view.evaluateJavascript(script, null)
        }
      }
    )

  AndroidView(
    factory = { context ->
      WebView(context).apply {
        settings.javaScriptEnabled = true
        setBackgroundColor(Color.TRANSPARENT)
        addJavascriptInterface(
          ReaderJSInterface(contentLoaded = contentLoaded),
          "ReaderJSInterface"
        )
      }
    },
    modifier = modifier,
    update = { webView ->
      webView.webViewClient = webViewClient

      coroutineScope.launch {
        val html = withContext(Dispatchers.Default) { ReaderHTML.createOrGet() }
        webView.loadDataWithBaseURL(
          /* baseUrl = */ link ?: "",
          /* data = */ html,
          /* mimeType = */ "text/html",
          /* encoding = */ "UTF-8",
          /* historyUrl = */ null
        )
      }
    }
  )
}

private class ReaderJSInterface(private val contentLoaded: (String) -> Unit) {
  @JavascriptInterface
  fun onContentParsed(result: String) {
    contentLoaded(result)
  }
}
