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

package dev.sasikanth.rss.reader.main.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.DrawerDefaults
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.backhandler.BackHandler
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import dev.sasikanth.rss.reader.resources.icons.Bookmark
import dev.sasikanth.rss.reader.resources.icons.BookmarkFilled
import dev.sasikanth.rss.reader.resources.icons.Home
import dev.sasikanth.rss.reader.resources.icons.HomeFilled
import dev.sasikanth.rss.reader.resources.icons.Search
import dev.sasikanth.rss.reader.resources.icons.Settings
import dev.sasikanth.rss.reader.resources.icons.SettingsFilled
import dev.sasikanth.rss.reader.resources.icons.TwineIcons
import dev.sasikanth.rss.reader.ui.AppTheme
import dev.sasikanth.rss.reader.utils.LocalWindowSizeClass
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import twine.shared.generated.resources.Res
import twine.shared.generated.resources.bookmarks
import twine.shared.generated.resources.postsSearchHint
import twine.shared.generated.resources.screenHome
import twine.shared.generated.resources.settings

@Composable
internal fun MainScreen(
  homeContent: @Composable (openDrawer: () -> Unit) -> Unit,
  searchContent: @Composable (openDrawer: () -> Unit) -> Unit,
  bookmarksContent: @Composable (openDrawer: () -> Unit) -> Unit,
  settingsContent: @Composable (openDrawer: () -> Unit) -> Unit,
  modifier: Modifier = Modifier,
) {
  val windowSizeClass = LocalWindowSizeClass.current
  val useNavigationRail = windowSizeClass.widthSizeClass != WindowWidthSizeClass.Compact

  var selectedDestination by rememberSaveable { mutableStateOf(MainDestination.Home) }
  var isNavigationRailVisible by remember { mutableStateOf(true) }
  val drawerState = rememberDrawerState(DrawerValue.Closed)
  val scope = rememberCoroutineScope()

  @OptIn(ExperimentalComposeUiApi::class)
  BackHandler(enabled = selectedDestination != MainDestination.Home) {
    selectedDestination = MainDestination.Home
  }

  val openDrawer = {
    if (useNavigationRail) {
      isNavigationRailVisible = !isNavigationRailVisible
    } else {
      scope.launch { drawerState.open() }
    }
    Unit
  }

  val goBackToHome = { selectedDestination = MainDestination.Home }

  if (useNavigationRail) {
    Row(modifier = modifier.fillMaxSize()) {
      AnimatedVisibility(
        visible = isNavigationRailVisible,
        enter = slideInHorizontally(initialOffsetX = { -it }) + expandHorizontally(),
        exit = slideOutHorizontally(targetOffsetX = { -it }) + shrinkHorizontally(),
      ) {
        val navigationRailItemColors =
          NavigationRailItemDefaults.colors(
            selectedIconColor = AppTheme.colorScheme.tintedForeground,
            unselectedIconColor = AppTheme.colorScheme.onSurfaceVariant,
            indicatorColor = Color.Transparent,
          )

        NavigationRail(
          modifier = Modifier.fillMaxHeight().clip(RectangleShape),
          containerColor = AppTheme.colorScheme.backdrop,
          contentColor = AppTheme.colorScheme.onSurface,
        ) {
          Spacer(Modifier.weight(1f))
          MainDestination.entries.forEach { destination ->
            val selected = selectedDestination == destination
            NavigationRailItem(
              selected = selected,
              onClick = { selectedDestination = destination },
              icon = {
                val icon = if (selected) destination.selectedIcon else destination.icon

                Icon(imageVector = icon, contentDescription = stringResource(destination.label))
              },
              label = null,
              colors = navigationRailItemColors
            )
          }
          Spacer(Modifier.weight(1f))
        }
      }

      Box(modifier = Modifier.weight(1f)) {
        when (selectedDestination) {
          MainDestination.Home -> homeContent(openDrawer)
          MainDestination.Search -> searchContent(goBackToHome)
          MainDestination.Bookmarks -> bookmarksContent(goBackToHome)
          MainDestination.Settings -> settingsContent(goBackToHome)
        }
      }
    }
  } else {
    ModalNavigationDrawer(
      modifier = modifier,
      drawerState = drawerState,
      drawerContent = {
        val drawerItemColors =
          NavigationDrawerItemDefaults.colors(
            selectedIconColor = AppTheme.colorScheme.tintedForeground,
            unselectedIconColor = AppTheme.colorScheme.onSurfaceVariant,
            selectedTextColor = AppTheme.colorScheme.tintedForeground,
            unselectedTextColor = AppTheme.colorScheme.onSurfaceVariant,
            selectedContainerColor = Color.Transparent,
            unselectedContainerColor = Color.Transparent,
          )

        ModalDrawerSheet(
          modifier =
            Modifier.clip(DrawerDefaults.shape)
              .background(
                Brush.horizontalGradient(
                  0.0f to AppTheme.colorScheme.backdrop,
                  0.65f to AppTheme.colorScheme.backdrop,
                  0.75f to AppTheme.colorScheme.backdrop.copy(alpha = 0.85f),
                  0.85f to AppTheme.colorScheme.backdrop.copy(alpha = 0.45f),
                  1.0f to Color.Transparent,
                )
              ),
          drawerContainerColor = Color.Transparent,
          drawerContentColor = AppTheme.colorScheme.onSurface,
        ) {
          Spacer(Modifier.weight(1f))

          MainDestination.entries.forEach { destination ->
            val selected = selectedDestination == destination
            NavigationDrawerItem(
              label = { Text(stringResource(destination.label)) },
              selected = selected,
              modifier =
                Modifier.padding(horizontal = 12.dp)
                  .then(
                    if (selected) {
                      Modifier.background(
                        brush =
                          Brush.horizontalGradient(
                            0.0f to AppTheme.colorScheme.primaryContainer,
                            0.6f to AppTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                            0.85f to Color.Transparent,
                            1.0f to Color.Transparent,
                          ),
                        shape = RoundedCornerShape(12.dp)
                      )
                    } else {
                      Modifier
                    }
                  ),
              onClick = {
                scope.launch {
                  selectedDestination = destination
                  drawerState.close()
                }
              },
              icon = {
                val icon = if (selected) destination.selectedIcon else destination.icon

                Icon(imageVector = icon, contentDescription = stringResource(destination.label))
              },
              colors = drawerItemColors
            )
          }

          Spacer(Modifier.weight(1f))
        }
      }
    ) {
      when (selectedDestination) {
        MainDestination.Home -> homeContent(openDrawer)
        MainDestination.Search -> searchContent(goBackToHome)
        MainDestination.Bookmarks -> bookmarksContent(goBackToHome)
        MainDestination.Settings -> settingsContent(goBackToHome)
      }
    }
  }
}

private enum class MainDestination(
  val icon: ImageVector,
  val selectedIcon: ImageVector,
  val label: StringResource,
) {
  Home(icon = TwineIcons.Home, selectedIcon = TwineIcons.HomeFilled, label = Res.string.screenHome),
  Search(
    icon = TwineIcons.Search,
    selectedIcon = TwineIcons.Search,
    label = Res.string.postsSearchHint
  ),
  Bookmarks(
    icon = TwineIcons.Bookmark,
    selectedIcon = TwineIcons.BookmarkFilled,
    label = Res.string.bookmarks
  ),
  Settings(
    icon = TwineIcons.Settings,
    selectedIcon = TwineIcons.SettingsFilled,
    label = Res.string.settings
  ),
}
