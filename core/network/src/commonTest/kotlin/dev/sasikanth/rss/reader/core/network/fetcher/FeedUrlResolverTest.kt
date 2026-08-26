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

import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.engine.mock.respondError
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json

class FeedUrlResolverTest {

  private val failingOEmbed = MockEngine { respondError(HttpStatusCode.NotFound) }

  private fun resolver(engine: MockEngine = failingOEmbed): FeedUrlResolver {
    val client =
      HttpClient(engine) { install(ContentNegotiation) { json(Json { ignoreUnknownKeys = true }) } }

    return FeedUrlResolver(client)
  }

  private fun oEmbedResponding(authorUrl: String) = MockEngine {
    respond(
      content = """{"author_url":"$authorUrl"}""",
      headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()),
    )
  }

  @Test
  fun resolve_shouldAppendRssToSubredditLinks() = runTest {
    val expected = "https://www.reddit.com/r/androiddev/.rss"

    assertEquals(expected, resolver().resolve("https://www.reddit.com/r/androiddev"))
    assertEquals(expected, resolver().resolve("https://www.reddit.com/r/androiddev/"))
    assertEquals(expected, resolver().resolve("https://old.reddit.com/r/androiddev"))
    assertEquals(expected, resolver().resolve("reddit.com/r/androiddev"))
    assertEquals(expected, resolver().resolve("  https://www.reddit.com/r/androiddev  "))
  }

  @Test
  fun resolve_shouldNormaliseRedditUserLinks() = runTest {
    val expected = "https://www.reddit.com/user/spez/.rss"

    assertEquals(expected, resolver().resolve("https://www.reddit.com/user/spez"))
    assertEquals(expected, resolver().resolve("https://www.reddit.com/u/spez"))
  }

  @Test
  fun resolve_shouldLeaveNonRootRedditLinksUntouched() = runTest {
    val alreadyAFeed = "https://www.reddit.com/r/androiddev/.rss"
    val postLink = "https://www.reddit.com/r/androiddev/comments/abc123/some_post/"

    assertEquals(alreadyAFeed, resolver().resolve(alreadyAFeed))
    assertEquals(postLink, resolver().resolve(postLink))
  }

  @Test
  fun resolve_shouldExpandMastodonHandles() = runTest {
    assertEquals(
      "https://mastodon.social/@sasikanth.rss",
      resolver().resolve("@sasikanth@mastodon.social"),
    )
  }

  @Test
  fun resolve_shouldNotTreatEmailAddressesAsMastodonHandles() = runTest {
    val email = "hello@example.com"

    assertEquals(email, resolver().resolve(email))
  }

  @Test
  fun resolve_shouldBuildChannelFeedFromChannelLinks() = runTest {
    val expected = "https://www.youtube.com/feeds/videos.xml?channel_id=UCBJycsmduvYEL83R_U4JriQ"

    assertEquals(
      expected,
      resolver().resolve("https://www.youtube.com/channel/UCBJycsmduvYEL83R_U4JriQ"),
    )
    assertEquals(
      expected,
      resolver().resolve("https://www.youtube.com/channel/UCBJycsmduvYEL83R_U4JriQ/videos"),
    )
  }

  @Test
  fun resolve_shouldResolveVideoLinksToChannelFeedViaOEmbed() = runTest {
    val resolver =
      resolver(oEmbedResponding("https://www.youtube.com/channel/UCBJycsmduvYEL83R_U4JriQ"))

    assertEquals(
      "https://www.youtube.com/feeds/videos.xml?channel_id=UCBJycsmduvYEL83R_U4JriQ",
      resolver.resolve("https://www.youtube.com/watch?v=dQw4w9WgXcQ"),
    )
  }

  @Test
  fun resolve_shouldFallBackToAuthorUrlWhenOEmbedReturnsAHandle() = runTest {
    val resolver = resolver(oEmbedResponding("https://www.youtube.com/@mkbhd"))

    assertEquals("https://www.youtube.com/@mkbhd", resolver.resolve("https://youtu.be/dQw4w9WgXcQ"))
  }

  @Test
  fun resolve_shouldKeepOriginalVideoLinkWhenOEmbedFails() = runTest {
    val videoLink = "https://www.youtube.com/watch?v=dQw4w9WgXcQ"

    assertEquals(videoLink, resolver().resolve(videoLink))
  }

  @Test
  fun resolve_shouldLeaveOrdinaryLinksUntouched() = runTest {
    val link = "https://sasikanth.dev/rss.xml"
    val channelHandleLink = "https://www.youtube.com/@mkbhd"

    assertEquals(link, resolver().resolve(link))
    assertEquals(channelHandleLink, resolver().resolve(channelHandleLink))
  }
}
