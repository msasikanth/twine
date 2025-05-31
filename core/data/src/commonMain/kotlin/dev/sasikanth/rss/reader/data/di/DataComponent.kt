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
      postAdapter = Post.Adapter(dateAdapter = DateAdapter, syncedAtAdapter = DateAdapter),
      feedAdapter =
        Feed.Adapter(
          createdAtAdapter = DateAdapter,
          pinnedAtAdapter = DateAdapter,
          lastCleanUpAtAdapter = DateAdapter
        ),
      bookmarkAdapter = Bookmark.Adapter(dateAdapter = DateAdapter),
      feedGroupAdapter =
        FeedGroup.Adapter(
          createdAtAdapter = DateAdapter,
          updatedAtAdapter = DateAdapter,
          pinnedAtAdapter = DateAdapter
        )
    )
  }

  @Provides @AppScope fun providesMigrations(): Array<AfterVersion> = SQLCodeMigrations.migrations()

  @Provides
  @AppScope
  fun providesPrePopulateFeedQueries(): Array<String> {
    return arrayOf(
      // Kottke
      """
        INSERT OR IGNORE INTO feed(id, name, icon, description, homepageLink, createdAt, link)
        VALUES (
            'ba2ba021-2f69-55ad-9c21-cdf1a555e9bf',
            'kottke.org',
            'https://icon.horse/icon/kottke.org',
            "Jason Kottke's weblog, home of fine hypertext products since 1998",
            'https://kottke.org/',
            (strftime('%s', 'now') * 1000),
            'https://feeds.kottke.org/main'
        );
      """
        .trimIndent(),
      // HackerNews
      """
        INSERT OR IGNORE INTO feed(id, name, icon, description, homepageLink, createdAt, link)
        VALUES (
            'c90003bd-b1e6-5545-ba59-3d2128d658a7',
            'HN',
            'https://icon.horse/icon/news.ycombinator.com',
            'Links for the intellectually curious, ranked by readers.',
            'https://news.ycombinator.com/',
            (strftime('%s', 'now') * 1000),
            'https://news.ycombinator.com/rss'
        );
      """
        .trimIndent(),
      // TheVerge
      """
        INSERT OR IGNORE INTO feed(id, name, icon, description, homepageLink, createdAt, link)
        VALUES (
            'e8d31cec-2893-54d0-bcae-7f134713e532',
            'The Verge',
            'https://platform.theverge.com/wp-content/uploads/sites/2/2025/01/verge-rss-large_80b47e.png?w=150&h=150&crop=1',
            'The Verge is about technology and how it makes us feel. Founded in 2011, we offer our audience everything from breaking news to reviews to award-winning features and investigations, on our site, in video, and in podcasts.',
            'https://www.theverge.com',
            (strftime('%s', 'now') * 1000),
            'https://www.theverge.com/rss/index.xml'
        );
      """
        .trimIndent(),
      // New York Times > World News
      """
        INSERT OR IGNORE INTO feed(id, name, icon, description, homepageLink, createdAt, link)
        VALUES (
            '9ef86906-12bd-573a-bc19-ca1f2381793a',
            'NYT > World News',
            'https://static01.nyt.com/images/misc/NYT_logo_rss_250x40.png',
            '',
            'https://www.nytimes.com/section/world',
            (strftime('%s', 'now') * 1000),
            'https://rss.nytimes.com/services/xml/rss/nyt/World.xml'
        );
      """
        .trimIndent()
    )
  }

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

  @Provides
  fun providesFeedGroupFeedQueries(database: ReaderDatabase) = database.feedGroupFeedQueries
}
