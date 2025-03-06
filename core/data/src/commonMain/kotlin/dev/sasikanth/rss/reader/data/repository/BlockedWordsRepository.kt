/*
 * Copyright 2025 Sasikanth Miriyampalli
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

package dev.sasikanth.rss.reader.data.repository

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.benasher44.uuid.Uuid
import com.benasher44.uuid.uuidFrom
import dev.sasikanth.rss.reader.core.model.local.BlockedWord
import dev.sasikanth.rss.reader.data.database.BlockedWordsQueries
import dev.sasikanth.rss.reader.di.scopes.AppScope
import dev.sasikanth.rss.reader.util.DispatchersProvider
import dev.sasikanth.rss.reader.util.nameBasedUuidOf
import kotlinx.coroutines.withContext
import me.tatarka.inject.annotations.Inject

@Inject
@AppScope
class BlockedWordsRepository(
  private val blockedWordsQueries: BlockedWordsQueries,
  private val dispatchersProvider: DispatchersProvider,
) {

  suspend fun addWord(word: String) {
    withContext(dispatchersProvider.databaseWrite) {
      val uuid = nameBasedUuidOf(word.lowercase())
      blockedWordsQueries.insert(id = uuid.toString(), content = word)
    }
  }

  suspend fun removeWord(id: Uuid) {
    withContext(dispatchersProvider.databaseWrite) { blockedWordsQueries.remove(id.toString()) }
  }

  fun words() =
    blockedWordsQueries
      .words(mapper = { id, content -> BlockedWord(id = uuidFrom(id), content = content) })
      .asFlow()
      .mapToList(dispatchersProvider.databaseRead)
}
