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

package dev.sasikanth.rss.reader.core.model.local

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList

@Immutable
data class FeedReadCount(
  val feedId: String,
  val feedName: String,
  val feedIcon: String,
  val homepageLink: String,
  val readCount: Long,
)

@Immutable data class ReadingTrend(val date: String, val count: Long)

@Immutable
data class ReadingStatistics(
  val totalReadCount: Long,
  val topFeeds: ImmutableList<FeedReadCount>,
  val readingTrends: ImmutableList<ReadingTrend>,
)
