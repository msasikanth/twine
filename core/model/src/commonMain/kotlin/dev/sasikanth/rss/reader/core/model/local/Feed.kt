/*
 * Copyright 2023 Sasikanth Miriyampalli
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
import kotlin.time.Duration
import kotlin.time.Duration.Companion.hours
import kotlinx.datetime.Instant

@Immutable
data class Feed(
  override val id: String,
  val name: String,
  val icon: String,
  val description: String,
  val link: String,
  val homepageLink: String,
  val createdAt: Instant,
  override val pinnedAt: Instant?,
  val lastCleanUpAt: Instant? = null,
  val numberOfUnreadPosts: Long = 0L,
  val lastUpdatedAt: Instant? = null,
  val refreshInterval: Duration = 1.hours,
  val alwaysFetchSourceArticle: Boolean = false,
  override val sourceType: SourceType = SourceType.Feed,
  override val pinnedPosition: Double = 0.0,
  val showFeedFavIcon: Boolean = true,
) : Source
