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

package dev.sasikanth.rss.reader.bazqux.ui

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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.sasikanth.rss.reader.bazqux.BazQuxLoginError
import dev.sasikanth.rss.reader.bazqux.BazQuxLoginEvent
import dev.sasikanth.rss.reader.bazqux.BazQuxLoginState
import dev.sasikanth.rss.reader.bazqux.BazQuxLoginViewModel
import dev.sasikanth.rss.reader.components.AlertDialog
import dev.sasikanth.rss.reader.components.Button
import dev.sasikanth.rss.reader.components.SimpleTopAppBar
import dev.sasikanth.rss.reader.components.TextField
import dev.sasikanth.rss.reader.ui.AppTheme
import dev.sasikanth.rss.reader.utils.Constants
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.stringResource
import twine.shared.generated.resources.Res
import twine.shared.generated.resources.bazQuxClearDataDesc
import twine.shared.generated.resources.bazQuxClearDataPositive
import twine.shared.generated.resources.bazQuxClearDataTitle
import twine.shared.generated.resources.bazQuxErrorLoginFailed
import twine.shared.generated.resources.bazQuxErrorUnknown
import twine.shared.generated.resources.bazQuxLoginButton
import twine.shared.generated.resources.bazQuxLoginTitle
import twine.shared.generated.resources.bazQuxPassword
import twine.shared.generated.resources.bazQuxPasswordHint
import twine.shared.generated.resources.bazQuxUsername
import twine.shared.generated.resources.buttonCancel

const val BAZQUX_LOGIN_SUCCESS_KEY = "dev.sasikanth.twine.BAZQUX_LOGIN_SUCCESS"

@Composable
fun BazQuxLoginScreen(
  viewModel: BazQuxLoginViewModel,
  onLoginSuccess: () -> Unit,
  goBack: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val state by viewModel.state.collectAsStateWithLifecycle()

  LaunchedEffect(state.loginSuccess) {
    if (state.loginSuccess) {
      onLoginSuccess()
    }
  }

  BazQuxLoginContent(
    state = state,
    onEvent = viewModel::onEvent,
    goBack = goBack,
    modifier = modifier,
  )
}

@Composable
private fun BazQuxLoginContent(
  state: BazQuxLoginState,
  onEvent: (BazQuxLoginEvent) -> Unit,
  goBack: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val snackbarHostState = remember { SnackbarHostState() }
  val (usernameFocus, passwordFocus) = remember { FocusRequester.createRefs() }

  LaunchedEffect(state.error) {
    val error = state.error
    if (error != null) {
      val errorMessage =
        when (error) {
          BazQuxLoginError.LoginFailed -> getString(Res.string.bazQuxErrorLoginFailed)
          is BazQuxLoginError.Unknown ->
            error.message.ifBlank { getString(Res.string.bazQuxErrorUnknown) }
        }
      snackbarHostState.showSnackbar(message = errorMessage)
    }
  }

  if (state.showConfirmationDialog) {
    AlertDialog(
      title = stringResource(Res.string.bazQuxClearDataTitle),
      text = stringResource(Res.string.bazQuxClearDataDesc),
      confirmText = stringResource(Res.string.bazQuxClearDataPositive),
      dismissText = stringResource(Res.string.buttonCancel),
      onConfirm = { onEvent(BazQuxLoginEvent.OnConfirmClearDataClicked) },
      onDismiss = { onEvent(BazQuxLoginEvent.OnConfirmationDismissed) },
    )
  }

  Scaffold(
    modifier = modifier,
    topBar = {
      SimpleTopAppBar(title = stringResource(Res.string.bazQuxLoginTitle), onBackClick = goBack)
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
          containerColor = AppTheme.colorScheme.inverseSurface,
          contentColor = AppTheme.colorScheme.inverseOnSurface,
        )
      }
    },
    bottomBar = {
      Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        Button(
          modifier =
            Modifier.background(AppTheme.colorScheme.backdrop)
              .padding(horizontal = 24.dp, vertical = 8.dp)
              .navigationBarsPadding()
              .imePadding()
              .widthIn(max = Constants.MAX_CONTENT_WIDTH)
              .fillMaxWidth()
              .requiredHeight(56.dp),
          colors =
            ButtonDefaults.buttonColors(
              containerColor = AppTheme.colorScheme.inverseSurface,
              contentColor = AppTheme.colorScheme.inverseOnSurface,
            ),
          enabled = !state.isLoading && state.username.isNotBlank() && state.password.isNotBlank(),
          shape = MaterialTheme.shapes.extraLarge,
          onClick = { onEvent(BazQuxLoginEvent.OnLoginClicked) },
        ) {
          if (state.isLoading) {
            CircularProgressIndicator(
              color = AppTheme.colorScheme.primary,
              modifier = Modifier.requiredSize(24.dp),
              strokeWidth = 2.dp,
            )
          } else {
            Text(
              text = stringResource(Res.string.bazQuxLoginButton).uppercase(),
              style = MaterialTheme.typography.labelLarge,
              fontWeight = FontWeight.Medium,
            )
          }
        }
      }
    },
    containerColor = AppTheme.colorScheme.backdrop,
    contentColor = Color.Unspecified,
  ) { padding ->
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
      LazyColumn(
        modifier =
          Modifier.widthIn(max = Constants.MAX_CONTENT_WIDTH)
            .fillMaxSize()
            .padding(padding)
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
      ) {
        item { Spacer(Modifier.requiredHeight(8.dp)) }

        item {
          TextField(
            value = state.username,
            onValueChange = { onEvent(BazQuxLoginEvent.OnUsernameChanged(it)) },
            hint = stringResource(Res.string.bazQuxUsername),
            modifier =
              Modifier.fillMaxWidth().focusRequester(usernameFocus).focusProperties {
                next = passwordFocus
              },
            enabled = !state.isLoading,
            keyboardOptions =
              KeyboardOptions(
                autoCorrectEnabled = false,
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Next,
              ),
          )
        }

        item {
          TextField(
            value = state.password,
            onValueChange = { onEvent(BazQuxLoginEvent.OnPasswordChanged(it)) },
            hint = stringResource(Res.string.bazQuxPassword),
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
                imeAction = ImeAction.Done,
              ),
            keyboardActions =
              KeyboardActions(onDone = { onEvent(BazQuxLoginEvent.OnLoginClicked) }),
            supportingText = {
              Text(
                text = stringResource(Res.string.bazQuxPasswordHint),
                style = MaterialTheme.typography.labelMedium,
                color = AppTheme.colorScheme.onSurfaceVariant,
              )
            },
          )
        }
      }
    }
  }
}

@Preview(locale = "en")
@Composable
private fun BazQuxLoginPreview() {
  AppTheme { BazQuxLoginContent(state = BazQuxLoginState.DEFAULT, onEvent = {}, goBack = {}) }
}
