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

package dev.sasikanth.rss.reader.data.sync.auth

import java.net.HttpURLConnection
import java.net.Socket
import java.net.URI
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class OAuthRedirectServerTest {

  @Test
  fun should_capture_callback_and_respond_with_success_page() {
    val server = OAuthRedirectServer()
    var capturedUri: String? = null
    val latch = CountDownLatch(1)

    val redirectUri =
      server.start { uri ->
        capturedUri = uri
        latch.countDown()
      }

    assertNotNull(redirectUri)
    assertTrue(redirectUri.startsWith("http://127.0.0.1:"))
    assertTrue(redirectUri.endsWith("/callback"))

    val connection =
      URI("$redirectUri?code=test_code_123").toURL().openConnection() as HttpURLConnection
    val responseBody = connection.inputStream.readBytes().decodeToString()

    assertEquals(200, connection.responseCode)
    assertTrue(responseBody.contains("You can close this window"))
    assertTrue(latch.await(5, TimeUnit.SECONDS))
    assertEquals("$redirectUri?code=test_code_123", capturedUri)

    server.stop()
  }

  @Test
  fun should_survive_speculative_connections_and_unknown_paths() {
    val server = OAuthRedirectServer()
    var capturedUri: String? = null
    val latch = CountDownLatch(1)

    val redirectUri =
      server.start { uri ->
        capturedUri = uri
        latch.countDown()
      }
    assertNotNull(redirectUri)
    val port = URI(redirectUri).port

    // Browser-style speculative connection that sends nothing
    Socket("127.0.0.1", port).close()

    // Unrelated request, e.g. favicon
    val faviconConnection =
      URI("http://127.0.0.1:$port/favicon.ico").toURL().openConnection() as HttpURLConnection
    assertEquals(404, faviconConnection.responseCode)

    val connection = URI("$redirectUri?code=abc").toURL().openConnection() as HttpURLConnection
    assertEquals(200, connection.responseCode)
    assertTrue(latch.await(5, TimeUnit.SECONDS))
    assertEquals("$redirectUri?code=abc", capturedUri)

    server.stop()
  }
}
