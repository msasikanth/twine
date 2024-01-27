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

package dev.sasikanth.rss.reader.repository

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.benasher44.uuid.Uuid
import com.benasher44.uuid.uuid4
import dev.sasikanth.rss.reader.core.model.local.Tag
import dev.sasikanth.rss.reader.database.TagQueries
import dev.sasikanth.rss.reader.di.scopes.AppScope
import dev.sasikanth.rss.reader.util.DispatchersProvider
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock
import me.tatarka.inject.annotations.Inject

@Inject
@AppScope
class TagRepository(
  private val dispatchersProvider: DispatchersProvider,
  private val tagQueries: TagQueries,
) {

  suspend fun createTag(label: String) {
    withContext(dispatchersProvider.io) {
      val currentInstant = Clock.System.now()

      tagQueries.saveTag(
        id = uuid4(),
        label = label,
        createdAt = currentInstant,
        updatedAt = currentInstant
      )
    }
  }

  suspend fun deleteTag(id: Uuid) = withContext(dispatchersProvider.io) { tagQueries.deleteTag(id) }

  suspend fun updatedTag(label: String, id: Uuid) {
    withContext(dispatchersProvider.io) { tagQueries.updateTag(label = label, id = id) }
  }

  fun tags(label: String? = null): Flow<List<Tag>> {
    return tagQueries.tags(label.orEmpty(), ::Tag).asFlow().mapToList(dispatchersProvider.io)
  }
}
