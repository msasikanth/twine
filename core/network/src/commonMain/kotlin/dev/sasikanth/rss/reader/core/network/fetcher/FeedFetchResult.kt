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
package dev.sasikanth.rss.reader.core.network.fetcher

import dev.sasikanth.rss.reader.core.model.remote.FeedPayload
import io.ktor.http.HttpStatusCode

sealed interface FeedFetchResult {

  data class Success(val feedPayload: FeedPayload) : FeedFetchResult

  data class HttpStatusError(val statusCode: HttpStatusCode) : FeedFetchResult

  data class Error(val exception: Exception) : FeedFetchResult

  data object TooManyRedirects : FeedFetchResult
}
