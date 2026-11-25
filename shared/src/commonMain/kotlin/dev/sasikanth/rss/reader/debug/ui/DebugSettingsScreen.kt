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
package dev.sasikanth.rss.reader.debug.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.sasikanth.rss.reader.app.AppInfo
import dev.sasikanth.rss.reader.app.AppPlatform
import dev.sasikanth.rss.reader.billing.SubscriptionResult
import dev.sasikanth.rss.reader.components.DropdownMenu
import dev.sasikanth.rss.reader.components.DropdownMenuItem
import dev.sasikanth.rss.reader.components.SimpleTopAppBar
import dev.sasikanth.rss.reader.components.SubHeader
import dev.sasikanth.rss.reader.components.ToggleableButtonGroup
import dev.sasikanth.rss.reader.components.ToggleableButtonItem
import dev.sasikanth.rss.reader.components.TranslucentButton
import dev.sasikanth.rss.reader.data.repository.DatabaseStats
import dev.sasikanth.rss.reader.data.repository.DebugLogSeverity
import dev.sasikanth.rss.reader.data.repository.DebugSubscriptionOverride
import dev.sasikanth.rss.reader.data.sync.SyncState
import dev.sasikanth.rss.reader.debug.DebugSettingsEffect
import dev.sasikanth.rss.reader.debug.DebugSettingsEvent
import dev.sasikanth.rss.reader.debug.DebugSettingsState
import dev.sasikanth.rss.reader.debug.DebugSettingsViewModel
import dev.sasikanth.rss.reader.resources.icons.ArrowDown
import dev.sasikanth.rss.reader.resources.icons.TwineIcons
import dev.sasikanth.rss.reader.settings.ui.SettingsSwitchItem
import dev.sasikanth.rss.reader.settings.ui.items.SettingItem
import dev.sasikanth.rss.reader.ui.AppTheme
import dev.sasikanth.rss.reader.utils.LocalInAppRating
import dev.sasikanth.rss.reader.utils.iosBottomSafeAreaPadding
import dev.sasikanth.rss.reader.utils.restrictContentWidth
import dev.sasikanth.rss.reader.utils.toClipEntry
import kotlin.math.roundToInt
import kotlin.time.Instant
import kotlinx.coroutines.launch
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

@Composable
internal fun DebugSettingsScreen(
  viewModel: DebugSettingsViewModel,
  goBack: () -> Unit,
  openPaywall: () -> Unit,
  openChangelog: () -> Unit,
  navigateToOnboarding: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val state by viewModel.state.collectAsStateWithLifecycle()
  val snackbarHostState = remember { SnackbarHostState() }
  val inAppRating = LocalInAppRating.current
  val clipboard = LocalClipboard.current
  val coroutineScope = rememberCoroutineScope()

  LaunchedEffect(viewModel) {
    viewModel.effects.collect { effect ->
      when (effect) {
        is DebugSettingsEffect.ShowSnackbar -> {
          snackbarHostState.currentSnackbarData?.dismiss()
          snackbarHostState.showSnackbar(message = effect.message)
        }
        DebugSettingsEffect.NavigateToOnboarding -> navigateToOnboarding()
      }
    }
  }

  DebugSettingsContent(
    state = state,
    dispatch = viewModel::dispatch,
    snackbarHostState = snackbarHostState,
    goBack = goBack,
    openPaywall = openPaywall,
    openChangelog = openChangelog,
    onTriggerInAppRating = {
      coroutineScope.launch {
        inAppRating.request(ignoreCriteria = true)
        snackbarHostState.currentSnackbarData?.dismiss()
        snackbarHostState.showSnackbar(message = "In-app rating requested")
      }
    },
    onCopyDiagnostics = {
      coroutineScope.launch {
        clipboard.setClipEntry(viewModel.buildDiagnosticsSummary().toClipEntry())
        snackbarHostState.currentSnackbarData?.dismiss()
        snackbarHostState.showSnackbar(message = "Diagnostics copied to clipboard")
      }
    },
    modifier = modifier,
  )
}

@Composable
private fun DebugSettingsContent(
  state: DebugSettingsState,
  dispatch: (DebugSettingsEvent) -> Unit,
  snackbarHostState: SnackbarHostState,
  goBack: () -> Unit,
  openPaywall: () -> Unit,
  openChangelog: () -> Unit,
  onTriggerInAppRating: () -> Unit,
  onCopyDiagnostics: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val layoutDirection = LocalLayoutDirection.current

  Scaffold(
    modifier = modifier,
    topBar = { SimpleTopAppBar(title = "Developer Settings", onBackClick = goBack) },
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
          containerColor = AppTheme.colorScheme.inverseSurface,
          contentColor = AppTheme.colorScheme.inverseOnSurface,
        )
      }
    },
    containerColor = AppTheme.colorScheme.backdrop,
    contentColor = Color.Unspecified,
    content = { padding ->
      LazyColumn(
        modifier = Modifier.restrictContentWidth().iosBottomSafeAreaPadding(),
        contentPadding =
          PaddingValues(
            start = padding.calculateStartPadding(layoutDirection),
            top = padding.calculateTopPadding() + 8.dp,
            end = padding.calculateEndPadding(layoutDirection),
            bottom = padding.calculateBottomPadding() + 80.dp,
          ),
      ) {
        item { SubHeader(text = "Performance & Overlays") }

        item {
          SettingsSwitchItem(
            title = "Show FPS overlay",
            subtitle = "Live frame rate and frame time pill across the app",
            checked = state.showFpsOverlay,
            onValueChanged = { dispatch(DebugSettingsEvent.ToggleFpsOverlay(it)) },
          )
        }

        item {
          SettingsSwitchItem(
            title = "Show window size overlay",
            subtitle = "Display current WindowSizeClass and window dp dimensions",
            checked = state.showWindowSizeOverlay,
            onValueChanged = { dispatch(DebugSettingsEvent.ToggleWindowSizeOverlay(it)) },
          )
        }

        item { SubHeader(text = "Billing & Subscription") }

        item {
          SubscriptionOverrideSettingItem(
            subscriptionOverride = state.subscriptionOverride,
            effectiveSubscriptionResult = state.effectiveSubscriptionResult,
            onOverrideChanged = { dispatch(DebugSettingsEvent.UpdateSubscriptionOverride(it)) },
          )
        }

        item {
          SettingItem(
            title = "Open paywall",
            subtitle = "Preview the Twine Pro paywall screen",
            onClick = openPaywall,
          )
        }

        item { SubHeader(text = "Caches") }

        item {
          SettingItem(
            title = "Clear image & favicon caches",
            subtitle = "Evicts Coil memory and disk caches",
            onClick = { dispatch(DebugSettingsEvent.ClearImageAndFavIconCaches) },
          )
        }

        item {
          SettingItem(
            title = "Clear seed color cache",
            subtitle = "Clears LRU cache and resets stored post seed colors",
            onClick = { dispatch(DebugSettingsEvent.ClearSeedColorCache) },
          )
        }

        item {
          val cachedCount = state.databaseStats?.cachedArticlesCount?.toString() ?: "-"
          SettingItem(
            title = "Clear cached article content",
            subtitle =
              "Removes downloaded full articles ($cachedCount cached) while keeping feed summaries",
            onClick = { dispatch(DebugSettingsEvent.ClearCachedArticleContent) },
          )
        }

        item {
          SettingItem(
            title = "Clear discovery feeds cache",
            subtitle = "Forces Discovery to re-fetch from remote",
            onClick = { dispatch(DebugSettingsEvent.ClearDiscoveryCache) },
          )
        }

        item { SubHeader(text = "Sync & Widgets") }

        item {
          val syncStatusLabel =
            when (val syncState = state.syncState) {
              SyncState.Idle -> "Idle"
              is SyncState.InProgress -> "In progress (${(syncState.progress * 100).roundToInt()}%)"
              SyncState.Complete -> "Complete"
              is SyncState.Error -> "Error: ${syncState.exception.message ?: "unknown"}"
            }
          SettingItem(
            title = "Force full sync",
            subtitle =
              "Resets feed refresh intervals & errors, then pulls all feeds · Status: $syncStatusLabel",
            onClick = { dispatch(DebugSettingsEvent.ForceFullSync) },
          )
        }

        item {
          SettingItem(
            title = "Reset refresh policy",
            subtitle = "Clears last synced and last refreshed timestamps",
            onClick = { dispatch(DebugSettingsEvent.ResetRefreshPolicy) },
          )
        }

        item {
          SettingItem(
            title = "Refresh home screen widgets",
            subtitle = "Triggers an immediate widget update",
            onClick = { dispatch(DebugSettingsEvent.RefreshWidgets) },
          )
        }

        item { SubHeader(text = "Flows & Triggers") }

        item {
          SettingItem(
            title = "Reset onboarding",
            subtitle = "Marks onboarding incomplete and opens onboarding",
            onClick = { dispatch(DebugSettingsEvent.ResetOnboarding) },
          )
        }

        item {
          SettingItem(
            title = "Reset changelog state",
            subtitle = "Clears last seen version so changelog shows on next launch",
            onClick = { dispatch(DebugSettingsEvent.ResetChangelogState) },
          )
        }

        item {
          SettingItem(
            title = "Show changelog sheet",
            subtitle = "Opens the changelog sheet immediately",
            onClick = openChangelog,
          )
        }

        item {
          SettingItem(
            title = "Send test notification",
            subtitle = "Posts a sample notification",
            onClick = { dispatch(DebugSettingsEvent.SendTestNotification) },
          )
        }

        item {
          SettingItem(
            title = "Reset in-app rating state",
            subtitle = "Resets prompt cooldown and sets session eligibility",
            onClick = { dispatch(DebugSettingsEvent.ResetInAppRatingState) },
          )
        }

        item {
          SettingItem(
            title = "Trigger in-app rating",
            subtitle = "Requests platform review prompt bypassing cooldown",
            onClick = onTriggerInAppRating,
          )
        }

        item { SubHeader(text = "Logging & Diagnostics") }

        item {
          LogSeveritySettingItem(
            logSeverity = state.logSeverity,
            onSeverityChanged = { dispatch(DebugSettingsEvent.UpdateLogSeverity(it)) },
          )
        }

        item {
          DiagnosticsCard(
            state = state,
            onRefresh = { dispatch(DebugSettingsEvent.RefreshDiagnostics) },
            onCopy = onCopyDiagnostics,
          )
        }
      }
    },
  )
}

@Composable
private fun SubscriptionOverrideSettingItem(
  subscriptionOverride: DebugSubscriptionOverride,
  effectiveSubscriptionResult: SubscriptionResult?,
  onOverrideChanged: (DebugSubscriptionOverride) -> Unit,
) {
  val effectiveStatus =
    when (effectiveSubscriptionResult) {
      SubscriptionResult.Subscribed -> "Subscribed"
      SubscriptionResult.NotSubscribed -> "Not subscribed"
      is SubscriptionResult.Error -> "Error (${effectiveSubscriptionResult.e.message ?: "unknown"})"
      null -> "Checking…"
    }

  Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 12.dp)) {
    Text(
      text = "Subscription override",
      style = MaterialTheme.typography.titleMedium,
      color = AppTheme.colorScheme.onSurface,
    )
    Text(
      text = "Effective status: $effectiveStatus",
      style = MaterialTheme.typography.bodyMedium,
      color = AppTheme.colorScheme.onSurfaceVariant,
    )

    Spacer(modifier = Modifier.height(12.dp))

    val items =
      remember(subscriptionOverride) {
        DebugSubscriptionOverride.entries.map { override ->
          ToggleableButtonItem(
            label =
              when (override) {
                DebugSubscriptionOverride.Default -> "Default"
                DebugSubscriptionOverride.ForcePro -> "Force Pro"
                DebugSubscriptionOverride.ForceFree -> "Force Free"
              },
            isSelected = override == subscriptionOverride,
            identifier = override,
          )
        }
      }

    ToggleableButtonGroup(
      modifier = Modifier.fillMaxWidth(),
      items = items,
      onItemSelected = { item ->
        val selected = item.identifier as DebugSubscriptionOverride
        onOverrideChanged(selected)
      },
    )
  }
}

@Composable
private fun LogSeveritySettingItem(
  logSeverity: DebugLogSeverity,
  onSeverityChanged: (DebugLogSeverity) -> Unit,
) {
  var showDropdown by remember { mutableStateOf(false) }

  SettingItem(
    title = "Minimum log severity",
    subtitle = "Applies immediately to runtime Kermit logging",
    onClick = { showDropdown = !showDropdown },
    action = {
      Box {
        TranslucentButton(
          text = logSeverity.name,
          trailingIcon = TwineIcons.ArrowDown,
          onClick = { showDropdown = true },
        )

        DropdownMenu(expanded = showDropdown, onDismissRequest = { showDropdown = false }) {
          DebugLogSeverity.entries.forEach { severity ->
            DropdownMenuItem(
              text = severity.name,
              selected = severity == logSeverity,
              onClick = {
                onSeverityChanged(severity)
                showDropdown = false
              },
            )
          }
        }
      }
    },
  )
}

@Composable
private fun DiagnosticsCard(state: DebugSettingsState, onRefresh: () -> Unit, onCopy: () -> Unit) {
  val db = state.databaseStats
  val shape = RoundedCornerShape(16.dp)

  Column(
    modifier =
      Modifier.fillMaxWidth()
        .padding(horizontal = 24.dp, vertical = 12.dp)
        .clip(shape)
        .background(AppTheme.colorScheme.surfaceContainerLow, shape)
        .padding(16.dp),
    verticalArrangement = Arrangement.spacedBy(6.dp),
  ) {
    Text(
      text = "App & Database Diagnostics",
      style = MaterialTheme.typography.titleMedium,
      color = AppTheme.colorScheme.onSurface,
    )

    Spacer(modifier = Modifier.height(4.dp))

    DiagnosticsRow(
      label = "Version",
      value = "${state.appInfo.versionName} (${state.appInfo.versionCode})",
    )
    DiagnosticsRow(
      label = "Platform / Flavor",
      value = "${state.appInfo.platform} · ${if (state.appInfo.isFoss) "FOSS" else "Full"} · Debug",
    )
    DiagnosticsRow(label = "Sync account", value = state.signedInService?.name ?: "Local")
    DiagnosticsRow(label = "Feeds", value = db?.feedsCount?.toString() ?: "-")
    DiagnosticsRow(label = "Groups", value = db?.groupsCount?.toString() ?: "-")
    DiagnosticsRow(label = "Posts", value = db?.postsCount?.toString() ?: "-")
    DiagnosticsRow(label = "Unread posts", value = db?.unreadPostsCount?.toString() ?: "-")
    DiagnosticsRow(label = "Bookmarks", value = db?.bookmarksCount?.toString() ?: "-")
    DiagnosticsRow(label = "Cached articles", value = db?.cachedArticlesCount?.toString() ?: "-")
    DiagnosticsRow(label = "Last synced", value = formatInstant(state.lastSyncedAt))
    DiagnosticsRow(label = "Last refreshed", value = formatInstant(state.lastRefreshedAt))
    DiagnosticsRow(label = "Sessions", value = state.userSessionCount.toString())
    DiagnosticsRow(label = "Install date", value = formatInstant(state.installDate))
    DiagnosticsRow(label = "Last review prompt", value = formatInstant(state.lastReviewPromptDate))

    Text(
      text = "Cache path",
      style = MaterialTheme.typography.labelMedium,
      color = AppTheme.colorScheme.onSurfaceVariant,
      modifier = Modifier.padding(top = 4.dp),
    )
    Text(
      text = state.appInfo.cachePath(),
      style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
      color = AppTheme.colorScheme.onSurface,
    )

    Spacer(modifier = Modifier.height(8.dp))

    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
      TranslucentButton(text = "Refresh", onClick = onRefresh)
      TranslucentButton(text = "Copy diagnostics", onClick = onCopy)
    }
  }
}

@Composable
private fun DiagnosticsRow(label: String, value: String) {
  Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
    Text(
      text = label,
      style = MaterialTheme.typography.bodyMedium,
      color = AppTheme.colorScheme.onSurfaceVariant,
    )
    Text(
      text = value,
      style = MaterialTheme.typography.bodyMedium.copy(fontFamily = FontFamily.Monospace),
      color = AppTheme.colorScheme.onSurface,
      maxLines = 1,
      overflow = TextOverflow.Ellipsis,
      modifier = Modifier.padding(start = 16.dp),
    )
  }
}

private fun formatInstant(instant: Instant?): String {
  return instant?.toLocalDateTime(TimeZone.currentSystemDefault())?.toString() ?: "Never"
}

@Preview(locale = "en")
@Composable
private fun DebugSettingsPreview() {
  AppTheme {
    DebugSettingsContent(
      state =
        DebugSettingsState.default(
            appInfo =
              AppInfo(
                versionCode = 123,
                versionName = "3.10.0",
                isDebugBuild = true,
                isFoss = false,
                isGoogleDriveSupported = true,
                cachePath = { "/data/user/0/dev.sasikanth.rss.reader.debug/cache" },
                platform = AppPlatform.Android,
              )
          )
          .copy(
            effectiveSubscriptionResult = SubscriptionResult.Subscribed,
            databaseStats =
              DatabaseStats(
                feedsCount = 42,
                groupsCount = 5,
                postsCount = 1280,
                unreadPostsCount = 194,
                bookmarksCount = 27,
                cachedArticlesCount = 86,
              ),
          ),
      dispatch = {},
      snackbarHostState = remember { SnackbarHostState() },
      goBack = {},
      openPaywall = {},
      openChangelog = {},
      onTriggerInAppRating = {},
      onCopyDiagnostics = {},
    )
  }
}
