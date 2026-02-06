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
package dev.sasikanth.rss.reader.data.di

import app.cash.sqldelight.EnumColumnAdapter
import app.cash.sqldelight.db.AfterVersion
import app.cash.sqldelight.db.SqlDriver
import dev.sasikanth.rss.reader.data.database.BlockedWord
import dev.sasikanth.rss.reader.data.database.Feed
import dev.sasikanth.rss.reader.data.database.FeedGroup
import dev.sasikanth.rss.reader.data.database.Post
import dev.sasikanth.rss.reader.data.database.PostContent
import dev.sasikanth.rss.reader.data.database.ReaderDatabase
import dev.sasikanth.rss.reader.data.database.User
import dev.sasikanth.rss.reader.data.database.adapter.DateAdapter
import dev.sasikanth.rss.reader.data.database.adapter.PostFlagsAdapter
import dev.sasikanth.rss.reader.data.database.migrations.SQLCodeMigrations
import dev.sasikanth.rss.reader.data.repository.UserRepository
import dev.sasikanth.rss.reader.data.sync.CloudServiceProvider
import dev.sasikanth.rss.reader.data.sync.DefaultSyncCoordinator
import dev.sasikanth.rss.reader.data.sync.SyncCoordinator
import dev.sasikanth.rss.reader.data.sync.auth.OAuthManager
import dev.sasikanth.rss.reader.data.sync.auth.OAuthTokenProvider
import dev.sasikanth.rss.reader.data.sync.auth.RealOAuthManager
import dev.sasikanth.rss.reader.data.sync.auth.RealOAuthTokenProvider
import dev.sasikanth.rss.reader.data.sync.dropbox.DropboxCloudServiceProvider
import dev.sasikanth.rss.reader.data.sync.freshrss.FreshRssSyncProvider
import dev.sasikanth.rss.reader.data.sync.miniflux.MinifluxSyncProvider
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
          flagsAdapter = PostFlagsAdapter,
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
          pinnedAtAdapter = DateAdapter,
        ),
      postContentAdapter = PostContent.Adapter(createdAtAdapter = DateAdapter),
      blockedWordAdapter = BlockedWord.Adapter(updatedAtAdapter = DateAdapter),
      userAdapter = User.Adapter(serviceTypeAdapter = EnumColumnAdapter()),
    )
  }

  @Provides @AppScope fun providesMigrations(): Array<AfterVersion> = SQLCodeMigrations.migrations()

  @Provides
  @AppScope
  fun providesPrePopulateFeedQueries(): Array<String> {
    return emptyArray()
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
      onSignOut = { userRepository.deleteUser() },
    )

  @Provides
  @AppScope
  fun providesSyncProviders(
    cloudServiceProvider: DropboxCloudServiceProvider,
    freshRssSyncProvider: FreshRssSyncProvider,
    minifluxSyncProvider: MinifluxSyncProvider,
  ): Set<CloudServiceProvider> {
    return setOf(minifluxSyncProvider, freshRssSyncProvider, cloudServiceProvider)
  }

  @Provides fun providesPostContentQueries(database: ReaderDatabase) = database.postContentQueries

  @Provides fun providesAppConfigQueries(database: ReaderDatabase) = database.appConfigQueries
}
