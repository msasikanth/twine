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

package dev.sasikanth.rss.reader.feeds.ui.sheet.expanded

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.dp

@Composable
internal fun SourcesList(
  state: LazyListState,
  pinnedSources: LazyListScope.() -> Unit,
  allSources: LazyListScope.() -> Unit,
  searchResults: LazyListScope.() -> Unit,
  isInSearchMode: Boolean,
  padding: PaddingValues,
  modifier: Modifier = Modifier
) {
  val layoutDirection = LocalLayoutDirection.current

  LazyColumn(
    modifier = Modifier.fillMaxSize().then(modifier),
    state = state,
    contentPadding =
      PaddingValues(
        start = padding.calculateStartPadding(layoutDirection),
        end = padding.calculateEndPadding(layoutDirection),
        bottom = padding.calculateBottomPadding() + 64.dp,
        top = 8.dp
      ),
  ) {
    if (isInSearchMode) {
      searchResults()
    } else {
      pinnedSources()
      allSources()
    }
  }
}

internal fun bottomPaddingOfSourceItem(index: Int, itemCount: Int) =
  when {
    index < itemCount -> 8.dp
    else -> 0.dp
  }
