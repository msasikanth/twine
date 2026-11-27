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

import dev.sasikanth.rss.reader.core.network.utils.TestDispatchersProvider
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.core.toByteArray
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlinx.coroutines.test.runTest

class PodcastArtworkEnricherTest {

  private val dispatchersProvider = TestDispatchersProvider()

  private fun createEnricher(
    handler: suspend (url: String) -> Triple<HttpStatusCode, Map<String, String>, String>
  ): PodcastArtworkEnricher {
    val httpClient =
      HttpClient(MockEngine) {
        engine {
          dispatcher = dispatchersProvider.io
          addHandler { request ->
            val (status, headersMap, body) = handler(request.url.toString())
            val headersBuilder =
              headersOf(*headersMap.map { it.key to listOf(it.value) }.toTypedArray())
            respond(
              content = ByteReadChannel(body.toByteArray()),
              status = status,
              headers = headersBuilder,
            )
          }
        }
      }
    return PodcastArtworkEnricher(httpClient, dispatchersProvider)
  }

  @Test
  fun extractsItunesImageAndIgnoresHugeItemsAfterHeader() = runTest {
    val hugeItemPadding = "x".repeat(128 * 1024)
    val rssXml =
      """
      <?xml version="1.0" encoding="UTF-8"?>
      <rss version="2.0" xmlns:itunes="http://www.itunes.com/dtds/podcast-1.0.dtd">
        <channel>
          <title>Big Podcast</title>
          <link>https://example.com</link>
          <itunes:image href="https://cdn.example.com/show-3000.jpg" />
          <image><url>https://cdn.example.com/small-logo.png</url></image>
          <item><title>Ep 1</title><description>$hugeItemPadding</description></item>
        </channel>
      </rss>
      """
        .trimIndent()

    val enricher = createEnricher {
      Triple(HttpStatusCode.OK, mapOf(HttpHeaders.ContentType to "application/rss+xml"), rssXml)
    }

    assertEquals(
      "https://cdn.example.com/show-3000.jpg",
      enricher.fetchCoverArtUrl("https://example.com/feed.xml"),
    )
  }

  @Test
  fun extractsHighestResPodcastImagesSrcset() = runTest {
    val rssXml =
      """
      <?xml version="1.0" encoding="UTF-8"?>
      <rss version="2.0" xmlns:podcast="https://podcastindex.org/namespace/1.0">
        <channel>
          <title>Podcast 2.0</title>
          <podcast:images srcset="https://cdn.example.com/300.jpg 300w, https://cdn.example.com/1500.jpg 1500w" />
          <item><title>Ep 1</title></item>
        </channel>
      </rss>
      """
        .trimIndent()

    val enricher = createEnricher {
      Triple(HttpStatusCode.OK, mapOf(HttpHeaders.ContentType to "application/xml"), rssXml)
    }

    assertEquals(
      "https://cdn.example.com/1500.jpg",
      enricher.fetchCoverArtUrl("https://example.com/feed.xml"),
    )
  }

  @Test
  fun atomLogoIsExtractedWhileSmallAtomIconIsRejected() = runTest {
    val atomWithLogo =
      """
      <?xml version="1.0" encoding="UTF-8"?>
      <feed xmlns="http://www.w3.org/2005/Atom">
        <title>Atom Podcast</title>
        <icon>https://example.com/icon-16.png</icon>
        <logo>https://example.com/logo-1024.png</logo>
        <entry><title>Ep 1</title></entry>
      </feed>
      """
        .trimIndent()
    val atomOnlyIcon =
      """
      <?xml version="1.0" encoding="UTF-8"?>
      <feed xmlns="http://www.w3.org/2005/Atom">
        <title>Atom Blog</title>
        <icon>https://example.com/icon-16.png</icon>
        <entry><title>Post 1</title></entry>
      </feed>
      """
        .trimIndent()

    val enricher = createEnricher { url ->
      val body = if (url.endsWith("logo.xml")) atomWithLogo else atomOnlyIcon
      Triple(HttpStatusCode.OK, mapOf(HttpHeaders.ContentType to "application/atom+xml"), body)
    }

    assertEquals(
      "https://example.com/logo-1024.png",
      enricher.fetchCoverArtUrl("https://example.com/logo.xml"),
    )
    assertNull(enricher.fetchCoverArtUrl("https://example.com/icon.xml"))
  }

  @Test
  fun extractsJsonFeedIconEvenWhenTruncatedOver64Kb() = runTest {
    val hugeItemDescription = "a".repeat(80 * 1024)
    val jsonFeed =
      """
      {
        "version": "https://jsonfeed.org/version/1.1",
        "title": "JSON Podcast",
        "icon": "/artwork/show.png",
        "items": [
          {
            "id": "1",
            "content_text": "$hugeItemDescription"
          }
        ]
      }
      """
        .trimIndent()

    val enricher = createEnricher {
      Triple(HttpStatusCode.OK, mapOf(HttpHeaders.ContentType to "application/json"), jsonFeed)
    }

    assertEquals(
      "http://podcasts.example.com/artwork/show.png",
      enricher.fetchCoverArtUrl("https://podcasts.example.com/feed.json"),
    )
  }

  @Test
  fun followsRedirectsAndRejectsLowResFaviconUrls() = runTest {
    val rssWithLowRes =
      """
      <?xml version="1.0" encoding="UTF-8"?>
      <rss version="2.0"><channel>
        <image><url>https://www.google.com/s2/favicons?domain=example.com</url></image>
      </channel></rss>
      """
        .trimIndent()
    val rssRedirected =
      """
      <?xml version="1.0" encoding="UTF-8"?>
      <rss version="2.0" xmlns:googleplay="http://www.google.com/schemas/play-podcasts/1.0">
        <channel>
          <googleplay:image href="/assets/cover.jpg" />
        </channel>
      </rss>
      """
        .trimIndent()

    val enricher = createEnricher { url ->
      when (url) {
        "https://old.example.com/feed.xml" ->
          Triple(
            HttpStatusCode.MovedPermanently,
            mapOf(HttpHeaders.Location to "https://cdn.example.org/podcast.xml"),
            "",
          )
        "https://cdn.example.org/podcast.xml" ->
          Triple(
            HttpStatusCode.OK,
            mapOf(HttpHeaders.ContentType to "application/rss+xml"),
            rssRedirected,
          )
        else ->
          Triple(
            HttpStatusCode.OK,
            mapOf(HttpHeaders.ContentType to "application/rss+xml"),
            rssWithLowRes,
          )
      }
    }

    assertEquals(
      "http://cdn.example.org/assets/cover.jpg",
      enricher.fetchCoverArtUrl("https://old.example.com/feed.xml"),
    )
    assertNull(enricher.fetchCoverArtUrl("https://example.com/lowres.xml"))
  }

  @Test
  fun returnsNullGracefullyOnHttpErrorOrInvalidContent() = runTest {
    val enricher = createEnricher { url ->
      if (url.endsWith("error")) {
        Triple(HttpStatusCode.NotFound, emptyMap(), "Not found")
      } else {
        Triple(HttpStatusCode.OK, mapOf(HttpHeaders.ContentType to "text/html"), "<html></html>")
      }
    }

    assertNull(enricher.fetchCoverArtUrl("https://example.com/error"))
    assertNull(enricher.fetchCoverArtUrl("https://example.com/html"))
    assertNull(enricher.fetchCoverArtUrl(""))
  }
}
