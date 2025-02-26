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
package dev.sasikanth.rss.reader.data.di

import app.cash.sqldelight.db.AfterVersion
import app.cash.sqldelight.db.SqlDriver
import dev.sasikanth.rss.reader.data.database.Bookmark
import dev.sasikanth.rss.reader.data.database.Feed
import dev.sasikanth.rss.reader.data.database.FeedGroup
import dev.sasikanth.rss.reader.data.database.Post
import dev.sasikanth.rss.reader.data.database.ReaderDatabase
import dev.sasikanth.rss.reader.data.database.adapter.DateAdapter
import dev.sasikanth.rss.reader.data.database.adapter.ListToStringAdapter
import dev.sasikanth.rss.reader.data.database.migrations.SQLCodeMigrations
import dev.sasikanth.rss.reader.di.scopes.AppScope
import me.tatarka.inject.annotations.Provides

expect interface SqlDriverPlatformComponent

expect interface DataStorePlatformComponent

interface DataComponent : SqlDriverPlatformComponent, DataStorePlatformComponent {

  @Provides
  @AppScope
  fun providesDatabase(driver: SqlDriver): ReaderDatabase {
    return ReaderDatabase(
      driver = driver,
      postAdapter = Post.Adapter(dateAdapter = DateAdapter),
      feedAdapter =
        Feed.Adapter(
          createdAtAdapter = DateAdapter,
          pinnedAtAdapter = DateAdapter,
          lastCleanUpAtAdapter = DateAdapter
        ),
      bookmarkAdapter = Bookmark.Adapter(dateAdapter = DateAdapter),
      feedGroupAdapter =
        FeedGroup.Adapter(
          feedIdsAdapter = ListToStringAdapter,
          createdAtAdapter = DateAdapter,
          updatedAtAdapter = DateAdapter,
          pinnedAtAdapter = DateAdapter
        )
    )
  }

  @Provides @AppScope fun providesMigrations(): Array<AfterVersion> = SQLCodeMigrations.migrations()

  @Provides fun providesFeedQueries(database: ReaderDatabase) = database.feedQueries

  @Provides fun providesPostQueries(database: ReaderDatabase) = database.postQueries

  @Provides
  fun providesPostSearchFTSQueries(database: ReaderDatabase) = database.postSearchFTSQueries

  @Provides fun providesBookmarkQueries(database: ReaderDatabase) = database.bookmarkQueries

  @Provides
  fun providesFeedSearchFTSQueries(database: ReaderDatabase) = database.feedSearchFTSQueries

  @Provides fun providesFeedGroupQueries(database: ReaderDatabase) = database.feedGroupQueries

  @Provides fun providesSourceQueries(database: ReaderDatabase) = database.sourceQueries

  @Provides fun providesBlockedWordsQueries(database: ReaderDatabase) = database.blockedWordsQueries
}
