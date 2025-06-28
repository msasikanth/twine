/*
 * Copyright 2025 Sasikanth Miriyampalli
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

package dev.sasikanth.rss.reader.blockedwords

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.sasikanth.rss.reader.core.model.local.BlockedWord
import dev.sasikanth.rss.reader.resources.icons.ArrowBack
import dev.sasikanth.rss.reader.resources.icons.DeleteOutline
import dev.sasikanth.rss.reader.resources.icons.TwineIcons
import dev.sasikanth.rss.reader.ui.AppTheme
import dev.sasikanth.rss.reader.utils.KeyboardState
import dev.sasikanth.rss.reader.utils.keyboardVisibilityAsState
import org.jetbrains.compose.resources.stringResource
import twine.shared.generated.resources.Res
import twine.shared.generated.resources.blockedWords
import twine.shared.generated.resources.blockedWordsDesc
import twine.shared.generated.resources.blockedWordsEmpty
import twine.shared.generated.resources.blockedWordsHint
import twine.shared.generated.resources.buttonAdd
import twine.shared.generated.resources.delete

@Composable
fun BlockedWordsScreen(
  viewModel: BlockedWordsViewModel,
  goBack: () -> Unit,
  modifier: Modifier = Modifier
) {
  val state by viewModel.state.collectAsStateWithLifecycle()

  Scaffold(
    modifier = modifier,
    topBar = {
      Box {
        CenterAlignedTopAppBar(
          title = { Text(stringResource(Res.string.blockedWords)) },
          navigationIcon = {
            IconButton(onClick = { goBack() }) {
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
    containerColor = AppTheme.colorScheme.surfaceContainerLowest,
    contentColor = Color.Unspecified,
  ) { innerPadding ->
    Column(modifier = Modifier.padding(innerPadding).imePadding()) {
      Spacer(Modifier.requiredHeight(16.dp))

      var newBlockedWord by remember { mutableStateOf(TextFieldValue()) }
      val keyboardState by keyboardVisibilityAsState()
      val focusManager = LocalFocusManager.current

      LaunchedEffect(keyboardState) {
        if (keyboardState == KeyboardState.Closed) {
          focusManager.clearFocus()
        }
      }

      TextField(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp).requiredHeight(56.dp),
        value = newBlockedWord,
        onValueChange = { newBlockedWord = it },
        keyboardOptions =
          KeyboardOptions(
            autoCorrectEnabled = false,
            keyboardType = KeyboardType.Text,
            imeAction = ImeAction.Done
          ),
        keyboardActions =
          KeyboardActions(
            onDone = {
              viewModel.dispatch(BlockedWordsEvent.AddBlockedWord(newBlockedWord.text))
              newBlockedWord = TextFieldValue()
            }
          ),
        singleLine = true,
        textStyle = MaterialTheme.typography.labelLarge,
        shape = RoundedCornerShape(16.dp),
        enabled = true,
        colors =
          TextFieldDefaults.colors(
            unfocusedContainerColor = AppTheme.colorScheme.surfaceContainer,
            focusedContainerColor = AppTheme.colorScheme.surfaceContainer,
            disabledContainerColor = AppTheme.colorScheme.surfaceContainer,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            disabledIndicatorColor = Color.Transparent,
            errorIndicatorColor = Color.Transparent,
            cursorColor = AppTheme.colorScheme.tintedHighlight,
            selectionColors =
              TextSelectionColors(
                handleColor = AppTheme.colorScheme.tintedHighlight,
                backgroundColor = AppTheme.colorScheme.tintedHighlight.copy(0.4f)
              )
          ),
        placeholder = {
          Text(
            text = stringResource(Res.string.blockedWordsHint),
            style = MaterialTheme.typography.labelLarge,
            color = AppTheme.colorScheme.textEmphasisHigh
          )
        },
        trailingIcon = {
          val hasContent = newBlockedWord.text.isNotBlank()
          AnimatedVisibility(
            visible = hasContent,
            enter = fadeIn() + expandHorizontally(expandFrom = Alignment.Start),
            exit = fadeOut() + shrinkHorizontally(shrinkTowards = Alignment.Start)
          ) {
            IconButton(
              onClick = {
                viewModel.dispatch(BlockedWordsEvent.AddBlockedWord(newBlockedWord.text))
                newBlockedWord = TextFieldValue()
              }
            ) {
              Icon(
                imageVector = Icons.Rounded.Check,
                contentDescription = stringResource(Res.string.buttonAdd)
              )
            }
          }
        },
      )

      Spacer(Modifier.requiredHeight(16.dp))

      LazyColumn {
        item {
          Column {
            Text(
              modifier = Modifier.fillMaxWidth().padding(horizontal = 32.dp),
              text = stringResource(Res.string.blockedWordsDesc),
              style = MaterialTheme.typography.labelLarge,
              color = AppTheme.colorScheme.textEmphasisMed
            )

            HorizontalDivider(
              modifier = Modifier.padding(vertical = 16.dp),
              color = AppTheme.colorScheme.surfaceContainer
            )
          }
        }

        if (state.blockedWords.isEmpty()) {
          item {
            Text(
              modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp),
              text = stringResource(Res.string.blockedWordsEmpty),
              style = MaterialTheme.typography.labelLarge,
              color = AppTheme.colorScheme.textEmphasisMed,
              textAlign = TextAlign.Center,
            )
          }
        }

        itemsIndexed(items = state.blockedWords, key = { _, blockedWord -> blockedWord.id }) {
          index,
          blockedWord ->
          Column(modifier = Modifier.padding(horizontal = 24.dp).animateItem()) {
            BlockedWordItem(
              word = blockedWord,
              removeClicked = {
                viewModel.dispatch(BlockedWordsEvent.DeleteBlockedWord(blockedWord.id))
              }
            )

            if (index < state.blockedWords.lastIndex) {
              HorizontalDivider(
                modifier = Modifier.padding(vertical = 8.dp),
                color = AppTheme.colorScheme.surfaceContainer
              )
            }
          }
        }
      }
    }
  }
}

@Composable
private fun BlockedWordItem(
  word: BlockedWord,
  removeClicked: () -> Unit,
  modifier: Modifier = Modifier
) {
  Row(
    modifier = modifier.then(modifier),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Text(
      modifier = Modifier.weight(1f),
      text = word.content,
      style = MaterialTheme.typography.titleMedium,
      color = AppTheme.colorScheme.textEmphasisHigh,
    )

    IconButton(onClick = removeClicked) {
      Icon(
        imageVector = TwineIcons.DeleteOutline,
        contentDescription = stringResource(Res.string.delete)
      )
    }
  }
}
