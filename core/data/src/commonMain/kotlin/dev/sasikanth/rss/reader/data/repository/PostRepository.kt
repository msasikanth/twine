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

import androidx.paging.PagingSource
import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.paging3.QueryPagingSource
import dev.sasikanth.rss.reader.core.model.local.Post
import dev.sasikanth.rss.reader.core.model.local.PostFlag
import dev.sasikanth.rss.reader.core.model.local.PostsSortOrder
import dev.sasikanth.rss.reader.core.model.local.ResolvedPost
import dev.sasikanth.rss.reader.data.database.PostQueries
import dev.sasikanth.rss.reader.data.utils.Constants
import dev.sasikanth.rss.reader.di.scopes.AppScope
import dev.sasikanth.rss.reader.util.DispatchersProvider
import kotlin.time.Instant
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import me.tatarka.inject.annotations.Inject

@Inject
@AppScope
class PostRepository(
  private val postQueries: PostQueries,
  private val dispatchersProvider: DispatchersProvider,
) {

  suspend fun postsCountForFeed(feedId: String): Long {
    return withContext(dispatchersProvider.databaseRead) {
      postQueries.countPostsForFeed(feedId).executeAsOne()
    }
  }

  suspend fun allPostsCount(
    activeSourceIds: List<String>,
    unreadOnly: Boolean? = null,
    after: Instant = Instant.DISTANT_PAST,
    postsUpperBound: Instant = Instant.DISTANT_FUTURE,
  ): Long? {
    return withContext(dispatchersProvider.databaseRead) {
      postQueries
        .allPostsCount(
          isSourceIdsEmpty = activeSourceIds.isEmpty(),
          sourceIds = activeSourceIds,
          unreadOnly = unreadOnly,
          postsAfter = after,
          postsUpperBound = postsUpperBound,
        )
        .executeAsOneOrNull()
    }
  }

  fun allPosts(
    activeSourceIds: List<String>,
    postsSortOrder: PostsSortOrder,
    unreadOnly: Boolean? = null,
    after: Instant = Instant.DISTANT_PAST,
    postsUpperBound: Instant = Instant.DISTANT_FUTURE,
  ): PagingSource<Int, ResolvedPost> {
    return QueryPagingSource(
      countQuery =
        postQueries.allPostsCount(
          isSourceIdsEmpty = activeSourceIds.isEmpty(),
          sourceIds = activeSourceIds,
          unreadOnly = unreadOnly,
          postsAfter = after,
          postsUpperBound = postsUpperBound,
        ),
      transacter = postQueries,
      context = dispatchersProvider.databaseRead,
      queryProvider = { limit, offset ->
        when (postsSortOrder) {
          PostsSortOrder.Latest ->
            postQueries.allPostsLatest(
              postsAfter = after,
              postsUpperBound = postsUpperBound,
              isSourceIdsEmpty = activeSourceIds.isEmpty(),
              sourceIds = activeSourceIds,
              unreadOnly = unreadOnly,
              limit = limit,
              offset = offset,
              mapper = ::mapToResolvedPost,
            )
          PostsSortOrder.Oldest ->
            postQueries.allPostsOldest(
              postsAfter = after,
              postsUpperBound = postsUpperBound,
              isSourceIdsEmpty = activeSourceIds.isEmpty(),
              sourceIds = activeSourceIds,
              unreadOnly = unreadOnly,
              limit = limit,
              offset = offset,
              mapper = ::mapToResolvedPost,
            )
          PostsSortOrder.AddedLatest ->
            postQueries.allPostsAddedLatest(
              postsAfter = after,
              postsUpperBound = postsUpperBound,
              isSourceIdsEmpty = activeSourceIds.isEmpty(),
              sourceIds = activeSourceIds,
              unreadOnly = unreadOnly,
              limit = limit,
              offset = offset,
              mapper = ::mapToResolvedPost,
            )
          PostsSortOrder.AddedOldest ->
            postQueries.allPostsAddedOldest(
              postsAfter = after,
              postsUpperBound = postsUpperBound,
              isSourceIdsEmpty = activeSourceIds.isEmpty(),
              sourceIds = activeSourceIds,
              unreadOnly = unreadOnly,
              limit = limit,
              offset = offset,
              mapper = ::mapToResolvedPost,
            )
        }
      },
    )
  }

  fun featuredPosts(
    activeSourceIds: List<String>,
    postsSortOrder: PostsSortOrder,
    unreadOnly: Boolean? = null,
    after: Instant = Instant.DISTANT_PAST,
    featuredPostsAfter: Instant = Instant.DISTANT_PAST,
    postsUpperBound: Instant = Instant.DISTANT_FUTURE,
    limit: Long = Constants.NUMBER_OF_FEATURED_POSTS,
  ): Flow<List<ResolvedPost>> {
    val query =
      when (postsSortOrder) {
        PostsSortOrder.Latest ->
          postQueries.featuredPostsLatest(
            featuredPostsAfter = featuredPostsAfter,
            postsAfter = after,
            postsUpperBound = postsUpperBound,
            isSourceIdsEmpty = activeSourceIds.isEmpty(),
            sourceIds = activeSourceIds,
            unreadOnly = unreadOnly,
            limit = limit,
            mapper = ::mapToResolvedPost,
          )
        PostsSortOrder.Oldest ->
          postQueries.featuredPostsOldest(
            featuredPostsAfter = featuredPostsAfter,
            postsAfter = after,
            postsUpperBound = postsUpperBound,
            isSourceIdsEmpty = activeSourceIds.isEmpty(),
            sourceIds = activeSourceIds,
            unreadOnly = unreadOnly,
            limit = limit,
            mapper = ::mapToResolvedPost,
          )
        PostsSortOrder.AddedLatest ->
          postQueries.featuredPostsAddedLatest(
            featuredPostsAfter = featuredPostsAfter,
            postsAfter = after,
            postsUpperBound = postsUpperBound,
            isSourceIdsEmpty = activeSourceIds.isEmpty(),
            sourceIds = activeSourceIds,
            unreadOnly = unreadOnly,
            limit = limit,
            mapper = ::mapToResolvedPost,
          )
        PostsSortOrder.AddedOldest ->
          postQueries.featuredPostsAddedOldest(
            featuredPostsAfter = featuredPostsAfter,
            postsAfter = after,
            postsUpperBound = postsUpperBound,
            isSourceIdsEmpty = activeSourceIds.isEmpty(),
            sourceIds = activeSourceIds,
            unreadOnly = unreadOnly,
            limit = limit,
            mapper = ::mapToResolvedPost,
          )
      }

    return query.asFlow().mapToList(dispatchersProvider.databaseRead)
  }

  suspend fun featuredPostsBlocking(
    activeSourceIds: List<String>,
    postsSortOrder: PostsSortOrder,
    unreadOnly: Boolean? = null,
    after: Instant = Instant.DISTANT_PAST,
    featuredPostsAfter: Instant = Instant.DISTANT_PAST,
    postsUpperBound: Instant = Instant.DISTANT_FUTURE,
    limit: Long = Constants.NUMBER_OF_FEATURED_POSTS,
  ): List<ResolvedPost> {
    return withContext(dispatchersProvider.databaseRead) {
      val query =
        when (postsSortOrder) {
          PostsSortOrder.Latest ->
            postQueries.featuredPostsLatest(
              featuredPostsAfter = featuredPostsAfter,
              postsAfter = after,
              postsUpperBound = postsUpperBound,
              isSourceIdsEmpty = activeSourceIds.isEmpty(),
              sourceIds = activeSourceIds,
              unreadOnly = unreadOnly,
              limit = limit,
              mapper = ::mapToResolvedPost,
            )
          PostsSortOrder.Oldest ->
            postQueries.featuredPostsOldest(
              featuredPostsAfter = featuredPostsAfter,
              postsAfter = after,
              postsUpperBound = postsUpperBound,
              isSourceIdsEmpty = activeSourceIds.isEmpty(),
              sourceIds = activeSourceIds,
              unreadOnly = unreadOnly,
              limit = limit,
              mapper = ::mapToResolvedPost,
            )
          PostsSortOrder.AddedLatest ->
            postQueries.featuredPostsAddedLatest(
              featuredPostsAfter = featuredPostsAfter,
              postsAfter = after,
              postsUpperBound = postsUpperBound,
              isSourceIdsEmpty = activeSourceIds.isEmpty(),
              sourceIds = activeSourceIds,
              unreadOnly = unreadOnly,
              limit = limit,
              mapper = ::mapToResolvedPost,
            )
          PostsSortOrder.AddedOldest ->
            postQueries.featuredPostsAddedOldest(
              featuredPostsAfter = featuredPostsAfter,
              postsAfter = after,
              postsUpperBound = postsUpperBound,
              isSourceIdsEmpty = activeSourceIds.isEmpty(),
              sourceIds = activeSourceIds,
              unreadOnly = unreadOnly,
              limit = limit,
              mapper = ::mapToResolvedPost,
            )
        }

      query.executeAsList()
    }
  }

  fun nonFeaturedPosts(
    activeSourceIds: List<String>,
    postsSortOrder: PostsSortOrder,
    unreadOnly: Boolean? = null,
    after: Instant = Instant.DISTANT_PAST,
    featuredPostsAfter: Instant = Instant.DISTANT_PAST,
    postsUpperBound: Instant = Instant.DISTANT_FUTURE,
    numberOfFeaturedPosts: Long = Constants.NUMBER_OF_FEATURED_POSTS,
  ): PagingSource<Int, ResolvedPost> {
    return QueryPagingSource(
      countQuery =
        postQueries.nonFeaturedPostsCount(
          featuredPostsAfter = featuredPostsAfter,
          postsAfter = after,
          postsUpperBound = postsUpperBound,
          isSourceIdsEmpty = activeSourceIds.isEmpty(),
          sourceIds = activeSourceIds,
          unreadOnly = unreadOnly,
          numberOfFeaturedPosts = numberOfFeaturedPosts,
          postsSortOrder = postsSortOrder.name,
        ),
      transacter = postQueries,
      context = dispatchersProvider.databaseRead,
      queryProvider = { limit, offset ->
        when (postsSortOrder) {
          PostsSortOrder.Latest ->
            postQueries.nonFeaturedPostsLatest(
              featuredPostsAfter = featuredPostsAfter,
              postsAfter = after,
              postsUpperBound = postsUpperBound,
              isSourceIdsEmpty = activeSourceIds.isEmpty(),
              sourceIds = activeSourceIds,
              unreadOnly = unreadOnly,
              numberOfFeaturedPosts = numberOfFeaturedPosts,
              limit = limit,
              offset = offset,
              mapper = ::mapToResolvedPost,
            )
          PostsSortOrder.Oldest ->
            postQueries.nonFeaturedPostsOldest(
              featuredPostsAfter = featuredPostsAfter,
              postsAfter = after,
              postsUpperBound = postsUpperBound,
              isSourceIdsEmpty = activeSourceIds.isEmpty(),
              sourceIds = activeSourceIds,
              unreadOnly = unreadOnly,
              numberOfFeaturedPosts = numberOfFeaturedPosts,
              limit = limit,
              offset = offset,
              mapper = ::mapToResolvedPost,
            )
          PostsSortOrder.AddedLatest ->
            postQueries.nonFeaturedPostsAddedLatest(
              featuredPostsAfter = featuredPostsAfter,
              postsAfter = after,
              postsUpperBound = postsUpperBound,
              isSourceIdsEmpty = activeSourceIds.isEmpty(),
              sourceIds = activeSourceIds,
              unreadOnly = unreadOnly,
              numberOfFeaturedPosts = numberOfFeaturedPosts,
              limit = limit,
              offset = offset,
              mapper = ::mapToResolvedPost,
            )
          PostsSortOrder.AddedOldest ->
            postQueries.nonFeaturedPostsAddedOldest(
              featuredPostsAfter = featuredPostsAfter,
              postsAfter = after,
              postsUpperBound = postsUpperBound,
              isSourceIdsEmpty = activeSourceIds.isEmpty(),
              sourceIds = activeSourceIds,
              unreadOnly = unreadOnly,
              numberOfFeaturedPosts = numberOfFeaturedPosts,
              limit = limit,
              offset = offset,
              mapper = ::mapToResolvedPost,
            )
        }
      },
    )
  }

  suspend fun postPosition(
    postId: String,
    activeSourceIds: List<String>,
    postsSortOrder: PostsSortOrder,
    sourceId: String? = null,
    unreadOnly: Boolean? = null,
    after: Instant = Instant.DISTANT_PAST,
    postsUpperBound: Instant = Instant.DISTANT_FUTURE,
  ): Int? {
    return withContext(dispatchersProvider.databaseRead) {
      val post =
        if (sourceId != null) {
          postQueries.post(id = postId, sourceId = sourceId, mapper = ::Post).executeAsOneOrNull()
        } else {
          postQueries.postById(id = postId, mapper = ::Post).executeAsList().firstOrNull()
        } ?: return@withContext null

      val query =
        when (postsSortOrder) {
          PostsSortOrder.Latest ->
            postQueries.postPositionLatest(
              postId = postId,
              isSourceIdsEmpty = activeSourceIds.isEmpty(),
              sourceIds = activeSourceIds,
              unreadOnly = unreadOnly,
              postsAfter = after,
              postsUpperBound = postsUpperBound,
              postDate = post.postDate,
              postCreatedAt = post.createdAt,
            )
          PostsSortOrder.Oldest ->
            postQueries.postPositionOldest(
              postId = postId,
              isSourceIdsEmpty = activeSourceIds.isEmpty(),
              sourceIds = activeSourceIds,
              unreadOnly = unreadOnly,
              postsAfter = after,
              postsUpperBound = postsUpperBound,
              postDate = post.postDate,
              postCreatedAt = post.createdAt,
            )
          PostsSortOrder.AddedLatest ->
            postQueries.postPositionAddedLatest(
              postId = postId,
              isSourceIdsEmpty = activeSourceIds.isEmpty(),
              sourceIds = activeSourceIds,
              unreadOnly = unreadOnly,
              postsAfter = after,
              postsUpperBound = postsUpperBound,
              postDate = post.postDate,
              postCreatedAt = post.createdAt,
            )
          PostsSortOrder.AddedOldest ->
            postQueries.postPositionAddedOldest(
              postId = postId,
              isSourceIdsEmpty = activeSourceIds.isEmpty(),
              sourceIds = activeSourceIds,
              unreadOnly = unreadOnly,
              postsAfter = after,
              postsUpperBound = postsUpperBound,
              postDate = post.postDate,
              postCreatedAt = post.createdAt,
            )
        }

      query.executeAsOne().toInt()
    }
  }

  suspend fun nonFeaturedPostPosition(
    postId: String,
    activeSourceIds: List<String>,
    postsSortOrder: PostsSortOrder,
    sourceId: String? = null,
    unreadOnly: Boolean? = null,
    after: Instant = Instant.DISTANT_PAST,
    featuredPostsAfter: Instant = Instant.DISTANT_PAST,
    postsUpperBound: Instant = Instant.DISTANT_FUTURE,
    numberOfFeaturedPosts: Long = Constants.NUMBER_OF_FEATURED_POSTS,
  ): Int? {
    return withContext(dispatchersProvider.databaseRead) {
      val post =
        if (sourceId != null) {
          postQueries.post(id = postId, sourceId = sourceId, mapper = ::Post).executeAsOneOrNull()
        } else {
          postQueries.postById(id = postId, mapper = ::Post).executeAsList().firstOrNull()
        } ?: return@withContext null

      val query =
        when (postsSortOrder) {
          PostsSortOrder.Latest ->
            postQueries.nonFeaturedPostPositionLatest(
              postId = postId,
              featuredPostsAfter = featuredPostsAfter,
              postsAfter = after,
              postsUpperBound = postsUpperBound,
              numberOfFeaturedPosts = numberOfFeaturedPosts,
              isSourceIdsEmpty = activeSourceIds.isEmpty(),
              sourceIds = activeSourceIds,
              unreadOnly = unreadOnly,
              postDate = post.postDate,
              postCreatedAt = post.createdAt,
            )
          PostsSortOrder.Oldest ->
            postQueries.nonFeaturedPostPositionOldest(
              postId = postId,
              featuredPostsAfter = featuredPostsAfter,
              postsAfter = after,
              postsUpperBound = postsUpperBound,
              numberOfFeaturedPosts = numberOfFeaturedPosts,
              isSourceIdsEmpty = activeSourceIds.isEmpty(),
              sourceIds = activeSourceIds,
              unreadOnly = unreadOnly,
              postDate = post.postDate,
              postCreatedAt = post.createdAt,
            )
          PostsSortOrder.AddedLatest ->
            postQueries.nonFeaturedPostPositionAddedLatest(
              postId = postId,
              featuredPostsAfter = featuredPostsAfter,
              postsAfter = after,
              postsUpperBound = postsUpperBound,
              numberOfFeaturedPosts = numberOfFeaturedPosts,
              isSourceIdsEmpty = activeSourceIds.isEmpty(),
              sourceIds = activeSourceIds,
              unreadOnly = unreadOnly,
              postDate = post.postDate,
              postCreatedAt = post.createdAt,
            )
          PostsSortOrder.AddedOldest ->
            postQueries.nonFeaturedPostPositionAddedOldest(
              postId = postId,
              featuredPostsAfter = featuredPostsAfter,
              postsAfter = after,
              postsUpperBound = postsUpperBound,
              numberOfFeaturedPosts = numberOfFeaturedPosts,
              isSourceIdsEmpty = activeSourceIds.isEmpty(),
              sourceIds = activeSourceIds,
              unreadOnly = unreadOnly,
              postDate = post.postDate,
              postCreatedAt = post.createdAt,
            )
        }

      query.executeAsOne().toInt()
    }
  }

  suspend fun post(postId: String): Post {
    return withContext(dispatchersProvider.databaseRead) {
      postQueries.postById(postId, ::Post).executeAsList().first()
    }
  }

  suspend fun postOrNull(postId: String): Post? {
    return withContext(dispatchersProvider.databaseRead) {
      postQueries.postById(postId, ::Post).executeAsList().firstOrNull()
    }
  }

  suspend fun resolvedPostById(postId: String): ResolvedPost? {
    return withContext(dispatchersProvider.databaseRead) {
      postQueries.resolvedPostById(id = postId, mapper = ::mapToResolvedPost).executeAsOneOrNull()
    }
  }

  suspend fun hasPost(id: String): Boolean {
    return withContext(dispatchersProvider.databaseRead) {
      postQueries.postById(id).executeAsOneOrNull() != null
    }
  }

  suspend fun postsWithImagesAndNoSeedColor(limit: Long): List<ResolvedPost> {
    return withContext(dispatchersProvider.databaseRead) {
      postQueries
        .postsWithImagesAndNoSeedColor(limit = limit, mapper = ::mapToResolvedPost)
        .executeAsList()
    }
  }

  suspend fun postByLink(link: String): Post? {
    return withContext(dispatchersProvider.databaseRead) {
      postQueries.postByLink(link, ::Post).executeAsOneOrNull()
    }
  }

  suspend fun postsByLinks(links: Set<String>): List<Post> {
    return withContext(dispatchersProvider.databaseRead) {
      postQueries.postsByLinks(links, ::Post).executeAsList()
    }
  }

  suspend fun updateAudioProgress(postId: String, audioProgress: Long) {
    withContext(dispatchersProvider.databaseWrite) {
      postQueries.updateAudioProgress(audioProgress = audioProgress, id = postId)
    }
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
    remoteId: String?,
    feedName: String,
    feedIcon: String,
    feedHomepageLink: String,
    alwaysFetchFullArticle: Boolean,
    showFeedFavIcon: Boolean,
    feedContentReadingTime: Long?,
    articleContentReadingTime: Long?,
    seedColor: Long?,
    audioProgress: Long,
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
      alwaysFetchFullArticle = alwaysFetchFullArticle,
      showFeedFavIcon = showFeedFavIcon,
      feedContentReadingTime = feedContentReadingTime?.toInt(),
      articleContentReadingTime = articleContentReadingTime?.toInt(),
      seedColor = seedColor?.toInt(),
      remoteId = remoteId,
      audioProgress = audioProgress,
    )
  }
}
