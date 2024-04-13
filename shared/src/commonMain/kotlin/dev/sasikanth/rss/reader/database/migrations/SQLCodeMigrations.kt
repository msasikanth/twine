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

package dev.sasikanth.rss.reader.database.migrations

import app.cash.sqldelight.Query
import app.cash.sqldelight.db.AfterVersion
import app.cash.sqldelight.db.QueryResult
import app.cash.sqldelight.db.SqlCursor
import app.cash.sqldelight.db.SqlDriver
import dev.sasikanth.rss.reader.utils.nameBasedUuidOf

object SQLCodeMigrations {

  fun migrations(): Array<AfterVersion> {
    return arrayOf(afterVersion12(), afterVersion13())
  }

  private fun afterVersion13(): AfterVersion {
    return AfterVersion(13) { driver ->
      val feedIds = FeedsIdsQuery(driver).executeAsList()
      feedIds.forEach { feedId -> migrateFeedsLinkIdsToUuid(feedId, driver) }
    }
  }

  private fun afterVersion12(): AfterVersion {
    return AfterVersion(12) { driver ->
      val ids = PostsIdsQuery(driver).executeAsList()
      ids.forEach { id -> migratePostLinkIdsToUuid(driver, id) }
    }
  }

  private fun migrateFeedsLinkIdsToUuid(oldFeedId: String, driver: SqlDriver) {
    val newFeedId = nameBasedUuidOf(oldFeedId).toString()
    driver.execute(
      identifier = null,
      sql = "UPDATE feed SET id = '$newFeedId' WHERE id = '$oldFeedId'",
      parameters = 0,
      binders = null
    )

    driver.execute(
      identifier = null,
      sql = "UPDATE feed_search SET id = '$newFeedId' WHERE id = '$oldFeedId'",
      parameters = 0,
      binders = null
    )

    driver.execute(
      identifier = null,
      sql = "UPDATE post SET sourceId = '$newFeedId' WHERE sourceId = '$oldFeedId'",
      parameters = 0,
      binders = null
    )

    driver.execute(
      identifier = null,
      sql = "UPDATE bookmark SET sourceId = '$newFeedId' WHERE sourceId = '$oldFeedId'",
      parameters = 0,
      binders = null
    )
  }

  private fun migratePostLinkIdsToUuid(driver: SqlDriver, oldPostId: String) {
    val newPostId = nameBasedUuidOf(oldPostId).toString()

    driver.execute(
      identifier = null,
      sql = "UPDATE post SET id = '$newPostId' WHERE id = '$oldPostId'",
      parameters = 0,
      binders = null
    )

    driver.execute(
      identifier = null,
      sql = "UPDATE post_search SET id = '$newPostId' WHERE id = '$oldPostId'",
      parameters = 0,
      binders = null
    )

    driver.execute(
      identifier = null,
      sql = "UPDATE bookmark SET id = '$newPostId' WHERE id = '$oldPostId'",
      parameters = 0,
      binders = null
    )
  }
}

private class FeedsIdsQuery(
  private val driver: SqlDriver,
) : Query<String>(mapper = { cursor -> cursor.getString(0)!! }) {
  override fun addListener(listener: Listener) {
    driver.addListener("feed", listener = listener)
  }

  override fun removeListener(listener: Listener) {
    driver.removeListener("feed", listener = listener)
  }

  override fun <R> execute(mapper: (SqlCursor) -> QueryResult<R>): QueryResult<R> =
    driver.executeQuery(null, "SELECT id FROM feed", mapper, 0, null)

  override fun toString(): String = "Feed.sq:feedIds"
}

private class PostsIdsQuery(
  private val driver: SqlDriver,
) : Query<String>(mapper = { cursor -> cursor.getString(0)!! }) {
  override fun addListener(listener: Listener) {
    driver.addListener("post", listener = listener)
  }

  override fun removeListener(listener: Listener) {
    driver.removeListener("post", listener = listener)
  }

  override fun <R> execute(mapper: (SqlCursor) -> QueryResult<R>): QueryResult<R> =
    driver.executeQuery(null, "SELECT id FROM post", mapper, 0, null)

  override fun toString(): String = "Post.sq:posts"
}
