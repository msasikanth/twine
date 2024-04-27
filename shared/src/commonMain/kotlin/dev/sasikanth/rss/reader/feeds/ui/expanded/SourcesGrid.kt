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

package dev.sasikanth.rss.reader.feeds.ui.expanded

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.dp

@Composable
internal fun SourcesGrid(
  pinnedSources: LazyGridScope.() -> Unit,
  allSources: LazyGridScope.() -> Unit,
  searchResults: LazyGridScope.() -> Unit,
  isInSearchMode: Boolean,
  padding: PaddingValues,
  modifier: Modifier = Modifier
) {
  val layoutDirection = LocalLayoutDirection.current

  LazyVerticalGrid(
    modifier = Modifier.fillMaxSize().then(modifier),
    columns = GridCells.Fixed(2),
    contentPadding =
      PaddingValues(
        start = padding.calculateStartPadding(layoutDirection),
        end = padding.calculateEndPadding(layoutDirection),
        bottom = padding.calculateBottomPadding() + 64.dp,
        top = 8.dp
      ),
  ) {
    if (isInSearchMode) {
      pinnedSources()
      allSources()
    } else {
      searchResults()
    }
  }
}

internal fun bottomPaddingOfSourceItem(index: Int, itemCount: Int) =
  when {
    index < itemCount -> 8.dp
    else -> 0.dp
  }

internal fun topPaddingOfSourceItem(gridItemSpan: GridItemSpan, index: Int) =
  when {
    gridItemSpan.currentLineSpan == 2 && index > 0 -> 8.dp
    gridItemSpan.currentLineSpan == 1 && index > 1 -> 8.dp
    else -> 0.dp
  }

internal fun endPaddingOfSourceItem(gridItemSpan: GridItemSpan, index: Int) =
  when {
    gridItemSpan.currentLineSpan == 2 || (gridItemSpan.currentLineSpan == 1 && index % 2 == 1) ->
      24.dp
    else -> 8.dp
  }

internal fun startPaddingOfSourceItem(gridItemSpan: GridItemSpan, index: Int) =
  when {
    gridItemSpan.currentLineSpan == 2 || (gridItemSpan.currentLineSpan == 1 && index % 2 == 0) ->
      24.dp
    else -> 8.dp
  }
