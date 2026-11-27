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

package dev.sasikanth.rss.reader.core.network.parser.xml

import com.fleeksoft.ksoup.Ksoup
import com.fleeksoft.ksoup.parseSource
import dev.sasikanth.rss.reader.core.model.local.MediaChapter
import dev.sasikanth.rss.reader.core.model.remote.FeedPayload
import dev.sasikanth.rss.reader.core.model.remote.PostPayload
import dev.sasikanth.rss.reader.core.network.parser.common.ArticleHtmlParser
import dev.sasikanth.rss.reader.core.network.parser.xml.XmlFeedParser.Companion.ATTR_HREF
import dev.sasikanth.rss.reader.core.network.parser.xml.XmlFeedParser.Companion.ATTR_REL
import dev.sasikanth.rss.reader.core.network.parser.xml.XmlFeedParser.Companion.ATTR_SRCSET
import dev.sasikanth.rss.reader.core.network.parser.xml.XmlFeedParser.Companion.ATTR_TYPE
import dev.sasikanth.rss.reader.core.network.parser.xml.XmlFeedParser.Companion.ATTR_VALUE_ALTERNATE
import dev.sasikanth.rss.reader.core.network.parser.xml.XmlFeedParser.Companion.TAG_ATOM_ENTRY
import dev.sasikanth.rss.reader.core.network.parser.xml.XmlFeedParser.Companion.TAG_ATOM_FEED
import dev.sasikanth.rss.reader.core.network.parser.xml.XmlFeedParser.Companion.TAG_AUTHOR
import dev.sasikanth.rss.reader.core.network.parser.xml.XmlFeedParser.Companion.TAG_CONTENT
import dev.sasikanth.rss.reader.core.network.parser.xml.XmlFeedParser.Companion.TAG_GOOGLEPLAY_IMAGE
import dev.sasikanth.rss.reader.core.network.parser.xml.XmlFeedParser.Companion.TAG_ICON
import dev.sasikanth.rss.reader.core.network.parser.xml.XmlFeedParser.Companion.TAG_ITUNES_IMAGE
import dev.sasikanth.rss.reader.core.network.parser.xml.XmlFeedParser.Companion.TAG_LINK
import dev.sasikanth.rss.reader.core.network.parser.xml.XmlFeedParser.Companion.TAG_LOGO
import dev.sasikanth.rss.reader.core.network.parser.xml.XmlFeedParser.Companion.TAG_MEDIA_CONTENT
import dev.sasikanth.rss.reader.core.network.parser.xml.XmlFeedParser.Companion.TAG_MEDIA_GROUP
import dev.sasikanth.rss.reader.core.network.parser.xml.XmlFeedParser.Companion.TAG_MEDIA_THUMBNAIL
import dev.sasikanth.rss.reader.core.network.parser.xml.XmlFeedParser.Companion.TAG_NAME
import dev.sasikanth.rss.reader.core.network.parser.xml.XmlFeedParser.Companion.TAG_PODCAST_CHAPTERS
import dev.sasikanth.rss.reader.core.network.parser.xml.XmlFeedParser.Companion.TAG_PODCAST_IMAGES
import dev.sasikanth.rss.reader.core.network.parser.xml.XmlFeedParser.Companion.TAG_PSC_CHAPTERS
import dev.sasikanth.rss.reader.core.network.parser.xml.XmlFeedParser.Companion.TAG_PUBLISHED
import dev.sasikanth.rss.reader.core.network.parser.xml.XmlFeedParser.Companion.TAG_SUBTITLE
import dev.sasikanth.rss.reader.core.network.parser.xml.XmlFeedParser.Companion.TAG_SUMMARY
import dev.sasikanth.rss.reader.core.network.parser.xml.XmlFeedParser.Companion.TAG_TITLE
import dev.sasikanth.rss.reader.core.network.parser.xml.XmlFeedParser.Companion.TAG_UPDATED
import dev.sasikanth.rss.reader.core.network.parser.xml.XmlFeedParser.Companion.TAG_URI
import dev.sasikanth.rss.reader.core.network.utils.UrlUtils
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsChannel
import io.ktor.utils.io.asSource
import me.tatarka.inject.annotations.Inject
import org.kobjects.ktxml.api.EventType
import org.kobjects.ktxml.api.XmlPullParser

@Inject
class AtomContentParser(httpClient: HttpClient, override val articleHtmlParser: ArticleHtmlParser) :
  XmlContentParser() {

  private val youTubeIconHttpClient = httpClient.config { followRedirects = true }

  private companion object {
    private const val SELECTOR_OG_IMAGE = "meta[property=og:image]"
    private const val ATTR_CONTENT = "content"
  }

  override suspend fun parse(feedUrl: String, parser: XmlPullParser): FeedPayload {
    parser.require(EventType.START_TAG, parser.namespace, TAG_ATOM_FEED)

    var title: String? = null
    var description: String? = null
    var link: String? = null
    var smallIconUrl: String? = null
    var logoUrl: String? = null
    var itunesImage: String? = null
    var podcastImages: String? = null
    var googlePlayImage: String? = null
    var authorName: String? = null
    var authorUri: String? = null
    var firstPost: PostPayload? = null

    while (parser.next() != EventType.END_TAG) {
      if (parser.eventType != EventType.START_TAG) continue
      when (val name = parser.name) {
        TAG_TITLE -> {
          title = parser.nextText()
        }
        TAG_LINK -> {
          if (link.isNullOrBlank()) {
            link = readAtomLink(name, parser)
          } else {
            parser.skipSubTree()
          }
        }
        TAG_SUBTITLE -> {
          description = parser.nextText()
        }
        TAG_ICON -> {
          val value = parser.nextText().trim().ifBlank { null }
          if (smallIconUrl.isNullOrBlank()) {
            smallIconUrl = value
          }
        }
        TAG_LOGO -> {
          val value = parser.nextText().trim().ifBlank { null }
          if (logoUrl.isNullOrBlank()) {
            logoUrl = value
          }
        }
        TAG_ITUNES_IMAGE -> {
          val href = parser.getAttributeValue(parser.namespace, ATTR_HREF)?.trim()?.ifBlank { null }
          parser.skipSubTree()
          if (itunesImage.isNullOrBlank()) {
            itunesImage = href
          }
        }
        TAG_PODCAST_IMAGES -> {
          val srcset = parser.getAttributeValue(parser.namespace, ATTR_SRCSET)
          parser.skipSubTree()
          if (podcastImages.isNullOrBlank()) {
            podcastImages = parseSrcsetHighestResUrl(srcset)
          }
        }
        TAG_GOOGLEPLAY_IMAGE -> {
          val href = parser.getAttributeValue(parser.namespace, ATTR_HREF)?.trim()?.ifBlank { null }
          parser.skipSubTree()
          if (googlePlayImage.isNullOrBlank()) {
            googlePlayImage = href
          }
        }
        TAG_AUTHOR -> {
          forEachChildTag(parser, TAG_AUTHOR) { authorTag ->
            when (authorTag) {
              TAG_NAME -> authorName = parser.nextText()
              TAG_URI -> authorUri = parser.nextText()
              else -> parser.skipSubTree()
            }
          }
        }
        TAG_ATOM_ENTRY -> {
          val host = UrlUtils.extractHost(link ?: feedUrl)
          firstPost = readAtomEntry(parser, host)
          if (firstPost != null) {
            break
          }
        }
        else -> parser.skipSubTree()
      }
    }

    if (UrlUtils.isYouTubeLink(feedUrl)) {
      title = authorName?.ifBlank { null } ?: title
      link = authorUri?.ifBlank { null } ?: link
    }

    val parsedCoverArt = itunesImage ?: podcastImages ?: googlePlayImage ?: logoUrl
    val (iconUrl, coverArtUrl) =
      if (UrlUtils.isYouTubeLink(feedUrl) && !link.isNullOrBlank()) {
        val youtubeImage = youtubeChannelImage(link)
        (youtubeImage ?: parsedCoverArt ?: smallIconUrl) to (youtubeImage ?: parsedCoverArt)
      } else {
        (parsedCoverArt ?: smallIconUrl) to parsedCoverArt
      }

    val postHostLink = UrlUtils.extractHost(link ?: feedUrl)

    return createFeedPayload(
      name = title,
      description = description,
      icon = iconUrl,
      coverArtUrl = coverArtUrl,
      homepageLink = link,
      link = feedUrl,
      posts =
        postsFlow(
          parser = parser,
          firstPost = firstPost,
          containerTag = TAG_ATOM_FEED,
          itemTag = TAG_ATOM_ENTRY,
          readItem = { readAtomEntry(it, postHostLink) },
        ),
    )
  }

  private suspend fun youtubeChannelImage(link: String): String? {
    return try {
      val response = youTubeIconHttpClient.get(urlString = link)
      val document = Ksoup.parseSource(response.bodyAsChannel().asSource(), baseUri = link)
      document.selectFirst(SELECTOR_OG_IMAGE)?.attr(ATTR_CONTENT)?.ifBlank { null }
    } catch (e: Exception) {
      null
    }
  }

  private fun readAtomEntry(parser: XmlPullParser, hostLink: String?): PostPayload? {
    parser.require(EventType.START_TAG, null, "entry")

    var title: String? = null
    var link: String? = null
    var rawContentHtml: String? = null
    var rawSummaryHtml: String? = null
    var mediaGroupDescription: String? = null
    var mediaGroupImage: String? = null
    var mediaGroupAudioUrl: String? = null
    var date: String? = null
    var itunesImage: String? = null
    var podcastImages: String? = null
    var googlePlayImage: String? = null
    var mediaImage: String? = null
    var enclosureImage: String? = null
    var audioUrl: String? = null
    var mediaContentAudioUrl: String? = null
    var chaptersUrl: String? = null
    var chapters: List<MediaChapter> = emptyList()

    forEachChildTag(parser, TAG_ATOM_ENTRY) { tagName ->
      when (tagName) {
        TAG_TITLE -> {
          title = parser.nextText()
        }
        TAG_LINK -> {
          val rel = parser.getAttributeValue(parser.namespace, ATTR_REL)
          val href = parser.getAttributeValue(parser.namespace, ATTR_HREF)?.trim()?.ifBlank { null }
          val type = parser.getAttributeValue(parser.namespace, ATTR_TYPE)

          if (rel == "enclosure") {
            if (type?.startsWith("audio/", ignoreCase = true) == true && audioUrl.isNullOrBlank()) {
              audioUrl = href
            } else if (
              type?.startsWith("image/", ignoreCase = true) == true &&
                enclosureImage.isNullOrBlank()
            ) {
              enclosureImage = href
            }
          }

          if (link.isNullOrBlank() && (rel == ATTR_VALUE_ALTERNATE || rel.isNullOrBlank())) {
            link = href
          }
          parser.skipSubTree()
        }
        TAG_CONTENT -> {
          rawContentHtml = readAtomContent(parser)
        }
        TAG_SUMMARY -> {
          rawSummaryHtml = readAtomContent(parser)
        }
        TAG_PUBLISHED,
        TAG_UPDATED -> {
          if (date.isNullOrBlank()) {
            date = parser.nextText()
          } else {
            parser.skipSubTree()
          }
        }
        TAG_ITUNES_IMAGE -> {
          val href = parser.getAttributeValue(parser.namespace, ATTR_HREF)?.trim()?.ifBlank { null }
          parser.skipSubTree()
          if (itunesImage.isNullOrBlank()) {
            itunesImage = href
          }
        }
        TAG_PODCAST_IMAGES -> {
          val srcset = parser.getAttributeValue(parser.namespace, ATTR_SRCSET)
          parser.skipSubTree()
          if (podcastImages.isNullOrBlank()) {
            podcastImages = parseSrcsetHighestResUrl(srcset)
          }
        }
        TAG_GOOGLEPLAY_IMAGE -> {
          val href = parser.getAttributeValue(parser.namespace, ATTR_HREF)?.trim()?.ifBlank { null }
          parser.skipSubTree()
          if (googlePlayImage.isNullOrBlank()) {
            googlePlayImage = href
          }
        }
        TAG_MEDIA_THUMBNAIL -> {
          val url = readMediaImageUrl(parser)
          if (mediaImage.isNullOrBlank()) {
            mediaImage = url
          }
        }
        TAG_MEDIA_CONTENT -> {
          val mediaContent = readMediaContent(parser)
          if (mediaImage.isNullOrBlank() && !mediaContent.imageUrl.isNullOrBlank()) {
            mediaImage = mediaContent.imageUrl
          }
          if (mediaContentAudioUrl.isNullOrBlank() && !mediaContent.audioUrl.isNullOrBlank()) {
            mediaContentAudioUrl = mediaContent.audioUrl
          }
        }
        TAG_MEDIA_GROUP -> {
          val mediaGroupResult = readMediaGroup(parser)
          if (mediaGroupImage.isNullOrBlank()) {
            mediaGroupImage = mediaGroupResult.image
          }
          if (mediaGroupAudioUrl.isNullOrBlank()) {
            mediaGroupAudioUrl = mediaGroupResult.audioUrl
          }
          mediaGroupDescription =
            mediaGroupDescription.orEmpty().ifBlank { mediaGroupResult.description }
        }
        TAG_PODCAST_CHAPTERS -> {
          val parsedUrl = readPodcastChaptersUrl(parser, hostLink)
          if (chaptersUrl.isNullOrBlank() && parsedUrl != null) {
            chaptersUrl = parsedUrl
          }
        }
        TAG_PSC_CHAPTERS -> {
          val parsedChapters = readPodloveChapters(parser, hostLink)
          if (chapters.isEmpty() && parsedChapters.isNotEmpty()) {
            chapters = parsedChapters
          }
        }
        else -> parser.skipSubTree()
      }
    }

    val postContent =
      resolveDeferredPostContent(primaryHtml = rawContentHtml, secondaryHtml = rawSummaryHtml)
    val resolvedImage =
      itunesImage
        ?: podcastImages
        ?: googlePlayImage
        ?: mediaGroupImage
        ?: mediaImage
        ?: enclosureImage
        ?: postContent?.heroImage

    return createPostPayload(
      title = title,
      link = link,
      description = postContent?.textContent?.ifBlank { null } ?: mediaGroupDescription,
      rawContent = postContent?.rawContent,
      imageUrl = resolvedImage,
      audioUrl = audioUrl ?: mediaContentAudioUrl ?: mediaGroupAudioUrl ?: postContent?.audioUrl,
      date = date,
      hostLink = hostLink,
      chaptersUrl = chaptersUrl,
      chapters = chapters,
    )
  }

  private fun readAtomContent(parser: XmlPullParser): String {
    val type = parser.getAttributeValue(parser.namespace, XmlFeedParser.ATTR_TYPE)
    val isXhtml = type == "xhtml" || type == "application/xhtml+xml"
    val builder = StringBuilder()
    var depth = 1
    val emptyTagStack = mutableListOf<Boolean>()

    while (depth > 0) {
      when (parser.next()) {
        EventType.START_TAG -> {
          depth++
          val isEmpty = parser.isEmptyElementTag
          emptyTagStack.add(isEmpty)
          val isOuterXhtmlDiv =
            isXhtml && depth == 2 && (parser.name == "div" || parser.name == "xhtml:div")
          if (!isOuterXhtmlDiv) {
            builder.append('<').append(parser.name)
            for (i in 0 until parser.attributeCount) {
              val attrName = parser.getAttributeName(i)
              val attrPrefix = parser.getAttributePrefix(i)
              val attrValue = parser.getAttributeValue(i)
              builder.append(' ')
              if (!attrPrefix.isNullOrEmpty()) {
                builder.append(attrPrefix).append(':')
              }
              builder
                .append(attrName)
                .append("=\"")
                .append(escapeHtmlAttribute(attrValue))
                .append('"')
            }
            if (isEmpty) {
              builder.append(" />")
            } else {
              builder.append('>')
            }
          }
        }
        EventType.END_TAG -> {
          val isOuterXhtmlDiv =
            isXhtml && depth == 2 && (parser.name == "div" || parser.name == "xhtml:div")
          depth--
          if (depth > 0) {
            val wasEmpty = emptyTagStack.removeAt(emptyTagStack.lastIndex)
            if (!isOuterXhtmlDiv && !wasEmpty) {
              builder.append("</").append(parser.name).append('>')
            }
          }
        }
        EventType.TEXT,
        EventType.CDSECT,
        EventType.ENTITY_REF -> {
          val text = parser.text
          if (depth > 1 || isXhtml) {
            builder.append(escapeHtmlText(text))
          } else {
            builder.append(text)
          }
        }
        EventType.IGNORABLE_WHITESPACE -> {
          builder.append(parser.text)
        }
        EventType.END_DOCUMENT -> break
        else -> {}
      }
    }

    return builder.toString().trimIndent()
  }

  private fun escapeHtmlText(text: String): String {
    if ('&' !in text && '<' !in text && '>' !in text) return text
    return buildString(text.length) {
      for (ch in text) {
        when (ch) {
          '&' -> append("&amp;")
          '<' -> append("&lt;")
          '>' -> append("&gt;")
          else -> append(ch)
        }
      }
    }
  }

  private fun escapeHtmlAttribute(value: String): String {
    if ('&' !in value && '"' !in value && '<' !in value && '>' !in value) return value
    return buildString(value.length) {
      for (ch in value) {
        when (ch) {
          '&' -> append("&amp;")
          '"' -> append("&quot;")
          '<' -> append("&lt;")
          '>' -> append("&gt;")
          else -> append(ch)
        }
      }
    }
  }

  private fun readAtomLink(tagName: String, parser: XmlPullParser): String? {
    var link: String? = null
    parser.require(EventType.START_TAG, parser.namespace, tagName)
    val relType = parser.getAttributeValue(parser.namespace, ATTR_REL)
    if (relType == ATTR_VALUE_ALTERNATE || relType.isNullOrBlank()) {
      link = parser.getAttributeValue(parser.namespace, ATTR_HREF)
    }
    parser.nextTag()
    parser.require(EventType.END_TAG, parser.namespace, tagName)
    return link
  }
}
