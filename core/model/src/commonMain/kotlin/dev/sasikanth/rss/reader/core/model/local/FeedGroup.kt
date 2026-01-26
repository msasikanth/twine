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

package dev.sasikanth.rss.reader.core.model.local

import androidx.compose.runtime.Immutable
import kotlin.time.Instant

@Immutable
data class FeedGroup(
  override val id: String,
  val name: String,
  val feedIds: List<String>,
  val feedHomepageLinks: List<String>,
  val feedIconLinks: List<String>,
  val feedShowFavIconSettings: List<Boolean>,
  val numberOfUnreadPosts: Long = 0,
  val createdAt: Instant,
  val updatedAt: Instant,
  override val pinnedAt: Instant?,
  override val sourceType: SourceType = SourceType.FeedGroup,
  override val pinnedPosition: Double = 0.0,
  val isDeleted: Boolean = false,
  val remoteId: String? = null,
) : Source
