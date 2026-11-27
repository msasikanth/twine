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

package dev.sasikanth.rss.reader.core.model.local

import androidx.compose.runtime.Immutable
import kotlinx.serialization.Serializable

@Immutable
@Serializable
data class MediaChapter(
  val timestampMs: Long,
  val endTimestampMs: Long? = null,
  val title: String,
  val isSponsor: Boolean = false,
  val url: String? = null,
  val imageUrl: String? = null,
) {
  val formattedTime: String
    get() = formatMediaChapterTimestamp(timestampMs)
}

fun formatMediaChapterTimestamp(timestampMs: Long): String {
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

private val SponsorBracketedMarkerRegex =
  Regex(
    "(?:^|\\s)[\\[(]\\s*(?:ad|ads|sponsor|sponsors|sponsored|promo|commercial|advertisement)(?:\\s*[:\\-–—]\\s*[^\\]\\)]*)?\\s*[\\])]",
    RegexOption.IGNORE_CASE,
  )
private val SponsorLeadingLabelRegex =
  Regex(
    "^(?:ad|ads|sponsor|sponsors|sponsored|promo|commercial|advertisement)(?:\\s*[:|]\\s*|\\s+[-–—]\\s+|$)",
    RegexOption.IGNORE_CASE,
  )
private val SponsorTrailingMarkerRegex =
  Regex(
    "\\s*[-–—:|]\\s*(?:ad|ads|sponsor|sponsors|sponsored|promo|advertisement)\\s*$",
    RegexOption.IGNORE_CASE,
  )
private val SponsorPhraseRegex =
  Regex(
    "(?:" +
      "\\b(?:ad|ads)\\s+(?:break|read|spot|segment|message)s?\\b|" +
      "\\b(?:pre|mid|post)-?roll(?:\\s+(?:ad|ads|break|sponsor)s?)?\\b|" +
      "\\bcommercial(?:\\s+break|s)?\\b|" +
      "\\badvertisement\\b|" +
      "\\bpaid\\s+(?:promotion|partnership|sponsorship|ad)\\b|" +
      "\\bsponsored\\s+(?:by|segment|message|spot|break|read)\\b|" +
      "\\bsponsor\\s+(?:break|message|spot|read|segment|shoutout|mention)s?\\b|" +
      "\\bbrought\\s+to\\s+you\\s+by\\b|" +
      "\\b(?:a\\s+word|message)\\s+from\\s+our\\s+sponsors?\\b|" +
      "\\bthanks\\s+to\\s+our\\s+sponsors?\\b|" +
      "\\b(?:today's|this\\s+episode's|our)\\s+sponsors?\\b|" +
      "\\bpromo\\s+(?:code|break|spot)s?\\b|" +
      "^(?:partner|presented\\s+by|supported\\s+by|powered\\s+by)\\b" +
      ")",
    RegexOption.IGNORE_CASE,
  )

fun isSponsorChapterTitle(title: String): Boolean {
  val trimmed = title.trim()
  if (trimmed.isEmpty()) return false

  return SponsorBracketedMarkerRegex.containsMatchIn(trimmed) ||
    SponsorLeadingLabelRegex.containsMatchIn(trimmed) ||
    SponsorTrailingMarkerRegex.containsMatchIn(trimmed) ||
    SponsorPhraseRegex.containsMatchIn(trimmed)
}

fun parseNormalPlayTimeMs(raw: String): Long? {
  val trimmed = raw.trim()
  if (trimmed.isEmpty()) return null

  val parts = trimmed.split(':')
  return when (parts.size) {
    1 -> parseSecondsWithMillis(parts[0])
    2 -> {
      val minutes = parts[0].toLongOrNull() ?: return null
      val secondsMs = parseSecondsWithMillis(parts[1]) ?: return null
      if (minutes < 0L || secondsMs >= 60_000L) return null
      (minutes * 60_000L) + secondsMs
    }
    3 -> {
      val hours = parts[0].toLongOrNull() ?: return null
      val minutes = parts[1].toLongOrNull() ?: return null
      val secondsMs = parseSecondsWithMillis(parts[2]) ?: return null
      if (hours < 0L || minutes !in 0L..59L || secondsMs >= 60_000L) return null
      (hours * 3_600_000L) + (minutes * 60_000L) + secondsMs
    }
    else -> null
  }
}

private fun parseSecondsWithMillis(token: String): Long? {
  val dotIndex = token.indexOf('.')
  if (dotIndex < 0) {
    val seconds = token.toLongOrNull() ?: return null
    if (seconds < 0L) return null
    return seconds * 1000L
  }

  val wholePart = token.substring(0, dotIndex)
  val fracPart = token.substring(dotIndex + 1)
  if (wholePart.isEmpty() || fracPart.isEmpty() || !fracPart.all { it.isDigit() }) return null

  val seconds = wholePart.toLongOrNull() ?: return null
  if (seconds < 0L) return null

  val millis = fracPart.take(3).padEnd(3, '0').toLongOrNull() ?: return null
  return (seconds * 1000L) + millis
}
