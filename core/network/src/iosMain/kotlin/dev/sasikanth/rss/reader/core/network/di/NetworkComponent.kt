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

package dev.sasikanth.rss.reader.core.network.di

import dev.sasikanth.rss.reader.app.AppInfo
import dev.sasikanth.rss.reader.di.scopes.AppScope
import io.ktor.client.HttpClient
import io.ktor.client.engine.darwin.Darwin
import io.ktor.client.plugins.timeout
import io.ktor.client.request.request
import me.tatarka.inject.annotations.Provides
import platform.posix._SC_PAGESIZE
import platform.posix.sysconf

actual interface NetworkComponent {

  @Provides
  @AppScope
  fun providesHttpClient(appInfo: AppInfo): HttpClient {
    return httpClient(
      engine = Darwin,
      appInfo = appInfo,
      config = {
        configureRequest {
          setAllowsCellularAccess(true)
          request { timeout { connectTimeoutMillis = 10_000 } }
        }
      }
    )
  }

  @Provides
  @AppScope
  fun providesPlatformPageSize(): Long {
    return sysconf(_SC_PAGESIZE)
  }
}
