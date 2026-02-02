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
data class MinifluxFeed(
  val id: Long,
  val title: String,
  @SerialName("site_url") val siteUrl: String,
  @SerialName("feed_url") val feedUrl: String,
  val category: MinifluxCategory,
  val icon: MinifluxFeedIcon,
)

@Serializable
data class MinifluxFeedIcon(@SerialName("external_icon_id") val externalIconId: String)
