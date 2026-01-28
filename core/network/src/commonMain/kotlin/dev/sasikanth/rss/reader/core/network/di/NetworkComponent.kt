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

import co.touchlab.kermit.Logger as KermitLogger
import dev.sasikanth.rss.reader.app.AppInfo
import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngineConfig
import io.ktor.client.engine.HttpClientEngineFactory
import io.ktor.client.plugins.UserAgent
import io.ktor.client.plugins.cache.HttpCache
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.plugins.resources.Resources
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

expect interface NetworkComponent

fun <T : HttpClientEngineConfig> httpClient(
  appInfo: AppInfo,
  engine: HttpClientEngineFactory<T>,
  config: T.() -> Unit
): HttpClient {
  return HttpClient(engine) {
    followRedirects = false

    engine { config() }

    install(UserAgent) {
      agent = "Twine/${appInfo.versionName} (https://github.com/msasikanth/twine)"
    }

    install(HttpCache)

    install(Logging) {
      level = LogLevel.INFO
      logger =
        object : Logger {
          override fun log(message: String) {
            KermitLogger.i("HttpClient") {
              """
              |---
              |$message
              |---
            """
                .trimMargin("|")
            }
          }
        }
    }

    install(ContentNegotiation) { json(Json { ignoreUnknownKeys = true }) }

    install(Resources)
  }
}
