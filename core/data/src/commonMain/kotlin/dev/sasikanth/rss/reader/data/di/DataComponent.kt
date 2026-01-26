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
import dev.sasikanth.rss.reader.data.database.BlockedWord
import dev.sasikanth.rss.reader.data.database.Feed
import dev.sasikanth.rss.reader.data.database.FeedGroup
import dev.sasikanth.rss.reader.data.database.Post
import dev.sasikanth.rss.reader.data.database.PostContent
import dev.sasikanth.rss.reader.data.database.ReaderDatabase
import dev.sasikanth.rss.reader.data.database.adapter.DateAdapter
import dev.sasikanth.rss.reader.data.database.adapter.PostFlagsAdapter
import dev.sasikanth.rss.reader.data.database.migrations.SQLCodeMigrations
import dev.sasikanth.rss.reader.data.repository.UserRepository
import dev.sasikanth.rss.reader.data.sync.CloudServiceProvider
import dev.sasikanth.rss.reader.data.sync.DefaultSyncCoordinator
import dev.sasikanth.rss.reader.data.sync.DropboxCloudServiceProvider
import dev.sasikanth.rss.reader.data.sync.FreshRssSyncProvider
import dev.sasikanth.rss.reader.data.sync.OAuthManager
import dev.sasikanth.rss.reader.data.sync.OAuthTokenProvider
import dev.sasikanth.rss.reader.data.sync.RealOAuthManager
import dev.sasikanth.rss.reader.data.sync.RealOAuthTokenProvider
import dev.sasikanth.rss.reader.data.sync.SyncCoordinator
import dev.sasikanth.rss.reader.di.scopes.AppScope
import io.ktor.client.HttpClient
import me.tatarka.inject.annotations.Provides

expect interface SqlDriverPlatformComponent

expect interface DataStorePlatformComponent

interface DataComponent :
  SqlDriverPlatformComponent, DataStorePlatformComponent, UserDataComponent {

  @Provides
  @AppScope
  fun providesSyncCoordinator(coordinator: DefaultSyncCoordinator): SyncCoordinator = coordinator

  @Provides
  @AppScope
  fun providesDatabase(driver: SqlDriver): ReaderDatabase {
    return ReaderDatabase(
      driver = driver,
      postAdapter =
        Post.Adapter(
          postDateAdapter = DateAdapter,
          createdAtAdapter = DateAdapter,
          updatedAtAdapter = DateAdapter,
          syncedAtAdapter = DateAdapter,
          flagsAdapter = PostFlagsAdapter
        ),
      feedAdapter =
        Feed.Adapter(
          createdAtAdapter = DateAdapter,
          pinnedAtAdapter = DateAdapter,
          lastCleanUpAtAdapter = DateAdapter,
          lastUpdatedAtAdapter = DateAdapter,
        ),
      feedGroupAdapter =
        FeedGroup.Adapter(
          createdAtAdapter = DateAdapter,
          updatedAtAdapter = DateAdapter,
          pinnedAtAdapter = DateAdapter
        ),
      postContentAdapter =
        PostContent.Adapter(
          createdAtAdapter = DateAdapter,
        ),
      blockedWordAdapter =
        BlockedWord.Adapter(
          updatedAtAdapter = DateAdapter,
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
        INSERT OR IGNORE INTO feed(id, name, icon, description, link, homepageLink, createdAt, pinnedAt)
        VALUES (
            'ba2ba021-2f69-55ad-9c21-cdf1a555e9bf',
            'Kottke',
            "Jason Kottke's weblog, home of fine hypertext products since 1998",
            'https://icon.horse/icon/kottke.org',
            'https://feeds.kottke.org/main',
            'https://kottke.org/',
            (strftime('%s', 'now') * 1000),
            (strftime('%s', 'now') * 1000)
        );
      """
        .trimIndent(),
      // HackerNews
      """
        INSERT OR IGNORE INTO feed(id, name, icon, description, link, homepageLink, createdAt, pinnedAt)
        VALUES (
            'c90003bd-b1e6-5545-ba59-3d2128d658a7',
            'HN',
            'Links for the intellectually curious, ranked by readers.',
            'https://icon.horse/icon/news.ycombinator.com',
            'https://news.ycombinator.com/rss',
            'https://news.ycombinator.com/',
            (strftime('%s', 'now') * 1000),
            (strftime('%s', 'now') * 1000)
        );
      """
        .trimIndent(),
      // TheVerge
      """
        INSERT OR IGNORE INTO feed(id, name, icon, description, link, homepageLink, createdAt, pinnedAt)
        VALUES (
            'e8d31cec-2893-54d0-bcae-7f134713e532',
            'The Verge',
            'The Verge is about technology and how it makes us feel. Founded in 2011, we offer our audience everything from breaking news to reviews to award-winning features and investigations, on our site, in video, and in podcasts.',
            'https://platform.theverge.com/wp-content/uploads/sites/2/2025/01/verge-rss-large_80b47e.png?w=150&h=150&crop=1',
            'https://www.theverge.com/rss/index.xml',
            'https://www.theverge.com',
            (strftime('%s', 'now') * 1000),
            (strftime('%s', 'now') * 1000)
        );
      """
        .trimIndent(),
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

  @Provides fun providesUserQueries(database: ReaderDatabase) = database.userQueries

  @Provides
  @AppScope
  fun providesOAuthTokenProvider(userRepository: UserRepository): OAuthTokenProvider =
    RealOAuthTokenProvider(userRepository)

  @Provides
  @AppScope
  fun providesOAuthManager(
    httpClient: HttpClient,
    tokenProvider: OAuthTokenProvider,
    userRepository: UserRepository,
  ): OAuthManager = RealOAuthManager(httpClient, tokenProvider, userRepository)

  @Provides
  @AppScope
  fun providesDropboxSyncProvider(
    httpClient: HttpClient,
    tokenProvider: OAuthTokenProvider,
    userRepository: UserRepository,
  ): DropboxCloudServiceProvider =
    DropboxCloudServiceProvider(
      httpClient = httpClient,
      tokenProvider = tokenProvider,
      onSignOut = { userRepository.deleteUser() }
    )

  @Provides
  @AppScope
  fun providesSyncProviders(
    cloudServiceProvider: DropboxCloudServiceProvider,
    freshRssSyncProvider: FreshRssSyncProvider,
  ): Set<CloudServiceProvider> {
    return setOf(freshRssSyncProvider, cloudServiceProvider)
  }

  @Provides fun providesPostContentQueries(database: ReaderDatabase) = database.postContentQueries

  @Provides fun providesAppConfigQueries(database: ReaderDatabase) = database.appConfigQueries
}
