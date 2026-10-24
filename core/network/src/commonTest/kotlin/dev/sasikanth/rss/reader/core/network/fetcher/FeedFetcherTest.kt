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
import dev.sasikanth.rss.reader.core.network.utils.feedUrl
import dev.sasikanth.rss.reader.core.network.utils.rssXmlContent
import dev.sasikanth.rss.reader.util.DispatchersProvider
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.HttpHeaders
import io.ktor.http.headersOf
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.runTest

class FeedFetcherTest {

  /** [FeedFetcher] calls `limitedParallelism`, which the test dispatchers do not support. */
  private class FetcherDispatchersProvider : DispatchersProvider {
    override val main: CoroutineDispatcher = Dispatchers.Default
    override val io: CoroutineDispatcher = Dispatchers.Default
    override val default: CoroutineDispatcher = Dispatchers.Default
    override val databaseRead: CoroutineDispatcher = Dispatchers.Default
    override val databaseWrite: CoroutineDispatcher = Dispatchers.Default
  }

  private fun fetcherRespondingWith(contentType: String?): FeedFetcher {
    val dispatchersProvider = FetcherDispatchersProvider()
    val articleHtmlParser = ArticleHtmlParser()
    val httpClient =
      HttpClient(MockEngine) {
        engine {
          addHandler {
            respond(
              content = rssXmlContent,
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

  private suspend fun fetchWithContentType(contentType: String?): FeedFetchResult =
    fetcherRespondingWith(contentType).fetch(feedUrl)

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
}
