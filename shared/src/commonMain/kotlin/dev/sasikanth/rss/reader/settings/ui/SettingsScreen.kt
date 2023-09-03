package dev.sasikanth.rss.reader.settings.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.dp
import dev.sasikanth.rss.reader.resources.strings.LocalStrings
import dev.sasikanth.rss.reader.settings.SettingsEvent
import dev.sasikanth.rss.reader.settings.SettingsPresenter
import dev.sasikanth.rss.reader.ui.AppTheme

@Composable
fun SettingsScreen(settingsPresenter: SettingsPresenter, modifier: Modifier = Modifier) {
  val state by settingsPresenter.state.collectAsState()
  val layoutDirection = LocalLayoutDirection.current

  Scaffold(
    modifier = modifier,
    topBar = {
      Box {
        CenterAlignedTopAppBar(
          title = { Text(LocalStrings.current.settings) },
          navigationIcon = {
            IconButton(onClick = { settingsPresenter.dispatch(SettingsEvent.BackClicked) }) {
              Icon(Icons.Rounded.ArrowBack, contentDescription = null)
            }
          },
          colors =
            TopAppBarDefaults.topAppBarColors(
              containerColor = AppTheme.colorScheme.surface,
              navigationIconContentColor = AppTheme.colorScheme.onSurface,
              titleContentColor = AppTheme.colorScheme.onSurface,
              actionIconContentColor = AppTheme.colorScheme.onSurface
            ),
        )

        Divider(
          modifier = Modifier.fillMaxWidth().align(Alignment.BottomStart),
          color = AppTheme.colorScheme.surfaceContainer
        )
      }
    },
    content = {
      Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
          contentPadding =
            PaddingValues(
              start = it.calculateStartPadding(layoutDirection),
              end = it.calculateEndPadding(layoutDirection),
              bottom = it.calculateBottomPadding() + 64.dp
            ),
          modifier = Modifier.padding(top = it.calculateTopPadding())
        ) {}
      }
    },
    containerColor = Color.Unspecified,
    contentColor = Color.Unspecified
  )
}
