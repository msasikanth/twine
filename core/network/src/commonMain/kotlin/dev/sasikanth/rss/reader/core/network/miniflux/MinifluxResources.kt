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

package dev.sasikanth.rss.reader.core.network.miniflux

import io.ktor.resources.Resource
import kotlinx.serialization.Serializable

@Serializable
@Resource("/v1")
class MinifluxApi {

  @Serializable @Resource("me") class Me(val parent: MinifluxApi = MinifluxApi())

  @Serializable @Resource("feeds") class Feeds(val parent: MinifluxApi = MinifluxApi())

  @Serializable
  @Resource("feeds/{feedId}")
  class Feed(val parent: MinifluxApi = MinifluxApi(), val feedId: Long) {

    @Serializable
    @Resource("entries")
    class Entries(
      val parent: Feed,
      val status: List<String>? = null,
      val limit: Int? = null,
      val offset: Int? = null,
      val after: Long? = null,
      val starred: String? = null,
      val order: String? = "published_at",
      val direction: String? = "desc",
    )
  }

  @Serializable
  @Resource("entries")
  class Entries(
    val parent: MinifluxApi = MinifluxApi(),
    val status: List<String>? = null,
    val limit: Int? = null,
    val offset: Int? = null,
    val after: Long? = null,
    val starred: String? = null,
    val order: String? = "published_at",
    val direction: String? = "desc",
  )

  @Serializable @Resource("entries") class UpdateEntries(val parent: MinifluxApi = MinifluxApi())

  @Serializable
  @Resource("entries/{entryId}/bookmark")
  class ToggleEntryBookmark(val parent: MinifluxApi = MinifluxApi(), val entryId: Long)

  @Serializable
  @Resource("feeds/{feedId}/entries")
  class FeedEntries(
    val parent: MinifluxApi = MinifluxApi(),
    val feedId: Long,
    val status: String? = null,
    val limit: Int? = null,
    val offset: Int? = null,
    val after: Long? = null,
  )

  @Serializable @Resource("categories") class Categories(val parent: MinifluxApi = MinifluxApi())

  @Serializable
  @Resource("categories/{categoryId}")
  class Category(val parent: MinifluxApi = MinifluxApi(), val categoryId: Long)
}
