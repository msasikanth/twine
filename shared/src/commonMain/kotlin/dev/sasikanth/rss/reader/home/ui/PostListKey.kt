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

package dev.sasikanth.rss.reader.home.ui

import dev.sasikanth.rss.reader.core.model.local.ResolvedPost
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

private val json = Json

@Serializable
data class PostListKey(val postId: String, val feedId: String) {

  fun encode(): String = json.encodeToString(this)

  companion object {

    fun from(post: ResolvedPost): PostListKey {
      return PostListKey(postId = post.id, feedId = post.sourceId)
    }

    fun decode(key: String): PostListKey = json.decodeFromString(key)
  }
}
