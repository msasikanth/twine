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
import dev.sasikanth.rss.reader.core.network.utils.TestDispatchersProvider
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.core.toByteArray
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlinx.coroutines.test.runTest

class PodcastChaptersFetcherTest {

  private val dispatchersProvider = TestDispatchersProvider()

  private fun createFetcher(
    handler:
      suspend (url: String, rangeHeader: String?) -> Triple<
          HttpStatusCode,
          Map<String, String>,
          ByteArray,
        >
  ): PodcastChaptersFetcher {
    val httpClient =
      HttpClient(MockEngine) {
        engine {
          dispatcher = dispatchersProvider.io
          addHandler { request ->
            val range = request.headers[HttpHeaders.Range]
            val (status, headersMap, body) = handler(request.url.toString(), range)
            val headersBuilder =
              headersOf(*headersMap.map { it.key to listOf(it.value) }.toTypedArray())
            respond(content = ByteReadChannel(body), status = status, headers = headersBuilder)
          }
        }
      }
    return PodcastChaptersFetcher(httpClient, dispatchersProvider)
  }

  @Test
  fun fetchesAndParsesPodcasting2JsonChaptersIncludingTocSponsorFlag() = runTest {
    val jsonBody =
      """
      {
        "version": "1.2.0",
        "chapters": [
          { "startTime": 0, "endTime": 120.5, "title": "Intro", "url": "https://example.com/intro" },
          { "startTime": 120.5, "endTime": 180, "title": "Word from Acme", "toc": false },
          { "startTime": 180, "title": "   " },
          { "startTime": 200, "title": "Deep Dive", "img": "/art/ch3.jpg" }
        ]
      }
      """
        .trimIndent()

    val fetcher = createFetcher { _, _ ->
      Triple(
        HttpStatusCode.OK,
        mapOf(HttpHeaders.ContentType to "application/json+chapters"),
        jsonBody.toByteArray(),
      )
    }

    val chapters = fetcher.fetchPodcasting2Chapters("https://cdn.example.com/chapters.json")

    assertEquals(
      listOf(
        MediaChapter(
          timestampMs = 0L,
          endTimestampMs = 120_500L,
          title = "Intro",
          isSponsor = false,
          url = "https://example.com/intro",
        ),
        MediaChapter(
          timestampMs = 120_500L,
          endTimestampMs = 180_000L,
          title = "Word from Acme",
          isSponsor = true,
        ),
        MediaChapter(
          timestampMs = 200_000L,
          title = "Deep Dive",
          isSponsor = false,
          imageUrl = "http://cdn.example.com/art/ch3.jpg",
        ),
      ),
      chapters,
    )
  }

  @Test
  fun malformedJsonOrHttpFailureReturnsNull() = runTest {
    val malformedFetcher = createFetcher { _, _ ->
      Triple(HttpStatusCode.OK, emptyMap(), "{not-json".toByteArray())
    }
    assertNull(malformedFetcher.fetchPodcasting2Chapters("https://example.com/bad.json"))

    val errorFetcher = createFetcher { _, _ ->
      Triple(HttpStatusCode.InternalServerError, emptyMap(), ByteArray(0))
    }
    assertNull(errorFetcher.fetchEmbeddedMp3Chapters("https://example.com/ep.mp3"))
  }

  @Test
  fun parsesId3v23AndId3v24ChaptersAcrossEncodingsAndFollowUpRangeRequest() = runTest {
    val apicPadding = buildId3Frame(version = 4, id = "APIC", payload = ByteArray(140 * 1024) { 1 })
    val chap1 =
      buildChapFrame(
        version = 4,
        elementId = "chp0",
        startTimeMs = 0L,
        endTimeMs = 60_000L,
        subFrames =
          listOf(
            buildTextFrame(version = 4, id = "TIT2", encoding = 3, text = "Intro — UTF8"),
            buildWxxxFrame(version = 4, encoding = 0, url = "https://example.com/c1"),
          ),
      )
    val chap2 =
      buildChapFrame(
        version = 4,
        elementId = "chp1",
        startTimeMs = 60_000L,
        endTimeMs = 120_000L,
        subFrames =
          listOf(buildTextFrame(version = 4, id = "TIT2", encoding = 1, text = "Sponsor: Linear")),
      )
    val fullTag = wrapId3Tag(version = 4, body = apicPadding + chap1 + chap2)
    val mp3Bytes = fullTag + ByteArray(2048) { 0xFF.toByte() }

    val rangeHeadersSeen = mutableListOf<String?>()
    val fetcher = createFetcher { _, range ->
      rangeHeadersSeen += range
      val (start, end) = parseRangeHeader(range, mp3Bytes.size)
      val slice = mp3Bytes.copyOfRange(start, minOf(end + 1, mp3Bytes.size))
      Triple(
        HttpStatusCode.PartialContent,
        mapOf(
          HttpHeaders.ContentRange to "bytes $start-${start + slice.size - 1}/${mp3Bytes.size}"
        ),
        slice,
      )
    }

    val chapters = fetcher.fetchEmbeddedMp3Chapters("https://cdn.example.com/episode.mp3")

    assertEquals(2, rangeHeadersSeen.size)
    assertEquals("bytes=0-131071", rangeHeadersSeen[0])
    assertEquals(
      listOf(
        MediaChapter(
          timestampMs = 0L,
          endTimestampMs = 60_000L,
          title = "Intro — UTF8",
          isSponsor = false,
          url = "https://example.com/c1",
        ),
        MediaChapter(
          timestampMs = 60_000L,
          endTimestampMs = 120_000L,
          title = "Sponsor: Linear",
          isSponsor = true,
        ),
      ),
      chapters,
    )
  }

  @Test
  fun parsesId3v23Latin1AndUtf16BeChaptersAndReturnsEmptyForNonId3() = runTest {
    val chap1 =
      buildChapFrame(
        version = 3,
        elementId = "c0",
        startTimeMs = 10_000L,
        endTimeMs = 0xFFFFFFFFL,
        subFrames =
          listOf(buildTextFrame(version = 3, id = "TIT2", encoding = 0, text = "Latin1 Intro")),
      )
    val chap2 =
      buildChapFrame(
        version = 3,
        elementId = "c1",
        startTimeMs = 90_000L,
        endTimeMs = 150_000L,
        subFrames =
          listOf(buildTextFrame(version = 3, id = "TIT2", encoding = 2, text = "[Ad] Vanta")),
      )
    val v23Tag = wrapId3Tag(version = 3, body = chap1 + chap2)

    val fetcher = createFetcher { url, _ ->
      if (url.endsWith("plain.mp3")) {
        Triple(
          HttpStatusCode.OK,
          emptyMap(),
          byteArrayOf(0xFF.toByte(), 0xFB.toByte(), 0x90.toByte(), 0x00),
        )
      } else {
        Triple(HttpStatusCode.PartialContent, emptyMap(), v23Tag)
      }
    }

    assertEquals(emptyList(), fetcher.fetchEmbeddedMp3Chapters("https://cdn.example.com/plain.mp3"))
    assertEquals(
      listOf(
        MediaChapter(timestampMs = 10_000L, endTimestampMs = null, title = "Latin1 Intro"),
        MediaChapter(
          timestampMs = 90_000L,
          endTimestampMs = 150_000L,
          title = "[Ad] Vanta",
          isSponsor = true,
        ),
      ),
      fetcher.fetchEmbeddedMp3Chapters("https://cdn.example.com/v23.mp3"),
    )
  }

  private fun parseRangeHeader(header: String?, totalSize: Int): Pair<Int, Int> {
    val clean = header?.removePrefix("bytes=") ?: return 0 to (totalSize - 1)
    val parts = clean.split('-')
    return parts[0].toInt() to parts[1].toInt()
  }

  private fun wrapId3Tag(version: Int, body: ByteArray): ByteArray {
    val header = ByteArray(10)
    header[0] = 'I'.code.toByte()
    header[1] = 'D'.code.toByte()
    header[2] = '3'.code.toByte()
    header[3] = version.toByte()
    writeSynchsafeInt(header, 6, body.size)
    return header + body
  }

  private fun buildId3Frame(version: Int, id: String, payload: ByteArray): ByteArray {
    val header = ByteArray(10)
    for (i in 0 until 4) header[i] = id[i].code.toByte()
    if (version == 4) {
      writeSynchsafeInt(header, 4, payload.size)
    } else {
      writeBigEndianInt(header, 4, payload.size)
    }
    return header + payload
  }

  private fun buildChapFrame(
    version: Int,
    elementId: String,
    startTimeMs: Long,
    endTimeMs: Long,
    subFrames: List<ByteArray>,
  ): ByteArray {
    val elemBytes = elementId.encodeToByteArray() + byteArrayOf(0)
    val timing = ByteArray(16)
    writeBigEndianInt(timing, 0, startTimeMs.toInt())
    writeBigEndianInt(timing, 4, endTimeMs.toInt())
    val nested = subFrames.fold(ByteArray(0)) { acc, next -> acc + next }
    return buildId3Frame(version, "CHAP", elemBytes + timing + nested)
  }

  private fun buildTextFrame(version: Int, id: String, encoding: Int, text: String): ByteArray {
    val encoded =
      when (encoding) {
        0 -> text.map { it.code.toByte() }.toByteArray()
        1 -> byteArrayOf(0xFF.toByte(), 0xFE.toByte()) + encodeUtf16(text, bigEndian = false)
        2 -> encodeUtf16(text, bigEndian = true)
        else -> text.encodeToByteArray()
      }
    return buildId3Frame(version, id, byteArrayOf(encoding.toByte()) + encoded)
  }

  private fun buildWxxxFrame(version: Int, encoding: Int, url: String): ByteArray {
    val payload = byteArrayOf(encoding.toByte(), 0x00) + url.encodeToByteArray()
    return buildId3Frame(version, "WXXX", payload)
  }

  private fun encodeUtf16(text: String, bigEndian: Boolean): ByteArray {
    val out = ByteArray(text.length * 2)
    text.forEachIndexed { idx, ch ->
      val code = ch.code
      if (bigEndian) {
        out[idx * 2] = (code ushr 8).toByte()
        out[idx * 2 + 1] = (code and 0xFF).toByte()
      } else {
        out[idx * 2] = (code and 0xFF).toByte()
        out[idx * 2 + 1] = (code ushr 8).toByte()
      }
    }
    return out
  }

  private fun writeSynchsafeInt(target: ByteArray, offset: Int, value: Int) {
    target[offset] = ((value ushr 21) and 0x7F).toByte()
    target[offset + 1] = ((value ushr 14) and 0x7F).toByte()
    target[offset + 2] = ((value ushr 7) and 0x7F).toByte()
    target[offset + 3] = (value and 0x7F).toByte()
  }

  private fun writeBigEndianInt(target: ByteArray, offset: Int, value: Int) {
    target[offset] = ((value ushr 24) and 0xFF).toByte()
    target[offset + 1] = ((value ushr 16) and 0xFF).toByte()
    target[offset + 2] = ((value ushr 8) and 0xFF).toByte()
    target[offset + 3] = (value and 0xFF).toByte()
  }
}
