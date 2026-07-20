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

package dev.sasikanth.rss.reader.feedhealth

sealed interface FeedHealthEvent {
  data object LoadHealthData : FeedHealthEvent

  data class UnsubscribeFeed(val feedId: String) : FeedHealthEvent

  data class RetryFeed(val feedId: String) : FeedHealthEvent

  data object ClearRetryMessage : FeedHealthEvent

  data object UndoUnsubscribe : FeedHealthEvent

  data class ToggleFeedSelection(val feedId: String) : FeedHealthEvent

  data object ClearSelection : FeedHealthEvent

  data object UnsubscribeSelectedFeeds : FeedHealthEvent
}
