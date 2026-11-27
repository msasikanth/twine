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
import dev.sasikanth.rss.reader.core.model.local.MediaChapter
import dev.sasikanth.rss.reader.core.model.local.ResolvedPost
import dev.sasikanth.rss.reader.core.model.local.isSponsorChapterTitle
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

private const val MAX_BARE_TRAILING_LINE_LENGTH = 140
private const val TIMESTAMP_PATTERN = "\\d{1,2}:\\d{2}(?::\\d{2})?"

private val HtmlBreakOrBlockRegex =
  Regex("</?(?:br|p|div|li|tr|h[1-6]|blockquote)(?:\\s[^>]*)?/?>", RegexOption.IGNORE_CASE)
private val HtmlTagRegex = Regex("<[^>]+>")
private val LeadingListPrefixRegex = Regex("^(?:[-*+•>]\\s+|\\d+[.)]\\s+)")
private val BracketedMarkerRegex = Regex("(?:\\[$TIMESTAMP_PATTERN\\]|\\($TIMESTAMP_PATTERN\\))")
private val BracketedMarkerSplitRegex =
  Regex("(?=(?:\\[$TIMESTAMP_PATTERN\\]|\\($TIMESTAMP_PATTERN\\)))")
private val LeadingRangeRegex =
  Regex(
    "^(?:\\*\\*|__)?(?:[\\[(]\\s*($TIMESTAMP_PATTERN)\\s*[-–—]\\s*($TIMESTAMP_PATTERN)\\s*[\\])]|($TIMESTAMP_PATTERN)\\s*[-–—]\\s*($TIMESTAMP_PATTERN))(?:\\([^)]*\\))?(?:\\*\\*|__)?(.*)$"
  )
private val LeadingSingleRegex =
  Regex(
    "^(?:\\*\\*|__)?(?:\\[($TIMESTAMP_PATTERN)\\]|\\(($TIMESTAMP_PATTERN)\\)|($TIMESTAMP_PATTERN))(?:\\([^)]*\\))?(?:\\*\\*|__)?(.*)$"
  )
private val TrailingBracketedRegex =
  Regex(
    "^(.+?)\\s*(?:\\*\\*|__)?[\\[(]\\s*($TIMESTAMP_PATTERN)(?:\\s*[-–—]\\s*($TIMESTAMP_PATTERN))?\\s*[\\])](?:\\([^)]*\\))?(?:\\*\\*|__)?$"
  )
private val TrailingBareRegex =
  Regex(
    "^(.+?)(?:\\s*[-–—|•:]|\\t|\\s{2,})\\s*($TIMESTAMP_PATTERN)(?:\\s*[-–—]\\s*($TIMESTAMP_PATTERN))?(.*)$"
  )
private val MarkdownLinkRegex = Regex("\\[([^\\]]+)\\]\\([^)]+\\)")
private val LeadingTitleSeparatorRegex = Regex("^[\\s\\-–—:|•]+")
private val TrailingTitleSeparatorRegex = Regex("[\\s\\-–—:|•]+$")
private val ClockOrTimezonePrefixRegex =
  Regex(
    "^(?:am|pm|a\\.m\\.|p\\.m\\.|utc|gmt|est|edt|cst|cdt|mst|mdt|pst|pdt)\\b.*",
    RegexOption.IGNORE_CASE,
  )
private val TrailingClockPrepositionRegex =
  Regex("(?:^|\\s)(?:at|until|till|from|around|by|since|before|after)$", RegexOption.IGNORE_CASE)
private val InlineTimestampRegex = Regex("(?<![:/\\w.])($TIMESTAMP_PATTERN)(?![:\\w.])")
private val InlineFooterDividerRegex = Regex("\\s+(?:[-=_]{3,}|https?://).*$")

private data class ParsedChapterCandidate(val chapter: MediaChapter, val isBareTrailing: Boolean)

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
      .replace("\u00A0", " ")
      .replace("\u200B", "")
      .replace("\uFEFF", "")
      .replace("&ndash;", "–")
      .replace("&mdash;", "—")
      .replace("&#8211;", "–")
      .replace("&#8212;", "—")

  val candidates = mutableListOf<ParsedChapterCandidate>()

  normalizedText.lineSequence().forEach { rawLine ->
    val line = rawLine.trim()
    if (line.isEmpty()) return@forEach

    val lineWithoutBullet = line.replace(LeadingListPrefixRegex, "").trim()
    val shouldSplitInline =
      BracketedMarkerRegex.findAll(lineWithoutBullet).take(2).count() > 1 &&
        LeadingSingleRegex.containsMatchIn(lineWithoutBullet)

    if (shouldSplitInline) {
      line.split(BracketedMarkerSplitRegex).forEach { segment ->
        val trimmed = segment.trim()
        if (trimmed.isNotEmpty()) {
          parseChapterCandidate(trimmed)?.let { candidates += it }
        }
      }
    } else {
      val singleCandidate = parseChapterCandidate(line)
      if (singleCandidate != null) {
        candidates += singleCandidate
      } else {
        candidates += extractInlineTimestampSequence(line)
      }
    }
  }

  val totalCount = candidates.size
  return candidates
    .asSequence()
    .filter { candidate -> !candidate.isBareTrailing || totalCount >= 2 }
    .map { it.chapter }
    .distinctBy { it.timestampMs }
    .sortedBy { it.timestampMs }
    .toList()
}

private fun extractInlineTimestampSequence(line: String): List<ParsedChapterCandidate> {
  val validMatches =
    InlineTimestampRegex.findAll(line)
      .mapNotNull { match ->
        val tsMs = parseTimestampMs(match.groupValues[1]) ?: return@mapNotNull null
        val beforeText = line.substring(0, match.range.first).trimEnd()
        if (TrailingClockPrepositionRegex.containsMatchIn(beforeText)) return@mapNotNull null
        val afterText = line.substring(match.range.last + 1).trimStart()
        if (ClockOrTimezonePrefixRegex.matches(afterText)) return@mapNotNull null
        match to tsMs
      }
      .toList()

  if (validMatches.size < 3) return emptyList()

  var bestSequence: List<MatchResult> = emptyList()
  for (startIdx in validMatches.indices) {
    val sequence = mutableListOf(validMatches[startIdx].first)
    var lastTs = validMatches[startIdx].second
    for (j in (startIdx + 1) until validMatches.size) {
      val (match, ts) = validMatches[j]
      if (ts > lastTs) {
        sequence += match
        lastTs = ts
      }
    }
    if (sequence.size > bestSequence.size) {
      bestSequence = sequence
    }
    if (bestSequence.size >= validMatches.size - startIdx) break
  }

  if (bestSequence.size < 3) return emptyList()

  val results = mutableListOf<ParsedChapterCandidate>()
  bestSequence.forEachIndexed { index, match ->
    val start = match.range.first
    val end =
      if (index + 1 < bestSequence.size) bestSequence[index + 1].range.first else line.length
    var slice = line.substring(start, end).trim()
    if (index == bestSequence.lastIndex) {
      slice = slice.replace(InlineFooterDividerRegex, "").trim()
    }
    parseChapterCandidate(slice)?.let { candidate ->
      results += candidate.copy(isBareTrailing = false)
    }
  }
  return results
}

private fun parseChapterCandidate(segment: String): ParsedChapterCandidate? {
  val lineWithoutBullet = segment.replace(LeadingListPrefixRegex, "").trim()
  if (lineWithoutBullet.isEmpty()) return null

  LeadingRangeRegex.matchEntire(lineWithoutBullet)?.let { match ->
    val startToken = match.groupValues[1].ifEmpty { match.groupValues[3] }
    val endToken = match.groupValues[2].ifEmpty { match.groupValues[4] }
    val rawRemainder = match.groupValues[5]

    val startMs = parseTimestampMs(startToken) ?: return@let
    val endMs = parseTimestampMs(endToken)?.takeIf { it > startMs }
    val cleanedTitle = cleanChapterTitle(rawRemainder) ?: return@let
    return ParsedChapterCandidate(
      chapter = buildChapter(startMs, endMs, cleanedTitle),
      isBareTrailing = false,
    )
  }

  LeadingSingleRegex.matchEntire(lineWithoutBullet)?.let { match ->
    val timestampToken =
      match.groupValues[1].ifEmpty { match.groupValues[2].ifEmpty { match.groupValues[3] } }
    val rawRemainder = match.groupValues[4]

    val startMs = parseTimestampMs(timestampToken) ?: return@let
    val cleanedTitle = cleanChapterTitle(rawRemainder) ?: return@let
    return ParsedChapterCandidate(
      chapter = buildChapter(startMs, null, cleanedTitle),
      isBareTrailing = false,
    )
  }

  TrailingBracketedRegex.matchEntire(lineWithoutBullet)?.let { match ->
    val rawTitle = match.groupValues[1]
    val startToken = match.groupValues[2]
    val endToken = match.groupValues[3]

    val startMs = parseTimestampMs(startToken) ?: return@let
    val endMs =
      if (endToken.isNotEmpty()) parseTimestampMs(endToken)?.takeIf { it > startMs } else null
    val cleanedTitle = cleanChapterTitle(rawTitle) ?: return@let
    if (TrailingClockPrepositionRegex.containsMatchIn(cleanedTitle)) return@let
    return ParsedChapterCandidate(
      chapter = buildChapter(startMs, endMs, cleanedTitle),
      isBareTrailing = false,
    )
  }

  if (lineWithoutBullet.length <= MAX_BARE_TRAILING_LINE_LENGTH) {
    TrailingBareRegex.matchEntire(lineWithoutBullet)?.let { match ->
      val rawTitle = match.groupValues[1]
      val startToken = match.groupValues[2]
      val endToken = match.groupValues[3]
      val trailingRemainder = match.groupValues[4].replace("**", "").replace("__", "").trim()
      if (trailingRemainder.isNotEmpty()) return null

      val startMs = parseTimestampMs(startToken) ?: return null
      val endMs =
        if (endToken.isNotEmpty()) parseTimestampMs(endToken)?.takeIf { it > startMs } else null
      val cleanedTitle = cleanChapterTitle(rawTitle) ?: return null
      if (TrailingClockPrepositionRegex.containsMatchIn(cleanedTitle)) return null

      return ParsedChapterCandidate(
        chapter = buildChapter(startMs, endMs, cleanedTitle),
        isBareTrailing = true,
      )
    }
  }

  return null
}

private fun buildChapter(startMs: Long, endMs: Long?, title: String): MediaChapter {
  return MediaChapter(
    timestampMs = startMs,
    endTimestampMs = endMs,
    title = title,
    isSponsor = isSponsorChapterTitle(title),
  )
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
      .replace(TrailingTitleSeparatorRegex, "")
      .replace(MarkdownLinkRegex, "$1")
      .replace("**", "")
      .replace("__", "")
      .trim()

  if (stripped.length < 2) return null
  if (!stripped.any { it.isLetter() }) return null
  if (ClockOrTimezonePrefixRegex.matches(stripped)) return null

  return stripped
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
