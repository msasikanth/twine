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

package dev.sasikanth.rss.reader.favicons

import coil3.PlatformContext
import coil3.annotation.ExperimentalCoilApi
import coil3.fetch.FetchResult
import coil3.network.CacheStrategy
import coil3.network.ConnectivityChecker
import coil3.network.DeDupeConcurrentRequestStrategy
import coil3.network.NetworkClient
import coil3.network.NetworkFetcher
import coil3.network.NetworkRequest
import coil3.network.NetworkResponse
import coil3.network.NetworkResponseBody
import coil3.request.CachePolicy
import coil3.request.Options
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlinx.coroutines.test.runTest
import okio.BufferedSink
import okio.FileSystem
import okio.Path

private const val HOMEPAGE_URL = "https://example.com/"
private const val ICON_PATH = "/favicon-32x32.png"
private const val EXPECTED_ICON_URL = "https://example.com$ICON_PATH"
private const val GOOGLE_FALLBACK_PREFIX = "https://www.google.com/s2/favicons"

@OptIn(ExperimentalCoilApi::class)
class FavIconFetcherTest {

  @Test
  fun fetch_withIconLinkInSmallPage_resolvesFavIconFromHtml() = runTest {
    val attempt = attemptFetch(htmlWithIconLink(paddingBytes = 0))

    assertEquals(EXPECTED_ICON_URL, attempt.favIconUrl)
    assertTrue(attempt.result.isSuccess)
  }

  @Test
  fun fetch_withIconLinkInPageOver64KB_resolvesFavIconFromHtmlInsteadOfFallingBackToGoogle() =
    runTest {
      val attempt = attemptFetch(htmlWithIconLink(paddingBytes = 100 * 1024))

      assertEquals(EXPECTED_ICON_URL, attempt.favIconUrl)
      assertTrue(attempt.result.isSuccess)
    }

  @Test
  fun fetch_withNoIconLink_fallsBackToGoogleFavIconService() = runTest {
    val html = "<html><head><title>No icon here</title></head><body>Hello</body></html>"

    val attempt = attemptFetch(html)

    assertTrue(attempt.favIconUrl.startsWith(GOOGLE_FALLBACK_PREFIX))
  }

  private fun htmlWithIconLink(paddingBytes: Int): String {
    val padding = if (paddingBytes > 0) "<!-- ${"x".repeat(paddingBytes)} -->" else ""
    return """
      <html>
        <head>
          <link rel="icon" href="$ICON_PATH">
        </head>
        <body>$padding</body>
      </html>
      """
      .trimIndent()
  }

  private class FetchAttempt(val favIconUrl: String, val result: Result<FetchResult>)

  /**
   * Delivers the whole body in a single [BufferedSink.write] call. Real ktor engines stream in
   * fixed 8KB chunks that always land exactly on MAX_HTML_READ_SIZE's 64KB boundary and never
   * exercise FavIconFetcher's close()-flush-after-limit path; this reproduces that path instead.
   */
  private class BulkNetworkClient(private val responses: Map<String, ByteArray>) : NetworkClient {
    override suspend fun <T> executeRequest(
      request: NetworkRequest,
      block: suspend (response: NetworkResponse) -> T,
    ): T {
      val bytes = requireNotNull(responses[request.url]) { "No stub for ${request.url}" }
      return block(NetworkResponse(code = 200, body = BulkResponseBody(bytes)))
    }
  }

  private class BulkResponseBody(private val bytes: ByteArray) : NetworkResponseBody {
    override suspend fun writeTo(sink: BufferedSink) {
      sink.write(bytes)
    }

    override suspend fun writeTo(fileSystem: FileSystem, path: Path) {
      fileSystem.write(path) { write(bytes) }
    }

    override fun close() = Unit
  }

  private suspend fun attemptFetch(homepageHtml: String): FetchAttempt {
    var capturedFavIconUrl: String? = null

    val networkClient =
      lazy<NetworkClient> {
        BulkNetworkClient(
          mapOf(
            HOMEPAGE_URL to homepageHtml.toByteArray(Charsets.UTF_8),
            EXPECTED_ICON_URL to byteArrayOf(1, 2, 3, 4),
          )
        )
      }
    val cacheStrategy = lazy { CacheStrategy.DEFAULT }
    val options =
      Options(context = PlatformContext.INSTANCE, diskCachePolicy = CachePolicy.DISABLED)

    val fetcher =
      FavIconFetcher(
        url = HOMEPAGE_URL,
        options = options,
        networkClient = networkClient,
        diskCache = lazy { null },
        cacheStrategy = cacheStrategy,
        networkFetcher = { favIconUrl ->
          capturedFavIconUrl = favIconUrl
          NetworkFetcher(
            url = favIconUrl,
            options = options.copy(diskCacheKey = HOMEPAGE_URL),
            networkClient = networkClient,
            diskCache = lazy { null },
            cacheStrategy = cacheStrategy,
            connectivityChecker = lazy { ConnectivityChecker.ONLINE },
            concurrentRequestStrategy = lazy { DeDupeConcurrentRequestStrategy() },
          )
        },
      )

    val result = runCatching { fetcher.fetch() }

    return FetchAttempt(favIconUrl = checkNotNull(capturedFavIconUrl), result = result)
  }
}
