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

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.arkivanov.decompose.extensions.compose.jetbrains.stack.Children
import com.arkivanov.decompose.extensions.compose.jetbrains.stack.animation.StackAnimation
import com.arkivanov.decompose.extensions.compose.jetbrains.stack.animation.plus
import com.arkivanov.essenty.backhandler.BackHandler
import dev.sasikanth.rss.reader.bookmarks.ui.BookmarksScreen
import dev.sasikanth.rss.reader.components.DynamicContentTheme
import dev.sasikanth.rss.reader.components.ImageLoader
import dev.sasikanth.rss.reader.components.LocalDynamicColorState
import dev.sasikanth.rss.reader.components.LocalImageLoader
import dev.sasikanth.rss.reader.components.rememberDynamicColorState
import dev.sasikanth.rss.reader.home.ui.HomeScreen
import dev.sasikanth.rss.reader.repository.BrowserType
import dev.sasikanth.rss.reader.resources.strings.ProvideStrings
import dev.sasikanth.rss.reader.search.ui.SearchScreen
import dev.sasikanth.rss.reader.settings.ui.SettingsScreen
import dev.sasikanth.rss.reader.utils.LocalWindowSizeClass
import me.tatarka.inject.annotations.Assisted
import me.tatarka.inject.annotations.Inject

typealias App = @Composable (openLink: (String, BrowserType) -> Unit) -> Unit

@Inject
@Composable
@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
fun App(
  appPresenter: AppPresenter,
  imageLoader: ImageLoader,
  @Assisted openLink: (String, BrowserType) -> Unit
) {
  val dynamicColorState = rememberDynamicColorState(imageLoader = imageLoader)

  CompositionLocalProvider(
    LocalImageLoader provides imageLoader,
    LocalWindowSizeClass provides calculateWindowSizeClass(),
    LocalDynamicColorState provides dynamicColorState
  ) {
    DynamicContentTheme(dynamicColorState) {
      ProvideStrings {
        val state by appPresenter.state.collectAsState()

        Children(
          modifier = Modifier.fillMaxSize(),
          stack = appPresenter.screenStack,
          animation =
            backAnimation(
              backHandler = appPresenter.backHandler,
              onBack = appPresenter::onBackClicked
            )
        ) { child ->
          when (val screen = child.instance) {
            is Screen.Home ->
              HomeScreen(
                homePresenter = screen.presenter,
                openLink = { openLink(it, state.browserType) },
                modifier = Modifier.fillMaxSize()
              )
            is Screen.Search ->
              SearchScreen(
                searchPresenter = screen.presenter,
                openLink = { openLink(it, state.browserType) },
                modifier = Modifier.fillMaxSize()
              )
            is Screen.Bookmarks ->
              BookmarksScreen(
                bookmarksPresenter = screen.presenter,
                openLink = { openLink(it, state.browserType) },
                modifier = Modifier.fillMaxSize()
              )
            is Screen.Settings ->
              SettingsScreen(
                settingsPresenter = screen.presenter,
                modifier = Modifier.fillMaxSize()
              )
          }
        }
      }
    }
  }
}

expect fun <C : Any, T : Any> backAnimation(
  backHandler: BackHandler,
  onBack: () -> Unit,
): StackAnimation<C, T>
