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

import app.cash.sqldelight.driver.native.inMemoryDriver
import dev.sasikanth.rss.reader.data.database.ReaderDatabase
import kotlin.test.Test
import kotlinx.coroutines.test.runTest

class CrossFeedBlockedWordsIosTest {

  @Test
  fun blockedFeedFollowedByUnblockedDuplicateAndRefreshKeepsBothBlocked() = runTest {
    val driver = inMemoryDriver(ReaderDatabase.Schema)
    try {
      CrossFeedBlockedWordsTestSuite(driver)
        .blockedFeedFollowedByUnblockedDuplicateAndRefreshKeepsBothBlocked()
    } finally {
      driver.close()
    }
  }

  @Test
  fun unblockedFeedFollowedByBlockedDuplicateBlocksBothAndRemovingWordUnblocksBoth() = runTest {
    val driver = inMemoryDriver(ReaderDatabase.Schema)
    try {
      CrossFeedBlockedWordsTestSuite(driver)
        .unblockedFeedFollowedByBlockedDuplicateBlocksBothAndRemovingWordUnblocksBoth()
    } finally {
      driver.close()
    }
  }

  @Test
  fun singleFeedAndCrossFeedEditedPostsUnblockWhenNoCopyMatchesBlockedWord() = runTest {
    val driver = inMemoryDriver(ReaderDatabase.Schema)
    try {
      CrossFeedBlockedWordsTestSuite(driver)
        .singleFeedAndCrossFeedEditedPostsUnblockWhenNoCopyMatchesBlockedWord()
    } finally {
      driver.close()
    }
  }

  @Test
  fun directUpsertAndSyncRepositoryUpsertPreserveCrossFeedHiddenFlag() = runTest {
    val driver = inMemoryDriver(ReaderDatabase.Schema)
    try {
      CrossFeedBlockedWordsTestSuite(driver)
        .directUpsertAndSyncRepositoryUpsertPreserveCrossFeedHiddenFlag()
    } finally {
      driver.close()
    }
  }
}
