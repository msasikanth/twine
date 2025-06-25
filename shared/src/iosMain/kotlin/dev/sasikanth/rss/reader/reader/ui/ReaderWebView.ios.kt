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

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.UIKitInteropProperties
import androidx.compose.ui.viewinterop.UIKitView
import dev.sasikanth.rss.reader.utils.asJSString
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.readValue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import platform.CoreGraphics.CGRectZero
import platform.Foundation.NSString
import platform.Foundation.NSURL
import platform.UIKit.UIColor
import platform.WebKit.WKNavigation
import platform.WebKit.WKNavigationDelegateProtocol
import platform.WebKit.WKScriptMessage
import platform.WebKit.WKScriptMessageHandlerProtocol
import platform.WebKit.WKUserContentController
import platform.WebKit.WKWebView
import platform.WebKit.WKWebViewConfiguration
import platform.WebKit.javaScriptEnabled
import platform.darwin.NSObject

@OptIn(ExperimentalForeignApi::class)
@Composable
internal actual fun ReaderWebView(
  link: String?,
  content: String?,
  postImage: String?,
  fetchFullArticle: Boolean,
  contentLoaded: (String) -> Unit,
  modifier: Modifier,
) {
  val coroutineScope = rememberCoroutineScope()
  val navigationDelegate =
    remember(link, fetchFullArticle) {
      @Suppress("PARAMETER_NAME_CHANGED_ON_OVERRIDE")
      object : NSObject(), WKNavigationDelegateProtocol {
        override fun webView(webView: WKWebView, didFinishNavigation: WKNavigation?) {
          val script =
            """
          parseReaderContent(
            ${link.asJSString},
            ${content.asJSString},
            ${postImage.orEmpty().asJSString},
            $fetchFullArticle
          ).then(result => window.webkit.messageHandlers.readerMessageHandler.postMessage(result))
        """
              .trimIndent()

          webView.evaluateJavaScript(script, null)
        }
      }
    }

  val messageHandler =
    remember(link) {
      object : NSObject(), WKScriptMessageHandlerProtocol {
        override fun userContentController(
          userContentController: WKUserContentController,
          didReceiveScriptMessage: WKScriptMessage
        ) {
          if (didReceiveScriptMessage.name == "readerMessageHandler") {
            val messageBody = didReceiveScriptMessage.body as? NSString
            messageBody?.let { contentLoaded(it.toString()) }
          }
        }
      }
    }

  val contentController =
    remember(link) {
      WKUserContentController().apply {
        addScriptMessageHandler(messageHandler, "readerMessageHandler")
      }
    }
  val configuration =
    remember(link) {
      WKWebViewConfiguration().apply {
        userContentController = contentController
        preferences.javaScriptEnabled = true
      }
    }

  UIKitView(
    factory = {
      WKWebView(frame = CGRectZero.readValue(), configuration = configuration).apply {
        setOpaque(false)
        backgroundColor = UIColor.clearColor
      }
    },
    modifier = modifier,
    update = {
      it.setNavigationDelegate(navigationDelegate)

      coroutineScope.launch {
        val html = withContext(Dispatchers.Default) { ReaderHTML.createOrGet() }
        it.loadHTMLString(string = html, baseURL = NSURL.URLWithString(link ?: ""))
      }
    },
    onRelease = { it.navigationDelegate = null },
    properties =
      UIKitInteropProperties(
        isInteractive = false,
        isNativeAccessibilityEnabled = false,
      )
  )
}
