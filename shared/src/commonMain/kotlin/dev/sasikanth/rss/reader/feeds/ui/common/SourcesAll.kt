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

package dev.sasikanth.rss.reader.feeds.ui.common

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.itemKey
import dev.sasikanth.rss.reader.components.CircularIconButton
import dev.sasikanth.rss.reader.components.DropdownMenu
import dev.sasikanth.rss.reader.components.DropdownMenuDivider
import dev.sasikanth.rss.reader.components.DropdownMenuItem
import dev.sasikanth.rss.reader.core.model.local.Feed
import dev.sasikanth.rss.reader.core.model.local.FeedGroup
import dev.sasikanth.rss.reader.core.model.local.Source
import dev.sasikanth.rss.reader.core.model.local.SourceType
import dev.sasikanth.rss.reader.data.repository.FeedsOrderBy
import dev.sasikanth.rss.reader.feeds.SourceListItem
import dev.sasikanth.rss.reader.feeds.ui.FeedGroupItem
import dev.sasikanth.rss.reader.feeds.ui.FeedListItem
import dev.sasikanth.rss.reader.resources.icons.Add
import dev.sasikanth.rss.reader.resources.icons.Check
import dev.sasikanth.rss.reader.resources.icons.Edit
import dev.sasikanth.rss.reader.resources.icons.MarkAllAsRead
import dev.sasikanth.rss.reader.resources.icons.NewGroup
import dev.sasikanth.rss.reader.resources.icons.RemoveFeed
import dev.sasikanth.rss.reader.resources.icons.Sort
import dev.sasikanth.rss.reader.resources.icons.TwineIcons
import dev.sasikanth.rss.reader.ui.AppTheme
import dev.sasikanth.rss.reader.utils.bottomPaddingOfSourceItem
import org.jetbrains.compose.resources.stringResource
import twine.shared.generated.resources.Res
import twine.shared.generated.resources.actionAddTo
import twine.shared.generated.resources.actionSelect
import twine.shared.generated.resources.allFeeds
import twine.shared.generated.resources.buttonAddFeed
import twine.shared.generated.resources.edit
import twine.shared.generated.resources.feedOptionRemove
import twine.shared.generated.resources.feedsFilterSectionTitle
import twine.shared.generated.resources.feedsFilterUnreadOnly
import twine.shared.generated.resources.feedsNoUnread
import twine.shared.generated.resources.feedsSortAlphabetical
import twine.shared.generated.resources.feedsSortLatest
import twine.shared.generated.resources.feedsSortOldest
import twine.shared.generated.resources.feedsSortSectionTitle
import twine.shared.generated.resources.markAllAsRead
import twine.shared.generated.resources.sort
import twine.shared.generated.resources.sourcesCount

internal fun LazyListScope.allSources(
  numberOfFeedGroups: Int,
  sources: LazyPagingItems<SourceListItem>,
  hideFeedsWithoutUnreadPosts: Boolean,
  selectedSources: Set<Source>,
  activeSource: Source?,
  canShowUnreadPostsCount: Boolean,
  isInMultiSelectMode: Boolean,
  onSourceClick: (Source) -> Unit,
  onToggleSourceSelection: (Source) -> Unit,
  onPinClick: (Source) -> Unit,
  onSourceEditClick: (Source) -> Unit,
  onAddToGroupClick: (Source) -> Unit,
  onRemoveSourceClick: (Source) -> Unit,
  onMarkAsReadClick: (Source) -> Unit,
) {
  if (sources.itemCount > 0) {
    items(
      count = sources.itemCount,
      key =
        sources.itemKey {
          when (it) {
            SourceListItem.Separator -> "Separator"
            is SourceListItem.SourceItem -> it.source.id
          }
        },
      contentType = {
        when (val sourceItem = sources[it]) {
          SourceListItem.Separator -> "Separator"
          is SourceListItem.SourceItem -> {
            if (sourceItem.source.sourceType == SourceType.FeedGroup) {
              "FeedGroupItem"
            } else {
              "FeedListItem"
            }
          }
          null -> null
        }
      },
    ) { index ->
      when (val sourceItem = sources[index]) {
        SourceListItem.Separator -> {
          Spacer(Modifier.requiredHeight(8.dp))
        }
        is SourceListItem.SourceItem -> {
          when (val source = sourceItem.source) {
            is FeedGroup -> {
              val startPadding = 8.dp
              val endPadding = 8.dp
              val topPadding = 4.dp
              val bottomPadding = bottomPaddingOfSourceItem(index, sources.itemCount)

              FeedGroupItem(
                feedGroup = source,
                canShowUnreadPostsCount = canShowUnreadPostsCount,
                isInMultiSelectMode = isInMultiSelectMode,
                selected =
                  if (isInMultiSelectMode) {
                    selectedSources.any { it.id == source.id }
                  } else {
                    activeSource?.id == source.id
                  },
                onFeedGroupSelected = onToggleSourceSelection,
                onFeedGroupClick = onSourceClick,
                onPinClick = onPinClick,
                modifier =
                  Modifier.padding(
                      start = startPadding,
                      top = topPadding,
                      end = endPadding,
                      bottom = bottomPadding,
                    )
                    .animateItem(),
                dropdownMenuContent = { onDismiss ->
                  DropdownMenuItem(
                    text = stringResource(Res.string.markAllAsRead),
                    leadingIcon = TwineIcons.MarkAllAsRead,
                    enabled = source.numberOfUnreadPosts > 0,
                    onClick = {
                      onMarkAsReadClick(source)
                      onDismiss()
                    },
                  )

                  DropdownMenuItem(
                    text = stringResource(Res.string.edit),
                    leadingIcon = TwineIcons.Edit,
                    onClick = {
                      onSourceEditClick(source)
                      onDismiss()
                    },
                  )

                  DropdownMenuItem(
                    text = stringResource(Res.string.actionSelect),
                    leadingIcon = TwineIcons.Check,
                    onClick = {
                      onToggleSourceSelection(source)
                      onDismiss()
                    },
                  )

                  DropdownMenuDivider()

                  DropdownMenuItem(
                    text = stringResource(Res.string.feedOptionRemove),
                    leadingIcon = TwineIcons.RemoveFeed,
                    onClick = {
                      onRemoveSourceClick(source)
                      onDismiss()
                    },
                  )
                },
              )
            }
            is Feed -> {
              // When there are even number of feed groups, we are offsetting the index
              // to make sure the spacing is properly applied to feed list items after the
              // separator.
              val transformedIndex =
                if (numberOfFeedGroups % 2 == 0 && numberOfFeedGroups > 0) {
                  index - 1
                } else {
                  index
                }
              val startPadding = 8.dp
              val endPadding = 8.dp
              val topPadding = 4.dp
              val bottomPadding = bottomPaddingOfSourceItem(transformedIndex, sources.itemCount)

              FeedListItem(
                feed = source,
                canShowUnreadPostsCount = canShowUnreadPostsCount,
                isInMultiSelectMode = isInMultiSelectMode,
                isFeedSelected =
                  if (isInMultiSelectMode) {
                    selectedSources.any { it.id == source.id }
                  } else {
                    activeSource?.id == source.id
                  },
                onFeedClick = onSourceClick,
                onFeedSelected = onToggleSourceSelection,
                onPinClick = onPinClick,
                onSyncErrorClick = onSourceEditClick,
                modifier =
                  Modifier.padding(
                      start = startPadding,
                      top = topPadding,
                      end = endPadding,
                      bottom = bottomPadding,
                    )
                    .animateItem(),
                dropdownMenuContent = { onDismiss ->
                  DropdownMenuItem(
                    text = stringResource(Res.string.markAllAsRead),
                    leadingIcon = TwineIcons.MarkAllAsRead,
                    enabled = source.numberOfUnreadPosts > 0,
                    onClick = {
                      onMarkAsReadClick(source)
                      onDismiss()
                    },
                  )

                  DropdownMenuItem(
                    text = stringResource(Res.string.actionAddTo),
                    leadingIcon = TwineIcons.NewGroup,
                    onClick = {
                      onAddToGroupClick(source)
                      onDismiss()
                    },
                  )

                  DropdownMenuItem(
                    text = stringResource(Res.string.edit),
                    leadingIcon = TwineIcons.Edit,
                    onClick = {
                      onSourceEditClick(source)
                      onDismiss()
                    },
                  )

                  DropdownMenuItem(
                    text = stringResource(Res.string.actionSelect),
                    leadingIcon = TwineIcons.Check,
                    onClick = {
                      onToggleSourceSelection(source)
                      onDismiss()
                    },
                  )

                  DropdownMenuDivider()

                  DropdownMenuItem(
                    text = stringResource(Res.string.feedOptionRemove),
                    leadingIcon = TwineIcons.RemoveFeed,
                    onClick = {
                      onRemoveSourceClick(source)
                      onDismiss()
                    },
                  )
                },
              )
            }
          }
        }
        null -> {
          // no-op
        }
      }
    }
  } else if (hideFeedsWithoutUnreadPosts && sources.loadState.refresh is LoadState.NotLoading) {
    item { NoUnreadFeeds() }
  }
}

@Composable
private fun NoUnreadFeeds() {
  Text(
    modifier = Modifier.fillMaxWidth().padding(horizontal = 32.dp, vertical = 24.dp),
    text = stringResource(Res.string.feedsNoUnread),
    style = MaterialTheme.typography.labelLarge,
    color = AppTheme.colorScheme.onSurfaceVariant,
    textAlign = TextAlign.Center,
  )
}

@Composable
internal fun AllFeedsHeader(
  feedsCount: Int,
  visibleFeedsCount: Int,
  feedsSortOrder: FeedsOrderBy,
  hideFeedsWithoutUnreadPosts: Boolean,
  onFeedsSortChanged: (FeedsOrderBy) -> Unit,
  onHideFeedsWithoutUnreadPostsChanged: (Boolean) -> Unit,
  modifier: Modifier = Modifier,
  showAddButton: Boolean = true,
  onAddNewFeedClick: (() -> Unit)? = null,
) {
  Row(
    modifier =
      Modifier.then(modifier).padding(horizontal = 24.dp).padding(top = 8.dp, bottom = 16.dp),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    var showSortDropdown by remember { mutableStateOf(false) }
    val allFeedsLabel = stringResource(Res.string.allFeeds)

    Column(
      modifier =
        Modifier.weight(1f).clearAndSetSemantics {
          contentDescription = "${allFeedsLabel}: ${visibleFeedsCount}"
        },
      horizontalAlignment = Alignment.Start,
    ) {
      Text(
        text = allFeedsLabel,
        style = MaterialTheme.typography.titleMedium,
        color = AppTheme.colorScheme.onSurface,
      )

      Text(
        text = stringResource(Res.string.sourcesCount, visibleFeedsCount),
        style = MaterialTheme.typography.bodySmall,
        color = AppTheme.colorScheme.onSurfaceVariant,
      )
    }

    AnimatedVisibility(visible = feedsCount > 0, enter = fadeIn(), exit = fadeOut()) {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Spacer(Modifier.requiredWidth(12.dp))

        Box {
          CircularIconButton(
            icon = TwineIcons.Sort,
            label = stringResource(Res.string.sort),
            onClick = { showSortDropdown = true },
          )

          DropdownMenu(
            modifier = Modifier.widthIn(min = 132.dp),
            expanded = showSortDropdown,
            onDismissRequest = { showSortDropdown = false },
          ) {
            DropdownMenuSectionTitle(text = stringResource(Res.string.feedsSortSectionTitle))

            FeedsOrderBy.entries
              .filter { it != FeedsOrderBy.Pinned }
              .forEach { sortOrder ->
                val label =
                  when (sortOrder) {
                    FeedsOrderBy.Latest -> stringResource(Res.string.feedsSortLatest)
                    FeedsOrderBy.Oldest -> stringResource(Res.string.feedsSortOldest)
                    FeedsOrderBy.Alphabetical -> stringResource(Res.string.feedsSortAlphabetical)
                    FeedsOrderBy.Pinned -> {
                      throw IllegalStateException(
                        "Cannot use the following feed sort order here: $feedsSortOrder"
                      )
                    }
                  }

                DropdownMenuItem(
                  selected = feedsSortOrder == sortOrder,
                  text = label,
                  onClick = {
                    onFeedsSortChanged(sortOrder)
                    showSortDropdown = false
                  },
                )
              }

            DropdownMenuDivider()

            DropdownMenuSectionTitle(text = stringResource(Res.string.feedsFilterSectionTitle))

            DropdownMenuItem(
              selected = hideFeedsWithoutUnreadPosts,
              text = stringResource(Res.string.feedsFilterUnreadOnly),
              trailingIcon = if (hideFeedsWithoutUnreadPosts) TwineIcons.Check else null,
              onClick = {
                onHideFeedsWithoutUnreadPostsChanged(!hideFeedsWithoutUnreadPosts)
                showSortDropdown = false
              },
            )
          }
        }
      }
    }

    if (showAddButton) {
      Spacer(Modifier.width(12.dp))

      CircularIconButton(
        icon = TwineIcons.Add,
        label = stringResource(Res.string.buttonAddFeed),
        backgroundColor = AppTheme.colorScheme.inverseSurface,
        contentColor = AppTheme.colorScheme.inverseOnSurface,
        borderColor = Color.Transparent,
        onClick = { onAddNewFeedClick?.invoke() },
      )
    }
  }
}

@Composable
private fun DropdownMenuSectionTitle(text: String) {
  Text(
    modifier = Modifier.padding(horizontal = 20.dp).padding(top = 12.dp, bottom = 4.dp),
    text = text,
    style = MaterialTheme.typography.labelMedium,
    color = AppTheme.colorScheme.onSurfaceVariant,
  )
}
