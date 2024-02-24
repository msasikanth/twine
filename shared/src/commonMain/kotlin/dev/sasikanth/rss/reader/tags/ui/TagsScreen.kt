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

package dev.sasikanth.rss.reader.tags.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import app.cash.paging.compose.collectAsLazyPagingItems
import dev.sasikanth.rss.reader.resources.icons.ArrowBack
import dev.sasikanth.rss.reader.resources.icons.NewTag
import dev.sasikanth.rss.reader.resources.icons.TwineIcons
import dev.sasikanth.rss.reader.resources.strings.LocalStrings
import dev.sasikanth.rss.reader.tags.TagsEvent
import dev.sasikanth.rss.reader.tags.TagsPresenter
import dev.sasikanth.rss.reader.ui.AppTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Composable
fun TagsScreen(tagsPresenter: TagsPresenter, selectedTag: String?, modifier: Modifier = Modifier) {

  val state by tagsPresenter.state.collectAsState()
  val tags = state.tags.collectAsLazyPagingItems()

  val listState = rememberLazyListState()
  var showCreateTagDialog by remember { mutableStateOf(false) }

  Scaffold(
    modifier = modifier,
    topBar = {
      Box {
        CenterAlignedTopAppBar(
          title = { Text(LocalStrings.current.tags) },
          navigationIcon = {
            IconButton(onClick = { tagsPresenter.dispatch(TagsEvent.BackClicked) }) {
              Icon(TwineIcons.ArrowBack, contentDescription = null)
            }
          },
          actions = {
            IconButton(onClick = { showCreateTagDialog = true }) {
              Icon(TwineIcons.NewTag, contentDescription = LocalStrings.current.newTag)
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
    content = { padding ->
      if (tags.loadState.refresh == LoadState.Loading) {
        Box(modifier, contentAlignment = Alignment.Center) {
          CircularProgressIndicator(color = AppTheme.colorScheme.tintedForeground)
        }
      } else {
        LazyColumn(
          modifier = Modifier.fillMaxSize(),
          contentPadding =
            PaddingValues(
              bottom = padding.calculateBottomPadding(),
              top = padding.calculateTopPadding()
            ),
          state = listState
        ) {
          items(tags.itemCount) { index ->
            val tag = tags[index]
            if (tag != null) {
              Box {
                TagItem(
                  tag = tag,
                  isSelected = selectedTag == tag.id.toString(),
                  onTagClicked = { tagsPresenter.dispatch(TagsEvent.TagClicked(tag)) }
                )

                if (index < tags.itemCount) {
                  Divider(color = AppTheme.colorScheme.surfaceContainer)
                }
              }
            }
          }
        }
      }
    },
    containerColor = AppTheme.colorScheme.surfaceContainerLowest,
    contentColor = Color.Unspecified
  )

  if (showCreateTagDialog) {
    val coroutineScope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState()
    val focusManager = LocalFocusManager.current
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) { focusRequester.requestFocus() }

    ModalBottomSheet(
      onDismissRequest = {
        showCreateTagDialog = false
        focusManager.clearFocus()
      },
      containerColor = AppTheme.colorScheme.surfaceContainerLowest,
      contentColor = AppTheme.colorScheme.textEmphasisHigh,
      sheetState = sheetState
    ) {
      Column(
        modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        var tagName by remember { mutableStateOf(TextFieldValue()) }

        TextField(
          modifier = Modifier.requiredHeight(56.dp).fillMaxWidth().focusRequester(focusRequester),
          value = tagName,
          onValueChange = { tagName = it },
          keyboardOptions =
            KeyboardOptions(
              imeAction = ImeAction.Done,
              capitalization = KeyboardCapitalization.Words
            ),
          keyboardActions =
            KeyboardActions(onDone = { tagsPresenter.dispatch(TagsEvent.CreateTag(tagName.text)) }),
          singleLine = true,
          textStyle = MaterialTheme.typography.bodyLarge,
          shape = RoundedCornerShape(16.dp),
          colors =
            TextFieldDefaults.colors(
              unfocusedIndicatorColor = Color.Transparent,
              focusedIndicatorColor = Color.Transparent,
              errorIndicatorColor = Color.Transparent,
              disabledIndicatorColor = Color.Transparent
            ),
          placeholder = {
            Text(
              text = LocalStrings.current.newTagHint,
              style = MaterialTheme.typography.bodyLarge,
              color = AppTheme.colorScheme.textEmphasisMed
            )
          }
        )

        Button(
          modifier = Modifier.requiredHeight(52.dp).fillMaxWidth(),
          onClick = { createTag(coroutineScope, sheetState, tagsPresenter, tagName) },
          shape = RoundedCornerShape(16.dp),
          colors =
            ButtonDefaults.elevatedButtonColors(
              containerColor = AppTheme.colorScheme.tintedSurface,
              contentColor = AppTheme.colorScheme.tintedForeground
            )
        ) {
          Text(LocalStrings.current.tagSaveButton, style = MaterialTheme.typography.labelLarge)
        }
      }
    }
  }
}

private fun createTag(
  coroutineScope: CoroutineScope,
  sheetState: SheetState,
  tagsPresenter: TagsPresenter,
  tagName: TextFieldValue
) {
  coroutineScope
    .launch { sheetState.hide() }
    .invokeOnCompletion { tagsPresenter.dispatch(TagsEvent.CreateTag(tagName.text)) }
}
