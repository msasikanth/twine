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

package dev.sasikanth.rss.reader.core.network.fetcher

import dev.sasikanth.rss.reader.core.model.local.MediaChapter
import dev.sasikanth.rss.reader.core.model.local.isSponsorChapterTitle
import dev.sasikanth.rss.reader.core.network.utils.UrlUtils
import dev.sasikanth.rss.reader.util.DispatchersProvider
import io.ktor.client.HttpClient
import io.ktor.client.request.headers
import io.ktor.client.request.prepareGet
import io.ktor.client.statement.bodyAsChannel
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.isSuccess
import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.cancel
import io.ktor.utils.io.readAvailable
import kotlin.math.roundToLong
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.withContext
import kotlinx.io.Buffer
import kotlinx.io.readByteArray
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import me.tatarka.inject.annotations.Inject

@Inject
class PodcastChaptersFetcher(
  httpClient: HttpClient,
  private val dispatchersProvider: DispatchersProvider,
) {

  private companion object {
    private const val MAX_JSON_CHAPTERS_BYTES = 512 * 1024
    private const val INITIAL_ID3_RANGE_BYTES = 128 * 1024
    private const val MAX_ID3_TAG_BYTES = 1024 * 1024
    private const val CHUNK_SIZE = 4096

    private const val ID3_HEADER_SIZE = 10
    private const val ID3_FLAG_UNSYNC = 0x80
    private const val ID3_FLAG_EXT_HEADER = 0x40

    private const val FRAME_FLAG_GROUPING = 0x40
    private const val FRAME_FLAG_COMPRESSION = 0x08
    private const val FRAME_FLAG_ENCRYPTION = 0x04
    private const val FRAME_FLAG_UNSYNC = 0x02
    private const val FRAME_FLAG_DATA_LENGTH = 0x01

    private const val ENCODING_ISO_8859_1 = 0
    private const val ENCODING_UTF16_BOM = 1
    private const val ENCODING_UTF16_BE = 2
    private const val ENCODING_UTF8 = 3
  }

  private val client by lazy { httpClient.config { followRedirects = true } }
  private val json = Json { ignoreUnknownKeys = true }

  suspend fun fetchPodcasting2Chapters(chaptersUrl: String): List<MediaChapter>? {
    if (chaptersUrl.isBlank()) return null

    return withContext(dispatchersProvider.io) {
      try {
        client
          .prepareGet(chaptersUrl) {
            headers {
              append(HttpHeaders.Accept, "application/json+chapters,application/json,*/*;q=0.5")
            }
          }
          .execute { response ->
            if (!response.status.isSuccess()) return@execute null

            val bytes = readBoundedBytes(response.bodyAsChannel(), MAX_JSON_CHAPTERS_BYTES)
            if (bytes.isEmpty()) return@execute null

            val host = UrlUtils.extractHost(response.call.request.url.toString())
            parsePodcasting2Json(bytes.decodeToString().removePrefix("\uFEFF"), host)
          }
      } catch (e: CancellationException) {
        throw e
      } catch (_: Exception) {
        null
      }
    }
  }

  suspend fun fetchEmbeddedMp3Chapters(audioUrl: String): List<MediaChapter>? {
    if (audioUrl.isBlank()) return null

    return withContext(dispatchersProvider.io) {
      try {
        val initialFetch =
          fetchRangeBytes(audioUrl, start = 0, endInclusive = INITIAL_ID3_RANGE_BYTES - 1)
            ?: return@withContext null
        val initialBytes = initialFetch.bytes
        val id3Header = readId3Header(initialBytes) ?: return@withContext emptyList()

        val totalNeeded = minOf(ID3_HEADER_SIZE + id3Header.tagSize, MAX_ID3_TAG_BYTES)
        val combinedBytes =
          if (
            totalNeeded > initialBytes.size &&
              initialBytes.size >= INITIAL_ID3_RANGE_BYTES &&
              initialFetch.status == HttpStatusCode.PartialContent
          ) {
            val followUp =
              fetchRangeBytes(audioUrl, start = initialBytes.size, endInclusive = totalNeeded - 1)
            when {
              followUp == null -> initialBytes
              followUp.status == HttpStatusCode.PartialContent -> initialBytes + followUp.bytes
              followUp.bytes.size >= initialBytes.size -> followUp.bytes
              else -> initialBytes
            }
          } else {
            initialBytes
          }

        parseId3v2Chapters(combinedBytes)
      } catch (e: CancellationException) {
        throw e
      } catch (_: Exception) {
        null
      }
    }
  }

  private suspend fun fetchRangeBytes(
    url: String,
    start: Int,
    endInclusive: Int,
  ): RangeFetchResult? {
    val maxBytes = (endInclusive - start + 1).coerceAtLeast(0)
    if (maxBytes == 0) return RangeFetchResult(HttpStatusCode.PartialContent, ByteArray(0))

    return client
      .prepareGet(url) {
        headers {
          append(HttpHeaders.Range, "bytes=$start-$endInclusive")
          append(HttpHeaders.AcceptEncoding, "identity")
        }
      }
      .execute { response ->
        if (!response.status.isSuccess()) return@execute null
        val limit =
          if (response.status == HttpStatusCode.PartialContent) maxBytes
          else minOf(endInclusive + 1, MAX_ID3_TAG_BYTES)
        val bytes = readBoundedBytes(response.bodyAsChannel(), limit)
        RangeFetchResult(status = response.status, bytes = bytes)
      }
  }

  private suspend fun readBoundedBytes(channel: ByteReadChannel, maxBytes: Int): ByteArray {
    val accumulated = Buffer()
    val chunk = ByteArray(CHUNK_SIZE)
    var totalRead = 0

    try {
      while (totalRead < maxBytes) {
        val toRead = minOf(chunk.size, maxBytes - totalRead)
        val bytesRead = channel.readAvailable(chunk, 0, toRead)
        if (bytesRead < 0) break
        if (bytesRead == 0) continue

        accumulated.write(chunk, 0, bytesRead)
        totalRead += bytesRead
      }
    } finally {
      channel.cancel()
    }

    return accumulated.readByteArray()
  }

  internal fun parsePodcasting2Json(
    jsonText: String,
    hostLink: String? = null,
  ): List<MediaChapter>? {
    val payload =
      runCatching { json.decodeFromString<Podcasting2ChaptersPayload>(jsonText) }.getOrNull()
        ?: return null

    return payload.chapters
      .mapNotNull { raw ->
        val title = raw.title?.trim()?.ifBlank { null } ?: return@mapNotNull null
        val startMs = (raw.startTime * 1000.0).roundToLong().coerceAtLeast(0L)
        val endMs = raw.endTime?.let { (it * 1000.0).roundToLong() }?.takeIf { it > startMs }
        MediaChapter(
          timestampMs = startMs,
          endTimestampMs = endMs,
          title = title,
          isSponsor = !raw.toc || isSponsorChapterTitle(title),
          url = raw.url?.trim()?.ifBlank { null }?.let { UrlUtils.safeUrl(hostLink, it) ?: it },
          imageUrl = raw.img?.trim()?.ifBlank { null }?.let { UrlUtils.safeUrl(hostLink, it) ?: it },
        )
      }
      .distinctBy { it.timestampMs }
      .sortedBy { it.timestampMs }
  }

  internal fun parseId3v2Chapters(bytes: ByteArray): List<MediaChapter> {
    val header = readId3Header(bytes) ?: return emptyList()
    val rawEnd = minOf(bytes.size, ID3_HEADER_SIZE + header.tagSize)
    if (rawEnd <= ID3_HEADER_SIZE) return emptyList()

    val rawBody = bytes.copyOfRange(ID3_HEADER_SIZE, rawEnd)
    val hasTagUnsync = (header.flags and ID3_FLAG_UNSYNC) != 0
    val body =
      if (header.majorVersion == 3 && hasTagUnsync) {
        removeUnsynchronisation(rawBody, 0, rawBody.size)
      } else {
        rawBody
      }

    var cursor = 0
    if ((header.flags and ID3_FLAG_EXT_HEADER) != 0) {
      if (cursor + 4 > body.size) return emptyList()
      cursor +=
        if (header.majorVersion == 3) {
          4 + readBigEndianInt(body, cursor)
        } else {
          readSynchsafeInt(body, cursor) ?: return emptyList()
        }
      if (cursor < 0 || cursor >= body.size) return emptyList()
    }

    val chapters = mutableListOf<MediaChapter>()
    while (cursor + ID3_HEADER_SIZE <= body.size) {
      if (body[cursor] == 0.toByte()) break

      val frameId = readFrameId(body, cursor) ?: break
      val frameSize = readFrameSize(body, cursor + 4, header.majorVersion) ?: break
      val formatFlags = body[cursor + 9].toInt() and 0xFF
      cursor += ID3_HEADER_SIZE

      if (frameSize <= 0 || cursor + frameSize > body.size) break

      val frameEnd = cursor + frameSize
      if (frameId == "CHAP") {
        parseChapFrame(
            bytes = body,
            start = cursor,
            end = frameEnd,
            majorVersion = header.majorVersion,
            formatFlags = formatFlags,
            globalUnsync = header.majorVersion == 4 && hasTagUnsync,
          )
          ?.let { chapters += it }
      }

      cursor = frameEnd
    }

    return chapters.distinctBy { it.timestampMs }.sortedBy { it.timestampMs }
  }

  private fun parseChapFrame(
    bytes: ByteArray,
    start: Int,
    end: Int,
    majorVersion: Int,
    formatFlags: Int,
    globalUnsync: Boolean,
  ): MediaChapter? {
    if (
      majorVersion == 4 &&
        ((formatFlags and FRAME_FLAG_COMPRESSION) != 0 ||
          (formatFlags and FRAME_FLAG_ENCRYPTION) != 0)
    ) {
      return null
    }

    var contentStart = start
    if (majorVersion == 4) {
      if ((formatFlags and FRAME_FLAG_GROUPING) != 0) {
        contentStart += 1
      }
      if ((formatFlags and FRAME_FLAG_DATA_LENGTH) != 0) {
        contentStart += 4
      }
      if (contentStart >= end) return null
    }

    val payload =
      if (majorVersion == 4 && ((formatFlags and FRAME_FLAG_UNSYNC) != 0 || globalUnsync)) {
        removeUnsynchronisation(bytes, contentStart, end)
      } else {
        bytes.copyOfRange(contentStart, end)
      }

    val elementIdEnd = findNullTerminator(payload, 0) ?: return null
    val timingOffset = elementIdEnd + 1
    if (timingOffset + 16 > payload.size) return null

    val startTimeMs = readUnsignedBigEndianInt(payload, timingOffset)
    val rawEndTimeMs = readUnsignedBigEndianInt(payload, timingOffset + 4)
    val endTimeMs = rawEndTimeMs.takeIf { it != 0xFFFFFFFFL && it > startTimeMs }

    var subCursor = timingOffset + 16
    var title: String? = null
    var url: String? = null

    while (subCursor + ID3_HEADER_SIZE <= payload.size) {
      if (payload[subCursor] == 0.toByte()) break
      val subId = readFrameId(payload, subCursor) ?: break
      val subSize = readFrameSize(payload, subCursor + 4, majorVersion) ?: break
      val subFormatFlags = payload[subCursor + 9].toInt() and 0xFF
      subCursor += ID3_HEADER_SIZE
      if (subSize <= 0 || subCursor + subSize > payload.size) break

      val subEnd = subCursor + subSize
      when (subId) {
        "TIT2" -> {
          if (title == null) {
            title = decodeTextFrame(payload, subCursor, subEnd, majorVersion, subFormatFlags)
          }
        }
        "WXXX" -> {
          if (url == null) {
            url = decodeWxxxFrame(payload, subCursor, subEnd, majorVersion, subFormatFlags)
          }
        }
      }
      subCursor = subEnd
    }

    val finalTitle = title?.trim()?.ifBlank { null } ?: return null
    return MediaChapter(
      timestampMs = startTimeMs,
      endTimestampMs = endTimeMs,
      title = finalTitle,
      isSponsor = isSponsorChapterTitle(finalTitle),
      url = url,
    )
  }

  private fun decodeTextFrame(
    bytes: ByteArray,
    start: Int,
    end: Int,
    majorVersion: Int,
    formatFlags: Int,
  ): String? {
    val frameData = unwrapSubFrame(bytes, start, end, majorVersion, formatFlags) ?: return null
    if (frameData.size <= 1) return null

    val encoding = frameData[0].toInt() and 0xFF
    return decodeEncodedText(frameData, 1, frameData.size, encoding)?.trim()?.ifBlank { null }
  }

  private fun decodeWxxxFrame(
    bytes: ByteArray,
    start: Int,
    end: Int,
    majorVersion: Int,
    formatFlags: Int,
  ): String? {
    val frameData = unwrapSubFrame(bytes, start, end, majorVersion, formatFlags) ?: return null
    if (frameData.size <= 2) return null

    val encoding = frameData[0].toInt() and 0xFF
    val urlStart =
      if (encoding == ENCODING_UTF16_BOM || encoding == ENCODING_UTF16_BE) {
        val nulIdx = findDoubleNullTerminator(frameData, 1) ?: return null
        nulIdx + 2
      } else {
        val nulIdx = findNullTerminator(frameData, 1) ?: return null
        nulIdx + 1
      }
    if (urlStart >= frameData.size) return null

    return decodeIso88591(frameData, urlStart, frameData.size).trim().ifBlank { null }
  }

  private fun unwrapSubFrame(
    bytes: ByteArray,
    start: Int,
    end: Int,
    majorVersion: Int,
    formatFlags: Int,
  ): ByteArray? {
    if (
      majorVersion == 4 &&
        ((formatFlags and FRAME_FLAG_COMPRESSION) != 0 ||
          (formatFlags and FRAME_FLAG_ENCRYPTION) != 0)
    ) {
      return null
    }
    var offset = start
    if (majorVersion == 4) {
      if ((formatFlags and FRAME_FLAG_GROUPING) != 0) offset += 1
      if ((formatFlags and FRAME_FLAG_DATA_LENGTH) != 0) offset += 4
    }
    if (offset >= end) return null
    return if (majorVersion == 4 && (formatFlags and FRAME_FLAG_UNSYNC) != 0) {
      removeUnsynchronisation(bytes, offset, end)
    } else {
      bytes.copyOfRange(offset, end)
    }
  }

  private fun decodeEncodedText(bytes: ByteArray, start: Int, end: Int, encoding: Int): String? {
    if (start >= end) return null
    return when (encoding) {
      ENCODING_ISO_8859_1 -> decodeIso88591(bytes, start, end)
      ENCODING_UTF8 -> bytes.decodeToString(start, end).substringBefore('\u0000')
      ENCODING_UTF16_BE -> decodeUtf16(bytes, start, end, bigEndian = true)
      ENCODING_UTF16_BOM -> {
        if (end - start < 2) return null
        val b0 = bytes[start].toInt() and 0xFF
        val b1 = bytes[start + 1].toInt() and 0xFF
        when {
          b0 == 0xFF && b1 == 0xFE -> decodeUtf16(bytes, start + 2, end, bigEndian = false)
          b0 == 0xFE && b1 == 0xFF -> decodeUtf16(bytes, start + 2, end, bigEndian = true)
          else -> decodeUtf16(bytes, start, end, bigEndian = true)
        }
      }
      else -> null
    }
  }

  private fun decodeIso88591(bytes: ByteArray, start: Int, end: Int): String {
    val sb = StringBuilder(end - start)
    for (i in start until end) {
      val value = bytes[i].toInt() and 0xFF
      if (value == 0) break
      sb.append(value.toChar())
    }
    return sb.toString()
  }

  private fun decodeUtf16(bytes: ByteArray, start: Int, end: Int, bigEndian: Boolean): String {
    val sb = StringBuilder((end - start) / 2)
    var i = start
    while (i + 1 < end) {
      val b0 = bytes[i].toInt() and 0xFF
      val b1 = bytes[i + 1].toInt() and 0xFF
      val codeUnit = if (bigEndian) (b0 shl 8) or b1 else (b1 shl 8) or b0
      if (codeUnit == 0) break
      sb.append(codeUnit.toChar())
      i += 2
    }
    return sb.toString()
  }

  private fun readId3Header(bytes: ByteArray): Id3Header? {
    if (bytes.size < ID3_HEADER_SIZE) return null
    if (
      bytes[0] != 'I'.code.toByte() ||
        bytes[1] != 'D'.code.toByte() ||
        bytes[2] != '3'.code.toByte()
    ) {
      return null
    }
    val majorVersion = bytes[3].toInt() and 0xFF
    if (majorVersion != 3 && majorVersion != 4) return null

    val flags = bytes[5].toInt() and 0xFF
    val tagSize = readSynchsafeInt(bytes, 6) ?: return null
    return Id3Header(majorVersion = majorVersion, flags = flags, tagSize = tagSize)
  }

  private fun readFrameId(bytes: ByteArray, offset: Int): String? {
    val chars = CharArray(4)
    for (i in 0 until 4) {
      val ch = (bytes[offset + i].toInt() and 0xFF).toChar()
      if (ch !in 'A'..'Z' && ch !in '0'..'9') return null
      chars[i] = ch
    }
    return chars.concatToString()
  }

  private fun readFrameSize(bytes: ByteArray, offset: Int, majorVersion: Int): Int? {
    return if (majorVersion == 4) {
      readSynchsafeInt(bytes, offset) ?: readBigEndianInt(bytes, offset).takeIf { it >= 0 }
    } else {
      readBigEndianInt(bytes, offset).takeIf { it >= 0 }
    }
  }

  private fun readSynchsafeInt(bytes: ByteArray, offset: Int): Int? {
    val b0 = bytes[offset].toInt() and 0xFF
    val b1 = bytes[offset + 1].toInt() and 0xFF
    val b2 = bytes[offset + 2].toInt() and 0xFF
    val b3 = bytes[offset + 3].toInt() and 0xFF
    if (((b0 or b1 or b2 or b3) and 0x80) != 0) return null
    return (b0 shl 21) or (b1 shl 14) or (b2 shl 7) or b3
  }

  private fun readBigEndianInt(bytes: ByteArray, offset: Int): Int {
    return ((bytes[offset].toInt() and 0xFF) shl 24) or
      ((bytes[offset + 1].toInt() and 0xFF) shl 16) or
      ((bytes[offset + 2].toInt() and 0xFF) shl 8) or
      (bytes[offset + 3].toInt() and 0xFF)
  }

  private fun readUnsignedBigEndianInt(bytes: ByteArray, offset: Int): Long {
    return ((bytes[offset].toLong() and 0xFFL) shl 24) or
      ((bytes[offset + 1].toLong() and 0xFFL) shl 16) or
      ((bytes[offset + 2].toLong() and 0xFFL) shl 8) or
      (bytes[offset + 3].toLong() and 0xFFL)
  }

  private fun removeUnsynchronisation(bytes: ByteArray, start: Int, end: Int): ByteArray {
    val out = ArrayList<Byte>(end - start)
    var i = start
    while (i < end) {
      val current = bytes[i]
      out.add(current)
      if (current == 0xFF.toByte() && i + 1 < end && bytes[i + 1] == 0x00.toByte()) {
        i += 2
      } else {
        i++
      }
    }
    return out.toByteArray()
  }

  private fun findNullTerminator(bytes: ByteArray, start: Int): Int? {
    for (i in start until bytes.size) {
      if (bytes[i] == 0.toByte()) return i
    }
    return null
  }

  private fun findDoubleNullTerminator(bytes: ByteArray, start: Int): Int? {
    var i = start
    while (i + 1 < bytes.size) {
      if (bytes[i] == 0.toByte() && bytes[i + 1] == 0.toByte()) return i
      i += 2
    }
    return null
  }

  private data class Id3Header(val majorVersion: Int, val flags: Int, val tagSize: Int)

  private data class RangeFetchResult(val status: HttpStatusCode, val bytes: ByteArray)

  @Serializable
  private data class Podcasting2ChaptersPayload(
    val chapters: List<Podcasting2Chapter> = emptyList()
  )

  @Serializable
  private data class Podcasting2Chapter(
    val startTime: Double,
    val endTime: Double? = null,
    val title: String? = null,
    val img: String? = null,
    val url: String? = null,
    val toc: Boolean = true,
  )
}
