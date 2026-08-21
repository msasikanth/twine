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

private const val PREFIX = "post_key_"

data class PostListKey(val postId: String, val feedId: String) {

  fun encode(): String = encode(postId, feedId)

  companion object {

    fun from(post: ResolvedPost): PostListKey {
      return PostListKey(postId = post.id, feedId = post.sourceId)
    }

    /**
     * Builds the list key straight from [post]. Lazy list key lambdas run per item on the measure
     * path, so this skips the intermediate [PostListKey] the two-step form allocates.
     */
    fun encode(post: ResolvedPost): String = encode(post.id, post.sourceId)

    private fun encode(postId: String, feedId: String): String = "$PREFIX${postId}_${feedId}"

    /**
     * Extracts just the post id from an encoded key. Callers that only need the id should prefer
     * this over [decodeSafe], which additionally allocates the feed id substring and a
     * [PostListKey] wrapper — meaningful when it runs per visible item while scrolling.
     */
    fun decodePostId(key: String): String? {
      val separatorIndex = separatorIndexOf(key) ?: return null
      return key.substring(PREFIX.length, separatorIndex)
    }

    fun decodeSafe(key: String): PostListKey? {
      val separatorIndex = separatorIndexOf(key) ?: return null
      return PostListKey(
        postId = key.substring(PREFIX.length, separatorIndex),
        feedId = key.substring(separatorIndex + 1),
      )
    }

    private fun separatorIndexOf(key: String): Int? {
      if (!key.startsWith(PREFIX)) return null
      // feedId is the segment after the last underscore; splitting on every
      // underscore would corrupt any id that itself contains one.
      val separatorIndex = key.lastIndexOf('_')
      if (separatorIndex <= PREFIX.length || separatorIndex == key.lastIndex) return null
      return separatorIndex
    }
  }
}
