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

// Desktop browsers can't deliver custom-scheme redirects back to the JVM process, so the
// JVM actual captures the OAuth redirect on a loopback HTTP server (RFC 8252 section 7.3)
// instead. Android and iOS receive redirects as deep links and don't start a server.
internal expect class OAuthRedirectServer() {

  /**
   * Starts listening for the OAuth redirect and returns the redirect URI to use in the
   * authorization request, or null when this platform receives redirects via deep links.
   */
  fun start(onRedirect: (String) -> Unit): String?

  fun stop()
}
