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
import androidx.compose.material3.Surface
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.arkivanov.decompose.extensions.compose.jetbrains.subscribeAsState
import dev.sasikanth.rss.reader.bookmarks.ui.BookmarksScreen
import dev.sasikanth.rss.reader.components.DynamicContentTheme
import dev.sasikanth.rss.reader.components.ImageLoader
import dev.sasikanth.rss.reader.components.LocalImageLoader
import dev.sasikanth.rss.reader.components.rememberDynamicColorState
import dev.sasikanth.rss.reader.home.ui.HomeScreen
import dev.sasikanth.rss.reader.repository.BrowserType
import dev.sasikanth.rss.reader.resources.strings.ProvideStrings
import dev.sasikanth.rss.reader.search.ui.SearchScreen
import dev.sasikanth.rss.reader.ui.AppTheme
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
  CompositionLocalProvider(
    LocalImageLoader provides imageLoader,
    LocalWindowSizeClass provides calculateWindowSizeClass()
  ) {
    var imageUrl by rememberSaveable { mutableStateOf<String?>(null) }
    val dynamicColorState = rememberDynamicColorState()

    LaunchedEffect(imageUrl) {
      if (imageUrl != null) {
        dynamicColorState.updateColorsFromImageUrl(imageUrl!!)
      } else {
        dynamicColorState.reset()
      }
    }

    DynamicContentTheme(dynamicColorState) {
      ProvideStrings {
        Surface(
          modifier = Modifier.fillMaxSize(),
          color = AppTheme.colorScheme.surfaceContainerLowest
        ) {
          val screenStack by appPresenter.screenStack.subscribeAsState()

          when (val screen = screenStack.active.instance) {
            is Screen.Home ->
              HomeScreen(
                homePresenter = screen.presenter,
                onFeaturedItemChange = { imageUrl = it },
                openLink = { openLink(it, BrowserType.Default) }
              )
            is Screen.Search -> {
              SearchScreen(
                searchPresenter = screen.presenter,
                openLink = { openLink(it, BrowserType.Default) }
              )
            }
            is Screen.Bookmarks -> {
              BookmarksScreen(
                bookmarksPresenter = screen.presenter,
                openLink = { openLink(it, BrowserType.Default) }
              )
            }
          }
        }
      }
    }
  }
}
