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

package dev.sasikanth.rss.reader.settings.ui.items

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.paging.PagingData
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import androidx.paging.filter
import dev.sasikanth.rss.reader.components.IconButton
import dev.sasikanth.rss.reader.components.ModalSheet
import dev.sasikanth.rss.reader.components.TranslucentButton
import dev.sasikanth.rss.reader.components.image.FeedIcon
import dev.sasikanth.rss.reader.core.model.local.Feed
import dev.sasikanth.rss.reader.core.model.local.FeedGroup
import dev.sasikanth.rss.reader.core.model.local.Source
import dev.sasikanth.rss.reader.feeds.ui.FeedGroupItem
import dev.sasikanth.rss.reader.resources.icons.ArrowDown
import dev.sasikanth.rss.reader.resources.icons.Close
import dev.sasikanth.rss.reader.resources.icons.Home
import dev.sasikanth.rss.reader.resources.icons.HomeFilled
import dev.sasikanth.rss.reader.resources.icons.TwineIcons
import dev.sasikanth.rss.reader.ui.AppTheme
import dev.sasikanth.rss.reader.ui.LocalTranslucentStyles
import dev.sasikanth.rss.reader.utils.KeyboardState
import dev.sasikanth.rss.reader.utils.keyboardVisibilityAsState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import org.jetbrains.compose.resources.stringResource
import twine.shared.generated.resources.Res
import twine.shared.generated.resources.appBarAllFeeds
import twine.shared.generated.resources.feedsSearchHint
import twine.shared.generated.resources.settingsDefaultSourceSubtitle
import twine.shared.generated.resources.settingsDefaultSourceTitle

@Composable
internal fun DefaultSourceSettingItem(
  defaultSource: Source?,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  SettingItem(
    modifier = modifier,
    title = stringResource(Res.string.settingsDefaultSourceTitle),
    subtitle = stringResource(Res.string.settingsDefaultSourceSubtitle),
    onClick = onClick,
    action = {
      val label =
        when (defaultSource) {
          is Feed -> defaultSource.name
          is FeedGroup -> defaultSource.name
          else -> stringResource(Res.string.appBarAllFeeds)
        }

      TranslucentButton(
        modifier = Modifier.widthIn(max = 180.dp),
        text = label,
        trailingIcon = TwineIcons.ArrowDown,
        onClick = onClick,
      )
    },
  )
}

@Composable
internal fun DefaultSourceSelectionSheet(
  sources: Flow<PagingData<Source>>,
  selectedSourceId: String?,
  onSourceSelected: (String?) -> Unit,
  onDismiss: () -> Unit,
) {
  val sheetContainerColor = AppTheme.colorScheme.surfaceContainerLow

  ModalSheet(
    onDismissRequest = onDismiss,
    containerColor = sheetContainerColor,
    contentColor = AppTheme.colorScheme.onSurface,
  ) {
    var searchQuery by remember { mutableStateOf(TextFieldValue()) }
    val filteredSources =
      remember(sources) {
        combine(sources, snapshotFlow { searchQuery.text }) { pagingData, query ->
          if (query.isBlank()) {
            pagingData
          } else {
            pagingData.filter { source ->
              val name =
                when (source) {
                  is Feed -> source.name
                  is FeedGroup -> source.name
                  else -> ""
                }
              name.contains(query, ignoreCase = true)
            }
          }
        }
      }
    val sourceItems = filteredSources.collectAsLazyPagingItems()

    Column(modifier = Modifier.fillMaxWidth()) {
      Text(
        modifier = Modifier.padding(horizontal = 24.dp).padding(top = 24.dp),
        text = stringResource(Res.string.settingsDefaultSourceTitle),
        color = AppTheme.colorScheme.onSurface,
        style = MaterialTheme.typography.titleLarge,
      )

      Spacer(Modifier.height(4.dp))

      Text(
        modifier = Modifier.padding(horizontal = 24.dp).padding(bottom = 16.dp),
        text = stringResource(Res.string.settingsDefaultSourceSubtitle),
        style = MaterialTheme.typography.labelLarge,
        color = AppTheme.colorScheme.onSurfaceVariant,
      )

      SearchBar(
        query = searchQuery,
        onQueryChange = { searchQuery = it },
        onClearClick = { searchQuery = TextFieldValue() },
      )

      HorizontalDivider(color = AppTheme.colorScheme.outlineVariant)

      LazyColumn(
        modifier = Modifier.weight(1f, fill = false),
        contentPadding = PaddingValues(top = 8.dp, bottom = 24.dp),
      ) {
        if (searchQuery.text.isBlank()) {
          item(key = "all_feeds", contentType = "AllFeedsItem") {
            AllFeedsOptionItem(
              selected = selectedSourceId == null,
              onClick = {
                onSourceSelected(null)
                onDismiss()
              },
              modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            )
          }
        }

        items(
          count = sourceItems.itemCount,
          key = sourceItems.itemKey { it.id },
          contentType = { sourceItems[it]?.sourceType },
        ) { index ->
          when (val source = sourceItems[index]) {
            is FeedGroup -> {
              FeedGroupItem(
                feedGroup = source,
                canShowUnreadPostsCount = false,
                isInMultiSelectMode = false,
                selected = selectedSourceId == source.id,
                onFeedGroupSelected = {},
                onFeedGroupClick = {
                  onSourceSelected(source.id)
                  onDismiss()
                },
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
              )
            }
            is Feed -> {
              DefaultFeedOptionItem(
                feed = source,
                selected = selectedSourceId == source.id,
                onClick = {
                  onSourceSelected(source.id)
                  onDismiss()
                },
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
              )
            }
            null -> {
              // no-op
            }
          }
        }
      }
    }
  }
}

@Composable
private fun SearchBar(
  query: TextFieldValue,
  onQueryChange: (TextFieldValue) -> Unit,
  onClearClick: () -> Unit,
) {
  val keyboardState by keyboardVisibilityAsState()
  val focusManager = LocalFocusManager.current
  val translucentStyle = LocalTranslucentStyles.current
  val colorScheme = AppTheme.colorScheme

  LaunchedEffect(keyboardState) {
    if (keyboardState == KeyboardState.Closed) {
      focusManager.clearFocus()
    }
  }

  OutlinedTextField(
    modifier =
      Modifier.fillMaxWidth().focusable(false).padding(horizontal = 16.dp).padding(bottom = 16.dp),
    value = query,
    onValueChange = onQueryChange,
    placeholder = {
      Text(
        text = stringResource(Res.string.feedsSearchHint),
        color = colorScheme.onSurfaceVariant,
        style = MaterialTheme.typography.bodyMedium,
      )
    },
    trailingIcon = {
      if (query.text.isNotBlank()) {
        IconButton(icon = TwineIcons.Close, contentDescription = null, onClick = onClearClick)
      }
    },
    shape = RoundedCornerShape(50),
    singleLine = true,
    textStyle = MaterialTheme.typography.bodyMedium,
    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
    colors =
      OutlinedTextFieldDefaults.colors(
        focusedBorderColor = translucentStyle.default.outline,
        unfocusedBorderColor = translucentStyle.default.outline,
        focusedTextColor = colorScheme.onSurfaceVariant,
        disabledTextColor = Color.Transparent,
        unfocusedContainerColor = translucentStyle.default.background,
        focusedContainerColor = translucentStyle.default.background,
      ),
  )
}

@Composable
private fun AllFeedsOptionItem(
  selected: Boolean,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val translucentStyle = LocalTranslucentStyles.current
  val backgroundColor by
    animateColorAsState(
      if (selected) {
        translucentStyle.default.background
      } else {
        Color.Transparent
      }
    )

  Box(
    modifier =
      Modifier.fillMaxWidth()
        .then(modifier)
        .clip(MaterialTheme.shapes.large)
        .drawBehind { drawRect(backgroundColor) }
        .clickable(onClick = onClick)
        .padding(8.dp)
  ) {
    Row(verticalAlignment = Alignment.CenterVertically) {
      Box(
        modifier =
          Modifier.requiredSize(36.dp)
            .background(translucentStyle.default.background, RoundedCornerShape(25)),
        contentAlignment = Alignment.Center,
      ) {
        Icon(
          modifier = Modifier.requiredSize(20.dp),
          imageVector = if (selected) TwineIcons.HomeFilled else TwineIcons.Home,
          contentDescription = null,
          tint = AppTheme.colorScheme.onSurface,
        )
      }

      Spacer(Modifier.requiredWidth(16.dp))

      Text(
        modifier = Modifier.weight(1f),
        text = stringResource(Res.string.appBarAllFeeds),
        style = MaterialTheme.typography.titleSmall,
        color = AppTheme.colorScheme.onSurface,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
      )
    }
  }
}

@Composable
private fun DefaultFeedOptionItem(
  feed: Feed,
  selected: Boolean,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val translucentStyle = LocalTranslucentStyles.current
  val backgroundColor by
    animateColorAsState(
      if (selected) {
        translucentStyle.default.background
      } else {
        Color.Transparent
      }
    )

  Box(
    modifier =
      Modifier.fillMaxWidth()
        .then(modifier)
        .clip(MaterialTheme.shapes.large)
        .drawBehind { drawRect(backgroundColor) }
        .clickable(onClick = onClick)
        .padding(8.dp)
  ) {
    Row(verticalAlignment = Alignment.CenterVertically) {
      FeedIcon(
        icon = feed.icon,
        homepageLink = feed.homepageLink,
        showFeedFavIcon = feed.showFeedFavIcon,
        contentDescription = null,
        modifier = Modifier.requiredSize(36.dp),
        contentScale = ContentScale.Crop,
      )

      Spacer(Modifier.requiredWidth(16.dp))

      Text(
        modifier = Modifier.weight(1f),
        text = feed.name,
        style = MaterialTheme.typography.titleSmall,
        color = AppTheme.colorScheme.onSurface,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
      )
    }
  }
}
