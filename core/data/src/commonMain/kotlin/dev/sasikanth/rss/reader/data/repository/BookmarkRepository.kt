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
package dev.sasikanth.rss.reader.data.repository

import app.cash.paging.PagingSource
import app.cash.sqldelight.paging3.QueryPagingSource
import dev.sasikanth.rss.reader.core.base.widget.WidgetUpdater
import dev.sasikanth.rss.reader.core.model.local.PostFlag
import dev.sasikanth.rss.reader.core.model.local.ResolvedPost
import dev.sasikanth.rss.reader.data.database.BookmarkQueries
import dev.sasikanth.rss.reader.data.database.PostQueries
import dev.sasikanth.rss.reader.data.database.TransactionRunner
import dev.sasikanth.rss.reader.di.scopes.AppScope
import dev.sasikanth.rss.reader.util.DispatchersProvider
import kotlin.time.Clock
import kotlin.time.Instant
import kotlinx.coroutines.withContext
import me.tatarka.inject.annotations.Inject

@Inject
@AppScope
class BookmarkRepository(
  private val bookmarkQueries: BookmarkQueries,
  private val postQueries: PostQueries,
  private val transactionRunner: TransactionRunner,
  private val widgetUpdater: WidgetUpdater,
  private val dispatchersProvider: DispatchersProvider,
) {
  private companion object {
    private const val SQLITE_BATCH_SIZE = 990
  }

  suspend fun updateBookmarkStatus(bookmarked: Boolean, id: String) {
    withContext(dispatchersProvider.databaseWrite) {
      postQueries.updateBookmarkStatus(
        bookmarked = if (bookmarked) 1L else 0L,
        id = id,
        updatedAt = Clock.System.now(),
      )
    }
    widgetUpdater.updateUnreadWidget()
  }

  suspend fun updateBookmarkStatus(postIds: Set<String>, bookmarked: Boolean) {
    val postIdsSnapshot = postIds.toList()
    val now = Clock.System.now()
    withContext(dispatchersProvider.databaseWrite) {
      transactionRunner.invoke {
        postIdsSnapshot.chunked(SQLITE_BATCH_SIZE).forEach { chunk ->
          postQueries.updatePostsBookmarkStatus(
            bookmarked = if (bookmarked) 1L else 0L,
            updatedAt = now,
            ids = chunk,
          )
        }
      }
    }
    widgetUpdater.updateUnreadWidget()
  }

  suspend fun deleteBookmark(id: String) {
    withContext(dispatchersProvider.databaseWrite) { bookmarkQueries.deleteBookmark(id) }
  }

  suspend fun allBookmarkIdsBlocking(): List<String> {
    return withContext(dispatchersProvider.databaseRead) {
      bookmarkQueries.allBookmarkIds().executeAsList()
    }
  }

  fun bookmarks(): PagingSource<Int, ResolvedPost> {
    return QueryPagingSource(
      countQuery = bookmarkQueries.countBookmarks(),
      transacter = bookmarkQueries,
      context = dispatchersProvider.databaseRead,
      queryProvider = { limit, offset ->
        bookmarkQueries.bookmarks(limit, offset, mapper = ::mapToResolvedPost)
      },
    )
  }

  private fun mapToResolvedPost(
    id: String,
    sourceId: String,
    title: String,
    description: String,
    imageUrl: String?,
    audioUrl: String?,
    date: Instant,
    createdAt: Instant,
    link: String,
    commentsLink: String?,
    flags: Set<PostFlag>,
    feedName: String,
    feedIcon: String,
    feedHomepageLink: String,
    showFeedFavIcon: Boolean,
    feedContentReadingTime: Long?,
    articleContentReadingTime: Long?,
    seedColor: Long?,
  ): ResolvedPost {
    return ResolvedPost(
      id = id,
      sourceId = sourceId,
      title = title,
      description = description,
      imageUrl = imageUrl,
      audioUrl = audioUrl,
      date = date,
      createdAt = createdAt,
      link = link,
      commentsLink = commentsLink,
      flags = flags,
      feedName = feedName,
      feedIcon = feedIcon,
      feedHomepageLink = feedHomepageLink,
      alwaysFetchFullArticle = true,
      showFeedFavIcon = showFeedFavIcon,
      feedContentReadingTime = feedContentReadingTime?.toInt(),
      articleContentReadingTime = articleContentReadingTime?.toInt(),
      seedColor = seedColor?.toInt(),
    )
  }
}
