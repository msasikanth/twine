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
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.multiplatform.webview.jsbridge.IJsMessageHandler
import com.multiplatform.webview.jsbridge.JsMessage
import com.multiplatform.webview.jsbridge.rememberWebViewJsBridge
import com.multiplatform.webview.web.LoadingState
import com.multiplatform.webview.web.WebView
import com.multiplatform.webview.web.WebViewNavigator
import com.multiplatform.webview.web.rememberWebViewNavigator
import com.multiplatform.webview.web.rememberWebViewStateWithHTMLData
import dev.sasikanth.rss.reader.core.network.utils.UrlUtils.isNostrUri
import dev.sasikanth.rss.reader.platform.LocalLinkHandler
import dev.sasikanth.rss.reader.reader.ReaderEvent
import dev.sasikanth.rss.reader.reader.ReaderHTMLColors
import dev.sasikanth.rss.reader.reader.ReaderPresenter
import dev.sasikanth.rss.reader.reader.ReaderState.PostMode.Idle
import dev.sasikanth.rss.reader.reader.ReaderState.PostMode.InProgress
import dev.sasikanth.rss.reader.reader.ReaderState.PostMode.RssContent
import dev.sasikanth.rss.reader.reader.ReaderState.PostMode.Source
import dev.sasikanth.rss.reader.reader.ui.ReaderRenderProgressHandler.ReaderRenderLoadingState
import dev.sasikanth.rss.reader.resources.icons.ArrowBack
import dev.sasikanth.rss.reader.resources.icons.ArticleShortcut
import dev.sasikanth.rss.reader.resources.icons.Bookmark
import dev.sasikanth.rss.reader.resources.icons.Bookmarked
import dev.sasikanth.rss.reader.resources.icons.Share
import dev.sasikanth.rss.reader.resources.icons.TwineIcons
import dev.sasikanth.rss.reader.resources.icons.Website
import dev.sasikanth.rss.reader.resources.strings.LocalStrings
import dev.sasikanth.rss.reader.share.LocalShareHandler
import dev.sasikanth.rss.reader.ui.AppTheme
import dev.sasikanth.rss.reader.util.DispatchersProvider
import dev.sasikanth.rss.reader.utils.asJSString
import dev.sasikanth.rss.reader.utils.hexString
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
internal fun ReaderScreen(
  presenter: ReaderPresenter,
  dispatchersProvider: DispatchersProvider,
  modifier: Modifier = Modifier
) {
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
              Icon(TwineIcons.ArrowBack, contentDescription = LocalStrings.current.buttonGoBack)
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

        HorizontalDivider(
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
              Icon(bookmarkIcon, contentDescription = LocalStrings.current.bookmark)
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

            when {
              state.content == null ||
                state.postMode == Idle ||
                state.postMode == RssContent ||
                state.postMode == Source -> {
                IconButton(
                  colors = IconButtonDefaults.iconButtonColors(containerColor = iconBackground),
                  onClick = {
                    coroutineScope.launch { presenter.dispatch(ReaderEvent.ArticleShortcutClicked) }
                  }
                ) {
                  Icon(
                    TwineIcons.ArticleShortcut,
                    contentDescription = LocalStrings.current.cdLoadFullArticle,
                    tint = iconTint
                  )
                }
              }
              (state.postMode == InProgress && state.content != null) -> {
                CircularProgressIndicator(
                  color = AppTheme.colorScheme.tintedForeground,
                  modifier = Modifier.requiredSize(24.dp)
                )
              }
            }
          }

          Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
            IconButton(
              onClick = {
                coroutineScope.launch {
                  if (state.link?.isNostrUri() == true) {
                    val nostrRef = state.link!!.removePrefix("nostr:")
                    val modifiedLink =
                      if (nostrRef.startsWith("naddr")) "https://highlighter.com/a/$nostrRef"
                      else "https://njump.me/$nostrRef"
                    linkHandler.openLink(modifiedLink)
                  } else {
                    linkHandler.openLink(state.link)
                  }
                }
              }
            ) {
              Icon(
                modifier = Modifier.requiredSize(24.dp),
                imageVector = TwineIcons.Website,
                contentDescription = LocalStrings.current.openWebsite
              )
            }
          }

          Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
            IconButton(onClick = { coroutineScope.launch { sharedHandler.share(state.link!!) } }) {
              Icon(TwineIcons.Share, contentDescription = LocalStrings.current.share)
            }
          }
        }
      }
    },
    containerColor = AppTheme.colorScheme.surfaceContainerLowest,
    contentColor = Color.Unspecified
  ) { paddingValues ->
    var renderingState by remember { mutableStateOf(ReaderRenderLoadingState.Loading) }

    if (state.canShowReaderView) {
      val navigator = rememberWebViewNavigator()
      val jsBridge = rememberWebViewJsBridge()

      DisposableEffect(jsBridge) {
        jsBridge.register(
          ReaderLinkHandler(
            openLink = { link -> coroutineScope.launch { linkHandler.openLink(link) } }
          )
        )

        jsBridge.register(
          ReaderRenderProgressHandler(
            renderState = { newRenderingState -> renderingState = newRenderingState }
          )
        )

        onDispose { jsBridge.clear() }
      }

      val codeBackgroundColor = AppTheme.colorScheme.surfaceContainerHighest.hexString()
      val textColor = AppTheme.colorScheme.onSurface.hexString()
      val linkColor = AppTheme.colorScheme.tintedForeground.hexString()
      val dividerColor = AppTheme.colorScheme.surfaceContainerHigh.hexString()

      val colors =
        ReaderHTMLColors(
          textColor = textColor,
          linkColor = linkColor,
          dividerColor = dividerColor,
          codeBackgroundColor = codeBackgroundColor
        )

      val webViewState = rememberWebViewStateWithHTMLData("")
      webViewState.webSettings.apply {
        this.backgroundColor = AppTheme.colorScheme.surfaceContainerLowest
        this.supportZoom = false
      }

      LaunchedEffect(state.content) {
        withContext(dispatchersProvider.io) {
          val htmlTemplate =
            ReaderHTML.create(
              title = state.title!!,
              feedName = state.feed!!.name,
              feedHomePageLink = state.feed!!.homepageLink,
              publishedAt = state.publishedAt!!
            )

          navigator.loadHtml(htmlTemplate, state.link)
        }
      }

      LaunchedEffect(webViewState.loadingState) {
        withContext(dispatchersProvider.io) {
          val hasHtmlTemplateLoaded =
            webViewState.loadingState == LoadingState.Finished && !state.content.isNullOrBlank()

          if (hasHtmlTemplateLoaded) {
            navigator.evaluateJavaScript(
              script =
                "renderReaderView(${state.link.asJSString}, ${state.content.asJSString}, ${colors.asJSString})"
            )
          }
        }
      }

      Box(Modifier.fillMaxSize().padding(paddingValues).padding(start = 16.dp)) {
        WebView(
          modifier = Modifier.fillMaxSize(),
          state = webViewState,
          navigator = navigator,
          webViewJsBridge = jsBridge,
          captureBackPresses = false,
        )
      }
    }

    when {
      renderingState == ReaderRenderLoadingState.Loading -> {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
          CircularProgressIndicator(
            color = AppTheme.colorScheme.tintedForeground,
            strokeWidth = 4.dp
          )
        }
      }
      !state.canShowReaderView && state.content.isNullOrBlank() -> {
        Text("No reader content")
      }
    }
  }
}

private class ReaderRenderProgressHandler(
  private val renderState: (ReaderRenderLoadingState) -> Unit,
) : IJsMessageHandler {

  enum class ReaderRenderLoadingState {
    Loading,
    Idle
  }

  override fun handle(
    message: JsMessage,
    navigator: WebViewNavigator?,
    callback: (String) -> Unit
  ) {
    if (message.params.isNotBlank()) {
      renderState(ReaderRenderLoadingState.valueOf(message.params))
    }
  }

  override fun methodName(): String {
    return "renderProgress"
  }
}
