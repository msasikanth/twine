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

import dev.sasikanth.rss.reader.core.model.local.Feed

sealed interface PinnedFeedsListItemType {

  val key: String
  val contentType: String

  data class PinnedFeedListItem(
    val feed: Feed,
    override val key: String = "PinnedFeed:${feed.link}",
    override val contentType: String = "PinnedFeed"
  ) : PinnedFeedsListItemType

  data class PinnedFeedsHeader(
    val isExpanded: Boolean,
    override val key: String = "PinnedFeedsHeader",
    override val contentType: String = "PinnedFeedsHeader"
  ) : PinnedFeedsListItemType
}
