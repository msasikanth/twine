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

package dev.sasikanth.rss.reader.reader

import dev.sasikanth.rss.reader.core.model.local.SearchSortOrder
import kotlinx.serialization.Serializable

@Serializable
data class ReaderScreenArgs(val postIndex: Int, val postId: String, val fromScreen: FromScreen) {

  @Serializable
  sealed interface FromScreen {

    @Serializable data object Home : FromScreen

    @Serializable
    data class Search(val searchQuery: String, val sortOrder: SearchSortOrder) : FromScreen

    @Serializable data object Bookmarks : FromScreen

    @Serializable data object UnreadWidget : FromScreen

    @Serializable data object AudioPlayer : FromScreen
  }
}
