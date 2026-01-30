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

package dev.sasikanth.rss.reader.freshrss.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Snackbar
import androidx.compose.material.SnackbarHost
import androidx.compose.material.SnackbarHostState
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.sasikanth.rss.reader.components.Button
import dev.sasikanth.rss.reader.components.CircularIconButton
import dev.sasikanth.rss.reader.components.TextField
import dev.sasikanth.rss.reader.freshrss.FreshRssLoginError
import dev.sasikanth.rss.reader.freshrss.FreshRssLoginEvent
import dev.sasikanth.rss.reader.freshrss.FreshRssLoginViewModel
import dev.sasikanth.rss.reader.resources.icons.ArrowBack
import dev.sasikanth.rss.reader.resources.icons.TwineIcons
import dev.sasikanth.rss.reader.ui.AppTheme
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.stringResource
import twine.shared.generated.resources.Res
import twine.shared.generated.resources.buttonCancel
import twine.shared.generated.resources.buttonGoBack
import twine.shared.generated.resources.freshRssClearDataDesc
import twine.shared.generated.resources.freshRssClearDataPositive
import twine.shared.generated.resources.freshRssClearDataTitle
import twine.shared.generated.resources.freshRssErrorLoginFailed
import twine.shared.generated.resources.freshRssErrorUnknown
import twine.shared.generated.resources.freshRssLoginButton
import twine.shared.generated.resources.freshRssLoginTitle
import twine.shared.generated.resources.freshRssPassword
import twine.shared.generated.resources.freshRssServerUrl
import twine.shared.generated.resources.freshRssUsername

const val FRESH_RSS_LOGIN_SUCCESS_KEY = "dev.sasikanth.twine.FRESH_RSS_LOGIN_SUCCESS"

@Composable
fun FreshRssLoginScreen(
  viewModel: FreshRssLoginViewModel,
  onLoginSuccess: () -> Unit,
  goBack: () -> Unit,
  modifier: Modifier = Modifier
) {
  val state by viewModel.state.collectAsStateWithLifecycle()
  val snackbarHostState = remember { SnackbarHostState() }
  val (urlFocus, usernameFocus, passwordFocus) = remember { FocusRequester.createRefs() }

  LaunchedEffect(state.loginSuccess, state.error) {
    if (state.loginSuccess) {
      onLoginSuccess()
    }

    if (state.error != null) {
      val errorMessage =
        when (val error = state.error!!) {
          FreshRssLoginError.LoginFailed -> getString(Res.string.freshRssErrorLoginFailed)
          is FreshRssLoginError.Unknown ->
            error.message.ifBlank { getString(Res.string.freshRssErrorUnknown) }
        }
      snackbarHostState.showSnackbar(message = errorMessage)
    }
  }

  if (state.showConfirmationDialog) {
    AlertDialog(
      onDismissRequest = { viewModel.onEvent(FreshRssLoginEvent.OnConfirmationDismissed) },
      title = {
        Text(
          text = stringResource(Res.string.freshRssClearDataTitle),
          color = AppTheme.colorScheme.textEmphasisHigh
        )
      },
      text = {
        Text(
          text = stringResource(Res.string.freshRssClearDataDesc),
          color = AppTheme.colorScheme.textEmphasisMed
        )
      },
      confirmButton = {
        TextButton(
          onClick = { viewModel.onEvent(FreshRssLoginEvent.OnConfirmClearDataClicked) },
          shape = MaterialTheme.shapes.large
        ) {
          Text(
            text = stringResource(Res.string.freshRssClearDataPositive),
            style = MaterialTheme.typography.labelLarge,
            color = AppTheme.colorScheme.tintedForeground
          )
        }
      },
      dismissButton = {
        TextButton(
          onClick = { viewModel.onEvent(FreshRssLoginEvent.OnConfirmationDismissed) },
          shape = MaterialTheme.shapes.large
        ) {
          Text(
            text = stringResource(Res.string.buttonCancel),
            style = MaterialTheme.typography.labelLarge,
            color = AppTheme.colorScheme.textEmphasisMed
          )
        }
      },
      containerColor = AppTheme.colorScheme.tintedSurface,
      titleContentColor = AppTheme.colorScheme.onSurface,
      textContentColor = AppTheme.colorScheme.onSurface,
    )
  }

  Scaffold(
    modifier = modifier,
    topBar = {
      Box {
        CenterAlignedTopAppBar(
          title = {
            Text(
              text = stringResource(Res.string.freshRssLoginTitle),
              color = AppTheme.colorScheme.onSurface,
              style = MaterialTheme.typography.titleMedium,
            )
          },
          navigationIcon = {
            CircularIconButton(
              modifier = Modifier.padding(start = 12.dp),
              icon = TwineIcons.ArrowBack,
              label = stringResource(Res.string.buttonGoBack),
              onClick = goBack
            )
          },
          colors =
            TopAppBarDefaults.topAppBarColors(
              containerColor = AppTheme.colorScheme.surface,
              navigationIconContentColor = AppTheme.colorScheme.onSurface,
              titleContentColor = AppTheme.colorScheme.onSurface,
              actionIconContentColor = AppTheme.colorScheme.onSurface
            ),
        )

        HorizontalDivider(
          modifier = Modifier.align(Alignment.BottomCenter),
          color = AppTheme.colorScheme.outlineVariant
        )
      }
    },
    snackbarHost = {
      SnackbarHost(hostState = snackbarHostState) { snackbarData ->
        Snackbar(
          modifier = Modifier.padding(12.dp),
          content = {
            Text(text = snackbarData.message, maxLines = 4, overflow = TextOverflow.Ellipsis)
          },
          action = null,
          actionOnNewLine = false,
          shape = SnackbarDefaults.shape,
          backgroundColor = AppTheme.colorScheme.surfaceContainerLow,
          contentColor = AppTheme.colorScheme.onSurface,
          elevation = 0.dp
        )
      }
    },
    bottomBar = {
      Button(
        modifier =
          Modifier.background(AppTheme.colorScheme.backdrop)
            .padding(horizontal = 24.dp, vertical = 8.dp)
            .navigationBarsPadding()
            .imePadding()
            .fillMaxWidth()
            .requiredHeight(56.dp),
        colors =
          ButtonDefaults.buttonColors(
            containerColor = AppTheme.colorScheme.inverseSurface,
            contentColor = AppTheme.colorScheme.inverseOnSurface,
          ),
        enabled =
          !state.isLoading &&
            state.url.isNotBlank() &&
            state.username.isNotBlank() &&
            state.password.isNotBlank(),
        shape = MaterialTheme.shapes.extraLarge,
        onClick = { viewModel.onEvent(FreshRssLoginEvent.OnLoginClicked) }
      ) {
        if (state.isLoading) {
          CircularProgressIndicator(
            color = AppTheme.colorScheme.tintedForeground,
            modifier = Modifier.requiredSize(24.dp),
            strokeWidth = 2.dp
          )
        } else {
          Text(
            text = stringResource(Res.string.freshRssLoginButton).uppercase(),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Medium,
          )
        }
      }
    },
    containerColor = AppTheme.colorScheme.backdrop,
  ) { padding ->
    LazyColumn(
      modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 24.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
      item { Spacer(Modifier.requiredHeight(8.dp)) }

      item {
        TextField(
          value = state.url,
          onValueChange = { viewModel.onEvent(FreshRssLoginEvent.OnUrlChanged(it)) },
          hint = stringResource(Res.string.freshRssServerUrl),
          modifier =
            Modifier.fillMaxWidth().focusRequester(urlFocus).focusProperties {
              next = usernameFocus
            },
          enabled = !state.isLoading,
          keyboardOptions =
            KeyboardOptions(
              autoCorrectEnabled = false,
              keyboardType = KeyboardType.Uri,
              imeAction = ImeAction.Next
            )
        )
      }

      item {
        TextField(
          value = state.username,
          onValueChange = { viewModel.onEvent(FreshRssLoginEvent.OnUsernameChanged(it)) },
          hint = stringResource(Res.string.freshRssUsername),
          modifier =
            Modifier.fillMaxWidth().focusRequester(usernameFocus).focusProperties {
              previous = urlFocus
              next = passwordFocus
            },
          enabled = !state.isLoading,
          keyboardOptions =
            KeyboardOptions(
              autoCorrectEnabled = false,
              keyboardType = KeyboardType.Email,
              imeAction = ImeAction.Next
            )
        )
      }

      item {
        TextField(
          value = state.password,
          onValueChange = { viewModel.onEvent(FreshRssLoginEvent.OnPasswordChanged(it)) },
          hint = stringResource(Res.string.freshRssPassword),
          modifier =
            Modifier.fillMaxWidth().focusRequester(passwordFocus).focusProperties {
              previous = usernameFocus
            },
          enabled = !state.isLoading,
          visualTransformation = PasswordVisualTransformation(),
          keyboardOptions =
            KeyboardOptions(
              autoCorrectEnabled = false,
              keyboardType = KeyboardType.Password,
              imeAction = ImeAction.Done
            ),
          keyboardActions =
            KeyboardActions(onDone = { viewModel.onEvent(FreshRssLoginEvent.OnLoginClicked) })
        )
      }
    }
  }
}
