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

import dev.sasikanth.rss.reader.core.network.FullArticleFetcher
import dev.sasikanth.rss.reader.core.network.miniflux.MinifluxSource
import dev.sasikanth.rss.reader.core.network.parser.common.ArticleHtmlParser
import dev.sasikanth.rss.reader.core.network.parser.json.JsonFeedParser
import dev.sasikanth.rss.reader.core.network.parser.xml.AtomContentParser
import dev.sasikanth.rss.reader.core.network.parser.xml.RDFContentParser
import dev.sasikanth.rss.reader.core.network.parser.xml.RSSContentParser
import dev.sasikanth.rss.reader.core.network.parser.xml.XmlFeedParser
import dev.sasikanth.rss.reader.core.network.utils.TestDispatchersProvider
import dev.sasikanth.rss.reader.core.network.utils.atomXmlContent
import dev.sasikanth.rss.reader.core.network.utils.feedUrl
import dev.sasikanth.rss.reader.core.network.utils.jsonFeed
import dev.sasikanth.rss.reader.core.network.utils.rdfXmlContent
import dev.sasikanth.rss.reader.core.network.utils.rssXmlContent
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.HttpHeaders
import io.ktor.http.headersOf
import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.core.toByteArray
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlinx.coroutines.test.runTest

class FeedFetcherTest {

  private fun fetcherRespondingWith(
    contentType: String?,
    content: String = rssXmlContent,
  ): FeedFetcher {
    val dispatchersProvider = TestDispatchersProvider()
    val articleHtmlParser = ArticleHtmlParser()
    val httpClient =
      HttpClient(MockEngine) {
        engine {
          dispatcher = dispatchersProvider.io
          addHandler {
            respond(
              content = ByteReadChannel(content.toByteArray()),
              headers =
                if (contentType == null) headersOf()
                else headersOf(HttpHeaders.ContentType, contentType),
            )
          }
        }
      }
    val user: suspend () -> Nothing? = { null }

    return FeedFetcher(
      httpClient = httpClient,
      xmlFeedParser =
        XmlFeedParser(
          rdfContentParser = RDFContentParser(articleHtmlParser),
          rssContentParser = RSSContentParser(articleHtmlParser),
          atomContentParser = AtomContentParser(httpClient, articleHtmlParser),
          dispatchersProvider = dispatchersProvider,
          platformPageSize = 4096L,
        ),
      jsonFeedParser =
        JsonFeedParser(
          dispatchersProvider = dispatchersProvider,
          articleHtmlParser = articleHtmlParser,
        ),
      fullArticleFetcher =
        FullArticleFetcher(
          user = user,
          httpClient = httpClient,
          minifluxSource =
            MinifluxSource(
              appHttpClient = httpClient,
              user = user,
              dispatchersProvider = dispatchersProvider,
            ),
          dispatchersProvider = dispatchersProvider,
        ),
      feedUrlResolver = FeedUrlResolver(httpClient),
      dispatchersProvider = dispatchersProvider,
    )
  }

  private suspend fun fetchWithContentType(
    contentType: String?,
    content: String = rssXmlContent,
  ): FeedFetchResult = fetcherRespondingWith(contentType, content).fetch(feedUrl)

  @Test
  fun feedsServedWithAVendorPrefixedXmlContentTypeShouldBeParsed() = runTest {
    // when
    val result = fetchWithContentType("application/x-rss+xml")

    // then
    assertTrue(result is FeedFetchResult.Success)
    assertEquals("Feed title", result.feedPayload.name)
  }

  @Test
  fun feedsServedWithAnyXmlContentTypeShouldBeParsed() = runTest {
    val contentTypes =
      listOf(
        "application/rss+xml",
        "application/atom+xml",
        "application/xml",
        "text/xml",
        "text/rss+xml",
        "text/atom+xml",
        "application/x-rss+xml",
        "application/x-atom+xml",
        "APPLICATION/RSS+XML",
        "application/rss+xml; charset=utf-8",
        null,
      )

    contentTypes.forEach { contentType ->
      // when
      val result = fetchWithContentType(contentType)

      // then
      assertTrue(result is FeedFetchResult.Success, "expected success for $contentType")
      assertEquals("Feed title", result.feedPayload.name)
    }
  }

  @Test
  fun feedsServedWithANonXmlContentTypeShouldStillBeRejected() = runTest {
    // when
    val result = fetchWithContentType("application/octet-stream")

    // then
    assertTrue(result is FeedFetchResult.Error)
    assertTrue(result.exception is UnsupportedOperationException)
  }

  @Test
  fun xmlFeedsServedAsTextPlainShouldBeParsed() = runTest {
    val cases =
      listOf(
        "text/plain" to rssXmlContent,
        "text/plain; charset=utf-8" to "\uFEFF  \n$rssXmlContent",
        "text/plain; charset=utf-8" to atomXmlContent,
        "text/plain" to rdfXmlContent,
      )

    cases.forEach { (contentType, content) ->
      val result = fetchWithContentType(contentType, content)

      assertTrue(result is FeedFetchResult.Success, "expected success for $contentType")
    }
  }

  @Test
  fun jsonFeedServedAsTextPlainShouldBeParsed() = runTest {
    // when
    val result = fetchWithContentType("text/plain; charset=utf-8", jsonFeed)

    // then
    assertTrue(result is FeedFetchResult.Success)
  }

  @Test
  fun nonFeedTextPlainContentShouldBeRejected() = runTest {
    // when
    val result = fetchWithContentType("text/plain; charset=utf-8", "Just some plain text")

    // then
    assertTrue(result is FeedFetchResult.Error)
    assertTrue(result.exception is UnsupportedOperationException)
  }
}
