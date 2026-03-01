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

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.sasikanth.rss.reader.components.SimpleTopAppBar
import dev.sasikanth.rss.reader.components.SubHeader
import dev.sasikanth.rss.reader.data.repository.BrowserType
import dev.sasikanth.rss.reader.settings.SettingsEvent
import dev.sasikanth.rss.reader.settings.SettingsViewModel
import dev.sasikanth.rss.reader.settings.ui.items.BlockedWordsSettingItem
import dev.sasikanth.rss.reader.settings.ui.items.MarkAsReadOnSettingItem
import dev.sasikanth.rss.reader.settings.ui.items.OpmlFeedSelectionSheet
import dev.sasikanth.rss.reader.settings.ui.items.PostsDeletionPeriodSettingItem
import dev.sasikanth.rss.reader.ui.AppTheme
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import twine.shared.generated.resources.Res
import twine.shared.generated.resources.settingsBrowserTypeSubtitle
import twine.shared.generated.resources.settingsBrowserTypeTitle
import twine.shared.generated.resources.settingsFeaturesAndBehaviors
import twine.shared.generated.resources.settingsFreeFeedLimitReached
import twine.shared.generated.resources.settingsHeaderBehaviour
import twine.shared.generated.resources.settingsHeaderReadingAndBrowsing
import twine.shared.generated.resources.settingsShowReaderViewSubtitle
import twine.shared.generated.resources.settingsShowReaderViewTitle
import twine.shared.generated.resources.settingsUpgradeToPremium

@Composable
internal fun SettingsBehaviorScreen(
  viewModel: SettingsViewModel,
  goBack: () -> Unit,
  openBlockedWords: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val state by viewModel.state.collectAsStateWithLifecycle()
  val layoutDirection = LocalLayoutDirection.current

  val snackbarHostState = remember { SnackbarHostState() }
  val freeFeedLimitReachedString = stringResource(Res.string.settingsFreeFeedLimitReached)
  val upgradeToPremiumString = stringResource(Res.string.settingsUpgradeToPremium)

  LaunchedEffect(state.showFreeFeedLimitWarning) {
    if (state.showFreeFeedLimitWarning) {
      launch {
        delay(2.seconds)
        viewModel.dispatch(SettingsEvent.MarkFreeFeedLimitWarningAsDone)
      }

      snackbarHostState.showSnackbar(
        message = freeFeedLimitReachedString,
        actionLabel = upgradeToPremiumString,
      )
    }
  }

  Scaffold(
    modifier = modifier,
    topBar = {
      SimpleTopAppBar(
        title = stringResource(Res.string.settingsFeaturesAndBehaviors),
        onBackClick = goBack,
      )
    },
    snackbarHost = {
      SnackbarHost(hostState = snackbarHostState) { snackbarData ->
        Snackbar(
          modifier = Modifier.padding(12.dp),
          content = {
            Text(
              text = snackbarData.visuals.message,
              maxLines = 4,
              overflow = TextOverflow.Ellipsis,
            )
          },
          action = {
            snackbarData.visuals.actionLabel?.let { actionLabel ->
              TextButton(
                onClick = { snackbarHostState.currentSnackbarData?.performAction() },
                colors =
                  ButtonDefaults.textButtonColors(contentColor = AppTheme.colorScheme.primary),
                shape = MaterialTheme.shapes.medium,
              ) {
                Text(text = actionLabel, style = MaterialTheme.typography.labelLarge)
              }
            }
          },
          containerColor = AppTheme.colorScheme.inverseSurface,
          contentColor = AppTheme.colorScheme.inverseOnSurface,
        )
      }
    },
    containerColor = AppTheme.colorScheme.backdrop,
    contentColor = Color.Unspecified,
    content = { padding ->
      if (state.opmlFeedsToSelect != null) {
        OpmlFeedSelectionSheet(
          feeds = state.opmlFeedsToSelect!!,
          onFeedsSelected = { viewModel.dispatch(SettingsEvent.OnOpmlFeedsSelected(it)) },
          onDismiss = { viewModel.dispatch(SettingsEvent.ClearOpmlFeedsToSelect) },
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
        item { SubHeader(text = stringResource(Res.string.settingsHeaderReadingAndBrowsing)) }

        item {
          SettingsSwitchItem(
            title = stringResource(Res.string.settingsShowReaderViewTitle),
            subtitle = stringResource(Res.string.settingsShowReaderViewSubtitle),
            checked = state.showReaderView,
            onValueChanged = { newValue ->
              viewModel.dispatch(SettingsEvent.ToggleShowReaderView(newValue))
            },
          )
        }

        item {
          SettingsSwitchItem(
            title = stringResource(Res.string.settingsBrowserTypeTitle),
            subtitle = stringResource(Res.string.settingsBrowserTypeSubtitle),
            checked = state.browserType == BrowserType.InApp,
            onValueChanged = { useInAppBrowser ->
              val newBrowserType =
                if (useInAppBrowser) {
                  BrowserType.InApp
                } else {
                  BrowserType.Default
                }
              viewModel.dispatch(SettingsEvent.UpdateBrowserType(newBrowserType))
            },
          )
        }

        item { SubHeader(text = stringResource(Res.string.settingsHeaderBehaviour)) }

        item {
          MarkAsReadOnSettingItem(articleMarkAsReadOn = state.markAsReadOn) {
            viewModel.dispatch(SettingsEvent.MarkAsReadOnChanged(it))
          }
        }

        item {
          PostsDeletionPeriodSettingItem(
            postsDeletionPeriod = state.postsDeletionPeriod,
            onValueChanged = { newValue ->
              viewModel.dispatch(SettingsEvent.PostsDeletionPeriodChanged(newValue))
            },
          )
        }

        item {
          BlockedWordsSettingItem(blockedWordsCount = state.blockedWordsCount) {
            openBlockedWords()
          }
        }
      }
    },
  )
}
