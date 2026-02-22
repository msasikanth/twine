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
package dev.sasikanth.rss.reader.settings.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.sasikanth.rss.reader.components.SimpleTopAppBar
import dev.sasikanth.rss.reader.components.SubHeader
import dev.sasikanth.rss.reader.components.ToggleableButtonGroup
import dev.sasikanth.rss.reader.components.ToggleableButtonItem
import dev.sasikanth.rss.reader.data.repository.AppThemeMode
import dev.sasikanth.rss.reader.settings.SettingsEvent
import dev.sasikanth.rss.reader.settings.SettingsViewModel
import dev.sasikanth.rss.reader.settings.ui.items.AmoledSettingItem
import dev.sasikanth.rss.reader.settings.ui.items.AppIconSelectionSheet
import dev.sasikanth.rss.reader.settings.ui.items.AppIconSettingItem
import dev.sasikanth.rss.reader.settings.ui.items.HomeLayoutSelector
import dev.sasikanth.rss.reader.settings.ui.items.ThemeVariantSettingItem
import dev.sasikanth.rss.reader.settings.ui.items.TwinePremiumBanner
import dev.sasikanth.rss.reader.ui.AppTheme
import org.jetbrains.compose.resources.stringResource
import twine.shared.generated.resources.Res
import twine.shared.generated.resources.homeViewMode
import twine.shared.generated.resources.settingsAppearanceAndLayout
import twine.shared.generated.resources.settingsHeaderTheme
import twine.shared.generated.resources.settingsThemeAuto
import twine.shared.generated.resources.settingsThemeDark
import twine.shared.generated.resources.settingsThemeLight

@Composable
internal fun SettingsAppearanceScreen(
  viewModel: SettingsViewModel,
  goBack: () -> Unit,
  openPaywall: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val state by viewModel.state.collectAsStateWithLifecycle()
  val layoutDirection = LocalLayoutDirection.current

  LaunchedEffect(state.openPaywall) {
    if (state.openPaywall) {
      openPaywall()
      viewModel.dispatch(SettingsEvent.MarkOpenPaywallAsDone)
    }
  }

  LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
    viewModel.dispatch(SettingsEvent.LoadSubscriptionStatus)
  }

  Scaffold(
    modifier = modifier,
    topBar = {
      SimpleTopAppBar(
        title = stringResource(Res.string.settingsAppearanceAndLayout),
        onBackClick = goBack,
      )
    },
    content = { padding ->
      if (state.showAppIconSelectionSheet) {
        AppIconSelectionSheet(
          currentAppIcon = state.appIcon,
          onAppIconChange = {
            viewModel.dispatch(SettingsEvent.OnAppIconChanged(it))
            viewModel.dispatch(SettingsEvent.CloseAppIconSelectionSheet)
          },
          onDismiss = { viewModel.dispatch(SettingsEvent.CloseAppIconSelectionSheet) },
        )
      }

      LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding =
          PaddingValues(
            start = padding.calculateStartPadding(layoutDirection) + settingsItemHorizontalPadding,
            top = padding.calculateTopPadding() + 8.dp,
            end = padding.calculateEndPadding(layoutDirection) + settingsItemHorizontalPadding,
            bottom = padding.calculateBottomPadding() + 80.dp,
          ),
      ) {
        // region Twine Premium banner
        item {
          AnimatedVisibility(
            visible = !state.appInfo.isFoss && state.subscriptionResult != null,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically(),
          ) {
            Column {
              TwinePremiumBanner(
                modifier = Modifier.animateItem(),
                subscriptionResult = state.subscriptionResult,
                onClick = { openPaywall() },
              )

              SettingsDivider()
            }
          }
        }
        // endregion

        item { SubHeader(text = stringResource(Res.string.homeViewMode)) }

        item {
          HomeLayoutSelector(
            homeViewMode = state.homeViewMode,
            onClick = { viewModel.dispatch(SettingsEvent.ChangeHomeViewMode(it)) },
          )
        }

        item { SettingsDivider(24.dp) }

        item { SubHeader(text = stringResource(Res.string.settingsHeaderTheme)) }

        item {
          ThemeVariantSettingItem(
            selectedThemeVariant = state.themeVariant,
            isSubscribed = state.isSubscribed,
            useDarkTheme = AppTheme.isDark,
            onThemeVariantChanged = {
              if (it.isPremium && !state.isSubscribed) {
                openPaywall()
              } else {
                viewModel.dispatch(SettingsEvent.OnThemeVariantChanged(it))
              }
            },
          )
        }

        item { SettingsDivider(24.dp) }

        item {
          AnimatedVisibility(
            visible = !state.themeVariant.isDarkModeOnly && !state.themeVariant.isLightModeOnly,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically(),
          ) {
            val appThemeMode = state.appThemeMode
            ToggleableButtonGroup(
              modifier =
                Modifier.fillMaxWidth()
                  .padding(horizontal = 24.dp)
                  .padding(top = 24.dp, bottom = 24.dp),
              items =
                listOf(
                  ToggleableButtonItem(
                    label = stringResource(Res.string.settingsThemeAuto),
                    isSelected = appThemeMode == AppThemeMode.Auto,
                    identifier = AppThemeMode.Auto,
                  ),
                  ToggleableButtonItem(
                    label = stringResource(Res.string.settingsThemeLight),
                    isSelected = appThemeMode == AppThemeMode.Light,
                    identifier = AppThemeMode.Light,
                  ),
                  ToggleableButtonItem(
                    label = stringResource(Res.string.settingsThemeDark),
                    isSelected = appThemeMode == AppThemeMode.Dark,
                    identifier = AppThemeMode.Dark,
                  ),
                ),
              onItemSelected = {
                viewModel.dispatch(
                  SettingsEvent.OnAppThemeModeChanged(it.identifier as AppThemeMode)
                )
              },
            )
          }
        }

        item {
          AnimatedVisibility(
            visible =
              AppTheme.isDark &&
                !state.themeVariant.isDarkModeOnly &&
                !state.themeVariant.isLightModeOnly,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically(),
          ) {
            AmoledSettingItem(
              useAmoled = state.useAmoled,
              onValueChanged = { newValue ->
                viewModel.dispatch(SettingsEvent.ToggleAmoled(newValue))
              },
            )
          }
        }

        if (state.canSubscribe) {
          item { SettingsDivider(24.dp) }

          item {
            AppIconSettingItem(
              appIcon = state.appIcon,
              isSubscribed = state.isSubscribed,
              onClick = { viewModel.dispatch(SettingsEvent.AppIconClicked) },
            )
          }
        }
      }
    },
    containerColor = AppTheme.colorScheme.backdrop,
    contentColor = Color.Unspecified,
  )
}
