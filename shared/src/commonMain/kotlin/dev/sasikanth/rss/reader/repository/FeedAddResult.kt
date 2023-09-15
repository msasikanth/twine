package dev.sasikanth.rss.reader.repository

import io.ktor.http.HttpStatusCode

sealed interface FeedAddResult {

  object Success : FeedAddResult

  data class HttpStatusError(val statusCode: HttpStatusCode) : FeedAddResult

  data class NetworkError(val exception: Exception) : FeedAddResult

  data class DatabaseError(val exception: Exception) : FeedAddResult

  object TooManyRedirects : FeedAddResult
}
