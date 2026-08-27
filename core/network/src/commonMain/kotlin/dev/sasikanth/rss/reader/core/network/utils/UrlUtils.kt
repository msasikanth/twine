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

  private val unconstrainedImagePrefixes =
    listOf(
      "https://imgs.xkcd.com/comics/",
      "https://preview.redd.it/",
      "https://i.redd.it/",
      "https://external-preview.redd.it/",
    )

  private val youtubeRegex =
    Regex(
      "(?:https?:)?(?://)?" +
        "(?:(?:www|m)\\.)?(?:youtube\\.com|youtu\\.be)" +
        "(?:/(?:[\\w\\-]+\\?v=|embed/|v/)?([\\w\\-]+)(?:\\S+)?)?"
    )

  private const val MAX_RES_THUMBNAIL = "maxresdefault.jpg"

  private const val HQ_THUMBNAIL = "hqdefault.jpg"

  private val ytImgThumbnailRegex =
    Regex(
      "^(https?://[\\w\\-]*\\.?ytimg\\.com/vi(?:_webp)?/[\\w\\-]{11}/)" +
        "((?:maxresdefault|hq720|sddefault|hqdefault|mqdefault|default)\\.jpg)",
      RegexOption.IGNORE_CASE,
    )

  private val absoluteUrlPattern = """^[a-zA-Z][a-zA-Z0-9\+\-\.]*:""".toRegex()

  private val youtubeVideoIdRegex =
    Regex(
      "^(?:https?:)?(?://)?(?:(?:www|m|music)\\.)?" +
        "(?:youtube(?:-nocookie)?\\.com/(?:watch\\?(?:[^&]*&)*v=|embed/|shorts/|live/|v/)" +
        "|youtu\\.be/)" +
        "([\\w\\-]{11})(?:[?&#/].*)?$"
    )

  fun isYouTubeLink(url: String): Boolean {
    return youtubeRegex.matches(url)
  }

  fun youTubeVideoId(url: String?): String? {
    if (url.isNullOrBlank()) return null
    return youtubeVideoIdRegex.find(url.trim())?.groupValues?.getOrNull(1)
  }

  fun youTubeThumbnail(url: String?): String? {
    val videoId = youTubeVideoId(url) ?: return null
    return "https://i.ytimg.com/vi/$videoId/$MAX_RES_THUMBNAIL"
  }

  /** Feeds advertise the 480x360 4:3 thumbnail, which is letterboxed for 16:9 videos. */
  fun upgradeYouTubeThumbnail(url: String?): String? {
    if (url.isNullOrBlank()) return null
    val (prefix, variant) = ytImgThumbnailRegex.find(url)?.destructured ?: return url

    return if (variant == MAX_RES_THUMBNAIL) url else prefix + MAX_RES_THUMBNAIL
  }

  fun youTubeThumbnailFallback(url: String?): String? {
    if (url.isNullOrBlank()) return null
    val (prefix, variant) = ytImgThumbnailRegex.find(url)?.destructured ?: return null

    return if (variant == MAX_RES_THUMBNAIL) prefix + HQ_THUMBNAIL else null
  }

  private val feedProvidedIconRegexes =
    listOf(
      Regex("^https?://(?:www\\.)?youtube\\.com/feeds/videos\\.xml\\?", RegexOption.IGNORE_CASE),
      Regex("^https?://(?:[\\w\\-]+\\.)*reddit\\.com/.+/\\.rss$", RegexOption.IGNORE_CASE),
      Regex("^https?://[^/]+/@[A-Za-z0-9_.\\-]+\\.rss$", RegexOption.IGNORE_CASE),
    )

  /** Sources whose site favicon is shared by every feed on the host. */
  fun prefersFeedProvidedIcon(feedLink: String): Boolean {
    val link = feedLink.trim()
    return feedProvidedIconRegexes.any { it.containsMatchIn(link) }
  }

  fun isUnconstrainedMedia(url: String): Boolean {
    return unconstrainedImagePrefixes.any { url.startsWith(it) }
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

  fun safeUrl(
    host: String?,
    url: String?,
    user: String? = null,
    password: String? = null,
  ): String? {
    if (host.isNullOrBlank()) return null

    if (url.isNullOrBlank()) return null

    val urlBuilder =
      if (isAbsoluteUrl(url)) {
        URLBuilder(url)
      } else {
        URLBuilder().apply { set(host = host, path = url) }
      }

    if (!user.isNullOrEmpty() && !password.isNullOrEmpty()) {
      urlBuilder.user = user
      urlBuilder.password = password
    }

    return urlBuilder.buildString()
  }

  private fun isAbsoluteUrl(url: String): Boolean {
    return absoluteUrlPattern.containsMatchIn(url)
  }
}
