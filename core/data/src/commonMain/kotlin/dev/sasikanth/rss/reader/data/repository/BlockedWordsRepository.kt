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

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.benasher44.uuid.Uuid
import com.benasher44.uuid.uuidFrom
import dev.sasikanth.rss.reader.core.model.local.BlockedWord
import dev.sasikanth.rss.reader.data.database.BlockedWordsQueries
import dev.sasikanth.rss.reader.data.database.PostQueries
import dev.sasikanth.rss.reader.data.database.TransactionRunner
import dev.sasikanth.rss.reader.data.sync.BlockedWordSyncEntity
import dev.sasikanth.rss.reader.di.scopes.AppScope
import dev.sasikanth.rss.reader.util.DispatchersProvider
import dev.sasikanth.rss.reader.util.nameBasedUuidOf
import kotlin.time.Clock
import kotlin.time.Instant
import kotlinx.coroutines.withContext
import me.tatarka.inject.annotations.Inject

@Inject
@AppScope
class BlockedWordsRepository(
  private val transactionRunner: TransactionRunner,
  private val blockedWordsQueries: BlockedWordsQueries,
  private val postQueries: PostQueries,
  private val dispatchersProvider: DispatchersProvider,
) {

  suspend fun addWord(word: String) {
    withContext(dispatchersProvider.databaseWrite) {
      val uuid = nameBasedUuidOf(word.lowercase())
      transactionRunner.invoke {
        blockedWordsQueries.insert(
          id = uuid.toString(),
          content = word,
          updatedAt = Clock.System.now(),
        )
        retroactiveBlockAndUnblock()
      }
    }
  }

  suspend fun removeWord(id: Uuid) {
    withContext(dispatchersProvider.databaseWrite) {
      transactionRunner.invoke {
        blockedWordsQueries.remove(id = id.toString(), updatedAt = Clock.System.now())
        retroactiveBlockAndUnblock()
      }
    }
  }

  fun words() =
    blockedWordsQueries
      .words(
        mapper = { id, content, isDeleted, updatedAt ->
          BlockedWord(id = uuidFrom(id), content = content)
        }
      )
      .asFlow()
      .mapToList(dispatchersProvider.databaseRead)

  suspend fun allBlockedWordsBlocking(): List<BlockedWord> {
    return withContext(dispatchersProvider.databaseRead) {
      blockedWordsQueries
        .allBlockedWords(
          mapper = { id, content, isDeleted, updatedAt ->
            BlockedWord(id = uuidFrom(id), content = content)
          }
        )
        .executeAsList()
    }
  }

  suspend fun upsertBlockedWords(blockedWords: List<BlockedWordSyncEntity>) {
    withContext(dispatchersProvider.databaseWrite) {
      transactionRunner.invoke {
        blockedWords.forEach { blockedWord ->
          blockedWordsQueries.upsertSyncBlockedWord(
            id = blockedWord.id,
            content = blockedWord.content,
            isDeleted = blockedWord.isDeleted,
            updatedAt = Instant.fromEpochMilliseconds(blockedWord.updatedAt),
          )
        }
        retroactiveBlockAndUnblock()
      }
    }
  }

  private fun retroactiveBlockAndUnblock() {
    val activeBlockedWords =
      blockedWordsQueries.words { _, content, _, _ -> content.lowercase() }.executeAsList()

    // 1. Retroactively block matching posts for all active blocked words
    activeBlockedWords.forEach { word -> postQueries.blockPostsWithWord(word) }

    // 2. Retroactively unblock posts that no longer match any active blocked word
    val blockedPosts =
      postQueries
        .blockedPosts { postId, title, description, _ -> Triple(postId, title, description) }
        .executeAsList()

    val postsToUnblock =
      blockedPosts
        .filter { (_, title, description) ->
          val titleLower = title.lowercase()
          val descLower = description.lowercase()
          activeBlockedWords.none { word -> titleLower.contains(word) || descLower.contains(word) }
        }
        .map { it.first }

    if (postsToUnblock.isNotEmpty()) {
      postQueries.unblockPosts(postsToUnblock)
    }
  }
}
