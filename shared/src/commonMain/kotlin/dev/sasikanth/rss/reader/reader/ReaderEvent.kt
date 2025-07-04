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

import dev.sasikanth.rss.reader.core.model.local.PostWithMetadata
import dev.sasikanth.rss.reader.data.repository.ReaderFont

sealed interface ReaderEvent {

  data class TogglePostBookmark(val postId: String, val currentBookmarkStatus: Boolean) :
    ReaderEvent

  data class PostPageChanged(val postIndex: Int, val post: PostWithMetadata) : ReaderEvent

  data class LoadFullArticleClicked(val postId: String) : ReaderEvent

  data class PostLoaded(val post: PostWithMetadata) : ReaderEvent

  data object ShowReaderCustomisations : ReaderEvent

  data object HideReaderCustomisations : ReaderEvent

  data class UpdateReaderFont(val font: ReaderFont) : ReaderEvent

  data class UpdateFontScaleFactor(val fontScaleFactor: Float) : ReaderEvent

  data class UpdateFontLineHeightFactor(val fontLineHeightFactor: Float) : ReaderEvent

  data object MarkOpenPaywallDone : ReaderEvent
}
