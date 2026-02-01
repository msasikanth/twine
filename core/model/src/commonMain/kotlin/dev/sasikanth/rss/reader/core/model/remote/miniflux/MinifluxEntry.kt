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

package dev.sasikanth.rss.reader.core.model.remote.miniflux

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MinifluxEntry(
  val id: Long,
  @SerialName("feed_id") val feedId: Long,
  val status: String,
  val title: String,
  val url: String,
  val author: String?,
  val content: String,
  @SerialName("published_at") val publishedAt: String,
  val starred: Boolean,
  @SerialName("comments_url") val commentsUrl: String?,
  val enclosures: List<MinifluxEnclosure> = emptyList()
)

@Serializable
data class MinifluxEnclosure(val url: String, @SerialName("mime_type") val mimeType: String)

@Serializable
data class MinifluxEntryContent(
  val content: String,
  @SerialName("reading_time") val readingTime: Long
)
