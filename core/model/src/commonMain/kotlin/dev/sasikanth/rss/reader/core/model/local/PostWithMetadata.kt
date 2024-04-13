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
import kotlinx.datetime.Instant

@Immutable
data class PostWithMetadata(
  val id: String,
  val title: String,
  val description: String,
  val imageUrl: String?,
  val date: Instant,
  val link: String,
  val bookmarked: Boolean,
  val feedName: String,
  val feedIcon: String,
  val feedLink: String,
  val commentsLink: String?,
  val read: Boolean,
)
