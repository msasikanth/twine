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

package dev.sasikanth.rss.reader.reader

import androidx.compose.runtime.Immutable
import dev.sasikanth.rss.reader.core.model.local.ResolvedPost
import dev.sasikanth.rss.reader.core.network.utils.UrlUtils
import io.ktor.http.URLBuilder

@Immutable
sealed interface ReaderContentType {
  data object Article : ReaderContentType

  data class Podcast(val audioUrl: String, val isPlayerAvailable: Boolean) : ReaderContentType

  data class Video(val videoUrl: String, val providerLabel: String) : ReaderContentType
}

fun ResolvedPost.classifyReaderContent(isAudioPlayerAvailable: Boolean): ReaderContentType {
  if (!audioUrl.isNullOrBlank()) {
    return ReaderContentType.Podcast(
      audioUrl = audioUrl!!,
      isPlayerAvailable = isAudioPlayerAvailable,
    )
  }

  val videoProvider = UrlUtils.videoProviderName(link)
  if (videoProvider != null) {
    return ReaderContentType.Video(videoUrl = link, providerLabel = videoProvider)
  }

  return ReaderContentType.Article
}

@Immutable
data class MediaChapter(val timestampMs: Long, val formattedTime: String, val title: String)

private val HtmlBreakOrBlockRegex =
  Regex("</?(?:br|p|div|li|tr|h[1-6]|blockquote)(?:\\s[^>]*)?/?>", RegexOption.IGNORE_CASE)
private val HtmlTagRegex = Regex("<[^>]+>")
private val BracketedMarkerSplitRegex =
  Regex("(?=(?:\\[\\d{1,2}:\\d{2}(?::\\d{2})?\\]|\\(\\d{1,2}:\\d{2}(?::\\d{2})?\\)))")
private val ChapterLineRegex =
  Regex(
    "^(?:[-*+•>]\\s+|\\d+[.)]\\s+)?(?:\\*\\*|__)?(?:\\[(\\d{1,2}:\\d{2}(?::\\d{2})?)\\]|\\((\\d{1,2}:\\d{2}(?::\\d{2})?)\\)|(\\d{1,2}:\\d{2}(?::\\d{2})?))(?:\\([^)]*\\))?(?:\\*\\*|__)?(.*)$"
  )
private val MarkdownLinkRegex = Regex("\\[([^\\]]+)\\]\\([^)]+\\)")
private val LeadingTitleSeparatorRegex = Regex("^[\\s\\-–—:|•]+")
private val ClockOrTimezonePrefixRegex =
  Regex(
    "^(?:am|pm|a\\.m\\.|p\\.m\\.|utc|gmt|est|edt|cst|cdt|mst|mdt|pst|pdt)\\b.*",
    RegexOption.IGNORE_CASE,
  )

fun extractTimestampChapters(text: String): List<MediaChapter> {
  if (text.isBlank()) return emptyList()

  val normalizedText =
    text
      .replace(HtmlBreakOrBlockRegex, "\n")
      .replace(HtmlTagRegex, "")
      .replace("&amp;", "&")
      .replace("&lt;", "<")
      .replace("&gt;", ">")
      .replace("&quot;", "\"")
      .replace("&#39;", "'")
      .replace("&apos;", "'")
      .replace("&nbsp;", " ")
      .replace("&ndash;", "–")
      .replace("&mdash;", "—")

  val chapters = mutableListOf<MediaChapter>()

  normalizedText.lineSequence().forEach { rawLine ->
    val splitLines = rawLine.split(BracketedMarkerSplitRegex)
    splitLines.forEach { segment ->
      val line = segment.trim()
      if (line.isEmpty()) return@forEach

      val match = ChapterLineRegex.matchEntire(line) ?: return@forEach
      val timestampToken =
        match.groupValues[1].ifEmpty { match.groupValues[2].ifEmpty { match.groupValues[3] } }
      val rawRemainder = match.groupValues[4]

      val timestampMs = parseTimestampMs(timestampToken) ?: return@forEach
      val cleanedTitle = cleanChapterTitle(rawRemainder) ?: return@forEach

      chapters +=
        MediaChapter(
          timestampMs = timestampMs,
          formattedTime = formatChapterTimestamp(timestampMs),
          title = cleanedTitle,
        )
    }
  }

  return chapters.distinctBy { it.timestampMs }.sortedBy { it.timestampMs }
}

private fun parseTimestampMs(token: String): Long? {
  val parts = token.split(':')
  val hours: Long
  val minutes: Long
  val seconds: Long

  when (parts.size) {
    2 -> {
      hours = 0L
      minutes = parts[0].toLongOrNull() ?: return null
      seconds = parts[1].toLongOrNull() ?: return null
      if (seconds !in 0L..59L) return null
    }
    3 -> {
      hours = parts[0].toLongOrNull() ?: return null
      minutes = parts[1].toLongOrNull() ?: return null
      seconds = parts[2].toLongOrNull() ?: return null
      if (minutes !in 0L..59L || seconds !in 0L..59L) return null
    }
    else -> return null
  }

  return ((hours * 3600L) + (minutes * 60L) + seconds) * 1000L
}

private fun cleanChapterTitle(rawRemainder: String): String? {
  val stripped =
    rawRemainder
      .replace(LeadingTitleSeparatorRegex, "")
      .replace(MarkdownLinkRegex, "$1")
      .replace("**", "")
      .replace("__", "")
      .trim()

  if (stripped.length < 2) return null
  if (!stripped.any { it.isLetter() }) return null
  if (ClockOrTimezonePrefixRegex.matches(stripped)) return null

  return stripped
}

private fun formatChapterTimestamp(timestampMs: Long): String {
  val totalSeconds = (timestampMs / 1000L).coerceAtLeast(0L)
  val seconds = totalSeconds % 60L
  val minutes = (totalSeconds / 60L) % 60L
  val hours = totalSeconds / 3600L

  return if (hours > 0L) {
    "${hours.toString().padStart(2, '0')}:${minutes.toString().padStart(2, '0')}:${seconds.toString().padStart(2, '0')}"
  } else {
    "${minutes.toString().padStart(2, '0')}:${seconds.toString().padStart(2, '0')}"
  }
}

fun buildVideoUrlAtTimestamp(videoUrl: String, timestampSeconds: Long): String {
  if (videoUrl.isBlank() || timestampSeconds <= 0L) return videoUrl

  return runCatching {
      when {
        UrlUtils.youTubeVideoId(videoUrl) != null -> {
          URLBuilder(videoUrl)
            .apply {
              if (pathSegments.any { it.equals("embed", ignoreCase = true) }) {
                parameters["start"] = timestampSeconds.toString()
              } else {
                parameters["t"] = timestampSeconds.toString()
              }
            }
            .buildString()
        }
        UrlUtils.vimeoVideoId(videoUrl) != null -> {
          URLBuilder(videoUrl).apply { fragment = "t=${timestampSeconds}s" }.buildString()
        }
        UrlUtils.isPeerTubeLink(videoUrl) -> {
          URLBuilder(videoUrl)
            .apply { parameters["start"] = timestampSeconds.toString() }
            .buildString()
        }
        else -> videoUrl
      }
    }
    .getOrDefault(videoUrl)
}
