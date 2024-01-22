/*
 * Copyright 2024 Sasikanth Miriyampalli
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

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import com.multiplatform.webview.jsbridge.rememberWebViewJsBridge
import com.multiplatform.webview.web.WebView
import com.multiplatform.webview.web.rememberWebViewNavigator
import com.multiplatform.webview.web.rememberWebViewStateWithHTMLData
import dev.sasikanth.material.color.utilities.utils.StringUtils
import dev.sasikanth.rss.reader.platform.LocalLinkHandler
import dev.sasikanth.rss.reader.reader.ReaderEvent
import dev.sasikanth.rss.reader.reader.ReaderPresenter
import dev.sasikanth.rss.reader.resources.icons.TwineIcons
import dev.sasikanth.rss.reader.resources.icons.Website
import dev.sasikanth.rss.reader.ui.AppTheme
import kotlinx.coroutines.launch

@Composable
internal fun ReaderScreen(presenter: ReaderPresenter, modifier: Modifier = Modifier) {
  val state by presenter.state.collectAsState()
  val coroutineScope = rememberCoroutineScope()
  val linkHandler = LocalLinkHandler.current
  val navigator = rememberWebViewNavigator()

  Scaffold(
    modifier = modifier,
    topBar = {
      Box {
        CenterAlignedTopAppBar(
          title = {},
          navigationIcon = {
            IconButton(onClick = { presenter.dispatch(ReaderEvent.BackClicked) }) {
              Icon(Icons.Rounded.Close, contentDescription = null)
            }
          },
          actions = {
            IconButton(onClick = { coroutineScope.launch { linkHandler.openLink(state.link) } }) {
              Icon(TwineIcons.Website, contentDescription = null)
            }
          },
          colors =
            TopAppBarDefaults.topAppBarColors(
              containerColor = AppTheme.colorScheme.surface,
              navigationIconContentColor = AppTheme.colorScheme.onSurface,
              titleContentColor = AppTheme.colorScheme.onSurface,
              actionIconContentColor = AppTheme.colorScheme.onSurface
            ),
        )

        Divider(
          modifier = Modifier.fillMaxWidth().align(Alignment.BottomStart),
          color = AppTheme.colorScheme.surfaceContainer
        )
      }
    },
    containerColor = AppTheme.colorScheme.surfaceContainerLowest,
    contentColor = Color.Unspecified
  ) { paddingValues ->
    val jsBridge = rememberWebViewJsBridge()

    LaunchedEffect(jsBridge) {
      jsBridge.register(
        ReaderLinkHandler(
          openLink = { link -> coroutineScope.launch { linkHandler.openLink(link) } }
        )
      )
    }

    when {
      state.content == null -> {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
          CircularProgressIndicator(
            color = AppTheme.colorScheme.tintedForeground,
            strokeWidth = 4.dp
          )
        }
      }
      state.content!!.isNotBlank() -> {
        val backgroundColor =
          StringUtils.hexFromArgb(AppTheme.colorScheme.surfaceContainerLowest.toArgb())
        val codeBackgroundColor =
          StringUtils.hexFromArgb(AppTheme.colorScheme.surfaceContainerHighest.toArgb())
        val textColor = StringUtils.hexFromArgb(AppTheme.colorScheme.onSurface.toArgb())
        val linkColor = StringUtils.hexFromArgb(AppTheme.colorScheme.tintedForeground.toArgb())

        val htmlTemplate = remember {
          // TODO: Extract out the HTML rendering and customisation to separate class
          //  with actual templating
          // language=HTML
          """
          <html lang="en">
          <head>
            <link rel="preconnect" href="https://fonts.googleapis.com">
            <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
            <link href="https://fonts.googleapis.com/css2?family=Golos+Text:wght@400;500&display=swap" rel="stylesheet">
            <link href="https://fonts.googleapis.com/css2?family=Source+Code+Pro&display=swap" rel="stylesheet">
            <title>${state.title}</title>
          </head>
          <style>
          body {
            padding-top: 16px;
            background-color: $backgroundColor;
            color: $textColor;
            font-family: 'Golos Text', sans-serif;
          }
          figure {
            margin: 0;
          }
          figcaption {
            margin-top: 8px;
          	font-size: 14px;
          	line-height: 1.6em;
          }
          .caption {
            font-size: 16px;
          }
          img, figure, video, div, object {
          	max-width: 100%;
          	height: auto !important;
          	margin: 0 auto;
          }
          a {
            color: $linkColor;
          }
          ul {
            list-style: none;
          }
          li::before {
            content: "\2022";
            color: $textColor;
            margin-right: 0.5em;
          }
          pre {
          	max-width: 100%;
          	margin: 0;
          	overflow: auto;
          	overflow-y: hidden;
          	word-wrap: normal;
          	word-break: normal;
          	border-radius: 4px;
            padding: 8px;
          }
          pre {
          	line-height: 1.4286em;
          }
          code, pre {
            font-family: 'Source Code Pro', monospace;
            font-size: 14px;
          	-webkit-hyphens: none;
          	background: $codeBackgroundColor;
          }
          code {
          	padding: 1px 2px;
          	border-radius: 2px;
          }
          pre code {
          	letter-spacing: -.027em;
          	font-size: 0.9375em;
          }
          .top-divider {
            margin-bottom: 12px;
          }
          </style>
          <body>
          <h1>${state.title}</h1>
          <p class="caption">Published: ${state.publishedAt}</p>
          <hr class="top-divider">
          ${state.content!!}
          
          <script>
            function handleLinkClick(event) {
                event.preventDefault();
                window.kmpJsBridge.callNative(
                  "linkHandler", 
                  event.target.href, 
                  {}
                );
            }
            
            var links = document.getElementsByTagName("a")
            for (var i=0, max=links.length; i<max; i++) {
              var link = links[i];
              link.addEventListener("click", handleLinkClick);
            }
          </script>
          </body>
          </html>
        """
            .trimIndent()
        }
        val webViewState = rememberWebViewStateWithHTMLData(htmlTemplate)

        Box(Modifier.fillMaxSize().padding(paddingValues).padding(horizontal = 16.dp)) {
          WebView(
            modifier = Modifier.fillMaxSize(),
            state = webViewState,
            navigator = navigator,
            webViewJsBridge = jsBridge
          )
        }
      }
      else -> {
        Text("No reader content")
      }
    }
  }
}
