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

package dev.sasikanth.rss.reader.accountselection.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.sasikanth.rss.reader.accountselection.AccountSelectionEffect
import dev.sasikanth.rss.reader.accountselection.AccountSelectionEvent
import dev.sasikanth.rss.reader.accountselection.AccountSelectionViewModel
import dev.sasikanth.rss.reader.components.SubHeader
import dev.sasikanth.rss.reader.core.model.local.ServiceType
import dev.sasikanth.rss.reader.platform.LocalLinkHandler
import dev.sasikanth.rss.reader.resources.icons.Dropbox
import dev.sasikanth.rss.reader.resources.icons.Freshrss
import dev.sasikanth.rss.reader.resources.icons.Home
import dev.sasikanth.rss.reader.resources.icons.Miniflux
import dev.sasikanth.rss.reader.resources.icons.StarShine
import dev.sasikanth.rss.reader.resources.icons.TwineIcons
import dev.sasikanth.rss.reader.ui.AppTheme
import org.jetbrains.compose.resources.stringResource
import twine.shared.generated.resources.Res
import twine.shared.generated.resources.accountSelectionCloudSync
import twine.shared.generated.resources.accountSelectionLocal
import twine.shared.generated.resources.accountSelectionLocalAccount
import twine.shared.generated.resources.accountSelectionSubtitle
import twine.shared.generated.resources.accountSelectionTitle
import twine.shared.generated.resources.settingsSyncDropbox
import twine.shared.generated.resources.settingsSyncFreshRSS
import twine.shared.generated.resources.settingsSyncMiniflux

@Composable
internal fun AccountSelectionScreen(
  viewModel: AccountSelectionViewModel,
  onNavigateToHome: () -> Unit,
  onNavigateToDiscovery: () -> Unit,
  openPaywall: () -> Unit,
  openFreshRssLogin: () -> Unit,
  openMinifluxLogin: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val state by viewModel.state.collectAsStateWithLifecycle()
  val linkHandler = LocalLinkHandler.current

  LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
    viewModel.dispatch(AccountSelectionEvent.Refresh)
  }

  LaunchedEffect(Unit) {
    viewModel.effects.collect { effect ->
      when (effect) {
        AccountSelectionEffect.NavigateToDiscovery -> onNavigateToDiscovery()
        AccountSelectionEffect.OpenPaywall -> openPaywall()
        AccountSelectionEffect.OpenFreshRssLogin -> openFreshRssLogin()
        AccountSelectionEffect.OpenMinifluxLogin -> openMinifluxLogin()
      }
    }
  }

  LaunchedEffect(state.authUrlToOpen) {
    state.authUrlToOpen?.let { url ->
      linkHandler.openLink(url, useInAppBrowser = true)
      viewModel.dispatch(AccountSelectionEvent.ClearAuthUrl)
    }
  }

  LaunchedEffect(state.user) {
    if (state.user != null) {
      onNavigateToHome()
    }
  }

  Scaffold(
    modifier = modifier,
    containerColor = AppTheme.colorScheme.backdrop,
    contentColor = Color.Unspecified,
    topBar = {
      Column(modifier = Modifier.fillMaxWidth().statusBarsPadding().padding(vertical = 16.dp)) {
        Text(
          modifier = Modifier.padding(horizontal = 24.dp),
          text = stringResource(Res.string.accountSelectionTitle),
          style = MaterialTheme.typography.headlineSmall,
          color = AppTheme.colorScheme.onSurface,
        )

        Spacer(Modifier.size(8.dp))

        Text(
          modifier = Modifier.padding(horizontal = 24.dp),
          text = stringResource(Res.string.accountSelectionSubtitle),
          style = MaterialTheme.typography.bodyMedium,
          color = AppTheme.colorScheme.onSurfaceVariant,
        )
      }
    },
  ) { padding ->
    LazyColumn(modifier = Modifier.fillMaxSize().padding(padding)) {
      item { SubHeader(text = stringResource(Res.string.accountSelectionLocal)) }

      item {
        AccountItem(
          label = stringResource(Res.string.accountSelectionLocalAccount),
          icon = TwineIcons.Home,
          onClick = { viewModel.dispatch(AccountSelectionEvent.LocalAccountClicked) },
        )
      }

      item {
        HorizontalDivider(
          modifier = Modifier.padding(vertical = 8.dp),
          color = AppTheme.colorScheme.outlineVariant,
        )
      }

      item { SubHeader(text = stringResource(Res.string.accountSelectionCloudSync)) }

      items(viewModel.availableProviders.toList()) { provider ->
        val label =
          when (provider.cloudService) {
            ServiceType.DROPBOX -> stringResource(Res.string.settingsSyncDropbox)
            ServiceType.FRESH_RSS -> stringResource(Res.string.settingsSyncFreshRSS)
            ServiceType.MINIFLUX -> stringResource(Res.string.settingsSyncMiniflux)
          }

        val icon =
          when (provider.cloudService) {
            ServiceType.DROPBOX -> TwineIcons.Dropbox
            ServiceType.FRESH_RSS -> TwineIcons.Freshrss
            ServiceType.MINIFLUX -> TwineIcons.Miniflux
          }

        AccountItem(
          label = label,
          icon = icon,
          isPremium = provider.isPremium,
          isSubscribed = state.isSubscribed,
          onClick = { viewModel.dispatch(AccountSelectionEvent.CloudServiceClicked(provider)) },
        )
      }
    }
  }
}

@Composable
private fun AccountItem(
  label: String,
  icon: androidx.compose.ui.graphics.vector.ImageVector,
  onClick: () -> Unit,
  isPremium: Boolean = false,
  isSubscribed: Boolean = false,
) {
  Box(
    modifier =
      Modifier.clickable(onClick = onClick)
        .fillMaxWidth()
        .padding(horizontal = 24.dp, vertical = 16.dp)
  ) {
    Row(
      horizontalArrangement = Arrangement.spacedBy(16.dp),
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Icon(imageVector = icon, contentDescription = null, tint = AppTheme.colorScheme.onSurface)

      Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
          text = label,
          style = MaterialTheme.typography.titleMedium,
          color = AppTheme.colorScheme.onSurface,
        )

        if (isPremium && !isSubscribed) {
          Spacer(Modifier.width(8.dp))

          Icon(
            modifier = Modifier.size(16.dp),
            imageVector = TwineIcons.StarShine,
            contentDescription = null,
            tint = AppTheme.colorScheme.primary,
          )
        }
      }
    }
  }
}
