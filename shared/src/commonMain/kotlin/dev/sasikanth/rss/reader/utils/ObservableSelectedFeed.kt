/*
 * Copyright 2023 Sasikanth Miriyampalli
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
package dev.sasikanth.rss.reader.utils

import dev.sasikanth.rss.reader.di.scopes.AppScope
import dev.sasikanth.rss.reader.models.local.Feed
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import me.tatarka.inject.annotations.Inject

@Inject
@AppScope
class ObservableSelectedFeed {

  private val mutex = Mutex()
  private val _selectedFeed = MutableStateFlow<Feed?>(null)
  val selectedFeed: Flow<Feed?>
    get() = _selectedFeed

  suspend fun selectFeed(feed: Feed) {
    mutex.withLock { _selectedFeed.value = feed }
  }

  suspend fun clearSelection() {
    mutex.withLock { _selectedFeed.value = null }
  }
}
