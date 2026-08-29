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
import dev.sasikanth.rss.reader.core.model.remote.FeedPayload
import dev.sasikanth.rss.reader.core.model.remote.PostPayload
import dev.sasikanth.rss.reader.core.network.parser.common.ArticleHtmlParser
import dev.sasikanth.rss.reader.core.network.parser.xml.XmlFeedParser.Companion.ATTR_HREF
import dev.sasikanth.rss.reader.core.network.parser.xml.XmlFeedParser.Companion.ATTR_REL
import dev.sasikanth.rss.reader.core.network.parser.xml.XmlFeedParser.Companion.ATTR_VALUE_ALTERNATE
import dev.sasikanth.rss.reader.core.network.parser.xml.XmlFeedParser.Companion.TAG_ATOM_ENTRY
import dev.sasikanth.rss.reader.core.network.parser.xml.XmlFeedParser.Companion.TAG_ATOM_FEED
import dev.sasikanth.rss.reader.core.network.parser.xml.XmlFeedParser.Companion.TAG_AUTHOR
import dev.sasikanth.rss.reader.core.network.parser.xml.XmlFeedParser.Companion.TAG_CONTENT
import dev.sasikanth.rss.reader.core.network.parser.xml.XmlFeedParser.Companion.TAG_ICON
import dev.sasikanth.rss.reader.core.network.parser.xml.XmlFeedParser.Companion.TAG_ITUNES_IMAGE
import dev.sasikanth.rss.reader.core.network.parser.xml.XmlFeedParser.Companion.TAG_LINK
import dev.sasikanth.rss.reader.core.network.parser.xml.XmlFeedParser.Companion.TAG_LOGO
import dev.sasikanth.rss.reader.core.network.parser.xml.XmlFeedParser.Companion.TAG_MEDIA_GROUP
import dev.sasikanth.rss.reader.core.network.parser.xml.XmlFeedParser.Companion.TAG_NAME
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
    var iconUrl: String? = null
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
          iconUrl = parser.nextText()
        }
        TAG_LOGO -> {
          iconUrl = parser.nextText()
        }
        TAG_ITUNES_IMAGE -> {
          iconUrl = parser.getAttributeValue(parser.namespace, ATTR_HREF)
          parser.skipSubTree()
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

    iconUrl =
      if (UrlUtils.isYouTubeLink(feedUrl) && !link.isNullOrBlank()) {
        youtubeChannelImage(link) ?: iconUrl
      } else {
        iconUrl
      }

    val postHostLink = UrlUtils.extractHost(link ?: feedUrl)

    return createFeedPayload(
      name = title,
      description = description,
      icon = iconUrl,
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
    var description: String? = null
    var rawContent: String? = null
    var date: String? = null
    var image: String? = null
    var audioUrl: String? = null

    forEachChildTag(parser, TAG_ATOM_ENTRY) { tagName ->
      when (tagName) {
        TAG_TITLE -> {
          title = parser.nextText()
        }
        TAG_LINK -> {
          val rel = parser.getAttributeValue(parser.namespace, ATTR_REL)
          val href = parser.getAttributeValue(parser.namespace, ATTR_HREF)
          val type = parser.getAttributeValue(parser.namespace, XmlFeedParser.ATTR_TYPE)

          if (rel == "enclosure" && type?.startsWith("audio/") == true) {
            audioUrl = href
          }

          if (link.isNullOrBlank() && (rel == ATTR_VALUE_ALTERNATE || rel.isNullOrBlank())) {
            link = href
          }
          parser.skipSubTree()
        }
        TAG_CONTENT,
        TAG_SUMMARY -> {
          val postContent = parsePostContent(parser)

          rawContent = postContent.rawContent
          image = postContent.heroImage ?: image
          description = postContent.textContent
          audioUrl = audioUrl ?: postContent.audioUrl
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
          val itunesImage = parser.getAttributeValue(parser.namespace, ATTR_HREF)
          parser.skipSubTree()
          if (image.isNullOrBlank()) {
            image = itunesImage
          }
        }
        in XmlFeedParser.imageTags -> {
          val mediaImage = readMediaImageUrl(parser)
          if (image.isNullOrBlank()) {
            image = mediaImage
          }
        }
        TAG_MEDIA_GROUP -> {
          val mediaGroupResult = readMediaGroup(parser)
          image = mediaGroupResult.image ?: image
          description = description.orEmpty().ifBlank { mediaGroupResult.description }
        }
        else -> parser.skipSubTree()
      }
    }

    return createPostPayload(
      title = title,
      link = link,
      description = description,
      rawContent = rawContent,
      imageUrl = image,
      audioUrl = audioUrl,
      date = date,
      hostLink = hostLink,
    )
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
