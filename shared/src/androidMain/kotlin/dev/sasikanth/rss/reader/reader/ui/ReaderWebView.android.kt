/*
 * Copyright 2025 Sasikanth Miriyampalli
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package dev.sasikanth.rss.reader.reader.ui

import android.annotation.SuppressLint
import android.graphics.Color
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import dev.sasikanth.rss.reader.utils.asJSString
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@SuppressLint("SetJavaScriptEnabled")
@Composable
actual fun ReaderWebView(
  link: String?,
  content: String?,
  postImage: String?,
  fetchFullArticle: Boolean,
  contentLoaded: (String) -> Unit,
  modifier: Modifier,
) {
  var html by remember(link) { mutableStateOf("") }

  LaunchedEffect(link) { html = withContext(Dispatchers.Default) { ReaderHTML.create() } }

  class ReaderJSInterface {
    @JavascriptInterface
    fun onContentParsed(result: String) {
      contentLoaded(result)
    }
  }

  val webViewClient =
    remember(link) {
      object : WebViewClient() {
        override fun onPageFinished(view: WebView, url: String) {
          val script =
            """
          parseReaderContent(
              ${link.asJSString},
              ${content.asJSString},
              ${postImage.orEmpty().asJSString},
              $fetchFullArticle
          ).then(result => window.ReaderJSInterface.onContentParsed(result))
        """
              .trimIndent()

          view.evaluateJavascript(script, null)
        }
      }
    }

  AndroidView(
    factory = { context ->
      WebView(context).apply {
        settings.javaScriptEnabled = true
        setBackgroundColor(Color.TRANSPARENT)
        addJavascriptInterface(ReaderJSInterface(), "ReaderJSInterface")
        this.webViewClient = webViewClient
      }
    },
    modifier = modifier,
    update = { webView ->
      if (html.isNotBlank()) {
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
