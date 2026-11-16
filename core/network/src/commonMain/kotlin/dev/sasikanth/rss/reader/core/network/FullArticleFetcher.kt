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
import com.fleeksoft.ksoup.nodes.Document
import com.fleeksoft.ksoup.nodes.Element
import dev.sasikanth.rss.reader.core.model.local.ServiceType
import dev.sasikanth.rss.reader.core.model.local.User
import dev.sasikanth.rss.reader.core.network.miniflux.MinifluxSource
import dev.sasikanth.rss.reader.util.DispatchersProvider
import io.ktor.client.HttpClient
import io.ktor.client.plugins.UserAgent
import io.ktor.client.request.get
import io.ktor.client.request.headers
import io.ktor.client.request.prepareGet
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.Url
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import kotlinx.coroutines.withContext
import me.tatarka.inject.annotations.Inject

@Inject
class FullArticleFetcher(
  private val user: suspend () -> User?,
  private val httpClient: HttpClient,
  private val minifluxSource: MinifluxSource,
  private val dispatchersProvider: DispatchersProvider,
) {

  companion object {
    private const val MAX_CONTENT_SIZE = 10 * 1024 * 1024 // 10MB
    private const val CHROME_VERSION = "140"
    private const val BROWSER_USER_AGENT =
      "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) " +
        "Chrome/$CHROME_VERSION.0.0.0 Safari/537.36"
    private val BLOCKED_STATUS_CODES =
      setOf(
        HttpStatusCode.Unauthorized,
        HttpStatusCode.Forbidden,
        HttpStatusCode.NotAcceptable,
        HttpStatusCode.TooManyRequests,
      )
  }

  private val fullArticleHttpClient by lazy {
    httpClient.config {
      followRedirects = true

      install(UserAgent) { agent = BROWSER_USER_AGENT }
    }
  }

  private val fallbackHttpClient by lazy { httpClient.config { followRedirects = true } }

  suspend fun fetch(url: String, remoteId: String? = null): Result<String> {
    return withContext(dispatchersProvider.io) {
      try {
        val user = user()
        val htmlDocument =
          when {
            user != null &&
              !(remoteId.isNullOrBlank()) &&
              user.serviceType == ServiceType.MINIFLUX -> {
              val content = minifluxSource.fetchEntryContent(remoteId.toLong()).content
              cleanHtml(content)
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
    return try {
      fetchArticleHtml(url, spoofBrowser = true)
    } catch (e: BlockedByServerException) {
      // Some CDNs reject the spoofed desktop browser headers, usually because the
      // Chrome version we pretend to be has since gone stale. Retry as ourselves.
      fetchArticleHtml(url, spoofBrowser = false)
    }
  }

  private suspend fun fetchArticleHtml(url: String, spoofBrowser: Boolean): String {
    val client = if (spoofBrowser) fullArticleHttpClient else fallbackHttpClient
    return client
      .prepareGet(url) {
        headers {
          append(
            HttpHeaders.Accept,
            "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.7",
          )
          append(HttpHeaders.AcceptLanguage, "en-US,en;q=0.9")

          if (spoofBrowser) {
            append(HttpHeaders.Connection, "keep-alive")
            append("Referer", url)
            append("Upgrade-Insecure-Requests", "1")
            append(
              "sec-ch-ua",
              "\"Not_A Brand\";v=\"8\", \"Chromium\";v=\"$CHROME_VERSION\", \"Google Chrome\";v=\"$CHROME_VERSION\"",
            )
            append("sec-ch-ua-mobile", "?0")
            append("sec-ch-ua-platform", "\"Windows\"")
            append("Sec-Fetch-Dest", "document")
            append("Sec-Fetch-Mode", "navigate")
            append("Sec-Fetch-Site", "none")
            append("Sec-Fetch-User", "?1")
          }
        }
      }
      .execute { response ->
        if (!response.status.isSuccess()) {
          if (spoofBrowser && response.status in BLOCKED_STATUS_CODES) {
            throw BlockedByServerException(response.status)
          }

          throw Exception("Failed to fetch article: ${response.status}")
        }

        val contentType =
          try {
            response.contentType()
          } catch (e: Exception) {
            null
          }

        val isHtml =
          contentType?.let {
            it.match(ContentType.Text.Html) || it.match(ContentType("application", "xhtml+xml"))
          } ?: false

        if (!isHtml) {
          throw Exception("No content to read")
        }

        val contentLength = response.headers[HttpHeaders.ContentLength]?.toLongOrNull()
        if (contentLength != null && contentLength > MAX_CONTENT_SIZE) {
          throw Exception("Content too large: $contentLength bytes")
        }

        val htmlContent = response.bodyAsText()
        if (isJsRequired(htmlContent)) {
          throw Exception("JavaScript is required to view this content")
        }

        val fragment = runCatching { Url(url).fragment.ifBlank { null } }.getOrNull()
        cleanHtml(htmlContent, fragment)
      }
  }

  private fun cleanHtml(htmlContent: String, fragment: String? = null): String {
    return Ksoup.parse(htmlContent)
      .also { document ->
        document.head().remove()
        document.select("script, style, noscript").remove()

        if (!fragment.isNullOrBlank()) {
          val fragmentHtml = extractFragmentHtml(document, fragment)
          if (fragmentHtml != null) {
            document.body().html(fragmentHtml)
          }
        }
      }
      .html()
  }

  private fun extractFragmentHtml(document: Document, fragment: String): String? {
    val rawTarget =
      document.getElementById(fragment)
        ?: document.getElementsByAttributeValue("name", fragment).firstOrNull()
        ?: return null

    val target =
      if (
        rawTarget.tagName() == "a" &&
          rawTarget.text().isBlank() &&
          headingLevel(rawTarget.parent()) != null
      ) {
        rawTarget.parent()!!
      } else {
        rawTarget
      }

    val targetHeadingLevel = headingLevel(target)
    if (targetHeadingLevel != null) {
      return buildString {
        append(target.outerHtml())
        var sibling = target.nextElementSibling()
        while (sibling != null) {
          val siblingHeadingLevel = headingLevel(sibling)
          if (siblingHeadingLevel != null && siblingHeadingLevel <= targetHeadingLevel) {
            break
          }
          append(sibling.outerHtml())
          sibling = sibling.nextElementSibling()
        }
      }
    }

    if (target.text().isBlank() && target.childrenSize() == 0) {
      return null
    }

    return target.outerHtml()
  }

  private fun headingLevel(element: Element?): Int? {
    val tagName = element?.tagName() ?: return null
    if (tagName.length == 2 && tagName[0] == 'h' && tagName[1] in '1'..'6') {
      return tagName[1].digitToInt()
    }
    return null
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

private class BlockedByServerException(status: HttpStatusCode) :
  Exception("Blocked by server: $status")
