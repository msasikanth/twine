/*
 * Copyright 2023 Sasikanth Miriyampalli
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
package dev.sasikanth.rss.reader.network

import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import io.ktor.http.URLBuilder
import io.ktor.http.URLProtocol
import me.tatarka.inject.annotations.Inject

@Inject
class FeedFetcher(private val httpClient: HttpClient, private val feedParser: FeedParser) {

  companion object {
    private const val MAX_REDIRECTS_ALLOWED = 3
  }

  private var redirectCount = 0

  suspend fun fetch(url: String): FeedFetchResult {
    return try {
      val transformedUrl = URLBuilder(url).apply { protocol = URLProtocol.HTTPS }.build()
      val response = httpClient.get(transformedUrl)

      when (response.status) {
        HttpStatusCode.OK -> {
          val feedPayload = feedParser.parse(xmlContent = response.bodyAsText(), feedUrl = url)
          FeedFetchResult.Success(feedPayload)
        }
        HttpStatusCode.MultipleChoices,
        HttpStatusCode.MovedPermanently,
        HttpStatusCode.Found,
        HttpStatusCode.SeeOther,
        HttpStatusCode.TemporaryRedirect,
        HttpStatusCode.PermanentRedirect -> {
          if (redirectCount < MAX_REDIRECTS_ALLOWED) {
            val newUrl = response.headers["Location"]
            if (newUrl != null) {
              redirectCount += 1
              fetch(newUrl)
            } else {
              FeedFetchResult.Error(Exception("Failed to fetch the feed"))
            }
          } else {
            FeedFetchResult.TooManyRedirects
          }
        }
        else -> {
          FeedFetchResult.HttpStatusError(statusCode = response.status)
        }
      }
    } catch (e: Exception) {
      FeedFetchResult.Error(e)
    }
  }
}
