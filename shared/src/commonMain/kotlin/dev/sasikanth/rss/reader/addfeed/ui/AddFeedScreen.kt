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

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Icon
import androidx.compose.material.Snackbar
import androidx.compose.material.SnackbarHost
import androidx.compose.material.SnackbarHostState
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.FocusRequester.Companion.FocusRequesterFactory.component1
import androidx.compose.ui.focus.FocusRequester.Companion.FocusRequesterFactory.component2
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.sasikanth.rss.reader.addfeed.AddFeedErrorType
import dev.sasikanth.rss.reader.addfeed.AddFeedEvent
import dev.sasikanth.rss.reader.addfeed.AddFeedViewModel
import dev.sasikanth.rss.reader.addfeed.FeedFetchingState
import dev.sasikanth.rss.reader.components.Button
import dev.sasikanth.rss.reader.components.CircularIconButton
import dev.sasikanth.rss.reader.components.OutlinedButton
import dev.sasikanth.rss.reader.components.Switch
import dev.sasikanth.rss.reader.components.TextField
import dev.sasikanth.rss.reader.core.model.local.FeedGroup
import dev.sasikanth.rss.reader.feeds.ui.sheet.collapsed.FeedGroupBottomBarItem
import dev.sasikanth.rss.reader.resources.icons.ArrowBack
import dev.sasikanth.rss.reader.resources.icons.Close
import dev.sasikanth.rss.reader.resources.icons.NewGroup
import dev.sasikanth.rss.reader.resources.icons.TwineIcons
import dev.sasikanth.rss.reader.ui.AppTheme
import dev.sasikanth.rss.reader.utils.ignoreHorizontalParentPadding
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.stringResource
import twine.shared.generated.resources.Res
import twine.shared.generated.resources.addToGroup
import twine.shared.generated.resources.alwaysFetchSourceArticle
import twine.shared.generated.resources.buttonAddFeed
import twine.shared.generated.resources.buttonGoBack
import twine.shared.generated.resources.errorFeedNotFound
import twine.shared.generated.resources.errorMalformedXml
import twine.shared.generated.resources.errorRequestTimeout
import twine.shared.generated.resources.errorServer
import twine.shared.generated.resources.errorTooManyRedirects
import twine.shared.generated.resources.errorUnAuthorized
import twine.shared.generated.resources.errorUnknownHttpStatus
import twine.shared.generated.resources.errorUnsupportedFeed
import twine.shared.generated.resources.feedEntryLinkHint
import twine.shared.generated.resources.feedEntryTitleHint
import twine.shared.generated.resources.showFeedFavIconTitle

@Composable
fun AddFeedScreen(
  viewModel: AddFeedViewModel,
  goBack: () -> Unit,
  openGroupSelection: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val state by viewModel.state.collectAsStateWithLifecycle()
  val snackbarHostState = remember { SnackbarHostState() }
  val (feedLinkFocus, feedTitleFocus) = remember { FocusRequester.createRefs() }

  LaunchedEffect(state.error, state.goBack) {
    feedLinkFocus.requestFocus()

    when {
      state.goBack -> {
        goBack()
        viewModel.dispatch(AddFeedEvent.MarkGoBackAsDone)
      }
      state.error != null -> {
        val errorMessage = errorMessageForErrorType(state.error!!)
        if (errorMessage != null) {
          snackbarHostState.showSnackbar(message = errorMessage)
          viewModel.dispatch(AddFeedEvent.MarkErrorAsShown)
        }
      }
    }
  }

  var feedLink by remember { mutableStateOf(TextFieldValue()) }
  var feedTitle by remember { mutableStateOf(TextFieldValue()) }

  Scaffold(
    modifier = modifier,
    topBar = {
      CenterAlignedTopAppBar(
        title = {},
        navigationIcon = {
          CircularIconButton(
            modifier = Modifier.padding(start = 12.dp),
            icon = TwineIcons.ArrowBack,
            label = stringResource(Res.string.buttonGoBack),
            onClick = { goBack() }
          )
        },
        colors =
          TopAppBarDefaults.topAppBarColors(
            containerColor = AppTheme.colorScheme.backdrop,
            navigationIconContentColor = AppTheme.colorScheme.onSurface,
            titleContentColor = AppTheme.colorScheme.onSurface,
            actionIconContentColor = AppTheme.colorScheme.onSurface
          ),
      )
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
          feedLink.text.isNotBlank() && state.feedFetchingState != FeedFetchingState.Loading,
        shape = MaterialTheme.shapes.extraLarge,
        onClick = {
          viewModel.dispatch(
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
            text = stringResource(Res.string.buttonAddFeed).uppercase(),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Medium,
          )
        }
      }
    },
    content = { paddingValues ->
      LazyVerticalGrid(
        modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp),
        columns = GridCells.Adaptive(minSize = 64.dp),
        contentPadding = paddingValues,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
      ) {
        item(span = { GridItemSpan(maxLineSpan) }) { Spacer(Modifier.requiredHeight(16.dp)) }

        item(
          span = { GridItemSpan(maxLineSpan) },
        ) {
          TextField(
            modifier =
              Modifier.fillMaxWidth().focusRequester(feedLinkFocus).focusProperties {
                next = feedTitleFocus
              },
            value = feedLink,
            onValueChange = { feedLink = it },
            hint = stringResource(Res.string.feedEntryLinkHint),
            keyboardOptions =
              KeyboardOptions(
                autoCorrectEnabled = false,
                keyboardType = KeyboardType.Uri,
                imeAction = ImeAction.Next
              )
          )
        }

        item(
          span = { GridItemSpan(maxLineSpan) },
        ) {
          TextField(
            modifier =
              Modifier.fillMaxWidth().focusRequester(feedTitleFocus).focusProperties {
                previous = feedLinkFocus
              },
            value = feedTitle,
            onValueChange = { feedTitle = it },
            hint = stringResource(Res.string.feedEntryTitleHint),
            keyboardOptions =
              KeyboardOptions(
                autoCorrectEnabled = false,
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Done
              ),
          )
        }

        item(span = { GridItemSpan(maxLineSpan) }) {
          Column {
            HorizontalDivider(
              modifier = Modifier.padding(vertical = 12.dp).ignoreHorizontalParentPadding(24.dp),
              color = AppTheme.colorScheme.outlineVariant,
            )

            FeedOptionSwitch(
              modifier = Modifier.ignoreHorizontalParentPadding(24.dp),
              title = stringResource(Res.string.alwaysFetchSourceArticle),
              checked = state.alwaysFetchSourceArticle,
              onValueChanged = { newValue ->
                viewModel.dispatch(AddFeedEvent.OnAlwaysFetchSourceArticleChanged(newValue))
              }
            )

            HorizontalDivider(
              color = AppTheme.colorScheme.outlineVariant,
            )

            FeedOptionSwitch(
              modifier = Modifier.ignoreHorizontalParentPadding(24.dp),
              title = stringResource(Res.string.showFeedFavIconTitle),
              checked = state.showFeedFavIcon,
              onValueChanged = { newValue ->
                viewModel.dispatch(AddFeedEvent.OnShowFeedFavIconChanged(newValue))
              }
            )

            HorizontalDivider(
              modifier = Modifier.padding(vertical = 12.dp).ignoreHorizontalParentPadding(24.dp),
              color = AppTheme.colorScheme.outlineVariant,
            )
          }
        }

        item(span = { GridItemSpan(maxLineSpan) }) {
          val shape = RoundedCornerShape(50)
          OutlinedButton(
            modifier = Modifier.padding(horizontal = 12.dp),
            shape = shape,
            onClick = { openGroupSelection() },
          ) {
            Icon(
              imageVector = TwineIcons.NewGroup,
              contentDescription = null,
              tint = AppTheme.colorScheme.onSurfaceVariant,
            )

            Spacer(Modifier.requiredWidth(12.dp))

            Text(
              text = stringResource(Res.string.addToGroup),
              style = MaterialTheme.typography.titleSmall,
              color = AppTheme.colorScheme.onSurfaceVariant,
            )
          }
        }

        items(state.selectedFeedGroups.toList()) { group ->
          GroupItem(group) { viewModel.dispatch(AddFeedEvent.OnRemoveGroupClicked(group)) }
        }
      }
    },
    containerColor = AppTheme.colorScheme.backdrop,
    contentColor = Color.Unspecified,
    contentWindowInsets = WindowInsets.systemBars
  )
}

@Composable
private fun FeedOptionSwitch(
  title: String,
  checked: Boolean,
  onValueChanged: (Boolean) -> Unit,
  modifier: Modifier = Modifier,
) {
  Row(
    modifier =
      modifier.clickable { onValueChanged(!checked) }.padding(vertical = 16.dp, horizontal = 24.dp),
    verticalAlignment = Alignment.CenterVertically
  ) {
    Text(
      modifier = Modifier.weight(1f),
      text = title,
      color = AppTheme.colorScheme.textEmphasisHigh,
      style = MaterialTheme.typography.titleMedium
    )

    Spacer(Modifier.requiredSize(16.dp))

    Switch(checked = checked, onCheckedChange = onValueChanged)
  }
}

@Composable
private fun GroupItem(
  group: FeedGroup,
  onClick: () -> Unit,
) {
  Column(
    modifier =
      Modifier.clip(MaterialTheme.shapes.small).clickable { onClick() }.padding(bottom = 4.dp),
    horizontalAlignment = Alignment.CenterHorizontally,
  ) {
    Box {
      FeedGroupBottomBarItem(feedGroup = group, canShowUnreadPostsCount = false)

      Box(
        modifier =
          Modifier.align(Alignment.TopEnd)
            .clip(CircleShape)
            .requiredSize(24.dp)
            .background(AppTheme.colorScheme.primary, CircleShape)
            .border(2.dp, AppTheme.colorScheme.backdrop, CircleShape),
        contentAlignment = Alignment.Center,
      ) {
        Icon(
          modifier = Modifier.requiredSize(12.dp),
          imageVector = TwineIcons.Close,
          contentDescription = null,
          tint = AppTheme.colorScheme.backdrop,
        )
      }
    }

    Text(
      text = group.name,
      style = MaterialTheme.typography.labelSmall,
      color = AppTheme.colorScheme.onSurface,
      textAlign = TextAlign.Center,
      overflow = TextOverflow.Ellipsis,
      maxLines = 1,
    )
  }
}

private suspend fun errorMessageForErrorType(errorType: AddFeedErrorType): String? {
  return when (errorType) {
    AddFeedErrorType.UnknownFeedType -> getString(Res.string.errorUnsupportedFeed)
    AddFeedErrorType.FailedToParseXML -> getString(Res.string.errorMalformedXml)
    AddFeedErrorType.Timeout -> getString(Res.string.errorRequestTimeout)
    is AddFeedErrorType.Unknown -> errorType.e.message
    is AddFeedErrorType.FeedNotFound ->
      getString(Res.string.errorFeedNotFound, errorType.statusCode.value)
    is AddFeedErrorType.ServerError -> getString(Res.string.errorServer, errorType.statusCode.value)
    AddFeedErrorType.TooManyRedirects -> getString(Res.string.errorTooManyRedirects)
    is AddFeedErrorType.UnAuthorized ->
      getString(Res.string.errorUnAuthorized, errorType.statusCode.value)
    is AddFeedErrorType.UnknownHttpStatusError ->
      getString(Res.string.errorUnknownHttpStatus, errorType.statusCode.value)
  }
}
