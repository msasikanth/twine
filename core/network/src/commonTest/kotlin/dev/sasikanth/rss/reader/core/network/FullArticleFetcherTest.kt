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
package dev.sasikanth.rss.reader.core.network

import dev.sasikanth.rss.reader.core.network.miniflux.MinifluxSource
import dev.sasikanth.rss.reader.core.network.utils.TestDispatchersProvider
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.headersOf
import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.core.toByteArray
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlinx.coroutines.test.runTest

class FullArticleFetcherTest {

  private fun fetcherRespondingWith(html: String): FullArticleFetcher {
    val dispatchersProvider = TestDispatchersProvider()
    val httpClient =
      HttpClient(MockEngine) {
        engine {
          dispatcher = dispatchersProvider.io
          addHandler {
            respond(
              content = ByteReadChannel(html.toByteArray()),
              headers = headersOf(HttpHeaders.ContentType, ContentType.Text.Html.toString()),
            )
          }
        }
      }
    val user: suspend () -> Nothing? = { null }

    return FullArticleFetcher(
      user = user,
      httpClient = httpClient,
      minifluxSource =
        MinifluxSource(
          appHttpClient = httpClient,
          user = user,
          dispatchersProvider = dispatchersProvider,
        ),
      dispatchersProvider = dispatchersProvider,
    )
  }

  @Test
  fun fetchWithFragmentExtractsMatchingContainerElement() = runTest {
    val html =
      """
      <!DOCTYPE html>
      <html>
        <head><title>Releases</title></head>
        <body>
          <nav id="table-of-contents"><h2>Table of contents</h2></nav>
          <main id="releases">
            <article id="2026111300">
              <h3><a href="#2026111300">2026111300</a></h3>
              <p>Tags:</p>
              <ul><li>Latest release changes</li></ul>
            </article>
            <article id="2026110800">
              <h3><a href="#2026110800">2026110800</a></h3>
              <p>Older release changes</p>
            </article>
          </main>
        </body>
      </html>
      """
        .trimIndent()

    val result = fetcherRespondingWith(html).fetch("https://example.com/releases#2026111300")
    val body = result.getOrThrow()

    assertTrue(body.contains("Latest release changes"))
    assertFalse(body.contains("Table of contents"))
    assertFalse(body.contains("Older release changes"))
  }

  @Test
  fun fetchWithFragmentExtractsHeadingSectionUntilNextSameLevelHeading() = runTest {
    val html =
      """
      <!DOCTYPE html>
      <html>
        <body>
          <h1>Changelog</h1>
          <h2 id="v2">Version 2</h2>
          <p>Version 2 summary</p>
          <h3>Details</h3>
          <ul><li>Item A</li></ul>
          <h2 id="v1">Version 1</h2>
          <p>Version 1 summary</p>
        </body>
      </html>
      """
        .trimIndent()

    val result = fetcherRespondingWith(html).fetch("https://example.com/changelog#v2")
    val body = result.getOrThrow()

    assertTrue(body.contains("Version 2"))
    assertTrue(body.contains("Version 2 summary"))
    assertTrue(body.contains("Item A"))
    assertFalse(body.contains("Version 1"))
    assertFalse(body.contains("Changelog"))
  }

  @Test
  fun fetchWithMissingFragmentFallsBackToFullCleanedDocument() = runTest {
    val html =
      """
      <!DOCTYPE html>
      <html>
        <body>
          <h1>Releases</h1>
          <p>Full page content</p>
        </body>
      </html>
      """
        .trimIndent()

    val result = fetcherRespondingWith(html).fetch("https://example.com/releases#missing")
    val body = result.getOrThrow()

    assertTrue(body.contains("Releases"))
    assertTrue(body.contains("Full page content"))
  }
}
