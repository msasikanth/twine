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

import dev.sasikanth.rss.reader.di.scopes.AppScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import me.tatarka.inject.annotations.Inject

@Inject
@AppScope
class ObservableSelectedPost {

  private val _selectedPost = MutableStateFlow<SelectedPost?>(null)
  val selectedPost: StateFlow<SelectedPost?> = _selectedPost

  fun updateSelectedPost(index: Int, postId: String?) {
    _selectedPost.value = SelectedPost(index, postId)
  }

  fun clear() {
    _selectedPost.value = null
  }
}

data class SelectedPost(val index: Int, val id: String?)
