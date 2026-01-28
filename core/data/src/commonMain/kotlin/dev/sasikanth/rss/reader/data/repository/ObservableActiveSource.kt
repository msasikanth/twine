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

import dev.sasikanth.rss.reader.core.model.local.Source
import dev.sasikanth.rss.reader.di.scopes.AppScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import me.tatarka.inject.annotations.Inject

@Inject
@AppScope
class ObservableActiveSource {

  private val _activeSources = MutableStateFlow<Source?>(null)
  val activeSource: Flow<Source?>
    get() = _activeSources

  fun changeActiveSource(source: Source) {
    _activeSources.value = source
  }

  fun clearSelection() {
    _activeSources.value = null
  }
}
