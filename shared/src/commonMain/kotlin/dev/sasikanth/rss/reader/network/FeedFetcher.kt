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

import com.mohamedrejeb.ksoup.html.parser.KsoupHtmlHandler
import com.mohamedrejeb.ksoup.html.parser.KsoupHtmlParser
import dev.sasikanth.rss.reader.utils.XmlParsingError
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import io.ktor.http.URLBuilder
import io.ktor.http.URLProtocol
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
import me.tatarka.inject.annotations.Inject

@Inject
class FeedFetcher(private val httpClient: HttpClient, private val feedParser: FeedParser) {

  companion object {
    private const val MAX_REDIRECTS_ALLOWED = 3
  }

  private var redirectCount = 0

  suspend fun fetch(
    url: String,
    transformUrl: Boolean = true,
    fetchPosts: Boolean
  ): FeedFetchResult {
    return try {
      // We are mainly doing this check to avoid creating duplicates while refreshing feeds
      // after the app update
      val transformedUrl =
        if (transformUrl) {
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
              host = url.replace(Regex("^https?://"), "").replace(Regex("^www\\."), "")
            }
            .build()
        } else {
          URLBuilder(url).apply { protocol = URLProtocol.HTTPS }.build()
        }

      val response = httpClient.get(transformedUrl.toString())

      when (response.status) {
        HttpStatusCode.OK -> {
          parseContent(response, transformedUrl.toString(), fetchPosts)
        }
        HttpStatusCode.MultipleChoices,
        HttpStatusCode.MovedPermanently,
        HttpStatusCode.Found,
        HttpStatusCode.SeeOther,
        HttpStatusCode.TemporaryRedirect,
        HttpStatusCode.PermanentRedirect -> {
          if (redirectCount < MAX_REDIRECTS_ALLOWED) {
            val newUrl = response.headers["Location"]
            if (newUrl != url && newUrl != null) {
              redirectCount += 1
              fetch(url = newUrl, fetchPosts = fetchPosts)
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

  private suspend fun parseContent(
    response: HttpResponse,
    url: String,
    fetchPosts: Boolean
  ): FeedFetchResult {
    val responseContent = response.bodyAsText()
    return try {
      val feedPayload =
        feedParser.parse(xmlContent = responseContent, feedUrl = url, fetchPosts = fetchPosts)
      FeedFetchResult.Success(feedPayload)
    } catch (e: Exception) {
      when (e) {
        // There are situation where XML parsers fail to identify if it's
        // a HTML document and fail, so trying to fetch link with HTML one
        // last time just to be safe if it fails with XML parsing issue.
        //
        // In some cases the link that is returned might be same as the original
        // causing it to loop. So, we are using the redirect check here.
        is HtmlContentException,
        is XmlParsingError -> {
          val feedUrl = fetchFeedLinkFromHtmlIfExists(responseContent, url)
          if (feedUrl != url && !feedUrl.isNullOrBlank() && redirectCount < MAX_REDIRECTS_ALLOWED) {
            redirectCount += 1
            fetch(url = feedUrl, fetchPosts = fetchPosts)
          } else {
            if (e is XmlParsingError) {
              throw e
            } else {
              throw UnsupportedOperationException()
            }
          }
        }
        else -> throw e
      }
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
