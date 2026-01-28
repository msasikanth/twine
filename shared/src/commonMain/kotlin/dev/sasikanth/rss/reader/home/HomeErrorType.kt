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
package dev.sasikanth.rss.reader.home

import io.ktor.http.HttpStatusCode

sealed interface HomeErrorType {
  data object UnknownFeedType : HomeErrorType

  data object FailedToParseXML : HomeErrorType

  data object Timeout : HomeErrorType

  data object TooManyRedirects : HomeErrorType

  data class UnAuthorized(val statusCode: HttpStatusCode) : HomeErrorType

  data class FeedNotFound(val statusCode: HttpStatusCode) : HomeErrorType

  data class ServerError(val statusCode: HttpStatusCode) : HomeErrorType

  data class UnknownHttpStatusError(val statusCode: HttpStatusCode) : HomeErrorType

  data class Unknown(val e: Exception) : HomeErrorType
}
