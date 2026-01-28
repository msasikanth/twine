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

package dev.sasikanth.rss.reader.core.network.utils

import io.ktor.http.URLBuilder
import io.ktor.http.Url
import io.ktor.http.set

object UrlUtils {

  private val comicStripUrls = listOf("https://imgs.xkcd.com/comics/")

  fun isYouTubeLink(url: String): Boolean {
    val youtubeRegex =
      Regex(
        "(?:https?:)?(?://)?" +
          "(?:(?:www|m)\\.)?(?:youtube\\.com|youtu\\.be)" +
          "(?:/(?:[\\w\\-]+\\?v=|embed/|v/)?([\\w\\-]+)(?:\\S+)?)?"
      )

    return youtubeRegex.matches(url)
  }

  fun isComicStrip(url: String): Boolean {
    return comicStripUrls.any { url.startsWith(it) }
  }

  fun fallbackFeedIcon(host: String): String {
    return "https://icon.horse/icon/$host"
  }

  fun extractHost(urlString: String): String {
    val host =
      if (urlString.startsWith("http://") || urlString.startsWith("https://")) {
        Url(urlString).host
      } else {
        urlString
      }

    return if (host == "localhost") {
      urlString
    } else {
      host
    }
  }

  fun safeUrl(host: String?, url: String?): String? {
    if (host.isNullOrBlank()) return null

    return if (!url.isNullOrBlank()) {
      if (isAbsoluteUrl(url)) {
        URLBuilder(url).buildString()
      } else {
        URLBuilder().apply { set(host = host, path = url) }.buildString()
      }
    } else {
      null
    }
  }

  private fun isAbsoluteUrl(url: String): Boolean {
    val pattern = """^[a-zA-Z][a-zA-Z0-9\+\-\.]*:""".toRegex()
    return pattern.containsMatchIn(url)
  }
}
