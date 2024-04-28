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
package dev.sasikanth.rss.reader.addfeed

import io.ktor.http.HttpStatusCode

sealed interface AddFeedErrorType {

  data object UnknownFeedType : AddFeedErrorType

  data object FailedToParseXML : AddFeedErrorType

  data object Timeout : AddFeedErrorType

  data object TooManyRedirects : AddFeedErrorType

  data class UnAuthorized(val statusCode: HttpStatusCode) : AddFeedErrorType

  data class FeedNotFound(val statusCode: HttpStatusCode) : AddFeedErrorType

  data class ServerError(val statusCode: HttpStatusCode) : AddFeedErrorType

  data class UnknownHttpStatusError(val statusCode: HttpStatusCode) : AddFeedErrorType

  data class Unknown(val e: Exception) : AddFeedErrorType
}
