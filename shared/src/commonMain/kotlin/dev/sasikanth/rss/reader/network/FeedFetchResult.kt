package dev.sasikanth.rss.reader.network

import dev.sasikanth.rss.reader.models.remote.FeedPayload
import io.ktor.http.HttpStatusCode

sealed interface FeedFetchResult {

  data class Success(val feedPayload: FeedPayload) : FeedFetchResult

  data class HttpStatusError(val statusCode: HttpStatusCode) : FeedFetchResult

  data class Error(val exception: Exception) : FeedFetchResult

  object TooManyRedirects : FeedFetchResult
}
