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

import kotlin.time.Instant

data class Post(
  val id: String,
  val sourceId: String,
  val title: String,
  val description: String,
  val imageUrl: String?,
  val postDate: Instant,
  val createdAt: Instant,
  val updatedAt: Instant,
  val syncedAt: Instant,
  val link: String,
  val commentsLink: String?,
  val flags: Set<PostFlag>,
  val remoteId: String? = null,
) {
  val bookmarked: Boolean
    get() = PostFlag.Bookmarked in flags

  val read: Boolean
    get() = PostFlag.Read in flags

  val hidden: Boolean
    get() = PostFlag.Hidden in flags
}
