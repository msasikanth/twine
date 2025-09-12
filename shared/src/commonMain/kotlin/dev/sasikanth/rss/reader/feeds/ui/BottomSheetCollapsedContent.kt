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

package dev.sasikanth.rss.reader.feeds.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import dev.sasikanth.rss.reader.core.model.local.Feed
import dev.sasikanth.rss.reader.core.model.local.FeedGroup
import dev.sasikanth.rss.reader.core.model.local.Source
import dev.sasikanth.rss.reader.ui.AppTheme
import org.jetbrains.compose.resources.stringResource
import twine.shared.generated.resources.Res
import twine.shared.generated.resources.noPinnedSources

@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun BottomSheetCollapsedContent(
  pinnedSources: List<Source>,
  numberOfFeeds: Int,
  activeSource: Source?,
  canShowUnreadPostsCount: Boolean,
  onSourceClick: (Source) -> Unit,
  onHomeSelected: () -> Unit,
  modifier: Modifier = Modifier
) {
  LazyRow(
    modifier = modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.spacedBy(12.dp),
    verticalAlignment = Alignment.CenterVertically,
    contentPadding = PaddingValues(start = 24.dp, end = 24.dp, top = 13.dp, bottom = 24.dp)
  ) {
    if (activeSource != null && activeSource.pinnedAt == null) {
      item {
        SourceItem(
          source = activeSource,
          activeSource = activeSource,
          canShowUnreadPostsCount = canShowUnreadPostsCount,
          onHomeSelected = onHomeSelected,
          onSourceClick = onSourceClick
        )
      }

      item {
        VerticalDivider(
          modifier = Modifier.requiredHeight(24.dp),
          color = AppTheme.colorScheme.outlineVariant,
        )
      }
    }

    items(pinnedSources.size) { index ->
      SourceItem(
        source = pinnedSources[index],
        activeSource = activeSource,
        canShowUnreadPostsCount = canShowUnreadPostsCount,
        onHomeSelected = onHomeSelected,
        onSourceClick = onSourceClick
      )
    }

    if (pinnedSources.isEmpty() && numberOfFeeds > 0 && activeSource == null) {
      item {
        Text(
          text = stringResource(Res.string.noPinnedSources),
          color = AppTheme.colorScheme.onSurfaceVariant,
          style = MaterialTheme.typography.bodyMedium,
          textAlign = TextAlign.Center,
          modifier = Modifier.fillParentMaxWidth().padding(top = 16.dp)
        )
      }
    }
  }
}

@Composable
private fun SourceItem(
  source: Source,
  activeSource: Source?,
  canShowUnreadPostsCount: Boolean,
  onHomeSelected: () -> Unit,
  onSourceClick: (Source) -> Unit,
) {
  when (val source = source) {
    is FeedGroup -> {
      FeedGroupBottomBarItem(
        feedGroup = source,
        canShowUnreadPostsCount = canShowUnreadPostsCount,
        hasActiveSource = activeSource != null,
        selected = activeSource?.id == source.id,
        onClick = {
          if (activeSource?.id == source.id) {
            onHomeSelected()
          } else {
            onSourceClick(source)
          }
        }
      )
    }
    is Feed -> {
      FeedBottomBarItem(
        badgeCount = source.numberOfUnreadPosts,
        homePageUrl = source.homepageLink,
        feedIconUrl = source.icon,
        canShowUnreadPostsCount = canShowUnreadPostsCount,
        hasActiveSource = activeSource != null,
        selected = activeSource?.id == source.id,
        onClick = {
          if (activeSource?.id == source.id) {
            onHomeSelected()
          } else {
            onSourceClick(source)
          }
        },
      )
    }
  }
}
