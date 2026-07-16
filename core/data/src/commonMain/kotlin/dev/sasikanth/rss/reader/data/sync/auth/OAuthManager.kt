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

import dev.sasikanth.rss.reader.core.model.local.ServiceType
import kotlinx.coroutines.flow.SharedFlow

interface OAuthManager {
  /**
   * Redirect URIs captured by the loopback server on platforms that can't receive custom-scheme
   * deep links (desktop). Never emits on Android/iOS.
   */
  val loopbackRedirects: SharedFlow<String>

  fun getAuthUrl(serviceType: ServiceType): String

  fun setPendingProvider(serviceType: ServiceType)

  suspend fun handleRedirect(uri: String): Boolean
}
