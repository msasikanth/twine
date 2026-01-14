/*
 * Copyright 2026 Sasikanth Miriyampalli
 *
 * Licensed under the GPL, Version 3.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.gnu.org/licenses/gpl-3.0.en.html
 *
 */

package dev.sasikanth.rss.reader.home.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import dev.sasikanth.rss.reader.components.ToggleableButtonGroup
import dev.sasikanth.rss.reader.components.ToggleableButtonItem
import dev.sasikanth.rss.reader.core.model.local.PostsSortOrder
import dev.sasikanth.rss.reader.core.model.local.PostsType
import dev.sasikanth.rss.reader.resources.icons.FilterList
import dev.sasikanth.rss.reader.resources.icons.Sort
import dev.sasikanth.rss.reader.resources.icons.TwineIcons
import dev.sasikanth.rss.reader.ui.AppTheme
import org.jetbrains.compose.resources.stringResource
import twine.shared.generated.resources.Res
import twine.shared.generated.resources.buttonApply
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
          .padding(top = 24.dp, bottom = 16.dp)
          .navigationBarsPadding()
    ) {
      SectionHeader(text = stringResource(Res.string.postsSortBy), icon = TwineIcons.Sort)

      Spacer(Modifier.requiredHeight(12.dp))

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
        onItemSelected = { selectedSortBy = it.identifier as SortBy },
      )

      Spacer(Modifier.requiredHeight(16.dp))

      SectionHeader(text = stringResource(Res.string.sort), icon = TwineIcons.Sort)

      Spacer(Modifier.requiredHeight(12.dp))

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
        onItemSelected = { selectedSortDirection = it.identifier as SortDirection },
      )

      Spacer(Modifier.requiredHeight(24.dp))

      SectionHeader(text = stringResource(Res.string.filter), icon = TwineIcons.FilterList)

      Spacer(Modifier.requiredHeight(12.dp))

      PostsFilterGrid(
        selectedPostsType = selectedPostsType,
        onPostTypeSelected = { selectedPostsType = it }
      )

      Spacer(Modifier.requiredHeight(24.dp))

      TextButton(
        modifier = Modifier.fillMaxWidth().requiredHeight(56.dp),
        onClick = {
          val postsSortOrder =
            when (selectedSortBy) {
              SortBy.Date ->
                if (selectedSortDirection == SortDirection.Newest) PostsSortOrder.Latest
                else PostsSortOrder.Oldest
              SortBy.Added ->
                if (selectedSortDirection == SortDirection.Newest) PostsSortOrder.AddedLatest
                else PostsSortOrder.AddedOldest
            }
          onApply(selectedPostsType, postsSortOrder)
        },
        colors =
          ButtonDefaults.textButtonColors(
            contentColor = AppTheme.colorScheme.tintedForeground,
            containerColor = AppTheme.colorScheme.tintedSurface
          ),
        shape = MaterialTheme.shapes.medium,
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
      ) {
        Text(
          text = stringResource(Res.string.buttonApply),
          style = MaterialTheme.typography.labelLarge
        )
      }
    }
  }
}

@Composable
private fun PostsFilterGrid(
  selectedPostsType: PostsType,
  onPostTypeSelected: (PostsType) -> Unit,
  modifier: Modifier = Modifier,
) {
  Column(modifier = modifier.fillMaxWidth()) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
      PostsFilterButton(
        label = getPostTypeLabel(PostsType.ALL),
        isSelected = selectedPostsType == PostsType.ALL,
        onClick = { onPostTypeSelected(PostsType.ALL) },
        modifier = Modifier.weight(1f)
      )
      PostsFilterButton(
        label = getPostTypeLabel(PostsType.UNREAD),
        isSelected = selectedPostsType == PostsType.UNREAD,
        onClick = { onPostTypeSelected(PostsType.UNREAD) },
        modifier = Modifier.weight(1f)
      )
    }

    Spacer(Modifier.requiredHeight(8.dp))

    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
      PostsFilterButton(
        label = getPostTypeLabel(PostsType.TODAY),
        isSelected = selectedPostsType == PostsType.TODAY,
        onClick = { onPostTypeSelected(PostsType.TODAY) },
        modifier = Modifier.weight(1f)
      )
      PostsFilterButton(
        label = getPostTypeLabel(PostsType.LAST_24_HOURS),
        isSelected = selectedPostsType == PostsType.LAST_24_HOURS,
        onClick = { onPostTypeSelected(PostsType.LAST_24_HOURS) },
        modifier = Modifier.weight(1f)
      )
    }
  }
}

@Composable
private fun PostsFilterButton(
  label: String,
  isSelected: Boolean,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val backgroundColor by
    animateColorAsState(
      if (isSelected) {
        AppTheme.colorScheme.tintedForeground
      } else {
        Color.Transparent
      }
    )
  val borderColor by
    animateColorAsState(
      if (isSelected) {
        Color.Transparent
      } else {
        AppTheme.colorScheme.surfaceContainerHigh
      }
    )
  val textColor by
    animateColorAsState(
      if (isSelected) {
        AppTheme.colorScheme.tintedSurface
      } else {
        AppTheme.colorScheme.tintedForeground
      }
    )

  Box(
    modifier =
      modifier
        .requiredHeight(56.dp)
        .clip(MaterialTheme.shapes.medium)
        .background(color = backgroundColor, shape = MaterialTheme.shapes.medium)
        .border(width = 1.dp, color = borderColor, shape = MaterialTheme.shapes.medium)
        .clickable(onClick = onClick)
        .padding(8.dp),
    contentAlignment = Alignment.Center
  ) {
    Text(
      text = label,
      style = MaterialTheme.typography.bodyMedium,
      fontWeight = FontWeight.Medium,
      color = textColor,
      textAlign = TextAlign.Center
    )
  }
}

@Composable
private fun SectionHeader(
  text: String,
  icon: ImageVector,
  modifier: Modifier = Modifier,
) {
  Row(
    modifier = modifier.padding(horizontal = 0.dp, vertical = 0.dp),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(8.dp)
  ) {
    Icon(
      imageVector = icon,
      contentDescription = null,
      tint = AppTheme.colorScheme.onSurfaceVariant,
      modifier = Modifier.requiredSize(20.dp)
    )
    Text(
      text = text,
      style = MaterialTheme.typography.titleMedium,
      color = AppTheme.colorScheme.onSurfaceVariant,
    )
  }
}

@Composable
private fun getPostTypeLabel(type: PostsType) =
  when (type) {
    PostsType.ALL -> stringResource(Res.string.postsAll)
    PostsType.UNREAD -> stringResource(Res.string.postsUnread)
    PostsType.TODAY -> stringResource(Res.string.postsToday)
    PostsType.LAST_24_HOURS -> stringResource(Res.string.postsLast24Hours)
  }

private enum class SortBy {
  Date,
  Added
}

private enum class SortDirection {
  Newest,
  Oldest
}
