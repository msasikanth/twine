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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
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
import dev.sasikanth.rss.reader.reader.ReaderState.PostMode.Idle
import dev.sasikanth.rss.reader.reader.ReaderState.PostMode.InProgress
import dev.sasikanth.rss.reader.reader.ReaderState.PostMode.RssContent
import dev.sasikanth.rss.reader.reader.ReaderState.PostMode.Source
import dev.sasikanth.rss.reader.resources.icons.ArticleShortcut
import dev.sasikanth.rss.reader.resources.icons.Bookmark
import dev.sasikanth.rss.reader.resources.icons.Bookmarked
import dev.sasikanth.rss.reader.resources.icons.Share
import dev.sasikanth.rss.reader.resources.icons.TwineIcons
import dev.sasikanth.rss.reader.resources.icons.Website
import dev.sasikanth.rss.reader.share.LocalShareHandler
import dev.sasikanth.rss.reader.ui.AppTheme
import kotlinx.coroutines.launch

@Composable
internal fun ReaderScreen(presenter: ReaderPresenter, modifier: Modifier = Modifier) {
  val state by presenter.state.collectAsState()
  val coroutineScope = rememberCoroutineScope()
  val linkHandler = LocalLinkHandler.current
  val sharedHandler = LocalShareHandler.current

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
    bottomBar = {
      Surface(
        color = AppTheme.colorScheme.surfaceContainerHigh,
        contentColor = AppTheme.colorScheme.onSurface,
      ) {
        Row(
          modifier =
            Modifier.fillMaxWidth()
              .windowInsetsPadding(WindowInsets.navigationBars)
              .padding(vertical = 8.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
            val bookmarkIcon =
              if (state.isBookmarked == true) {
                TwineIcons.Bookmarked
              } else {
                TwineIcons.Bookmark
              }
            IconButton(onClick = { presenter.dispatch(ReaderEvent.TogglePostBookmark) }) {
              Icon(bookmarkIcon, contentDescription = null)
            }
          }

          Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
            val iconTint =
              if (state.postMode == Source) {
                AppTheme.colorScheme.tintedForeground
              } else {
                AppTheme.colorScheme.onSurface
              }

            val iconBackground =
              if (state.postMode == Source) {
                AppTheme.colorScheme.tintedSurface
              } else {
                Color.Transparent
              }

            when (state.postMode) {
              Idle,
              RssContent,
              Source -> {
                IconButton(
                  colors = IconButtonDefaults.iconButtonColors(containerColor = iconBackground),
                  onClick = {
                    coroutineScope.launch { presenter.dispatch(ReaderEvent.ArticleShortcutClicked) }
                  }
                ) {
                  Icon(TwineIcons.ArticleShortcut, contentDescription = null, tint = iconTint)
                }
              }
              InProgress -> {
                CircularProgressIndicator(
                  color = AppTheme.colorScheme.tintedForeground,
                  modifier = Modifier.requiredSize(24.dp)
                )
              }
            }
          }

          Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
            IconButton(onClick = { coroutineScope.launch { linkHandler.openLink(state.link) } }) {
              Icon(TwineIcons.Website, contentDescription = null)
            }
          }

          Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
            IconButton(onClick = { coroutineScope.launch { sharedHandler.share(state.link) } }) {
              Icon(TwineIcons.Share, contentDescription = null)
            }
          }
        }
      }
    },
    containerColor = AppTheme.colorScheme.surfaceContainerLowest,
    contentColor = Color.Unspecified
  ) { paddingValues ->
    when {
      state.content == null -> {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
          CircularProgressIndicator(
            color = AppTheme.colorScheme.tintedForeground,
            strokeWidth = 4.dp
          )
        }
      }
      state.hasContent -> {
        val navigator = rememberWebViewNavigator()
        val jsBridge = rememberWebViewJsBridge()

        LaunchedEffect(jsBridge) {
          jsBridge.register(
            ReaderLinkHandler(
              openLink = { link -> coroutineScope.launch { linkHandler.openLink(link) } }
            )
          )
        }

        val codeBackgroundColor =
          StringUtils.hexFromArgb(AppTheme.colorScheme.surfaceContainerHighest.toArgb())
        val textColor = StringUtils.hexFromArgb(AppTheme.colorScheme.onSurface.toArgb())
        val linkColor = StringUtils.hexFromArgb(AppTheme.colorScheme.tintedForeground.toArgb())
        val dividerColor =
          StringUtils.hexFromArgb(AppTheme.colorScheme.surfaceContainerHigh.toArgb())

        val htmlTemplate =
          remember(state.content) {
            // TODO: Extract out the HTML rendering and customisation to separate class
            //  with actual templating
            // language=HTML
            """
          <html lang="en">
          <head>
            <link rel="preconnect" href="https://fonts.googleapis.com">
            <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
            <link href="https://fonts.googleapis.com/css2?family=Golos+Text:wght@400;500&display=swap" rel="stylesheet">
            <link href="https://fonts.googleapis.com/css2?family=Roboto+Slab:wght@300;400;500;700&display=swap" rel="stylesheet">
            <link href="https://fonts.googleapis.com/css2?family=Merriweather+Sans:wght@300;400;500;700&display=swap" rel="stylesheet">
            <link href="https://fonts.googleapis.com/css2?family=Source+Code+Pro&display=swap" rel="stylesheet">
            <title>${state.title}</title>
          </head>
          <style>
          body {
            padding-top: 16px;
            color: $textColor;
            font-family: 'Golos Text', sans-serif;
          }
          figure {
            margin: 0;
          }
          figcaption {
            margin-top: 8px;
          	font-size: 14px;
          	line-height: 1.6;
          }
          .caption {
            font-size: 12px;
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
          	line-height: 1.4286;
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
          	letter-spacing: -.027;
          	font-size: 1.15em;
          }
          .top-divider {
            margin-top: 12px;
            margin-bottom: 12px;
            border: 1px solid $dividerColor;
          }
          .grid-container {
            display: grid;
            row-gap: 16px;
          }
          </style>
          <body>
          <h1>${state.title}</h1>
          <hr class="top-divider">
          <div class="grid-container">
              <div><a href='${state.feed!!.homepageLink}'>${state.feed!!.name}</a></div>
              <div class="caption">${state.publishedAt}</div>
          </div>
          <hr class="top-divider">
          ${state.content!!}
          
          <script>
            function findHref(node, maxDepth = 4) {
              let currentDepth = 0;

              while (node && currentDepth < maxDepth) {
                if (node.tagName && node.tagName.toLowerCase() === 'a' && node.hasAttribute('href')) {
                  return node.getAttribute("href");
                }

                node = node.parentNode;
                currentDepth++;
              }

              return null;
            }

            function handleLinkClick(event) {
                try {
                  event.preventDefault();
                  var href = findHref(event.target);
          
                  window.kmpJsBridge.callNative(
                    "linkHandler", 
                    href, 
                    {}
                  );
                } catch(err) {
                  // no-op
                }
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
        webViewState.webSettings.apply {
          this.backgroundColor = AppTheme.colorScheme.surfaceContainerLowest
        }

        Box(Modifier.fillMaxSize().padding(paddingValues).padding(horizontal = 16.dp)) {
          WebView(
            modifier = Modifier.fillMaxSize(),
            state = webViewState,
            navigator = navigator,
            webViewJsBridge = jsBridge,
            captureBackPresses = false,
          )
        }
      }
      else -> {
        Text("No reader content")
      }
    }
  }
}
