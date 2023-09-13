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
package dev.sasikanth.rss.reader.network

import dev.sasikanth.rss.reader.models.remote.FeedPayload
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.http.URLBuilder
import io.ktor.http.URLProtocol
import kotlinx.coroutines.CoroutineDispatcher

internal expect fun feedFetcher(ioDispatcher: CoroutineDispatcher): FeedFetcher

internal class FeedFetcher(private val httpClient: HttpClient, private val feedParser: FeedParser) {

  suspend fun fetch(url: String): FeedPayload {
    val transformedUrl = URLBuilder(url).apply { protocol = URLProtocol.HTTPS }.build()
    val xml = httpClient.get(transformedUrl).bodyAsText()
    return feedParser.parse(xml, url)
  }
}
