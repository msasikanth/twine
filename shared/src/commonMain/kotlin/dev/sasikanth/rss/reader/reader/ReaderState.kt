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

package dev.sasikanth.rss.reader.reader

import androidx.compose.runtime.Immutable
import dev.sasikanth.rss.reader.core.model.local.PostWithMetadata
import dev.sasikanth.rss.reader.util.readerDateTimestamp

@Immutable
internal data class ReaderState(
  val link: String?,
  val title: String?,
  val description: String?,
  val content: String?,
  val publishedAt: String?,
  val isBookmarked: Boolean?,
  val postImage: String?,
  val commentsLink: String?,
  val fetchFullArticle: Boolean,
  val feedIcon: String,
  val feedHomePageLink: String,
  val feedName: String,
) {

  companion object {

    fun default(post: PostWithMetadata): ReaderState {
      val hasContent = post.description.isNotBlank() || post.rawContent.isNullOrBlank().not()
      return ReaderState(
        link = post.link,
        title = post.title,
        description = post.description,
        content = post.rawContent ?: post.description,
        publishedAt = post.date.readerDateTimestamp(),
        isBookmarked = post.bookmarked,
        postImage = post.imageUrl,
        commentsLink = post.commentsLink,
        fetchFullArticle = post.alwaysFetchFullArticle || hasContent.not(),
        feedIcon = post.feedIcon,
        feedHomePageLink = post.feedHomepageLink,
        feedName = post.feedName,
      )
    }
  }
}
