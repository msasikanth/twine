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

import dev.sasikanth.rss.reader.core.model.remote.FeedPayload
import dev.sasikanth.rss.reader.core.model.remote.PostPayload
import dev.sasikanth.rss.reader.core.network.parser.common.ArticleHtmlParser
import dev.sasikanth.rss.reader.core.network.parser.xml.XmlFeedParser.Companion.TAG_MEDIA_CONTENT
import dev.sasikanth.rss.reader.core.network.parser.xml.XmlFeedParser.Companion.TAG_MEDIA_THUMBNAIL
import dev.sasikanth.rss.reader.core.network.parser.xml.XmlFeedParser.Companion.TAG_URL
import dev.sasikanth.rss.reader.core.network.utils.UrlUtils
import dev.sasikanth.rss.reader.util.dateStringToEpochMillis
import dev.sasikanth.rss.reader.util.decodeHTMLString
import kotlin.time.Clock
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.kobjects.ktxml.api.EventType
import org.kobjects.ktxml.api.XmlPullParser

abstract class XmlContentParser {

  protected abstract val articleHtmlParser: ArticleHtmlParser

  abstract suspend fun parse(feedUrl: String, parser: XmlPullParser): FeedPayload

  protected fun postsFlow(
    parser: XmlPullParser,
    firstPost: PostPayload? = null,
    itemTag: String,
    readItem: (XmlPullParser) -> PostPayload?,
  ): Flow<PostPayload> = flow {
    if (firstPost != null) {
      emit(firstPost)
    }

    while (parser.next() != EventType.END_TAG) {
      if (parser.eventType != EventType.START_TAG) continue

      if (parser.name == itemTag) {
        val post = readItem(parser)
        if (post != null) {
          emit(post)
        }
      } else {
        parser.skipSubTree()
      }
    }
  }

  protected fun createFeedPayload(
    name: String?,
    description: String?,
    icon: String?,
    homepageLink: String?,
    link: String,
    posts: Flow<PostPayload>,
  ): FeedPayload {
    val host = UrlUtils.extractHost(homepageLink ?: link)
    val finalIcon =
      if (icon.isNullOrBlank()) {
        UrlUtils.fallbackFeedIcon(host)
      } else {
        icon
      }

    return FeedPayload(
      name = XmlFeedParser.cleanText(name ?: homepageLink)!!.decodeHTMLString(),
      description = XmlFeedParser.cleanText(description).orEmpty().decodeHTMLString(),
      icon = finalIcon,
      homepageLink = homepageLink ?: link,
      link = link,
      posts = posts,
    )
  }

  protected fun parsePostContent(parser: XmlPullParser): PostContent {
    val postHtmlContent = parser.nextText().trimIndent()
    val htmlContent = articleHtmlParser.parse(htmlContent = postHtmlContent)

    return PostContent(
      rawContent = htmlContent?.cleanedHtml,
      heroImage = htmlContent?.heroImage,
      textContent = htmlContent?.textContent?.ifBlank { null } ?: postHtmlContent.trim(),
    )
  }

  protected fun readMediaGroup(parser: XmlPullParser): MediaGroupResult {
    var image: String? = null
    var description: String? = null

    while (parser.next() != EventType.END_TAG) {
      if (parser.eventType != EventType.START_TAG) continue

      when (parser.name) {
        TAG_MEDIA_THUMBNAIL -> {
          image = parser.getAttributeValue(parser.namespace, TAG_URL)
          parser.nextTag()
        }
        TAG_MEDIA_CONTENT -> {
          description = parser.nextText()
        }
        else -> parser.skipSubTree()
      }
    }

    return MediaGroupResult(image, description)
  }

  protected fun createPostPayload(
    title: String?,
    link: String?,
    description: String?,
    rawContent: String?,
    imageUrl: String?,
    audioUrl: String?,
    date: String?,
    commentsLink: String? = null,
    hostLink: String?,
  ): PostPayload? {
    val postPubDateInMillis = date?.dateStringToEpochMillis()

    if (link.isNullOrBlank() || (title.isNullOrBlank() && description.isNullOrBlank())) {
      return null
    }

    return PostPayload(
      title = XmlFeedParser.cleanText(title).orEmpty().decodeHTMLString(),
      link = XmlFeedParser.cleanText(link)!!,
      description = description.orEmpty().decodeHTMLString(),
      rawContent = rawContent,
      fullContent = null,
      imageUrl = UrlUtils.safeUrl(hostLink, imageUrl),
      audioUrl = audioUrl,
      date = postPubDateInMillis ?: Clock.System.now().toEpochMilliseconds(),
      commentsLink = commentsLink?.trim(),
      isDateParsedCorrectly = postPubDateInMillis != null,
    )
  }

  protected data class PostContent(
    val rawContent: String?,
    val heroImage: String?,
    val textContent: String?,
  )

  protected data class MediaGroupResult(val image: String?, val description: String?)
}
