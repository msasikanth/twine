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

import java.io.IOException
import java.net.InetAddress
import java.net.ServerSocket
import kotlin.concurrent.thread

internal actual class OAuthRedirectServer {

  private var serverSocket: ServerSocket? = null

  actual fun start(onRedirect: (String) -> Unit): String? {
    stop()

    // Both Dropbox and Google ignore the port when matching loopback redirect URIs
    // (RFC 8252 section 7.3), so an ephemeral port is safe.
    val socket = ServerSocket(0, BACKLOG, InetAddress.getLoopbackAddress())
    socket.soTimeout = ACCEPT_TIMEOUT_MILLIS
    serverSocket = socket
    val baseUri = "http://127.0.0.1:${socket.localPort}"

    thread(isDaemon = true, name = "twine-oauth-redirect") {
      try {
        while (true) {
          // Browsers open speculative connections that never send a request, so failures
          // reading a single connection must not tear down the whole server.
          val delivered =
            socket.accept().use { connection ->
              try {
                connection.soTimeout = CONNECTION_READ_TIMEOUT_MILLIS
                val requestLine = connection.getInputStream().bufferedReader().readLine().orEmpty()
                val path = requestLine.split(" ").getOrNull(1).orEmpty()
                val isCallback = path.startsWith(CALLBACK_PATH)

                val body = if (isCallback) SUCCESS_RESPONSE else NOT_FOUND_RESPONSE
                val status = if (isCallback) "200 OK" else "404 Not Found"
                val bodyBytes = body.encodeToByteArray()
                connection.getOutputStream().apply {
                  write(
                    ("HTTP/1.1 $status\r\n" +
                        "Content-Type: text/html; charset=utf-8\r\n" +
                        "Content-Length: ${bodyBytes.size}\r\n" +
                        "Connection: close\r\n" +
                        "\r\n")
                      .encodeToByteArray()
                  )
                  write(bodyBytes)
                  flush()
                }

                if (isCallback) {
                  onRedirect(baseUri + path)
                }
                isCallback
              } catch (_: IOException) {
                false
              }
            }

          if (delivered) break
        }
      } catch (_: IOException) {
        // Server was stopped or timed out waiting for the redirect
      } finally {
        // Close only this thread's socket; stop() could close a newer server
        // started by a subsequent sign-in attempt.
        try {
          socket.close()
        } catch (_: IOException) {}
      }
    }

    return baseUri + CALLBACK_PATH
  }

  actual fun stop() {
    try {
      serverSocket?.close()
    } catch (_: IOException) {
      // ignore
    }
    serverSocket = null
  }

  private companion object {
    const val CALLBACK_PATH = "/callback"
    const val BACKLOG = 8
    const val ACCEPT_TIMEOUT_MILLIS = 5 * 60 * 1000
    const val CONNECTION_READ_TIMEOUT_MILLIS = 5 * 1000

    val SUCCESS_RESPONSE =
      """
      <!DOCTYPE html>
      <html>
        <head><meta charset="utf-8"><title>Twine</title></head>
        <body style="font-family: sans-serif; text-align: center; padding-top: 4rem;">
          <h2>Sign-in complete</h2>
          <p>You can close this window and return to Twine.</p>
        </body>
      </html>
      """
        .trimIndent()

    const val NOT_FOUND_RESPONSE = "<!DOCTYPE html><html><body>Not found</body></html>"
  }
}
