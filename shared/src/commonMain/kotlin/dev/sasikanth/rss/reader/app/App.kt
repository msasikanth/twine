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

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.SheetValue
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import coil3.ImageLoader
import coil3.annotation.ExperimentalCoilApi
import coil3.compose.setSingletonImageLoaderFactory
import com.arkivanov.decompose.extensions.compose.stack.Children
import com.arkivanov.decompose.extensions.compose.stack.animation.StackAnimation
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import com.arkivanov.essenty.backhandler.BackHandler
import dev.sasikanth.rss.reader.about.ui.AboutScreen
import dev.sasikanth.rss.reader.addfeed.ui.AddFeedScreen
import dev.sasikanth.rss.reader.bookmarks.ui.BookmarksScreen
import dev.sasikanth.rss.reader.data.repository.AppThemeMode
import dev.sasikanth.rss.reader.feed.ui.FeedInfoBottomSheet
import dev.sasikanth.rss.reader.group.ui.GroupScreen
import dev.sasikanth.rss.reader.groupselection.ui.GroupSelectionSheet
import dev.sasikanth.rss.reader.home.ui.HomeScreen
import dev.sasikanth.rss.reader.placeholder.PlaceholderScreen
import dev.sasikanth.rss.reader.platform.LinkHandler
import dev.sasikanth.rss.reader.platform.LocalLinkHandler
import dev.sasikanth.rss.reader.reader.ui.ReaderScreen
import dev.sasikanth.rss.reader.resources.strings.ProvideStrings
import dev.sasikanth.rss.reader.search.ui.SearchScreen
import dev.sasikanth.rss.reader.settings.ui.SettingsScreen
import dev.sasikanth.rss.reader.share.LocalShareHandler
import dev.sasikanth.rss.reader.share.ShareHandler
import dev.sasikanth.rss.reader.ui.AppTheme
import dev.sasikanth.rss.reader.ui.LocalDynamicColorState
import dev.sasikanth.rss.reader.ui.darkAppColorScheme
import dev.sasikanth.rss.reader.ui.lightAppColorScheme
import dev.sasikanth.rss.reader.ui.rememberDynamicColorState
import dev.sasikanth.rss.reader.util.DispatchersProvider
import dev.sasikanth.rss.reader.utils.LocalShowFeedFavIconSetting
import dev.sasikanth.rss.reader.utils.LocalWindowSizeClass
import me.tatarka.inject.annotations.Assisted
import me.tatarka.inject.annotations.Inject

typealias App = @Composable (onThemeChange: (useDarkTheme: Boolean) -> Unit) -> Unit

@Inject
@Composable
@OptIn(ExperimentalMaterial3WindowSizeClassApi::class, ExperimentalCoilApi::class)
fun App(
  appPresenter: AppPresenter,
  shareHandler: ShareHandler,
  linkHandler: LinkHandler,
  imageLoader: ImageLoader,
  dispatchersProvider: DispatchersProvider,
  @Assisted onThemeChange: (useDarkTheme: Boolean) -> Unit
) {
  setSingletonImageLoaderFactory { imageLoader }

  val appState by appPresenter.state.collectAsState()
  val dynamicColorState =
    rememberDynamicColorState(
      defaultLightAppColorScheme = lightAppColorScheme(),
      defaultDarkAppColorScheme = darkAppColorScheme(),
    )

  CompositionLocalProvider(
    LocalWindowSizeClass provides calculateWindowSizeClass(),
    LocalShareHandler provides shareHandler,
    LocalLinkHandler provides linkHandler,
    LocalDynamicColorState provides dynamicColorState,
    LocalShowFeedFavIconSetting provides appState.showFeedFavIcon
  ) {
    val isSystemInDarkTheme = isSystemInDarkTheme()
    val useDarkTheme =
      remember(isSystemInDarkTheme, appState.appThemeMode) {
        when (appState.appThemeMode) {
          AppThemeMode.Light -> false
          AppThemeMode.Dark -> true
          AppThemeMode.Auto -> isSystemInDarkTheme
        }
      }

    LaunchedEffect(useDarkTheme) { onThemeChange(useDarkTheme) }

    AppTheme(useDarkTheme = useDarkTheme) {
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
              Screen.Placeholder -> {
                PlaceholderScreen(modifier = fillMaxSizeModifier)
              }
              is Screen.Home -> {
                HomeScreen(
                  homePresenter = screen.presenter,
                  useDarkTheme = useDarkTheme,
                  modifier = fillMaxSizeModifier,
                  onBottomSheetStateChanged = { sheetValue ->
                    val tempUseDarkTheme =
                      if (sheetValue == SheetValue.Expanded) {
                        true
                      } else {
                        useDarkTheme
                      }

                    onThemeChange(tempUseDarkTheme)
                  }
                )
              }
              is Screen.Search -> {
                SearchScreen(searchPresenter = screen.presenter, modifier = fillMaxSizeModifier)
              }
              is Screen.Bookmarks -> {
                BookmarksScreen(
                  bookmarksPresenter = screen.presenter,
                  modifier = fillMaxSizeModifier
                )
              }
              is Screen.Settings -> {
                SettingsScreen(settingsPresenter = screen.presenter, modifier = fillMaxSizeModifier)
              }
              is Screen.About -> {
                AboutScreen(aboutPresenter = screen.presenter, modifier = fillMaxSizeModifier)
              }
              is Screen.Reader -> {
                ReaderScreen(
                  presenter = screen.presenter,
                  dispatchersProvider = dispatchersProvider,
                  modifier = fillMaxSizeModifier
                )
              }
              is Screen.AddFeed -> {
                AppTheme(useDarkTheme = true) {
                  AddFeedScreen(presenter = screen.presenter, modifier = fillMaxSizeModifier)
                }
              }
              is Screen.GroupDetails -> {
                AppTheme(useDarkTheme = true) {
                  GroupScreen(presenter = screen.presenter, modifier = fillMaxSizeModifier)
                }
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
              is Modals.GroupSelection -> GroupSelectionSheet(presenter = modal.presenter)
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
