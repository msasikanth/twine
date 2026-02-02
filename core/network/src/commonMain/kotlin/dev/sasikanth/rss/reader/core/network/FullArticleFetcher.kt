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

package dev.sasikanth.rss.reader.core.network

import com.fleeksoft.ksoup.Ksoup
import dev.sasikanth.rss.reader.core.model.local.ServiceType
import dev.sasikanth.rss.reader.core.model.local.User
import dev.sasikanth.rss.reader.core.network.miniflux.MinifluxSource
import dev.sasikanth.rss.reader.util.DispatchersProvider
import io.ktor.client.HttpClient
import io.ktor.client.plugins.UserAgent
import io.ktor.client.request.get
import io.ktor.client.request.headers
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpHeaders
import kotlinx.coroutines.withContext
import me.tatarka.inject.annotations.Inject

@Inject
class FullArticleFetcher(
  private val user: suspend () -> User?,
  private val httpClient: HttpClient,
  private val minifluxSource: MinifluxSource,
  private val dispatchersProvider: DispatchersProvider,
) {

  private val fullArticleHttpClient by lazy {
    httpClient.config {
      followRedirects = true

      install(UserAgent) {
        agent =
          "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36"
      }
    }
  }

  suspend fun fetch(url: String, remoteId: String? = null): Result<String> {
    return withContext(dispatchersProvider.io) {
      try {
        val user = user()
        val htmlDocument =
          when {
            user != null &&
              !(remoteId.isNullOrBlank()) &&
              user.serviceType == ServiceType.MINIFLUX -> {
              minifluxSource.fetchEntryContent(remoteId.toLong()).content
            }
            else -> {
              fetchFullArticleByUrl(url)
            }
          }

        Result.success(htmlDocument)
      } catch (e: Exception) {
        Result.failure(e)
      }
    }
  }

  private suspend fun fetchFullArticleByUrl(url: String): String {
    val htmlContent =
      fullArticleHttpClient
        .get(url) {
          headers {
            append(
              HttpHeaders.Accept,
              "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.7",
            )
            append(HttpHeaders.AcceptLanguage, "en-US,en;q=0.9")
            append(HttpHeaders.Connection, "keep-alive")
            append("Referer", url)
            append("Upgrade-Insecure-Requests", "1")
            append(
              "sec-ch-ua",
              "\"Not_A Brand\";v=\"8\", \"Chromium\";v=\"120\", \"Google Chrome\";v=\"120\"",
            )
            append("sec-ch-ua-mobile", "?0")
            append("sec-ch-ua-platform", "\"Windows\"")
            append("Sec-Fetch-Dest", "document")
            append("Sec-Fetch-Mode", "navigate")
            append("Sec-Fetch-Site", "none")
            append("Sec-Fetch-User", "?1")
          }
        }
        .bodyAsText()

    if (isJsRequired(htmlContent)) {
      throw Exception("JavaScript is required to view this content")
    }

    val htmlDocument = Ksoup.parse(htmlContent)

    htmlDocument.head().remove()

    return htmlDocument.html()
  }

  private fun isJsRequired(html: String): Boolean {
    return html.length < 5000 &&
      (html.contains("javascript", ignoreCase = true) ||
        html.contains("enable-javascript", ignoreCase = true) ||
        html.contains("js-required", ignoreCase = true)) &&
      (html.contains("enable", ignoreCase = true) ||
        html.contains("required", ignoreCase = true) ||
        html.contains("support", ignoreCase = true))
  }
}
