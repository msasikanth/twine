/*
 * Copyright 2023 Sasikanth Miriyampalli
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
package dev.sasikanth.rss.reader.app

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import coil3.ImageLoader
import coil3.annotation.ExperimentalCoilApi
import coil3.compose.setSingletonImageLoaderFactory
import com.arkivanov.decompose.extensions.compose.jetbrains.stack.Children
import com.arkivanov.decompose.extensions.compose.jetbrains.stack.animation.StackAnimation
import com.arkivanov.decompose.extensions.compose.jetbrains.subscribeAsState
import com.arkivanov.essenty.backhandler.BackHandler
import dev.sasikanth.rss.reader.about.ui.AboutScreen
import dev.sasikanth.rss.reader.bookmarks.ui.BookmarksScreen
import dev.sasikanth.rss.reader.components.DynamicContentTheme
import dev.sasikanth.rss.reader.components.LocalDynamicColorState
import dev.sasikanth.rss.reader.components.rememberDynamicColorState
import dev.sasikanth.rss.reader.feed.ui.FeedInfoBottomSheet
import dev.sasikanth.rss.reader.home.ui.HomeScreen
import dev.sasikanth.rss.reader.platform.LinkHandler
import dev.sasikanth.rss.reader.platform.LocalLinkHandler
import dev.sasikanth.rss.reader.reader.ui.ReaderScreen
import dev.sasikanth.rss.reader.resources.strings.ProvideStrings
import dev.sasikanth.rss.reader.search.ui.SearchScreen
import dev.sasikanth.rss.reader.settings.ui.SettingsScreen
import dev.sasikanth.rss.reader.share.LocalShareHandler
import dev.sasikanth.rss.reader.share.ShareHandler
import dev.sasikanth.rss.reader.utils.LocalWindowSizeClass
import me.tatarka.inject.annotations.Inject

typealias App = @Composable () -> Unit

@Inject
@Composable
@OptIn(ExperimentalMaterial3WindowSizeClassApi::class, ExperimentalCoilApi::class)
fun App(
  appPresenter: AppPresenter,
  shareHandler: ShareHandler,
  linkHandler: LinkHandler,
  imageLoader: ImageLoader,
) {
  setSingletonImageLoaderFactory { imageLoader }

  val dynamicColorState = rememberDynamicColorState(imageLoader = imageLoader)

  CompositionLocalProvider(
    LocalWindowSizeClass provides calculateWindowSizeClass(),
    LocalDynamicColorState provides dynamicColorState,
    LocalShareHandler provides shareHandler,
    LocalLinkHandler provides linkHandler
  ) {
    DynamicContentTheme(dynamicColorState) {
      ProvideStrings {
        Box {
          Children(
            modifier = Modifier.fillMaxSize(),
            stack = appPresenter.screenStack,
            animation =
            backAnimation(
              backHandler = appPresenter.backHandler,
              onBack = appPresenter::onBackClicked
            )
          ) { child ->
            val fillMaxSizeModifier = Modifier.fillMaxSize()
            when (val screen = child.instance) {
              is Screen.Home -> {
                HomeScreen(homePresenter = screen.presenter, modifier = fillMaxSizeModifier)
              }
              is Screen.Search -> {
                SearchScreen(searchPresenter = screen.presenter, modifier = fillMaxSizeModifier)
              }
              is Screen.Bookmarks -> {
                BookmarksScreen(bookmarksPresenter = screen.presenter, modifier = fillMaxSizeModifier)
              }
              is Screen.Settings -> {
                SettingsScreen(settingsPresenter = screen.presenter, modifier = fillMaxSizeModifier)
              }
              is Screen.About -> {
                AboutScreen(aboutPresenter = screen.presenter, modifier = fillMaxSizeModifier)
              }
              is Screen.Reader -> {
                ReaderScreen(presenter = screen.presenter, modifier = fillMaxSizeModifier)
              }
            }
          }

          val modals by appPresenter.modalStack.subscribeAsState()
          modals.child?.instance?.also { modal ->
            when (modal) {
              is Modals.FeedInfo ->
                FeedInfoBottomSheet(
                  feedPresenter = modal.presenter,
                )
            }
          }
        }
      }
    }
  }
}

internal expect fun <C : Any, T : Any> backAnimation(
  backHandler: BackHandler,
  onBack: () -> Unit,
): StackAnimation<C, T>
