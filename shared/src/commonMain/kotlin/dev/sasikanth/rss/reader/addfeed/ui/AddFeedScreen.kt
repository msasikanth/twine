/*
 * Copyright 2024 Sasikanth Miriyampalli
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

package dev.sasikanth.rss.reader.addfeed.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Snackbar
import androidx.compose.material.SnackbarHost
import androidx.compose.material.SnackbarHostState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowRight
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.twotone.FolderOpen
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.InputChip
import androidx.compose.material3.InputChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import dev.sasikanth.rss.reader.addfeed.AddFeedEffect
import dev.sasikanth.rss.reader.addfeed.AddFeedErrorType
import dev.sasikanth.rss.reader.addfeed.AddFeedEvent
import dev.sasikanth.rss.reader.addfeed.AddFeedPresenter
import dev.sasikanth.rss.reader.addfeed.FeedFetchingState
import dev.sasikanth.rss.reader.components.Button
import dev.sasikanth.rss.reader.components.OutlinedButton
import dev.sasikanth.rss.reader.resources.icons.ArrowBack
import dev.sasikanth.rss.reader.resources.icons.TwineIcons
import dev.sasikanth.rss.reader.resources.strings.LocalStrings
import dev.sasikanth.rss.reader.resources.strings.TwineStrings
import dev.sasikanth.rss.reader.ui.AppTheme
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalComposeUiApi::class, ExperimentalLayoutApi::class)
@Composable
fun AddFeedScreen(presenter: AddFeedPresenter, modifier: Modifier = Modifier) {
  val state by presenter.state.collectAsState()
  val strings = LocalStrings.current
  val snackbarHostState = remember { SnackbarHostState() }
  val (feedLinkFocus, feedTitleFocus) = remember { FocusRequester.createRefs() }
  val focusManager = LocalFocusManager.current

  LaunchedEffect(Unit) {
    feedLinkFocus.requestFocus()

    presenter.effects.collectLatest { effect ->
      when (effect) {
        is AddFeedEffect.ShowError -> {
          val errorMessage = errorMessageForErrorType(effect.addFeedErrorType, strings)
          if (errorMessage != null) {
            snackbarHostState.showSnackbar(message = errorMessage)
          }
        }
        AddFeedEffect.GoBack -> presenter.dispatch(AddFeedEvent.BackClicked)
      }
    }
  }

  var feedLink by remember { mutableStateOf(TextFieldValue()) }
  var feedTitle by remember { mutableStateOf(TextFieldValue()) }

  Scaffold(
    modifier = modifier,
    topBar = {
      Box {
        CenterAlignedTopAppBar(
          title = {},
          navigationIcon = {
            IconButton(onClick = { presenter.dispatch(AddFeedEvent.BackClicked) }) {
              Icon(TwineIcons.ArrowBack, contentDescription = null)
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

        HorizontalDivider(
          modifier = Modifier.fillMaxWidth().align(Alignment.BottomStart),
          color = AppTheme.colorScheme.surfaceContainer
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
          backgroundColor = SnackbarDefaults.color,
          contentColor = SnackbarDefaults.contentColor,
          elevation = 0.dp
        )
      }
    },
    bottomBar = {
      Button(
        modifier =
          Modifier.fillMaxWidth()
            .padding(horizontal = 24.dp)
            .windowInsetsPadding(WindowInsets.navigationBars),
        enabled =
          feedLink.text.isNotBlank() && state.feedFetchingState != FeedFetchingState.Loading,
        onClick = {
          presenter.dispatch(
            AddFeedEvent.AddFeedClicked(feedLink = feedLink.text, name = feedTitle.text)
          )
        }
      ) {
        if (state.feedFetchingState == FeedFetchingState.Loading) {
          CircularProgressIndicator(
            color = AppTheme.colorScheme.tintedForeground,
            modifier = Modifier.requiredSize(24.dp),
            strokeWidth = 2.dp
          )
        } else {
          Text(
            text = LocalStrings.current.buttonAddFeed,
            style = MaterialTheme.typography.labelLarge
          )
        }
      }
    },
    content = { paddingValues ->
      Column(
        modifier =
          Modifier.fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp)
            .padding(paddingValues),
        verticalArrangement = Arrangement.spacedBy(16.dp)
      ) {
        Spacer(Modifier.requiredHeight(16.dp))

        TextField(
          modifier =
            Modifier.fillMaxWidth().focusRequester(feedLinkFocus).focusProperties {
              next = feedTitleFocus
            },
          input = feedLink,
          onValueChange = { feedLink = it },
          hint = LocalStrings.current.feedEntryLinkHint,
          keyboardOptions =
            KeyboardOptions(
              imeAction = ImeAction.Next,
              keyboardType = KeyboardType.Uri,
              autoCorrect = false
            )
        )

        TextField(
          modifier =
            Modifier.fillMaxWidth().focusRequester(feedTitleFocus).focusProperties {
              previous = feedLinkFocus
            },
          input = feedTitle,
          onValueChange = { feedTitle = it },
          hint = LocalStrings.current.feedEntryTitleHint,
          keyboardOptions =
            KeyboardOptions(
              imeAction = ImeAction.Done,
              keyboardType = KeyboardType.Text,
              autoCorrect = false
            ),
        )

        OutlinedButton(
          modifier = Modifier.fillMaxWidth(),
          colors =
            ButtonDefaults.outlinedButtonColors(
              containerColor = AppTheme.colorScheme.surfaceContainerLowest,
              contentColor = AppTheme.colorScheme.tintedForeground
            ),
          border = BorderStroke(1.dp, AppTheme.colorScheme.tintedForeground),
          onClick = { presenter.dispatch(AddFeedEvent.OnGroupDropdownClicked) },
          content = {
            Text(modifier = Modifier.weight(1f), text = LocalStrings.current.buttonAddToGroup)

            Icon(imageVector = Icons.AutoMirrored.Filled.ArrowRight, contentDescription = null)
          }
        )

        FlowRow(
          horizontalArrangement = Arrangement.spacedBy(8.dp),
          verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
          state.selectedFeedGroups.forEach { selectedGroup ->
            InputChip(
              selected = false,
              onClick = { presenter.dispatch(AddFeedEvent.OnRemoveGroupClicked(selectedGroup)) },
              colors =
                InputChipDefaults.inputChipColors(
                  containerColor = AppTheme.colorScheme.tintedForeground,
                  labelColor = AppTheme.colorScheme.tintedBackground,
                  leadingIconColor = AppTheme.colorScheme.tintedBackground,
                  trailingIconColor = AppTheme.colorScheme.tintedBackground,
                ),
              border = null,
              shape = RoundedCornerShape(50),
              leadingIcon = {
                Icon(
                  modifier = Modifier.requiredSize(16.dp),
                  imageVector = Icons.TwoTone.FolderOpen,
                  contentDescription = null
                )
              },
              trailingIcon = {
                Icon(
                  modifier = Modifier.requiredSize(16.dp),
                  imageVector = Icons.Rounded.Close,
                  contentDescription = null
                )
              },
              label = { Text(text = selectedGroup.name) }
            )
          }
        }
      }
    },
    containerColor = AppTheme.colorScheme.surfaceContainerLowest,
    contentColor = Color.Unspecified,
    contentWindowInsets = WindowInsets.systemBars
  )
}

@Composable
fun TextField(
  input: TextFieldValue,
  hint: String,
  onValueChange: (TextFieldValue) -> Unit,
  modifier: Modifier = Modifier,
  keyboardActions: KeyboardActions = KeyboardActions(),
  keyboardOptions: KeyboardOptions = KeyboardOptions(),
  enabled: Boolean = true,
) {
  androidx.compose.material3.TextField(
    modifier = modifier.requiredHeight(56.dp).fillMaxWidth(),
    value = input,
    onValueChange = onValueChange,
    keyboardOptions = keyboardOptions,
    keyboardActions = keyboardActions,
    singleLine = true,
    textStyle = MaterialTheme.typography.labelLarge,
    shape = RoundedCornerShape(16.dp),
    enabled = enabled,
    colors =
      TextFieldDefaults.colors(
        unfocusedContainerColor = AppTheme.colorScheme.surfaceContainer,
        focusedContainerColor = AppTheme.colorScheme.surfaceContainer,
        disabledContainerColor = AppTheme.colorScheme.surfaceContainer,
        focusedIndicatorColor = Color.Transparent,
        unfocusedIndicatorColor = Color.Transparent,
        disabledIndicatorColor = Color.Transparent,
        errorIndicatorColor = Color.Transparent,
        cursorColor = AppTheme.colorScheme.tintedForeground,
        selectionColors =
          TextSelectionColors(
            handleColor = AppTheme.colorScheme.tintedForeground,
            backgroundColor = AppTheme.colorScheme.tintedForeground.copy(0.4f)
          )
      ),
    placeholder = {
      Text(
        text = hint,
        style = MaterialTheme.typography.labelLarge,
        color = AppTheme.colorScheme.tintedForeground.copy(alpha = 0.4f)
      )
    }
  )
}

private fun errorMessageForErrorType(
  errorType: AddFeedErrorType,
  twineStrings: TwineStrings
): String? {
  return when (errorType) {
    AddFeedErrorType.UnknownFeedType -> twineStrings.errorUnsupportedFeed
    AddFeedErrorType.FailedToParseXML -> twineStrings.errorMalformedXml
    AddFeedErrorType.Timeout -> twineStrings.errorRequestTimeout
    is AddFeedErrorType.Unknown -> errorType.e.message
    is AddFeedErrorType.FeedNotFound -> twineStrings.errorFeedNotFound(errorType.statusCode.value)
    is AddFeedErrorType.ServerError -> twineStrings.errorServer(errorType.statusCode.value)
    AddFeedErrorType.TooManyRedirects -> twineStrings.errorTooManyRedirects
    is AddFeedErrorType.UnAuthorized -> twineStrings.errorUnAuthorized(errorType.statusCode.value)
    is AddFeedErrorType.UnknownHttpStatusError ->
      twineStrings.errorUnknownHttpStatus(errorType.statusCode.value)
  }
}
