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

package dev.sasikanth.rss.reader.group

import androidx.paging.PagingData
import dev.sasikanth.rss.reader.core.model.local.Feed
import dev.sasikanth.rss.reader.core.model.local.FeedGroup
import dev.sasikanth.rss.reader.core.model.local.Source
import dev.sasikanth.rss.reader.data.repository.FeedsOrderBy
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

data class GroupState(
  val group: FeedGroup?,
  val feeds: Flow<PagingData<Feed>>,
  val selectedSources: Set<Source>,
  val feedsOrderBy: FeedsOrderBy,
) {

  val isInMultiSelectMode: Boolean
    get() = selectedSources.isNotEmpty()

  companion object {

    val DEFAULT =
      GroupState(
        group = null,
        feeds = emptyFlow(),
        selectedSources = emptySet(),
        feedsOrderBy = FeedsOrderBy.Latest
      )
  }
}
