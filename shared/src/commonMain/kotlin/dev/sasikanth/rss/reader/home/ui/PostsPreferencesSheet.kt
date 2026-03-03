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

package dev.sasikanth.rss.reader.home.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import dev.sasikanth.rss.reader.components.ToggleableButtonGroup
import dev.sasikanth.rss.reader.components.ToggleableButtonItem
import dev.sasikanth.rss.reader.core.model.local.PostsSortOrder
import dev.sasikanth.rss.reader.core.model.local.PostsType
import dev.sasikanth.rss.reader.resources.icons.CalendarClock
import dev.sasikanth.rss.reader.resources.icons.FilterList
import dev.sasikanth.rss.reader.resources.icons.Sort
import dev.sasikanth.rss.reader.resources.icons.TwineIcons
import dev.sasikanth.rss.reader.ui.AppTheme
import org.jetbrains.compose.resources.stringResource
import twine.shared.generated.resources.Res
import twine.shared.generated.resources.filter
import twine.shared.generated.resources.postsAll
import twine.shared.generated.resources.postsLast24Hours
import twine.shared.generated.resources.postsSortBy
import twine.shared.generated.resources.postsSortByPostAdded
import twine.shared.generated.resources.postsSortByPostDate
import twine.shared.generated.resources.postsSortLatest
import twine.shared.generated.resources.postsSortOldest
import twine.shared.generated.resources.postsToday
import twine.shared.generated.resources.postsUnread
import twine.shared.generated.resources.sort

@Composable
internal fun PostsPreferencesSheet(
  postsType: PostsType,
  postsSortOrder: PostsSortOrder,
  onApply: (PostsType, PostsSortOrder) -> Unit,
  onDismiss: () -> Unit,
  modifier: Modifier = Modifier,
) {
  ModalBottomSheet(
    modifier = modifier,
    onDismissRequest = onDismiss,
    containerColor = AppTheme.colorScheme.surfaceContainerLowest,
    contentColor = AppTheme.colorScheme.onSurface,
    sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
  ) {
    var selectedPostsType by remember(postsType) { mutableStateOf(postsType) }
    var selectedSortBy by
      remember(postsSortOrder) {
        mutableStateOf(
          when (postsSortOrder) {
            PostsSortOrder.Latest,
            PostsSortOrder.Oldest -> SortBy.Date
            PostsSortOrder.AddedLatest,
            PostsSortOrder.AddedOldest -> SortBy.Added
          }
        )
      }
    var selectedSortDirection by
      remember(postsSortOrder) {
        mutableStateOf(
          when (postsSortOrder) {
            PostsSortOrder.Latest,
            PostsSortOrder.AddedLatest -> SortDirection.Newest
            PostsSortOrder.Oldest,
            PostsSortOrder.AddedOldest -> SortDirection.Oldest
          }
        )
      }

    Column(
      modifier =
        Modifier.fillMaxWidth()
          .verticalScroll(rememberScrollState())
          .padding(horizontal = 24.dp)
          .padding(bottom = 32.dp)
          .navigationBarsPadding()
    ) {
      SectionHeader(text = stringResource(Res.string.postsSortBy), icon = TwineIcons.CalendarClock)

      ToggleableButtonGroup(
        modifier = Modifier.fillMaxWidth(),
        items =
          listOf(
            ToggleableButtonItem(
              label = stringResource(Res.string.postsSortByPostDate),
              isSelected = selectedSortBy == SortBy.Date,
              identifier = SortBy.Date,
            ),
            ToggleableButtonItem(
              label = stringResource(Res.string.postsSortByPostAdded),
              isSelected = selectedSortBy == SortBy.Added,
              identifier = SortBy.Added,
            ),
          ),
        onItemSelected = {
          selectedSortBy = it.identifier as SortBy
          val postsSortOrder = getSortedPostsOrder(selectedSortBy, selectedSortDirection)

          onApply(selectedPostsType, postsSortOrder)
        },
      )

      SectionHeader(text = stringResource(Res.string.sort), icon = TwineIcons.Sort)

      ToggleableButtonGroup(
        modifier = Modifier.fillMaxWidth(),
        items =
          listOf(
            ToggleableButtonItem(
              label = stringResource(Res.string.postsSortLatest),
              isSelected = selectedSortDirection == SortDirection.Newest,
              identifier = SortDirection.Newest,
            ),
            ToggleableButtonItem(
              label = stringResource(Res.string.postsSortOldest),
              isSelected = selectedSortDirection == SortDirection.Oldest,
              identifier = SortDirection.Oldest,
            ),
          ),
        onItemSelected = {
          selectedSortDirection = it.identifier as SortDirection
          val postsSortOrder = getSortedPostsOrder(selectedSortBy, selectedSortDirection)

          onApply(selectedPostsType, postsSortOrder)
        },
      )

      SectionHeader(text = stringResource(Res.string.filter), icon = TwineIcons.FilterList)

      ToggleableButtonGroup(
        modifier = Modifier.fillMaxWidth(),
        items =
          listOf(
            ToggleableButtonItem(
              label = stringResource(Res.string.postsAll),
              isSelected = selectedPostsType == PostsType.ALL,
              identifier = PostsType.ALL,
            ),
            ToggleableButtonItem(
              label = stringResource(Res.string.postsToday),
              isSelected = selectedPostsType == PostsType.TODAY,
              identifier = PostsType.TODAY,
            ),
            ToggleableButtonItem(
              label = stringResource(Res.string.postsLast24Hours),
              isSelected = selectedPostsType == PostsType.LAST_24_HOURS,
              identifier = PostsType.LAST_24_HOURS,
            ),
            ToggleableButtonItem(
              label = stringResource(Res.string.postsUnread),
              isSelected = selectedPostsType == PostsType.UNREAD,
              identifier = PostsType.UNREAD,
            ),
          ),
        onItemSelected = {
          selectedPostsType = it.identifier as PostsType
          val postsSortOrder = getSortedPostsOrder(selectedSortBy, selectedSortDirection)

          onApply(selectedPostsType, postsSortOrder)
        },
      )
    }
  }
}

private fun getSortedPostsOrder(
  selectedSortBy: SortBy,
  selectedSortDirection: SortDirection,
): PostsSortOrder {
  val postsSortOrder =
    when (selectedSortBy) {
      SortBy.Date ->
        if (selectedSortDirection == SortDirection.Newest) PostsSortOrder.Latest
        else PostsSortOrder.Oldest

      SortBy.Added ->
        if (selectedSortDirection == SortDirection.Newest) PostsSortOrder.AddedLatest
        else PostsSortOrder.AddedOldest
    }
  return postsSortOrder
}

@Composable
private fun SectionHeader(text: String, icon: ImageVector, modifier: Modifier = Modifier) {
  Row(
    modifier = modifier.padding(top = 24.dp, bottom = 12.dp),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(8.dp),
  ) {
    Icon(
      imageVector = icon,
      contentDescription = null,
      tint = AppTheme.colorScheme.onSurfaceVariant,
      modifier = Modifier.requiredSize(16.dp),
    )
    Text(
      text = text,
      style = MaterialTheme.typography.labelLarge,
      color = AppTheme.colorScheme.onSurfaceVariant,
    )
  }
}

private enum class SortBy {
  Date,
  Added,
}

private enum class SortDirection {
  Newest,
  Oldest,
}
