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
import kotlin.time.Instant
import kotlinx.serialization.Serializable

@Immutable
@Serializable
data class ResolvedPost(
  val id: String,
  val sourceId: String,
  val title: String,
  val description: String,
  val imageUrl: String?,
  val audioUrl: String?,
  val date: Instant,
  val createdAt: Instant,
  val link: String,
  val commentsLink: String?,
  val flags: Set<PostFlag>,
  val feedName: String,
  val feedIcon: String,
  val feedHomepageLink: String,
  val alwaysFetchFullArticle: Boolean,
  val showFeedFavIcon: Boolean,
  val feedContentReadingTime: Int? = null,
  val articleContentReadingTime: Int? = null,
  val remoteId: String? = null,
) {
  val bookmarked: Boolean
    get() = PostFlag.Bookmarked in flags

  val read: Boolean
    get() = PostFlag.Read in flags
}
