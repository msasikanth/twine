/*
 * Copyright 2025 Sasikanth Miriyampalli
 *
 * Licensed under the GPL, Version 3.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.gnu.org/licenses/gpl-3.0.en.html
 *
 */

package dev.sasikanth.rss.reader.data.repository

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToOneOrNull
import co.touchlab.kermit.Logger
import dev.sasikanth.rss.reader.core.model.local.PostContent
import dev.sasikanth.rss.reader.data.database.PostContentQueries
import dev.sasikanth.rss.reader.util.DispatchersProvider
import kotlin.time.Instant
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.withContext
import me.tatarka.inject.annotations.Inject

@Inject
class PostContentRepository(
  private val postContentQueries: PostContentQueries,
  private val dispatcherProvider: DispatchersProvider,
) {

  fun postContent(postId: String): Flow<PostContent?> {
    return postContentQueries
      .getByPostId(postId) { id, rawContent, htmlContent, _, createdAt ->
        PostContent(id, rawContent, htmlContent)
      }
      .asFlow()
      .mapToOneOrNull(dispatcherProvider.databaseRead)
      .catch { error ->
        Logger.e("PostContentError", error) { "Failed to load post content for $postId" }
        emit(null)
      }
  }

  suspend fun updateFullArticleContent(postId: String, htmlContent: String?) {
    withContext(dispatcherProvider.databaseWrite) {
      postContentQueries.updateHtmlContent(htmlContent, postId)
    }
  }

  suspend fun upsert(
    postId: String,
    rawContent: String?,
    htmlContent: String?,
    createdAt: Instant,
  ) {
    withContext(dispatcherProvider.databaseWrite) {
      postContentQueries.upsert(
        id = postId,
        rawContent = rawContent,
        rawContentLen = rawContent?.length?.toLong() ?: 0L,
        htmlContent = htmlContent,
        createdAt = createdAt,
      )
    }
  }
}
