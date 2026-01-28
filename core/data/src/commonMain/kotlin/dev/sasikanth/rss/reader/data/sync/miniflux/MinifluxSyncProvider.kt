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

package dev.sasikanth.rss.reader.data.sync.miniflux

import dev.sasikanth.rss.reader.core.model.local.ServiceType
import dev.sasikanth.rss.reader.data.repository.RssRepository
import dev.sasikanth.rss.reader.data.repository.SettingsRepository
import dev.sasikanth.rss.reader.data.repository.UserRepository
import dev.sasikanth.rss.reader.data.sync.APIServiceProvider
import dev.sasikanth.rss.reader.di.scopes.AppScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import me.tatarka.inject.annotations.Inject

@Inject
@AppScope
class MinifluxSyncProvider(
  private val userRepository: UserRepository,
  private val rssRepository: RssRepository,
  private val settingsRepository: SettingsRepository,
) : APIServiceProvider {

  override val cloudService: ServiceType = ServiceType.MINIFLUX

  override val isPremium: Boolean = true

  override fun isSignedIn(): Flow<Boolean> {
    return userRepository.user().map {
      it != null && it.serverUrl != null && it.serviceType == ServiceType.MINIFLUX
    }
  }

  override suspend fun isSignedInImmediate(): Boolean {
    val user = userRepository.currentUser()
    return user != null && user.serverUrl != null && user.serviceType == ServiceType.MINIFLUX
  }

  override suspend fun signOut() {
    userRepository.deleteUser()
    rssRepository.deleteAllLocalData()
  }
}
