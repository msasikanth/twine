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

package dev.sasikanth.rss.reader.blockedwords

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.clearText
import androidx.compose.foundation.text.input.rememberTextFieldState
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
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.sasikanth.rss.reader.components.CircularIconButton
import dev.sasikanth.rss.reader.core.model.local.BlockedWord
import dev.sasikanth.rss.reader.resources.icons.ArrowBack
import dev.sasikanth.rss.reader.resources.icons.DeleteOutline
import dev.sasikanth.rss.reader.resources.icons.TwineIcons
import dev.sasikanth.rss.reader.ui.AppTheme
import dev.sasikanth.rss.reader.ui.LocalTranslucentStyles
import dev.sasikanth.rss.reader.utils.KeyboardState
import dev.sasikanth.rss.reader.utils.keyboardVisibilityAsState
import org.jetbrains.compose.resources.stringResource
import twine.shared.generated.resources.Res
import twine.shared.generated.resources.blockedWords
import twine.shared.generated.resources.blockedWordsDesc
import twine.shared.generated.resources.blockedWordsEmpty
import twine.shared.generated.resources.blockedWordsHint
import twine.shared.generated.resources.buttonAdd
import twine.shared.generated.resources.buttonGoBack
import twine.shared.generated.resources.delete
import twine.shared.generated.resources.readLess
import twine.shared.generated.resources.readMore

@Composable
fun BlockedWordsScreen(
  viewModel: BlockedWordsViewModel,
  goBack: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val state by viewModel.state.collectAsStateWithLifecycle()
  val translucentStyles = LocalTranslucentStyles.current
  val containerShape = RoundedCornerShape(50)

  Scaffold(
    modifier = modifier,
    topBar = {
      Box {
        CenterAlignedTopAppBar(
          title = {
            Text(
              text = stringResource(Res.string.blockedWords),
              color = AppTheme.colorScheme.onSurface,
              style = MaterialTheme.typography.titleMedium,
            )
          },
          navigationIcon = {
            CircularIconButton(
              modifier = Modifier.padding(start = 12.dp),
              icon = TwineIcons.ArrowBack,
              label = stringResource(Res.string.buttonGoBack),
              onClick = { goBack() },
            )
          },
          colors =
            TopAppBarDefaults.topAppBarColors(
              containerColor = AppTheme.colorScheme.surface,
              navigationIconContentColor = AppTheme.colorScheme.onSurface,
              titleContentColor = AppTheme.colorScheme.onSurface,
              actionIconContentColor = AppTheme.colorScheme.onSurface,
            ),
        )

        HorizontalDivider(
          modifier = Modifier.fillMaxWidth().align(Alignment.BottomStart),
          color = AppTheme.colorScheme.outlineVariant,
        )
      }
    },
    containerColor = AppTheme.colorScheme.backdrop,
    contentColor = Color.Unspecified,
  ) { innerPadding ->
    val newBlockedWord = rememberTextFieldState()
    val keyboardState by keyboardVisibilityAsState()
    val focusManager = LocalFocusManager.current

    LaunchedEffect(keyboardState) {
      if (keyboardState == KeyboardState.Closed) {
        focusManager.clearFocus()
      }
    }

    LazyColumn(modifier = Modifier.padding(innerPadding).fillMaxSize().imePadding()) {
      item { Spacer(Modifier.requiredHeight(16.dp)) }

      item {
        TextField(
          modifier =
            Modifier.fillMaxWidth()
              .padding(horizontal = 24.dp)
              .requiredHeight(56.dp)
              .border(1.dp, translucentStyles.default.outline, containerShape),
          state = newBlockedWord,
          keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
          onKeyboardAction = {
            if (newBlockedWord.text.isNotBlank()) {
              viewModel.dispatch(BlockedWordsEvent.AddBlockedWord(newBlockedWord.text.toString()))
              newBlockedWord.clearText()
            }
          },
          lineLimits = TextFieldLineLimits.SingleLine,
          textStyle = MaterialTheme.typography.labelLarge,
          shape = containerShape,
          enabled = true,
          colors =
            TextFieldDefaults.colors(
              unfocusedContainerColor = translucentStyles.default.background,
              focusedContainerColor = translucentStyles.default.background,
              disabledContainerColor = translucentStyles.default.background,
              focusedIndicatorColor = Color.Transparent,
              unfocusedIndicatorColor = Color.Transparent,
              disabledIndicatorColor = Color.Transparent,
              errorIndicatorColor = Color.Transparent,
              cursorColor = AppTheme.colorScheme.primary,
              selectionColors =
                TextSelectionColors(
                  handleColor = AppTheme.colorScheme.tintedForeground,
                  backgroundColor = AppTheme.colorScheme.tintedForeground.copy(0.4f),
                ),
            ),
          placeholder = {
            Text(
              text = stringResource(Res.string.blockedWordsHint),
              style = MaterialTheme.typography.labelLarge,
              color = AppTheme.colorScheme.tintedForeground.copy(alpha = 0.4f),
            )
          },
          trailingIcon = {
            val hasContent = newBlockedWord.text.isNotBlank()
            AnimatedVisibility(
              visible = hasContent,
              enter = fadeIn() + expandHorizontally(expandFrom = Alignment.Start),
              exit = fadeOut() + shrinkHorizontally(shrinkTowards = Alignment.Start),
            ) {
              IconButton(
                onClick = {
                  viewModel.dispatch(
                    BlockedWordsEvent.AddBlockedWord(newBlockedWord.text.toString())
                  )
                  newBlockedWord.clearText()
                }
              ) {
                Icon(
                  imageVector = Icons.Rounded.Check,
                  contentDescription = stringResource(Res.string.buttonAdd),
                  tint = AppTheme.colorScheme.primary,
                )
              }
            }
          },
        )
      }

      item { Spacer(Modifier.requiredHeight(16.dp)) }

      item {
        var expanded by remember { mutableStateOf(false) }
        val text = stringResource(Res.string.blockedWordsDesc)
        val readMore = stringResource(Res.string.readMore)
        val readLess = stringResource(Res.string.readLess)

        Text(
          modifier =
            Modifier.fillMaxWidth()
              .padding(horizontal = 24.dp)
              .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = { expanded = !expanded },
              ),
          text =
            buildAnnotatedString {
              if (expanded) {
                append(text)
                append(" ")
                withStyle(
                  SpanStyle(
                    color = AppTheme.colorScheme.tintedForeground,
                    fontWeight = FontWeight.Bold,
                  )
                ) {
                  append(readLess)
                }
              } else {
                val maxCharCount = 120
                if (text.length > maxCharCount) {
                  append(text.take(maxCharCount).substringBeforeLast(" "))
                  append("... ")
                  withStyle(
                    SpanStyle(
                      color = AppTheme.colorScheme.tintedForeground,
                      fontWeight = FontWeight.Bold,
                    )
                  ) {
                    append(readMore)
                  }
                } else {
                  append(text)
                }
              }
            },
          style = MaterialTheme.typography.labelLarge,
          color = AppTheme.colorScheme.textEmphasisMed,
        )
      }

      item {
        HorizontalDivider(
          modifier = Modifier.padding(top = 16.dp),
          color = AppTheme.colorScheme.outlineVariant,
        )
      }

      if (state.blockedWords.isEmpty()) {
        item {
          Text(
            modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp, vertical = 24.dp),
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
        Column(modifier = Modifier.animateItem()) {
          BlockedWordItem(
            modifier = Modifier.padding(horizontal = 24.dp),
            word = blockedWord,
            removeClicked = {
              viewModel.dispatch(BlockedWordsEvent.DeleteBlockedWord(blockedWord.id))
            },
          )

          if (index < state.blockedWords.lastIndex) {
            HorizontalDivider(
              modifier = Modifier.fillMaxWidth(),
              color = AppTheme.colorScheme.outlineVariant,
            )
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
  modifier: Modifier = Modifier,
) {
  Row(
    modifier = modifier.fillMaxWidth().padding(vertical = 12.dp),
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
        contentDescription = stringResource(Res.string.delete),
        tint = AppTheme.colorScheme.textEmphasisHigh,
      )
    }
  }
}
