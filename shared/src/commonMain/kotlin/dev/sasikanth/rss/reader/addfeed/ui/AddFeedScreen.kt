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

package dev.sasikanth.rss.reader.addfeed.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.requiredHeightIn
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberBottomSheetState
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
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.sasikanth.rss.reader.addfeed.AddFeedEffect
import dev.sasikanth.rss.reader.addfeed.AddFeedErrorType
import dev.sasikanth.rss.reader.addfeed.AddFeedEvent
import dev.sasikanth.rss.reader.addfeed.AddFeedState
import dev.sasikanth.rss.reader.addfeed.AddFeedViewModel
import dev.sasikanth.rss.reader.addfeed.AddedFeed
import dev.sasikanth.rss.reader.addfeed.FeedFetchingState
import dev.sasikanth.rss.reader.components.Button
import dev.sasikanth.rss.reader.components.FeedIconPicker
import dev.sasikanth.rss.reader.components.IconButton
import dev.sasikanth.rss.reader.components.IconButtonSize
import dev.sasikanth.rss.reader.components.ModalSheet
import dev.sasikanth.rss.reader.components.Switch
import dev.sasikanth.rss.reader.components.TextField
import dev.sasikanth.rss.reader.core.model.local.FeedGroup
import dev.sasikanth.rss.reader.resources.icons.Add
import dev.sasikanth.rss.reader.resources.icons.Close
import dev.sasikanth.rss.reader.resources.icons.TwineIcons
import dev.sasikanth.rss.reader.ui.AppTheme
import dev.sasikanth.rss.reader.ui.LocalTranslucentStyles
import dev.sasikanth.rss.reader.utils.LocalInAppRating
import dev.sasikanth.rss.reader.utils.removeLineBreaks
import kotlinx.coroutines.flow.Flow
import org.jetbrains.compose.resources.stringResource
import twine.shared.generated.resources.Res
import twine.shared.generated.resources.addFeedAddedSubtitle
import twine.shared.generated.resources.addFeedAddedTitle
import twine.shared.generated.resources.addFeedGroupLabel
import twine.shared.generated.resources.addFeedSubtitle
import twine.shared.generated.resources.addFeedTitle
import twine.shared.generated.resources.addToGroup
import twine.shared.generated.resources.alwaysFetchSourceArticle
import twine.shared.generated.resources.buttonAddFeed
import twine.shared.generated.resources.buttonChange
import twine.shared.generated.resources.enableNotifications
import twine.shared.generated.resources.errorFeedNotFound
import twine.shared.generated.resources.errorMalformedXml
import twine.shared.generated.resources.errorRequestTimeout
import twine.shared.generated.resources.errorServer
import twine.shared.generated.resources.errorTooManyRedirects
import twine.shared.generated.resources.errorUnAuthorized
import twine.shared.generated.resources.errorUnknown
import twine.shared.generated.resources.errorUnknownHttpStatus
import twine.shared.generated.resources.errorUnsupportedFeed
import twine.shared.generated.resources.feedEntryLinkHint
import twine.shared.generated.resources.feedEntryTitleHint

private val FEED_OPTION_ROW_HEIGHT = 48.dp
private val FEED_OPTION_ROW_SPACING = 16.dp

private const val STEP_TRANSITION_MILLIS = 220
private const val STEP_FADE_MILLIS = 90

@Composable
fun AddFeedScreen(
  viewModel: AddFeedViewModel,
  dismiss: () -> Unit,
  groupSelectionResults: Flow<Set<String>>,
  onGroupSelectionConsumed: () -> Unit,
  openGroupSelection: (Set<String>) -> Unit,
  modifier: Modifier = Modifier,
) {
  val state by viewModel.state.collectAsStateWithLifecycle()
  val inAppRating = LocalInAppRating.current

  LaunchedEffect(Unit) {
    groupSelectionResults.collect { selectedGroupIds ->
      if (selectedGroupIds.isNotEmpty()) {
        viewModel.dispatch(AddFeedEvent.OnGroupsSelected(selectedGroupIds))
        onGroupSelectionConsumed()
      }
    }
  }

  LaunchedEffect(Unit) {
    viewModel.effects.collect { effect ->
      when (effect) {
        AddFeedEffect.RequestInAppRating -> inAppRating.request()
      }
    }
  }

  LaunchedEffect(state.goBack) {
    if (state.goBack) {
      dismiss()
      viewModel.dispatch(AddFeedEvent.MarkGoBackAsDone)
    }
  }

  AddFeedContent(
    state = state,
    dispatch = viewModel::dispatch,
    dismiss = dismiss,
    openGroupSelection = openGroupSelection,
    modifier = modifier,
  )
}

@Composable
private fun AddFeedContent(
  state: AddFeedState,
  dispatch: (AddFeedEvent) -> Unit,
  dismiss: () -> Unit,
  openGroupSelection: (Set<String>) -> Unit,
  modifier: Modifier = Modifier,
) {
  val addedFeed = state.addedFeed
  var feedLink by remember { mutableStateOf(TextFieldValue()) }
  val isLoading = state.feedFetchingState == FeedFetchingState.Loading

  AppTheme(useDarkTheme = true) {
    val translucentStyle = LocalTranslucentStyles.current
    val sheetState =
      rememberBottomSheetState(
        initialValue = SheetValue.Hidden,
        enabledValues = setOf(SheetValue.Hidden, SheetValue.Expanded),
      )

    ModalSheet(
      modifier = Modifier.then(modifier),
      sheetState = sheetState,
      onDismissRequest = {
        if (addedFeed != null) dispatch(AddFeedEvent.DoneClicked) else dismiss()
      },
      containerColor = translucentStyle.default.background.compositeOver(Color.Black),
      contentColor = Color.Unspecified,
      contentWindowInsets = {
        WindowInsets.systemBars
          .only(WindowInsetsSides.Bottom)
          .union(WindowInsets.ime.only(WindowInsetsSides.Bottom))
      },
    ) {
      Column(
        modifier =
          Modifier.weight(1f, fill = false)
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp)
      ) {
        AnimatedContent(
          targetState = addedFeed,
          transitionSpec = {
            val enter =
              fadeIn(tween(STEP_TRANSITION_MILLIS, delayMillis = STEP_FADE_MILLIS)) +
                slideInVertically(tween(STEP_TRANSITION_MILLIS, delayMillis = STEP_FADE_MILLIS)) {
                  it / 6
                }

            enter togetherWith fadeOut(tween(STEP_FADE_MILLIS))
          },
          contentKey = { it != null },
          label = "AddFeedStep",
        ) { step ->
          Column {
            if (step == null) {
              FeedLinkStep(
                state = state,
                feedLink = feedLink,
                onFeedLinkChanged = { feedLink = it },
                isSheetSettled = sheetState.currentValue == SheetValue.Expanded,
                dispatch = dispatch,
              )
            } else {
              FeedDetailsStep(
                state = state,
                addedFeed = step,
                dispatch = dispatch,
                openGroupSelection = openGroupSelection,
              )
            }
          }
        }
      }

      PrimaryButton(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 24.dp),
        text =
          if (addedFeed == null) {
            stringResource(Res.string.buttonAddFeed)
          } else {
            stringResource(Res.string.buttonChange)
          },
        isLoading = isLoading,
        enabled =
          if (addedFeed == null) {
            feedLink.text.isNotBlank() && !isLoading
          } else {
            addedFeed.name.isNotBlank()
          },
        onClick = {
          if (addedFeed == null) {
            dispatch(AddFeedEvent.AddFeedClicked(feedLink = feedLink.text, name = null))
          } else {
            dispatch(AddFeedEvent.DoneClicked)
          }
        },
      )
    }
  }
}

@Composable
private fun ColumnScope.FeedLinkStep(
  state: AddFeedState,
  feedLink: TextFieldValue,
  onFeedLinkChanged: (TextFieldValue) -> Unit,
  isSheetSettled: Boolean,
  dispatch: (AddFeedEvent) -> Unit,
) {
  val feedLinkFocus = remember { FocusRequester() }
  val isLoading = state.feedFetchingState == FeedFetchingState.Loading

  // Requesting focus while the sheet is still sliding up brings the keyboard in first.
  LaunchedEffect(isSheetSettled) {
    if (isSheetSettled) {
      feedLinkFocus.requestFocus()
    }
  }

  StepHeader(
    title = stringResource(Res.string.addFeedTitle),
    subtitle = stringResource(Res.string.addFeedSubtitle),
  )

  TextField(
    modifier = Modifier.fillMaxWidth().focusRequester(feedLinkFocus),
    value = feedLink,
    onValueChange = {
      onFeedLinkChanged(it)
      if (state.error != null) {
        dispatch(AddFeedEvent.MarkErrorAsShown)
      }
    },
    hint = stringResource(Res.string.feedEntryLinkHint),
    enabled = !isLoading,
    keyboardOptions =
      KeyboardOptions(
        autoCorrectEnabled = false,
        keyboardType = KeyboardType.Uri,
        imeAction = ImeAction.Done,
      ),
  )

  val error = state.error
  if (error != null) {
    Spacer(Modifier.height(8.dp))

    Text(
      modifier = Modifier.padding(horizontal = 16.dp),
      text = errorMessageForErrorType(error),
      style = MaterialTheme.typography.bodySmall,
      color = AppTheme.colorScheme.error,
    )
  }
}

@Composable
private fun ColumnScope.FeedDetailsStep(
  state: AddFeedState,
  addedFeed: AddedFeed,
  dispatch: (AddFeedEvent) -> Unit,
  openGroupSelection: (Set<String>) -> Unit,
) {
  StepHeader(
    title = stringResource(Res.string.addFeedAddedTitle),
    subtitle = stringResource(Res.string.addFeedAddedSubtitle),
  )

  FeedIconPicker(
    modifier = Modifier.align(Alignment.CenterHorizontally),
    icon = addedFeed.icon,
    homepageLink = addedFeed.homepageLink,
    showFeedFavIcon = addedFeed.showFeedFavIcon,
    onShowFeedFavIconChanged = { dispatch(AddFeedEvent.ShowFavIconToggled(it)) },
  )

  Spacer(Modifier.height(24.dp))

  TextField(
    modifier = Modifier.fillMaxWidth(),
    value = addedFeed.name,
    onValueChange = { dispatch(AddFeedEvent.FeedNameChanged(it.removeLineBreaks())) },
    hint = stringResource(Res.string.feedEntryTitleHint),
    keyboardOptions =
      KeyboardOptions(
        autoCorrectEnabled = false,
        keyboardType = KeyboardType.Text,
        imeAction = ImeAction.Done,
      ),
  )

  Spacer(Modifier.height(16.dp))

  Column(verticalArrangement = Arrangement.spacedBy(FEED_OPTION_ROW_SPACING)) {
    GroupRow(
      group = state.selectedFeedGroups.firstOrNull(),
      onSelectClick = { group -> openGroupSelection(setOfNotNull(group?.id)) },
      onClearClick = { group -> dispatch(AddFeedEvent.OnRemoveGroupClicked(group)) },
    )

    FeedOptionSwitch(
      title = stringResource(Res.string.alwaysFetchSourceArticle),
      checked = addedFeed.alwaysFetchSourceArticle,
      onValueChanged = { dispatch(AddFeedEvent.AlwaysFetchSourceArticleToggled(it)) },
    )

    FeedOptionSwitch(
      title = stringResource(Res.string.enableNotifications),
      checked = addedFeed.enableNotifications,
      onValueChanged = { dispatch(AddFeedEvent.NotificationsToggled(it)) },
    )
  }
}

@Composable
private fun StepHeader(title: String, subtitle: String) {
  Text(
    text = title,
    style = MaterialTheme.typography.titleLarge,
    color = AppTheme.colorScheme.onSurface,
  )

  Spacer(Modifier.height(4.dp))

  Text(
    text = subtitle,
    style = MaterialTheme.typography.bodyMedium,
    color = AppTheme.colorScheme.onSurfaceVariant,
  )

  Spacer(Modifier.height(24.dp))
}

@Composable
private fun GroupRow(
  group: FeedGroup?,
  onSelectClick: (FeedGroup?) -> Unit,
  onClearClick: (FeedGroup) -> Unit,
) {
  Row(
    modifier = Modifier.fillMaxWidth().requiredHeight(FEED_OPTION_ROW_HEIGHT),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Text(
      modifier =
        Modifier.weight(1f).clickable(
          interactionSource = remember { MutableInteractionSource() },
          indication = null,
        ) {
          onSelectClick(group)
        },
      text = stringResource(Res.string.addFeedGroupLabel),
      color = AppTheme.colorScheme.onSurface,
      style = MaterialTheme.typography.bodyLarge,
    )

    Spacer(Modifier.requiredWidth(16.dp))

    if (group == null) {
      IconButton(
        icon = TwineIcons.Add,
        contentDescription = stringResource(Res.string.addToGroup),
        size = IconButtonSize.Small,
        tint = AppTheme.colorScheme.primary,
        onClick = { onSelectClick(null) },
      )
    } else {
      GroupChip(
        name = group.name,
        onClick = { onSelectClick(group) },
        onClearClick = { onClearClick(group) },
      )
    }
  }
}

@Composable
private fun GroupChip(name: String, onClick: () -> Unit, onClearClick: () -> Unit) {
  val translucentStyle = LocalTranslucentStyles.current

  Row(
    modifier =
      Modifier.requiredHeightIn(min = 36.dp)
        .clip(CircleShape)
        .background(translucentStyle.default.background, CircleShape),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Text(
      modifier =
        Modifier.weight(1f, fill = false)
          .clickable(onClick = onClick)
          .padding(start = 16.dp, end = 8.dp, top = 8.dp, bottom = 8.dp),
      text = name,
      style = MaterialTheme.typography.labelLarge,
      color = AppTheme.colorScheme.onSurface,
      maxLines = 1,
      overflow = TextOverflow.MiddleEllipsis,
    )

    Box(
      modifier =
        Modifier.clip(CircleShape)
          .clickable(onClick = onClearClick)
          .padding(start = 4.dp, end = 12.dp, top = 8.dp, bottom = 8.dp),
      contentAlignment = Alignment.Center,
    ) {
      Icon(
        modifier = Modifier.requiredSize(16.dp),
        imageVector = TwineIcons.Close,
        contentDescription = null,
        tint = AppTheme.colorScheme.onSurfaceVariant,
      )
    }
  }
}

@Composable
private fun FeedOptionSwitch(title: String, checked: Boolean, onValueChanged: (Boolean) -> Unit) {
  val interactionSource = remember { MutableInteractionSource() }

  Row(
    modifier =
      Modifier.fillMaxWidth()
        .clickable(indication = null, interactionSource = interactionSource) {
          onValueChanged(!checked)
        }
        .requiredHeight(FEED_OPTION_ROW_HEIGHT),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Text(
      modifier = Modifier.weight(1f),
      text = title,
      color = AppTheme.colorScheme.onSurface,
      style = MaterialTheme.typography.bodyLarge,
    )

    Spacer(Modifier.requiredWidth(16.dp))

    Switch(
      interactionSource = interactionSource,
      checked = checked,
      onCheckedChange = onValueChanged,
    )
  }
}

@Composable
private fun PrimaryButton(
  text: String,
  isLoading: Boolean,
  enabled: Boolean,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Button(
    modifier = modifier.requiredHeight(48.dp),
    colors =
      ButtonDefaults.buttonColors(
        containerColor = AppTheme.colorScheme.inverseSurface,
        contentColor = AppTheme.colorScheme.inverseOnSurface,
      ),
    enabled = enabled,
    shape = CircleShape,
    onClick = onClick,
  ) {
    if (isLoading) {
      CircularProgressIndicator(
        color = AppTheme.colorScheme.inverseOnSurface,
        modifier = Modifier.requiredSize(20.dp),
        strokeWidth = 2.dp,
      )
    } else {
      Text(
        text = text,
        style = MaterialTheme.typography.labelLarge,
        color = AppTheme.colorScheme.inverseOnSurface,
        fontWeight = FontWeight.Medium,
      )
    }
  }
}

@Composable
private fun errorMessageForErrorType(errorType: AddFeedErrorType): String {
  return when (errorType) {
    AddFeedErrorType.UnknownFeedType -> stringResource(Res.string.errorUnsupportedFeed)
    AddFeedErrorType.FailedToParseXML -> stringResource(Res.string.errorMalformedXml)
    AddFeedErrorType.Timeout -> stringResource(Res.string.errorRequestTimeout)
    is AddFeedErrorType.Unknown -> errorType.e.message ?: stringResource(Res.string.errorUnknown)
    is AddFeedErrorType.FeedNotFound ->
      stringResource(Res.string.errorFeedNotFound, errorType.statusCode.value)
    is AddFeedErrorType.ServerError ->
      stringResource(Res.string.errorServer, errorType.statusCode.value)
    AddFeedErrorType.TooManyRedirects -> stringResource(Res.string.errorTooManyRedirects)
    is AddFeedErrorType.UnAuthorized ->
      stringResource(Res.string.errorUnAuthorized, errorType.statusCode.value)
    is AddFeedErrorType.UnknownHttpStatusError ->
      stringResource(Res.string.errorUnknownHttpStatus, errorType.statusCode.value)
  }
}
