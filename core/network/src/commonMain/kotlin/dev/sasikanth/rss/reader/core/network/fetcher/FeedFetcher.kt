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
package dev.sasikanth.rss.reader.core.network.fetcher

import com.mohamedrejeb.ksoup.html.parser.KsoupHtmlHandler
import com.mohamedrejeb.ksoup.html.parser.KsoupHtmlParser
import dev.sasikanth.rss.reader.core.network.parser.FeedParser
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.URLBuilder
import io.ktor.http.URLProtocol
import io.ktor.http.Url
import io.ktor.http.contentType
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
import me.tatarka.inject.annotations.Inject

@Inject
class FeedFetcher(private val httpClient: HttpClient, private val feedParser: FeedParser) {

  companion object {
    private const val MAX_REDIRECTS_ALLOWED = 5
  }

  suspend fun fetch(url: String, transformUrl: Boolean = true): FeedFetchResult {
    return fetch(url, transformUrl, redirectCount = 0)
  }

  private suspend fun fetch(
    url: String,
    transformUrl: Boolean,
    redirectCount: Int,
  ): FeedFetchResult {
    return if (redirectCount < MAX_REDIRECTS_ALLOWED) {
      try {
        // We are mainly doing this check to avoid creating duplicates while refreshing feeds
        // after the app update
        val transformedUrl = transformUrl(url, transformUrl)
        val response = httpClient.get(transformedUrl.toString())

        when (response.status) {
          HttpStatusCode.OK -> {
            parseContent(response, transformedUrl.toString(), redirectCount)
          }
          HttpStatusCode.MultipleChoices,
          HttpStatusCode.MovedPermanently,
          HttpStatusCode.Found,
          HttpStatusCode.SeeOther,
          HttpStatusCode.TemporaryRedirect,
          HttpStatusCode.PermanentRedirect -> {
            handleHttpRedirect(response, transformedUrl.toString(), redirectCount)
          }
          else -> {
            FeedFetchResult.HttpStatusError(statusCode = response.status)
          }
        }
      } catch (e: Exception) {
        FeedFetchResult.Error(e)
      }
    } else {
      FeedFetchResult.TooManyRedirects
    }
  }

  private suspend fun parseContent(
    response: HttpResponse,
    url: String,
    redirectCount: Int
  ): FeedFetchResult {
    val responseContent = response.bodyAsText()
    return if (response.contentType()?.withoutParameters() == ContentType.Text.Html) {
      val feedUrl = fetchFeedLinkFromHtmlIfExists(responseContent, url)
      if (feedUrl != url && !feedUrl.isNullOrBlank()) {
        fetch(url = feedUrl, transformUrl = false, redirectCount = redirectCount + 1)
      } else {
        throw UnsupportedOperationException()
      }
    } else {
      val feedPayload = feedParser.parse(xmlContent = responseContent, feedUrl = url)
      FeedFetchResult.Success(feedPayload)
    }
  }

  private suspend fun handleHttpRedirect(
    response: HttpResponse,
    url: String,
    redirectCount: Int
  ): FeedFetchResult {
    val newUrl = response.headers["Location"]
    return if (newUrl != url && !newUrl.isNullOrBlank()) {
      fetch(url = newUrl, transformUrl = false, redirectCount = redirectCount + 1)
    } else {
      FeedFetchResult.Error(Exception("Failed to fetch the feed"))
    }
  }

  private fun transformUrl(url: String, transformUrl: Boolean): Url {
    return if (transformUrl) {
      // Currently Ktor Url parses relative URLs,
      // if it fails to properly parse the given URL, it
      // default to localhost.
      //
      // This will cause the network call to fail,
      // so we are setting the host manually
      // https://youtrack.jetbrains.com/issue/KTOR-360
      URLBuilder()
        .apply {
          protocol = URLProtocol.HTTPS
          host = url.replace(Regex("^https?://"), "")
        }
        .build()
    } else {
      URLBuilder(url).apply { protocol = URLProtocol.HTTPS }.build()
    }
  }

  private suspend fun fetchFeedLinkFromHtmlIfExists(
    htmlContent: String,
    originalUrl: String
  ): String? {
    return suspendCoroutine { continuation ->
      var link: String? = null
      KsoupHtmlParser(
          handler =
            object : KsoupHtmlHandler {
              override fun onOpenTag(
                name: String,
                attributes: Map<String, String>,
                isImplied: Boolean
              ) {
                if (
                  link.isNullOrBlank() &&
                    name == "link" &&
                    (attributes["type"] == FeedParser.RSS_MEDIA_TYPE ||
                      attributes["type"] == FeedParser.ATOM_MEDIA_TYPE)
                ) {
                  link = attributes["href"]
                }
              }

              override fun onEnd() {
                val host = URLBuilder(originalUrl).build().host
                val rootUrl = "https://$host"
                val feedUrl = FeedParser.safeUrl(rootUrl, link)

                continuation.resume(feedUrl)
              }
            }
        )
        .parseComplete(htmlContent)
    }
  }
}
