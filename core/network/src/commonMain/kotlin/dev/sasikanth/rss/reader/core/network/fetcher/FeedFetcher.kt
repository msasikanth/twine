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

import com.fleeksoft.ksoup.Ksoup
import com.fleeksoft.ksoup.parseSource
import dev.sasikanth.rss.reader.core.network.parser.FeedParser
import dev.sasikanth.rss.reader.core.network.parser.FeedParser.Companion.ATOM_MEDIA_TYPE
import dev.sasikanth.rss.reader.core.network.parser.FeedParser.Companion.ATTR_HREF
import dev.sasikanth.rss.reader.core.network.parser.FeedParser.Companion.ATTR_TYPE
import dev.sasikanth.rss.reader.core.network.parser.FeedParser.Companion.RSS_MEDIA_TYPE
import dev.sasikanth.rss.reader.core.network.parser.FeedParser.Companion.TAG_LINK
import dev.sasikanth.rss.reader.core.network.utils.UrlUtils
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsBytes
import io.ktor.client.statement.bodyAsChannel
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.URLBuilder
import io.ktor.http.Url
import io.ktor.http.contentType
import io.ktor.http.isRelativePath
import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.asSource
import korlibs.io.lang.Charset
import korlibs.io.lang.Charsets
import me.tatarka.inject.annotations.Inject

@Inject
class FeedFetcher(private val httpClient: HttpClient, private val feedParser: FeedParser) {

  companion object {
    private const val MAX_REDIRECTS_ALLOWED = 5
  }

  suspend fun fetch(url: String): FeedFetchResult {
    return fetch(url, redirectCount = 0)
  }

  private suspend fun fetch(
    url: String,
    redirectCount: Int,
  ): FeedFetchResult {
    if (redirectCount >= MAX_REDIRECTS_ALLOWED) {
      return FeedFetchResult.TooManyRedirects
    }

    return try {
      val transformedUrl = buildFeedUrl(url)
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
          handleHttpRedirect(response, transformedUrl, redirectCount)
        }
        else -> {
          FeedFetchResult.HttpStatusError(statusCode = response.status)
        }
      }
    } catch (e: Exception) {
      FeedFetchResult.Error(e)
    }
  }

  private fun buildFeedUrl(urlString: String): Url {
    val url = Url(urlString)

    return if (url.isRelativePath) {
      URLBuilder()
        .apply {
          host = urlString
          protocol = url.protocol
        }
        .build()
    } else {
      url
    }
  }

  private suspend fun parseContent(
    response: HttpResponse,
    url: String,
    redirectCount: Int
  ): FeedFetchResult {
    val contentType = response.contentType()?.withoutParameters()

    when (contentType) {
      ContentType.Text.Html -> {
        val feedUrl = fetchFeedLinkFromHtmlIfExists(response.bodyAsChannel(), url)

        if (feedUrl != url && !feedUrl.isNullOrBlank()) {
          return fetch(url = feedUrl, redirectCount = redirectCount + 1)
        }
      }

      // There are scenarios where a feed doesn't have any content-type, rare, but it's possible
      // in those scenarios we default to trying to parse them as XML and if it fails, it fails.
      ContentType.Application.Atom,
      ContentType.Application.Rss,
      ContentType.Application.Xml,
      ContentType.Text.Xml,
      null -> {
        val content = response.bodyAsChannel()
        val responseCharset = response.contentType()?.parameter("charset")
        val charset = Charset.forName(responseCharset ?: Charsets.UTF8.name)

        val feedPayload = feedParser.parse(feedUrl = url, content = content, charset = charset)

        return FeedFetchResult.Success(feedPayload)
      }

      ContentType.Application.Json -> {
        // TODO: Replace it with a stream once KotlinX Serialization supports multiplatform streaming
        val content = response.bodyAsText()
        // TODO: Parse JSON feed
      }
    }

    throw UnsupportedOperationException("Unsupported content type: $contentType")
  }

  private suspend fun handleHttpRedirect(
    response: HttpResponse,
    url: Url,
    redirectCount: Int
  ): FeedFetchResult {
    val headerLocation = response.headers["Location"]
    val redirectToUrl = UrlUtils.safeUrl(host = url.host, url = headerLocation)

    if (redirectToUrl == url.toString() || redirectToUrl.isNullOrBlank()) {
      return FeedFetchResult.Error(Exception("Failed to fetch the feed"))
    }

    return fetch(url = redirectToUrl, redirectCount = redirectCount + 1)
  }

  private fun fetchFeedLinkFromHtmlIfExists(
    htmlContent: ByteReadChannel,
    originalUrl: String
  ): String? {
    val document =
      try {
        Ksoup.parseSource(htmlContent.asSource())
      } catch (t: Throwable) {
        return null
      }

    val linkElement =
      document.getElementsByTag(TAG_LINK).firstOrNull {
        val linkType = it.attr(ATTR_TYPE)
        linkType == RSS_MEDIA_TYPE || linkType == ATOM_MEDIA_TYPE
      }
        ?: return null
    val link = linkElement.attr(ATTR_HREF)
    val host = UrlUtils.extractHost(originalUrl)

    return UrlUtils.safeUrl(host, link)
  }
}
