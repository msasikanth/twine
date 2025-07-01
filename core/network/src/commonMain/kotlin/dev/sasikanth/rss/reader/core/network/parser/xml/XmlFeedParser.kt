/*
 * Copyright 2025 Sasikanth Miriyampalli
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package dev.sasikanth.rss.reader.core.network.parser.xml

import co.touchlab.kermit.Logger
import dev.sasikanth.rss.reader.core.model.remote.FeedPayload
import dev.sasikanth.rss.reader.exceptions.XmlParsingError
import dev.sasikanth.rss.reader.util.DispatchersProvider
import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.readRemaining
import korlibs.io.lang.Charset
import korlibs.io.lang.Charsets
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import kotlinx.io.readByteArray
import me.tatarka.inject.annotations.Inject
import org.kobjects.ktxml.api.XmlPullParserException
import org.kobjects.ktxml.mini.MiniXmlPullParser
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

@Inject
class XmlFeedParser(
  private val dispatchersProvider: DispatchersProvider,
  private val rssContentParser: RSSContentParser,
  private val atomContentParser: AtomContentParser,
  private val rdfContentParser: RDFContentParser,
) {

  suspend fun parse(
    content: ByteReadChannel,
    feedUrl: String,
    charset: Charset,
  ): FeedPayload {
    return try {
      withContext(dispatchersProvider.io) {
        val parser =
          MiniXmlPullParser(
            source = content.toCharIterator(charset),
            relaxed = true,
          )

        parser.nextTag()

        return@withContext when (parser.name) {
          RDF_TAG -> rdfContentParser.parse(feedUrl, parser)
          RSS_TAG -> rssContentParser.parse(feedUrl, parser)
          ATOM_TAG -> atomContentParser.parse(feedUrl, parser)
          HTML_TAG -> throw HtmlContentException()
          else -> throw UnsupportedOperationException("Unknown feed type: ${parser.name}")
        }
      }
    } catch (e: XmlPullParserException) {
      Logger.e(throwable = e) { "Failed to parse the feed" }
      throw XmlParsingError(e.stackTraceToString())
    }
  }

  companion object {
    const val RDF_TAG = "rdf:RDF"
    const val RSS_TAG = "rss"
    const val ATOM_TAG = "feed"
    const val HTML_TAG = "html"

    const val RSS_MEDIA_TYPE = "application/rss+xml"
    const val ATOM_MEDIA_TYPE = "application/atom+xml"

    private val htmlTag = Regex("<.+?>")
    private val blankLine = Regex("(?m)^[ \t]*\r?\n")

    internal val imageTags = setOf("media:content")

    internal const val TAG_RSS_CHANNEL = "channel"
    internal const val TAG_ATOM_FEED = "feed"
    internal const val TAG_RSS_ITEM = "item"
    internal const val TAG_ATOM_ENTRY = "entry"

    internal const val TAG_TITLE = "title"
    internal const val TAG_LINK = "link"
    internal const val TAG_URL = "url"
    internal const val TAG_DESCRIPTION = "description"
    internal const val TAG_ENCLOSURE = "enclosure"
    internal const val TAG_CONTENT_ENCODED = "content:encoded"
    internal const val TAG_CONTENT = "content"
    internal const val TAG_SUMMARY = "summary"
    internal const val TAG_SUBTITLE = "subtitle"
    internal const val TAG_PUB_DATE = "pubDate"
    internal const val TAG_DC_DATE = "dc:date"
    internal const val TAG_PUBLISHED = "published"
    internal const val TAG_UPDATED = "updated"
    internal const val TAG_FEATURED_IMAGE = "featuredImage"
    internal const val TAG_COMMENTS = "comments"
    internal const val TAG_FEED_IMAGE = "image"
    internal const val TAG_ICON = "icon"
    internal const val TAG_MEDIA_GROUP = "media:group"
    internal const val TAG_MEDIA_CONTENT = "media:description"
    internal const val TAG_MEDIA_THUMBNAIL = "media:thumbnail"

    internal const val ATTR_URL = "url"
    internal const val ATTR_TYPE = "type"
    internal const val ATTR_REL = "rel"
    internal const val ATTR_HREF = "href"
    internal const val ATTR_RDF_RESOURCE = "rdf:resource"

    internal const val ATTR_VALUE_ALTERNATE = "alternate"
    internal const val ATTR_VALUE_IMAGE = "image/jpeg"

    fun cleanText(text: String?) = text?.replace(htmlTag, "")?.replace(blankLine, "")?.trim()
  }
}

private fun ByteReadChannel.toCharIterator(
  charset: Charset,
  context: CoroutineContext = EmptyCoroutineContext
): CharIterator {
  return object : CharIterator() {

    private val DEFAULT_BUFFER_SIZE = 1024L

    private var encodingCharset: Charset? = null
    private var currentIndex = 0
    private var currentBuffer = ""
    private var isFirstRead = true

    override fun hasNext(): Boolean {
      if (currentIndex < currentBuffer.length) return true
      if (this@toCharIterator.isClosedForRead) return false

      val packet = runBlocking(context) { this@toCharIterator.readRemaining(DEFAULT_BUFFER_SIZE) }
      val bytes = packet.readByteArray()
      val encodingRegex = """<?xml.*encoding=["']([^"']+)["'].*?>""".toRegex()
      if (encodingCharset == null) {
        val encodingContent = buildString { Charsets.UTF8.decode(this, bytes) }
        encodingCharset = findEncodingCharset(encodingRegex, encodingContent, charset)
      }

      currentBuffer =
        buildString { (encodingCharset ?: charset).decode(this, bytes) }
          .replace("&acirc;&#128;&#148;", "&ndash;")
          .replace("&acirc;&#128;&#153;", "&apos;")

      if (isFirstRead && currentBuffer.startsWith("\uFEFF")) {
        currentBuffer = currentBuffer.substring(1)
      }
      isFirstRead = false

      packet.close()
      currentIndex = 0
      return currentBuffer.isNotEmpty()
    }

    private fun findEncodingCharset(
      encodingRegex: Regex,
      encodingContent: String,
      fallbackCharset: Charset,
    ) =
      (encodingRegex.find(encodingContent)?.groupValues?.get(1)?.let { encoding ->
        try {
          Charset.forName(encoding)
        } catch (e: Exception) {
          null
        }
      }
        ?: fallbackCharset)

    override fun nextChar(): Char {
      if (!hasNext()) throw NoSuchElementException()
      return currentBuffer[currentIndex++]
    }
  }
}

internal class HtmlContentException : Exception()
