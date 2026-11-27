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

import com.fleeksoft.ksoup.nodes.Entities
import dev.sasikanth.rss.reader.core.network.parser.xml.XmlContentParser
import dev.sasikanth.rss.reader.core.network.parser.xml.XmlFeedParser
import dev.sasikanth.rss.reader.core.network.utils.UrlUtils
import dev.sasikanth.rss.reader.core.network.utils.findEncodingCharset
import dev.sasikanth.rss.reader.util.DispatchersProvider
import io.ktor.client.HttpClient
import io.ktor.client.request.headers
import io.ktor.client.request.prepareGet
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsChannel
import io.ktor.http.BadContentTypeFormatException
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.cancel
import io.ktor.utils.io.readAvailable
import korlibs.io.lang.Charset
import korlibs.io.lang.Charsets
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.withContext
import kotlinx.io.Buffer
import kotlinx.io.readByteArray
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import me.tatarka.inject.annotations.Inject
import org.kobjects.ktxml.api.EventType
import org.kobjects.ktxml.mini.MiniXmlPullParser

@Inject
class PodcastArtworkEnricher(
  httpClient: HttpClient,
  private val dispatchersProvider: DispatchersProvider,
) {

  private companion object {
    private const val MAX_HEADER_BYTES = 64 * 1024
    private const val CHUNK_SIZE = 4096
    private const val ENCODING_SNIFF_BYTES = 4096

    private val bodyBoundaryRegex =
      Regex("<(?:item|entry)(?:[\\s>/]|$)|</(?:channel|feed)\\b", RegexOption.IGNORE_CASE)
  }

  private val client by lazy { httpClient.config { followRedirects = true } }
  private val json = Json { ignoreUnknownKeys = true }

  suspend fun fetchCoverArtUrl(feedUrl: String): String? {
    if (feedUrl.isBlank()) return null

    return withContext(dispatchersProvider.io) {
      try {
        client
          .prepareGet(feedUrl) {
            headers {
              append(
                HttpHeaders.Accept,
                "application/rss+xml,application/atom+xml,application/xml,application/json,text/xml;q=0.9,*/*;q=0.5",
              )
            }
          }
          .execute { response ->
            if (!response.status.isSuccess()) return@execute null

            val contentType =
              try {
                response.contentType()?.withoutParameters()
              } catch (_: BadContentTypeFormatException) {
                null
              }
            val charset = resolveCharset(response)
            val finalHost = UrlUtils.extractHost(response.call.request.url.toString())
            val bytes = readHeaderBytes(response.bodyAsChannel())
            if (bytes.isEmpty()) return@execute null

            val text = decodeHeaderBytes(bytes, charset).removePrefix("\uFEFF")
            val trimmed = text.trimStart()
            val rawArtwork =
              when {
                contentType == ContentType.Application.Json || trimmed.startsWith("{") ->
                  extractJsonIcon(trimmed)
                else -> extractXmlCoverArt(text)
              }

            val resolved =
              rawArtwork?.trim()?.ifBlank { null }?.let { UrlUtils.safeUrl(finalHost, it) }
            resolved?.takeUnless { UrlUtils.isLowResFaviconUrl(it) }
          }
      } catch (e: CancellationException) {
        throw e
      } catch (_: Exception) {
        null
      }
    }
  }

  private suspend fun readHeaderBytes(channel: ByteReadChannel): ByteArray {
    val accumulated = Buffer()
    val chunk = ByteArray(CHUNK_SIZE)
    var totalRead = 0

    try {
      while (totalRead < MAX_HEADER_BYTES) {
        val toRead = minOf(chunk.size, MAX_HEADER_BYTES - totalRead)
        val bytesRead = channel.readAvailable(chunk, 0, toRead)
        if (bytesRead < 0) break
        if (bytesRead == 0) continue

        accumulated.write(chunk, 0, bytesRead)
        totalRead += bytesRead

        val currentText = accumulated.peek().readByteArray().decodeToString()
        if (bodyBoundaryRegex.containsMatchIn(currentText)) {
          break
        }
      }
    } finally {
      channel.cancel()
    }

    return accumulated.readByteArray()
  }

  private fun resolveCharset(response: HttpResponse): Charset {
    val responseCharset =
      try {
        response.contentType()?.parameter("charset")
      } catch (_: BadContentTypeFormatException) {
        Charsets.UTF8.name
      }
    return Charset.forName(responseCharset ?: Charsets.UTF8.name)
  }

  private fun decodeHeaderBytes(bytes: ByteArray, fallbackCharset: Charset): String {
    val sniffSize = minOf(bytes.size, ENCODING_SNIFF_BYTES)
    val encodingContent = buildString { Charsets.UTF8.decode(this, bytes, 0, sniffSize) }
    val encodingCharset = findEncodingCharset(encodingContent, fallbackCharset)
    return buildString { encodingCharset.decode(this, bytes) }
  }

  internal fun extractXmlCoverArt(xml: String): String? {
    val normalizedXml =
      xml.replace("&acirc;&#128;&#148;", "&ndash;").replace("&acirc;&#128;&#153;", "&apos;")

    val parser =
      MiniXmlPullParser(
        source = normalizedXml.iterator(),
        relaxed = true,
        entityResolver = { name -> Entities.getByName(name).ifEmpty { null } },
      )

    var itunesImage: String? = null
    var podcastImages: String? = null
    var googlePlayImage: String? = null
    var logoUrl: String? = null
    var channelImageUrl: String? = null

    try {
      parser.nextTag()
      val rootTag = parser.name
      if (rootTag == XmlFeedParser.HTML_TAG) return null

      val containerTag =
        when (rootTag) {
          XmlFeedParser.RSS_TAG,
          XmlFeedParser.RDF_TAG -> XmlFeedParser.TAG_RSS_CHANNEL
          XmlFeedParser.ATOM_TAG -> XmlFeedParser.TAG_ATOM_FEED
          else -> return null
        }

      if (rootTag != XmlFeedParser.ATOM_TAG && !advanceToTag(parser, containerTag)) {
        return null
      }

      while (true) {
        val event = parser.next()
        if (event == EventType.END_DOCUMENT) break
        if (event == EventType.END_TAG && parser.name == containerTag) break
        if (event != EventType.START_TAG) continue

        when (parser.name) {
          XmlFeedParser.TAG_RSS_ITEM,
          XmlFeedParser.TAG_ATOM_ENTRY -> break
          XmlFeedParser.TAG_ITUNES_IMAGE -> {
            val href =
              parser.getAttributeValue(parser.namespace, XmlFeedParser.ATTR_HREF)?.trim()?.ifBlank {
                null
              }
            parser.skipSubTree()
            if (itunesImage.isNullOrBlank()) {
              itunesImage = href
            }
          }
          XmlFeedParser.TAG_PODCAST_IMAGES -> {
            val srcset = parser.getAttributeValue(parser.namespace, XmlFeedParser.ATTR_SRCSET)
            parser.skipSubTree()
            if (podcastImages.isNullOrBlank()) {
              podcastImages = XmlContentParser.parseSrcsetHighestResUrl(srcset)
            }
          }
          XmlFeedParser.TAG_GOOGLEPLAY_IMAGE -> {
            val href =
              parser.getAttributeValue(parser.namespace, XmlFeedParser.ATTR_HREF)?.trim()?.ifBlank {
                null
              }
            parser.skipSubTree()
            if (googlePlayImage.isNullOrBlank()) {
              googlePlayImage = href
            }
          }
          XmlFeedParser.TAG_LOGO -> {
            val text = parser.nextText().trim().ifBlank { null }
            if (logoUrl.isNullOrBlank()) {
              logoUrl = text
            }
          }
          XmlFeedParser.TAG_FEED_IMAGE -> {
            val parsedImage = readChannelOrRdfImage(parser)
            if (channelImageUrl.isNullOrBlank()) {
              channelImageUrl = parsedImage
            }
          }
          else -> parser.skipSubTree()
        }
      }
    } catch (e: CancellationException) {
      throw e
    } catch (_: Exception) {}

    return itunesImage ?: podcastImages ?: googlePlayImage ?: logoUrl ?: channelImageUrl
  }

  private fun advanceToTag(parser: MiniXmlPullParser, containerTag: String): Boolean {
    var depth = 0
    while (true) {
      when (parser.next()) {
        EventType.END_DOCUMENT -> return false
        EventType.START_TAG -> {
          if (depth == 0 && parser.name == containerTag) return true
          depth++
        }
        EventType.END_TAG -> if (depth > 0) depth--
        else -> {}
      }
    }
  }

  private fun readChannelOrRdfImage(parser: MiniXmlPullParser): String? {
    val rdfResource =
      parser.getAttributeValue(parser.namespace, XmlFeedParser.ATTR_RDF_RESOURCE)?.trim()?.ifBlank {
        null
      }
    if (!rdfResource.isNullOrBlank()) {
      parser.skipSubTree()
      return rdfResource
    }

    var imageUrl: String? = null
    while (true) {
      val event = parser.next()
      if (event == EventType.END_DOCUMENT) break
      if (event == EventType.END_TAG && parser.name == XmlFeedParser.TAG_FEED_IMAGE) break
      if (event != EventType.START_TAG) continue

      if (parser.name == XmlFeedParser.TAG_URL) {
        imageUrl = parser.nextText().trim().ifBlank { null }
      } else {
        parser.skipSubTree()
      }
    }

    return imageUrl
  }

  private fun extractJsonIcon(jsonText: String): String? {
    runCatching { json.decodeFromString<JsonFeedHeader>(jsonText).icon?.trim()?.ifBlank { null } }
      .getOrNull()
      ?.let {
        return it
      }

    return extractTopLevelJsonIcon(jsonText)
  }

  private fun extractTopLevelJsonIcon(jsonText: String): String? {
    var index = 0
    var depth = 0
    val length = jsonText.length

    while (index < length) {
      when (val ch = jsonText[index]) {
        '{',
        '[' -> {
          depth++
          index++
        }
        '}',
        ']' -> {
          depth--
          index++
        }
        '"' -> {
          val endQuote = findClosingQuote(jsonText, index) ?: return null
          val literal = jsonText.substring(index, endQuote + 1)
          index = endQuote + 1

          if (depth == 1) {
            val key = runCatching { json.decodeFromString<String>(literal) }.getOrNull()
            var cursor = index
            while (cursor < length && jsonText[cursor].isWhitespace()) cursor++
            if (cursor < length && jsonText[cursor] == ':') {
              cursor++
              while (cursor < length && jsonText[cursor].isWhitespace()) cursor++
              if (key == "icon" && cursor < length && jsonText[cursor] == '"') {
                val valueEnd = findClosingQuote(jsonText, cursor) ?: return null
                val valueLiteral = jsonText.substring(cursor, valueEnd + 1)
                return runCatching { json.decodeFromString<String>(valueLiteral) }
                  .getOrNull()
                  ?.trim()
                  ?.ifBlank { null }
              }
            }
          }
        }
        else -> index++
      }
    }

    return null
  }

  private fun findClosingQuote(text: String, startQuoteIndex: Int): Int? {
    var i = startQuoteIndex + 1
    while (i < text.length) {
      when (text[i]) {
        '\\' -> i += 2
        '"' -> return i
        else -> i++
      }
    }
    return null
  }

  @Serializable private data class JsonFeedHeader(val icon: String? = null)
}
