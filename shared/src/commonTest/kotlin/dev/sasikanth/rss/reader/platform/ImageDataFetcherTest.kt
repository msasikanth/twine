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

package dev.sasikanth.rss.reader.platform

import coil3.ImageLoader
import coil3.PlatformContext
import dev.sasikanth.rss.reader.util.DispatchersProvider
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest

class ImageDataFetcherTest {

  @Test
  fun fetchesPngFromHttpAndDetectsMimeAndExtension() = runTest {
    val pngBytes = byteArrayOf(0x89.toByte(), 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A, 0x01, 0x02)
    val fetcher =
      createFetcher(StandardTestDispatcher(testScheduler)) {
        respond(
          content = pngBytes,
          status = HttpStatusCode.OK,
          headers = headersOf(HttpHeaders.ContentType, "application/octet-stream"),
        )
      }

    val payload = fetcher.fetch("https://example.com/photos/avatar?size=large")

    assertNotNull(payload)
    assertContentEquals(pngBytes, payload.bytes)
    assertEquals("image/png", payload.mimeType)
    assertEquals("avatar.png", payload.fileName)
  }

  @Test
  fun fetchesJpegAndPreservesExtension() = runTest {
    val jpegBytes = byteArrayOf(0xFF.toByte(), 0xD8.toByte(), 0xFF.toByte(), 0xE0.toByte())
    val fetcher =
      createFetcher(StandardTestDispatcher(testScheduler)) {
        respond(content = jpegBytes, status = HttpStatusCode.OK)
      }

    val payload = fetcher.fetch("https://example.com/hero.jpeg")

    assertNotNull(payload)
    assertEquals("image/jpeg", payload.mimeType)
    assertEquals("hero.jpg", payload.fileName)
  }

  @Test
  fun returnsNullOnHttpError() = runTest {
    val fetcher =
      createFetcher(StandardTestDispatcher(testScheduler)) {
        respond(content = "Not Found", status = HttpStatusCode.NotFound)
      }

    val payload = fetcher.fetch("https://example.com/missing.png")

    assertNull(payload)
  }

  @Test
  fun decodesBase64DataUri() = runTest {
    val fetcher =
      createFetcher(StandardTestDispatcher(testScheduler)) {
        respond(content = ByteArray(0), status = HttpStatusCode.OK)
      }
    // 1x1 transparent PNG
    val dataUri =
      "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mNkYAAAAAYAAjCB0C8AAAAASUVORK5CYII="

    val payload = fetcher.fetch(dataUri)

    assertNotNull(payload)
    assertEquals("image/png", payload.mimeType)
    assertTrue(payload.fileName.endsWith(".png"))
    assertTrue(payload.bytes.isNotEmpty())
  }

  private fun createFetcher(
    dispatcher: CoroutineDispatcher,
    handler:
      suspend io.ktor.client.engine.mock.MockRequestHandleScope.(
        io.ktor.client.request.HttpRequestData
      ) -> io.ktor.client.request.HttpResponseData,
  ): ImageDataFetcher {
    val httpClient = HttpClient(MockEngine { request -> handler(request) })
    val imageLoader = ImageLoader.Builder(PlatformContext.INSTANCE).build()
    val dispatchersProvider =
      object : DispatchersProvider {
        override val main: CoroutineDispatcher = dispatcher
        override val io: CoroutineDispatcher = dispatcher
        override val default: CoroutineDispatcher = dispatcher
        override val databaseRead: CoroutineDispatcher = dispatcher
        override val databaseWrite: CoroutineDispatcher = dispatcher
      }
    return ImageDataFetcher(
      httpClient = httpClient,
      imageLoader = imageLoader,
      dispatchersProvider = dispatchersProvider,
    )
  }
}
