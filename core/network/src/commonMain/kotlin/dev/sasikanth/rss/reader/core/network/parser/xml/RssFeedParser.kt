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

import dev.sasikanth.rss.reader.core.model.local.MediaChapter
import dev.sasikanth.rss.reader.core.model.remote.FeedPayload
import dev.sasikanth.rss.reader.core.model.remote.PostPayload
import dev.sasikanth.rss.reader.core.network.parser.common.ArticleHtmlParser
import dev.sasikanth.rss.reader.core.network.parser.xml.XmlFeedParser.Companion.ATTR_HREF
import dev.sasikanth.rss.reader.core.network.parser.xml.XmlFeedParser.Companion.ATTR_SRCSET
import dev.sasikanth.rss.reader.core.network.parser.xml.XmlFeedParser.Companion.ATTR_TYPE
import dev.sasikanth.rss.reader.core.network.parser.xml.XmlFeedParser.Companion.ATTR_URL
import dev.sasikanth.rss.reader.core.network.parser.xml.XmlFeedParser.Companion.TAG_COMMENTS
import dev.sasikanth.rss.reader.core.network.parser.xml.XmlFeedParser.Companion.TAG_CONTENT_ENCODED
import dev.sasikanth.rss.reader.core.network.parser.xml.XmlFeedParser.Companion.TAG_DESCRIPTION
import dev.sasikanth.rss.reader.core.network.parser.xml.XmlFeedParser.Companion.TAG_ENCLOSURE
import dev.sasikanth.rss.reader.core.network.parser.xml.XmlFeedParser.Companion.TAG_FEATURED_IMAGE
import dev.sasikanth.rss.reader.core.network.parser.xml.XmlFeedParser.Companion.TAG_FEED_IMAGE
import dev.sasikanth.rss.reader.core.network.parser.xml.XmlFeedParser.Companion.TAG_GOOGLEPLAY_IMAGE
import dev.sasikanth.rss.reader.core.network.parser.xml.XmlFeedParser.Companion.TAG_ITUNES_IMAGE
import dev.sasikanth.rss.reader.core.network.parser.xml.XmlFeedParser.Companion.TAG_LINK
import dev.sasikanth.rss.reader.core.network.parser.xml.XmlFeedParser.Companion.TAG_MEDIA_CONTENT
import dev.sasikanth.rss.reader.core.network.parser.xml.XmlFeedParser.Companion.TAG_MEDIA_GROUP
import dev.sasikanth.rss.reader.core.network.parser.xml.XmlFeedParser.Companion.TAG_MEDIA_THUMBNAIL
import dev.sasikanth.rss.reader.core.network.parser.xml.XmlFeedParser.Companion.TAG_PODCAST_CHAPTERS
import dev.sasikanth.rss.reader.core.network.parser.xml.XmlFeedParser.Companion.TAG_PODCAST_IMAGES
import dev.sasikanth.rss.reader.core.network.parser.xml.XmlFeedParser.Companion.TAG_PSC_CHAPTERS
import dev.sasikanth.rss.reader.core.network.parser.xml.XmlFeedParser.Companion.TAG_PUB_DATE
import dev.sasikanth.rss.reader.core.network.parser.xml.XmlFeedParser.Companion.TAG_RSS_CHANNEL
import dev.sasikanth.rss.reader.core.network.parser.xml.XmlFeedParser.Companion.TAG_RSS_ITEM
import dev.sasikanth.rss.reader.core.network.parser.xml.XmlFeedParser.Companion.TAG_TITLE
import dev.sasikanth.rss.reader.core.network.parser.xml.XmlFeedParser.Companion.TAG_URL
import dev.sasikanth.rss.reader.core.network.utils.UrlUtils
import me.tatarka.inject.annotations.Inject
import org.kobjects.ktxml.api.EventType
import org.kobjects.ktxml.api.XmlPullParser

@Inject
class RSSContentParser(override val articleHtmlParser: ArticleHtmlParser) : XmlContentParser() {

  override suspend fun parse(feedUrl: String, parser: XmlPullParser): FeedPayload {
    advanceToContainerTag(parser, TAG_RSS_CHANNEL)

    var title: String? = null
    var link: String? = null
    var description: String? = null
    var itunesImage: String? = null
    var podcastImages: String? = null
    var googlePlayImage: String? = null
    var channelImageUrl: String? = null
    var firstPost: PostPayload? = null

    while (parser.next() != EventType.END_TAG) {
      if (parser.eventType != EventType.START_TAG) continue

      when (parser.name) {
        TAG_TITLE -> {
          title = parser.nextText()
        }
        TAG_LINK -> {
          if (link.isNullOrBlank()) {
            link = parser.nextText()
          } else {
            parser.skipSubTree()
          }
        }
        TAG_DESCRIPTION -> {
          description = parser.nextText()
        }
        TAG_FEED_IMAGE -> {
          val parsedUrl = readChannelImageUrl(parser)
          if (channelImageUrl.isNullOrBlank()) {
            channelImageUrl = parsedUrl
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
        TAG_RSS_ITEM -> {
          val host = UrlUtils.extractHost(link ?: feedUrl)
          firstPost = readRssItem(parser, host)
          if (firstPost != null) {
            break
          }
        }
        else -> parser.skipSubTree()
      }
    }

    val postHostLink = UrlUtils.extractHost(link ?: feedUrl)
    val artworkUrl = itunesImage ?: podcastImages ?: googlePlayImage ?: channelImageUrl

    return createFeedPayload(
      name = title,
      description = description,
      icon = artworkUrl,
      coverArtUrl = artworkUrl,
      homepageLink = link,
      link = feedUrl,
      posts =
        postsFlow(
          parser = parser,
          firstPost = firstPost,
          containerTag = TAG_RSS_CHANNEL,
          itemTag = TAG_RSS_ITEM,
          readItem = { readRssItem(it, postHostLink) },
        ),
    )
  }

  private fun readChannelImageUrl(parser: XmlPullParser): String? {
    parser.require(EventType.START_TAG, parser.namespace, parser.name)

    var imageUrl: String? = null
    while (parser.next() != EventType.END_TAG) {
      if (parser.eventType != EventType.START_TAG) continue
      if (parser.name == TAG_URL) {
        imageUrl = parser.nextText().trim().ifBlank { null }
      } else {
        parser.skipSubTree()
      }
    }

    return imageUrl
  }

  private fun readRssItem(parser: XmlPullParser, hostLink: String?): PostPayload? {
    parser.require(EventType.START_TAG, parser.namespace, TAG_RSS_ITEM)

    var title: String? = null
    var link: String? = null
    var rawDescription: String? = null
    var rawContentEncoded: String? = null
    var mediaGroupDescription: String? = null
    var mediaGroupImage: String? = null
    var mediaGroupAudioUrl: String? = null
    var date: String? = null
    var itunesImage: String? = null
    var podcastImages: String? = null
    var googlePlayImage: String? = null
    var mediaImage: String? = null
    var enclosureImage: String? = null
    var featuredImage: String? = null
    var audioUrl: String? = null
    var mediaContentAudioUrl: String? = null
    var commentsLink: String? = null
    var chaptersUrl: String? = null
    var chapters: List<MediaChapter> = emptyList()

    forEachChildTag(parser, TAG_RSS_ITEM) { name ->
      when {
        name == TAG_TITLE -> {
          title = parser.nextText()
        }
        link.isNullOrBlank() && (name == TAG_LINK || name == TAG_URL) -> {
          link = parser.nextText()
        }
        name == TAG_ENCLOSURE -> {
          val enclosureType = parser.getAttributeValue(parser.namespace, ATTR_TYPE)
          val enclosureUrl =
            parser.getAttributeValue(parser.namespace, ATTR_URL)?.trim()?.ifBlank { null }

          val isAudio = enclosureType?.startsWith("audio/", ignoreCase = true) == true
          val isImage = enclosureType?.startsWith("image/", ignoreCase = true) == true

          if (isAudio && audioUrl.isNullOrBlank()) {
            audioUrl = enclosureUrl
          }

          if (link.isNullOrBlank() && !isImage) {
            link = enclosureUrl
          }

          if (enclosureImage.isNullOrBlank() && isImage) {
            enclosureImage = enclosureUrl
          }

          parser.skipSubTree()
        }
        name == TAG_DESCRIPTION -> {
          rawDescription = parser.nextText().trimIndent()
        }
        name == TAG_CONTENT_ENCODED -> {
          rawContentEncoded = parser.nextText().trimIndent()
        }
        name == TAG_PUB_DATE -> {
          date = parser.nextText()
        }
        name == TAG_ITUNES_IMAGE -> {
          val href = parser.getAttributeValue(parser.namespace, ATTR_HREF)?.trim()?.ifBlank { null }
          parser.skipSubTree()
          if (itunesImage.isNullOrBlank()) {
            itunesImage = href
          }
        }
        name == TAG_PODCAST_IMAGES -> {
          val srcset = parser.getAttributeValue(parser.namespace, ATTR_SRCSET)
          parser.skipSubTree()
          if (podcastImages.isNullOrBlank()) {
            podcastImages = parseSrcsetHighestResUrl(srcset)
          }
        }
        name == TAG_GOOGLEPLAY_IMAGE -> {
          val href = parser.getAttributeValue(parser.namespace, ATTR_HREF)?.trim()?.ifBlank { null }
          parser.skipSubTree()
          if (googlePlayImage.isNullOrBlank()) {
            googlePlayImage = href
          }
        }
        name == TAG_MEDIA_THUMBNAIL -> {
          val url = readMediaImageUrl(parser)
          if (mediaImage.isNullOrBlank()) {
            mediaImage = url
          }
        }
        name == TAG_MEDIA_CONTENT -> {
          val mediaContent = readMediaContent(parser)
          if (mediaImage.isNullOrBlank() && !mediaContent.imageUrl.isNullOrBlank()) {
            mediaImage = mediaContent.imageUrl
          }
          if (mediaContentAudioUrl.isNullOrBlank() && !mediaContent.audioUrl.isNullOrBlank()) {
            mediaContentAudioUrl = mediaContent.audioUrl
          }
        }
        featuredImage.isNullOrBlank() && name == TAG_FEATURED_IMAGE -> {
          featuredImage = parser.nextText().trim().ifBlank { null }
        }
        commentsLink.isNullOrBlank() && name == TAG_COMMENTS -> {
          commentsLink = parser.nextText()
        }
        name == TAG_MEDIA_GROUP -> {
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
        name == TAG_PODCAST_CHAPTERS -> {
          val parsedUrl = readPodcastChaptersUrl(parser, hostLink)
          if (chaptersUrl.isNullOrBlank() && parsedUrl != null) {
            chaptersUrl = parsedUrl
          }
        }
        name == TAG_PSC_CHAPTERS -> {
          val parsedChapters = readPodloveChapters(parser, hostLink)
          if (chapters.isEmpty() && parsedChapters.isNotEmpty()) {
            chapters = parsedChapters
          }
        }
        else -> parser.skipSubTree()
      }
    }

    val postContent =
      resolveDeferredPostContent(primaryHtml = rawContentEncoded, secondaryHtml = rawDescription)
    val resolvedImage =
      itunesImage
        ?: podcastImages
        ?: googlePlayImage
        ?: mediaGroupImage
        ?: mediaImage
        ?: enclosureImage
        ?: featuredImage
        ?: postContent?.heroImage

    return createPostPayload(
      title = title,
      link = link,
      description = postContent?.textContent?.ifBlank { null } ?: mediaGroupDescription,
      rawContent = postContent?.rawContent,
      imageUrl = resolvedImage,
      audioUrl = audioUrl ?: mediaContentAudioUrl ?: mediaGroupAudioUrl ?: postContent?.audioUrl,
      date = date,
      commentsLink = commentsLink,
      hostLink = hostLink,
      chaptersUrl = chaptersUrl,
      chapters = chapters,
    )
  }
}
