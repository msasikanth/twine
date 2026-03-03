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

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.sasikanth.rss.reader.components.DropdownMenu
import dev.sasikanth.rss.reader.components.DropdownMenuItem
import dev.sasikanth.rss.reader.components.SimpleTopAppBar
import dev.sasikanth.rss.reader.components.SubHeader
import dev.sasikanth.rss.reader.components.ToggleableButtonGroup
import dev.sasikanth.rss.reader.components.ToggleableButtonItem
import dev.sasikanth.rss.reader.components.TranslucentButton
import dev.sasikanth.rss.reader.core.model.local.ResolvedPost
import dev.sasikanth.rss.reader.core.model.local.ThemeVariant
import dev.sasikanth.rss.reader.data.repository.AppThemeMode
import dev.sasikanth.rss.reader.data.repository.HomeViewMode
import dev.sasikanth.rss.reader.home.ui.CompactPostListItem
import dev.sasikanth.rss.reader.home.ui.PostListItem
import dev.sasikanth.rss.reader.home.ui.SimplePostListItem
import dev.sasikanth.rss.reader.resources.icons.ArrowDown
import dev.sasikanth.rss.reader.resources.icons.TwineIcons
import dev.sasikanth.rss.reader.settings.SettingsEvent
import dev.sasikanth.rss.reader.settings.SettingsViewModel
import dev.sasikanth.rss.reader.settings.ui.items.AppIconSelectionSheet
import dev.sasikanth.rss.reader.settings.ui.items.AppIconSettingItem
import dev.sasikanth.rss.reader.settings.ui.items.ThemeVariantSettingItem
import dev.sasikanth.rss.reader.ui.AppTheme
import dev.sasikanth.rss.reader.utils.LocalShowFeedFavIconSetting
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import org.jetbrains.compose.resources.stringResource
import twine.shared.generated.resources.Res
import twine.shared.generated.resources.homeViewMode
import twine.shared.generated.resources.homeViewModeCompact
import twine.shared.generated.resources.homeViewModeDefault
import twine.shared.generated.resources.homeViewModeSimple
import twine.shared.generated.resources.other
import twine.shared.generated.resources.settingsAmoledSubtitle
import twine.shared.generated.resources.settingsAmoledTitle
import twine.shared.generated.resources.settingsAppearanceAndLayout
import twine.shared.generated.resources.settingsHeaderTheme
import twine.shared.generated.resources.settingsShowFeaturedSectionSubtitle
import twine.shared.generated.resources.settingsShowFeaturedSectionTitle
import twine.shared.generated.resources.settingsShowPinnedSourcesSubtitle
import twine.shared.generated.resources.settingsShowPinnedSourcesTitle
import twine.shared.generated.resources.settingsShowUnreadCountSubtitle
import twine.shared.generated.resources.settingsShowUnreadCountTitle
import twine.shared.generated.resources.settingsThemeAuto
import twine.shared.generated.resources.settingsThemeDark
import twine.shared.generated.resources.settingsThemeLight
import twine.shared.generated.resources.showFeedFavIconDesc
import twine.shared.generated.resources.showFeedFavIconTitle
import twine.shared.generated.resources.themeVariantAmber
import twine.shared.generated.resources.themeVariantCoral
import twine.shared.generated.resources.themeVariantDynamic
import twine.shared.generated.resources.themeVariantForest
import twine.shared.generated.resources.themeVariantParchment
import twine.shared.generated.resources.themeVariantRaspberry
import twine.shared.generated.resources.themeVariantSkyline
import twine.shared.generated.resources.themeVariantSolarized

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
    containerColor = AppTheme.colorScheme.backdrop,
    contentColor = Color.Unspecified,
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
        item { SubHeader(text = stringResource(Res.string.settingsHeaderTheme)) }

        item {
          ThemeModeSelection(
            selectedThemeVariant = state.themeVariant,
            appThemeMode = state.appThemeMode,
            onAppThemeModeChanged = { viewModel.dispatch(SettingsEvent.OnAppThemeModeChanged(it)) },
          )
        }

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

        item {
          AnimatedVisibility(
            visible =
              AppTheme.isDark &&
                !state.themeVariant.isDarkModeOnly &&
                !state.themeVariant.isLightModeOnly,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically(),
          ) {
            SettingsSwitchItem(
              title = stringResource(Res.string.settingsAmoledTitle),
              subtitle = stringResource(Res.string.settingsAmoledSubtitle),
              checked = state.useAmoled,
              onValueChanged = { newValue ->
                viewModel.dispatch(SettingsEvent.ToggleAmoled(newValue))
              },
            )
          }
        }

        item { SubHeader(text = stringResource(Res.string.homeViewMode)) }

        item {
          LayoutSelection(
            homeViewMode = state.homeViewMode,
            onLayoutChanged = { viewModel.dispatch(SettingsEvent.ChangeHomeViewMode(it)) },
          )
        }

        item { SubHeader(text = stringResource(Res.string.other)) }

        item {
          SettingsSwitchItem(
            title = stringResource(Res.string.settingsShowFeaturedSectionTitle),
            subtitle = stringResource(Res.string.settingsShowFeaturedSectionSubtitle),
            checked = state.showFeaturedSection,
            onValueChanged = { viewModel.dispatch(SettingsEvent.ToggleShowFeaturedSection(it)) },
          )
        }

        item {
          SettingsSwitchItem(
            title = stringResource(Res.string.settingsShowPinnedSourcesTitle),
            subtitle = stringResource(Res.string.settingsShowPinnedSourcesSubtitle),
            checked = state.showPinnedSources,
            onValueChanged = { viewModel.dispatch(SettingsEvent.ToggleShowPinnedSources(it)) },
          )
        }

        item {
          SettingsSwitchItem(
            title = stringResource(Res.string.settingsShowUnreadCountTitle),
            subtitle = stringResource(Res.string.settingsShowUnreadCountSubtitle),
            checked = state.showUnreadPostsCount,
            onValueChanged = { viewModel.dispatch(SettingsEvent.ToggleShowUnreadPostsCount(it)) },
          )
        }

        item {
          SettingsSwitchItem(
            title = stringResource(Res.string.showFeedFavIconTitle),
            subtitle = stringResource(Res.string.showFeedFavIconDesc),
            checked = state.showFeedFavIcon,
            onValueChanged = { viewModel.dispatch(SettingsEvent.ToggleShowFeedFavIcon(it)) },
          )
        }

        item {
          AppIconSettingItem(
            appIcon = state.appIcon,
            isSubscribed = state.isSubscribed,
            onClick = { viewModel.dispatch(SettingsEvent.AppIconClicked) },
          )
        }
      }
    },
  )
}

@Composable
private fun ThemeModeSelection(
  selectedThemeVariant: ThemeVariant,
  appThemeMode: AppThemeMode,
  onAppThemeModeChanged: (AppThemeMode) -> Unit,
  modifier: Modifier = Modifier,
) {
  Row(
    modifier = modifier.fillMaxWidth().padding(horizontal = 24.dp),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.SpaceBetween,
  ) {
    Text(
      text = selectedThemeVariant.displayName(),
      style = MaterialTheme.typography.headlineMedium,
      color = AppTheme.colorScheme.onSurface,
      fontWeight = FontWeight.SemiBold,
    )

    ThemeModeSelector(appThemeMode = appThemeMode, onAppThemeModeChanged = onAppThemeModeChanged)
  }
}

@Composable
private fun ThemeModeSelector(
  appThemeMode: AppThemeMode,
  onAppThemeModeChanged: (AppThemeMode) -> Unit,
) {
  val density = LocalDensity.current
  var showDropdown by remember { mutableStateOf(false) }
  var buttonHeight by remember { mutableStateOf(Dp.Unspecified) }

  Box {
    TranslucentButton(
      modifier =
        Modifier.onGloballyPositioned { coordinates ->
          buttonHeight = with(density) { coordinates.size.height.toDp() }
        },
      text = appThemeMode.displayName(),
      trailingIcon = TwineIcons.ArrowDown,
      onClick = { showDropdown = true },
    )

    DropdownMenu(
      offset = DpOffset(0.dp, buttonHeight.unaryMinus()),
      expanded = showDropdown,
      onDismissRequest = { showDropdown = false },
      modifier = Modifier.widthIn(min = 96.dp),
    ) {
      AppThemeMode.entries.forEach { mode ->
        DropdownMenuItem(
          text = {
            val textColor =
              if (mode == appThemeMode) AppTheme.colorScheme.primary
              else AppTheme.colorScheme.onSurface
            Text(mode.displayName(), color = textColor)
          },
          onClick = {
            onAppThemeModeChanged(mode)
            showDropdown = false
          },
        )
      }
    }
  }
}

@Composable
private fun LayoutSelection(homeViewMode: HomeViewMode, onLayoutChanged: (HomeViewMode) -> Unit) {
  Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 16.dp)) {
    ToggleableButtonGroup(
      items =
        listOf(
          ToggleableButtonItem(
            label = stringResource(Res.string.homeViewModeDefault),
            isSelected = homeViewMode == HomeViewMode.Default,
            identifier = HomeViewMode.Default,
          ),
          ToggleableButtonItem(
            label = stringResource(Res.string.homeViewModeSimple),
            isSelected = homeViewMode == HomeViewMode.Simple,
            identifier = HomeViewMode.Simple,
          ),
          ToggleableButtonItem(
            label = stringResource(Res.string.homeViewModeCompact),
            isSelected = homeViewMode == HomeViewMode.Compact,
            identifier = HomeViewMode.Compact,
          ),
        ),
      onItemSelected = { onLayoutChanged(it.identifier as HomeViewMode) },
    )

    Spacer(Modifier.requiredHeight(16.dp))

    LayoutPreview(homeViewMode = homeViewMode)
  }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun LayoutPreview(homeViewMode: HomeViewMode) {
  val timeZone = TimeZone.currentSystemDefault()
  val mockPost =
    ResolvedPost(
      id = "1",
      sourceId = "1",
      title = "Phones are going to get weird next week",
      description = "Expect rotating camera rings, robot arms, and magnetic modules at MWC 2026...",
      link = "https://www.theverge.com/tech/885287/mwc-2026-what-to-expect-phones-mobile-weird",
      imageUrl =
        "https://platform.theverge.com/wp-content/uploads/sites/2/2026/02/honor-robot-phone.jpg?quality=90&strip=all&crop=0%2C0.011361054305837%2C100%2C99.977277891388&w=1440",
      audioUrl = null,
      date = LocalDate(2026, 2, 27).atStartOfDayIn(timeZone),
      createdAt = LocalDate(2026, 2, 27).atStartOfDayIn(timeZone),
      commentsLink = null,
      flags = emptySet(),
      feedName = "The Verge",
      feedIcon = "https://theverge.com/icon.png",
      feedHomepageLink =
        "https://platform.theverge.com/wp-content/uploads/sites/2/2025/01/verge-rss-large_80b47e.png?w=150&h=150&crop=1",
      alwaysFetchFullArticle = false,
      showFeedFavIcon = LocalShowFeedFavIconSetting.current,
      feedContentReadingTime = 2,
    )

  Box(
    modifier =
      Modifier.fillMaxWidth()
        .border(
          width = 1.dp,
          color = AppTheme.colorScheme.outlineVariant,
          shape = MaterialTheme.shapes.largeIncreased,
        )
        .padding(vertical = 8.dp)
  ) {
    AnimatedContent(homeViewMode) {
      when (it) {
        HomeViewMode.Default -> {
          PostListItem(
            item = mockPost,
            onClick = {},
            onPostBookmarkClick = {},
            onPostCommentsClick = {},
            onPostSourceClick = {},
            updatePostReadStatus = {},
          )
        }
        HomeViewMode.Simple -> {
          SimplePostListItem(
            item = mockPost,
            onClick = {},
            onPostBookmarkClick = {},
            onPostCommentsClick = {},
            onPostSourceClick = {},
            updatePostReadStatus = {},
          )
        }
        HomeViewMode.Compact -> {
          CompactPostListItem(
            item = mockPost,
            onClick = {},
            onPostBookmarkClick = {},
            onPostCommentsClick = {},
            updatePostReadStatus = {},
          )
        }
      }
    }

    Box(
      modifier =
        Modifier.matchParentSize()
          .clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = null,
            onClick = {
              // no-op
            },
          )
    )
  }
}

@Composable
private fun ThemeVariant.displayName(): String {
  return when (this) {
    ThemeVariant.Dynamic -> stringResource(Res.string.themeVariantDynamic)
    ThemeVariant.Solarized -> stringResource(Res.string.themeVariantSolarized)
    ThemeVariant.Forest -> stringResource(Res.string.themeVariantForest)
    ThemeVariant.Amber -> stringResource(Res.string.themeVariantAmber)
    ThemeVariant.Coral -> stringResource(Res.string.themeVariantCoral)
    ThemeVariant.Raspberry -> stringResource(Res.string.themeVariantRaspberry)
    ThemeVariant.Skyline -> stringResource(Res.string.themeVariantSkyline)
    ThemeVariant.Parchment -> stringResource(Res.string.themeVariantParchment)
  }
}

@Composable
private fun AppThemeMode.displayName(): String {
  return when (this) {
    AppThemeMode.Auto -> stringResource(Res.string.settingsThemeAuto)
    AppThemeMode.Light -> stringResource(Res.string.settingsThemeLight)
    AppThemeMode.Dark -> stringResource(Res.string.settingsThemeDark)
  }
}
