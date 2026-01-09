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
import dev.sasikanth.rss.reader.core.model.remote.FeedPayload
import dev.sasikanth.rss.reader.core.network.FullArticleFetcher
import dev.sasikanth.rss.reader.core.network.parser.json.JsonFeedParser
import dev.sasikanth.rss.reader.core.network.parser.xml.XmlFeedParser
import dev.sasikanth.rss.reader.core.network.parser.xml.XmlFeedParser.Companion.ATOM_MEDIA_TYPE
import dev.sasikanth.rss.reader.core.network.parser.xml.XmlFeedParser.Companion.ATTR_HREF
import dev.sasikanth.rss.reader.core.network.parser.xml.XmlFeedParser.Companion.ATTR_TYPE
import dev.sasikanth.rss.reader.core.network.parser.xml.XmlFeedParser.Companion.RSS_MEDIA_TYPE
import dev.sasikanth.rss.reader.core.network.parser.xml.XmlFeedParser.Companion.TAG_LINK
import dev.sasikanth.rss.reader.core.network.utils.UrlUtils
import dev.sasikanth.rss.reader.util.DispatchersProvider
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsChannel
import io.ktor.http.BadContentTypeFormatException
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.URLBuilder
import io.ktor.http.Url
import io.ktor.http.contentType
import io.ktor.http.isRelativePath
import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.asSource
import io.ktor.utils.io.readBuffer
import korlibs.io.lang.Charset
import korlibs.io.lang.Charsets
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import me.tatarka.inject.annotations.Inject

@Inject
class FeedFetcher(
  private val httpClient: HttpClient,
  private val xmlFeedParser: XmlFeedParser,
  private val jsonFeedParser: JsonFeedParser,
  private val fullArticleFetcher: FullArticleFetcher,
  private val dispatchersProvider: DispatchersProvider,
) {

  private val networkDispatcher = dispatchersProvider.io.limitedParallelism(10)

  companion object {
    private const val MAX_REDIRECTS_ALLOWED = 5
  }

  suspend fun fetch(url: String, fetchFullContent: Boolean = false): FeedFetchResult {
    return withContext(networkDispatcher) { fetch(url, fetchFullContent, redirectCount = 0) }
  }

  private suspend fun fetch(
    url: String,
    fetchFullContent: Boolean,
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
          parseContent(response, transformedUrl.toString(), fetchFullContent, redirectCount)
        }
        HttpStatusCode.MultipleChoices,
        HttpStatusCode.MovedPermanently,
        HttpStatusCode.Found,
        HttpStatusCode.SeeOther,
        HttpStatusCode.TemporaryRedirect,
        HttpStatusCode.PermanentRedirect -> {
          handleHttpRedirect(response, transformedUrl, fetchFullContent, redirectCount)
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
    fetchFullContent: Boolean,
    redirectCount: Int
  ): FeedFetchResult {
    val contentType =
      try {
        response.contentType()?.withoutParameters()
      } catch (e: BadContentTypeFormatException) {
        // If Ktor fails to identify content type properly we assume the content type is XML and
        // move forward with trying to parse the content accordingly.
        ContentType.Text.Xml
      }

    val responseChannel = response.bodyAsChannel()
    when (contentType) {
      ContentType.Text.Html -> {
        val feedUrl = fetchFeedLinkFromHtmlIfExists(responseChannel, url)

        if (feedUrl != url && !feedUrl.isNullOrBlank()) {
          return fetch(
            url = feedUrl,
            fetchFullContent = fetchFullContent,
            redirectCount = redirectCount + 1
          )
        }
      }

      // There are scenarios where a feed doesn't have any content-type, rare, but it's possible
      // in those scenarios we default to trying to parse them as XML and if it fails, it fails.
      ContentType.Application.Atom,
      ContentType.Application.Rss,
      ContentType.Application.Xml,
      ContentType.Text.Xml,
      ContentType("text", "atom+xml"),
      ContentType("text", "rss+xml"),
      null -> {
        val responseCharset =
          try {
            response.contentType()?.parameter("charset")
          } catch (e: BadContentTypeFormatException) {
            Charsets.UTF8.name
          }
        val charset = Charset.forName(responseCharset ?: Charsets.UTF8.name)

        var feedPayload =
          xmlFeedParser.parse(
            feedUrl = url,
            content = responseChannel,
            charset = charset,
          )

        if (fetchFullContent) {
          feedPayload = fetchFullContentForPosts(feedPayload)
        }

        return FeedFetchResult.Success(feedPayload)
      }
      ContentType.Application.Json -> {
        val jsonBuffer = responseChannel.readBuffer()
        var feedPayload =
          jsonFeedParser.parse(
            content = jsonBuffer,
            feedUrl = url,
          )

        if (fetchFullContent) {
          feedPayload = fetchFullContentForPosts(feedPayload)
        }

        return FeedFetchResult.Success(feedPayload)
      }
    }

    throw UnsupportedOperationException("Unsupported content type: $contentType")
  }

  private suspend fun fetchFullContentForPosts(feedPayload: FeedPayload): FeedPayload {
    return withContext(networkDispatcher) {
      val postsWithFullContent =
        feedPayload.posts
          .map { post ->
            async {
              val fullContent = fullArticleFetcher.fetch(post.link).getOrNull()
              post.copy(fullContent = fullContent)
            }
          }
          .awaitAll()

      feedPayload.copy(posts = postsWithFullContent)
    }
  }

  private suspend fun handleHttpRedirect(
    response: HttpResponse,
    url: Url,
    fetchFullContent: Boolean,
    redirectCount: Int
  ): FeedFetchResult {
    val headerLocation = response.headers["Location"]
    val redirectToUrl = UrlUtils.safeUrl(host = url.host, url = headerLocation)

    if (redirectToUrl == url.toString() || redirectToUrl.isNullOrBlank()) {
      return FeedFetchResult.Error(Exception("Failed to fetch the feed"))
    }

    return fetch(
      url = redirectToUrl,
      fetchFullContent = fetchFullContent,
      redirectCount = redirectCount + 1
    )
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
