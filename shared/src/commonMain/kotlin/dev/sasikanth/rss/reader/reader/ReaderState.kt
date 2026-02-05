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

import androidx.compose.runtime.Immutable
import app.cash.paging.PagingData
import dev.sasikanth.rss.reader.core.model.local.ResolvedPost
import dev.sasikanth.rss.reader.data.repository.ReaderFont
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

@Immutable
data class ReaderState(
  val activePostIndex: Int,
  val activePostId: String,
  val posts: Flow<PagingData<ResolvedPost>>,
  val showReaderCustomisations: Boolean,
  val selectedReaderFont: ReaderFont,
  val readerFontScaleFactor: Float,
  val readerLineHeightScaleFactor: Float,
  val openPaywall: Boolean,
) {

  companion object {

    fun default(initialPostIndex: Int, initialPostId: String): ReaderState {
      return ReaderState(
        activePostIndex = initialPostIndex,
        activePostId = initialPostId,
        posts = emptyFlow(),
        showReaderCustomisations = false,
        selectedReaderFont = ReaderFont.Golos,
        readerFontScaleFactor = 1f,
        readerLineHeightScaleFactor = 1f,
        openPaywall = false,
      )
    }
  }
}
