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

package dev.sasikanth.rss.reader.reader

import androidx.compose.runtime.Immutable
import app.cash.paging.PagingData
import dev.sasikanth.rss.reader.core.model.local.PostWithMetadata
import dev.sasikanth.rss.reader.data.repository.ReaderFont
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

@Immutable
data class ReaderState(
  val activePostIndex: Int,
  val activePostId: String,
  val loadFullArticleMap: Map<String, Boolean>,
  val posts: Flow<PagingData<PostWithMetadata>>,
  val showReaderCustomisations: Boolean,
  val selectedReaderFont: ReaderFont,
  val readerFontScaleFactor: Float,
  val readerLineHeightScaleFactor: Float,
  val openPaywall: Boolean,
) {

  fun canLoadFullPost(postId: String): Boolean {
    return loadFullArticleMap[postId] ?: false
  }

  companion object {

    fun default(initialPostIndex: Int, initialPostId: String): ReaderState {
      return ReaderState(
        activePostIndex = initialPostIndex,
        activePostId = initialPostId,
        loadFullArticleMap = emptyMap(),
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
