/*
 * Copyright 2023 Sasikanth Miriyampalli
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package dev.sasikanth.rss.reader.core.network.di

import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngineConfig
import io.ktor.client.engine.HttpClientEngineFactory
import io.ktor.client.plugins.cache.HttpCache
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import co.touchlab.kermit.Logger as KermitLogger

expect interface NetworkComponent

fun <T : HttpClientEngineConfig> httpClient(
  engine: HttpClientEngineFactory<T>,
  config: T.() -> Unit
): HttpClient {
  return HttpClient(engine) {
    followRedirects = false

    engine { config() }

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
  }
}
