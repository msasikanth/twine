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
import dev.sasikanth.rss.reader.core.network.parser.xml.XmlFeedParser.Companion.ATTR_URL
import dev.sasikanth.rss.reader.core.network.parser.xml.XmlFeedParser.Companion.TAG_MEDIA_CONTENT
import dev.sasikanth.rss.reader.core.network.parser.xml.XmlFeedParser.Companion.TAG_MEDIA_GROUP
import dev.sasikanth.rss.reader.core.network.parser.xml.XmlFeedParser.Companion.TAG_MEDIA_THUMBNAIL
import dev.sasikanth.rss.reader.core.network.utils.UrlUtils
import dev.sasikanth.rss.reader.util.dateStringToEpochMillis
import dev.sasikanth.rss.reader.util.decodeHTMLString
import kotlin.time.Clock
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.kobjects.ktxml.api.EventType
import org.kobjects.ktxml.api.XmlPullParser

abstract class XmlContentParser {

  private companion object {
    private const val FALLBACK_SCAN_LIMIT = 64 * 1024
    private const val FALLBACK_PREVIEW_LIMIT = 8 * 1024

    private val imgSrcRegex =
      Regex("""<img\b[^>]*?\bsrc\s*=\s*["']([^"']+)["']""", RegexOption.IGNORE_CASE)
  }

  protected abstract val articleHtmlParser: ArticleHtmlParser

  abstract suspend fun parse(feedUrl: String, parser: XmlPullParser): FeedPayload

  protected fun postsFlow(
    parser: XmlPullParser,
    firstPost: PostPayload? = null,
    containerTag: String,
    itemTag: String,
    readItem: (XmlPullParser) -> PostPayload?,
  ): Flow<PostPayload> = flow {
    if (firstPost != null) {
      emit(firstPost)
    }

    forEachChildTag(parser, containerTag) { name ->
      if (name == itemTag) {
        val post = readItem(parser)
        if (post != null) {
          emit(post)
        }
      } else {
        parser.skipSubTree()
      }
    }
  }

  /**
   * Loops over the direct children of the tag the parser is currently inside, stopping only at
   * [containerTag]'s end tag. Bailing on any end tag would truncate the rest of the container when
   * a single child leaves the parser misaligned.
   */
  protected inline fun forEachChildTag(
    parser: XmlPullParser,
    containerTag: String,
    block: (String) -> Unit,
  ) {
    while (true) {
      val eventType = parser.next()
      if (eventType == EventType.END_DOCUMENT) return
      if (eventType == EventType.END_TAG && parser.name == containerTag) return
      if (eventType != EventType.START_TAG) continue

      block(parser.name)
    }
  }

  /**
   * Reads a media image URL from either the `url` attribute or the element's text content, and
   * consumes the whole element. Feeds like MyAnimeList put the URL in the text, and feeds like The
   * Guardian nest `media:credit` inside `media:content`.
   */
  protected fun readMediaImageUrl(parser: XmlPullParser): String? {
    val urlFromAttribute = parser.getAttributeValue(parser.namespace, ATTR_URL)
    if (!urlFromAttribute.isNullOrBlank()) {
      parser.skipSubTree()
      return urlFromAttribute
    }

    return readTextContentAndSkipSubTree(parser)
  }

  protected fun readTextContentAndSkipSubTree(parser: XmlPullParser): String? {
    var text: String? = null
    var depth = 1

    while (depth > 0) {
      when (parser.next()) {
        EventType.START_TAG -> depth++
        EventType.END_TAG -> depth--
        EventType.TEXT -> if (depth == 1 && text.isNullOrBlank()) text = parser.text
        EventType.END_DOCUMENT -> break
        else -> {}
      }
    }

    return text?.trim()?.ifBlank { null }
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

    if (htmlContent == null) {
      val head = postHtmlContent.take(FALLBACK_SCAN_LIMIT)
      val preview = head.take(FALLBACK_PREVIEW_LIMIT)

      return PostContent(
        rawContent = null,
        heroImage = imgSrcRegex.find(head)?.groupValues?.get(1)?.ifBlank { null },
        textContent = XmlFeedParser.cleanText(preview).orEmpty().ifBlank { preview.trim() },
        audioUrl = null,
      )
    }

    return PostContent(
      rawContent = htmlContent.cleanedHtml,
      heroImage = htmlContent.heroImage,
      textContent = htmlContent.textContent.ifBlank { null } ?: postHtmlContent.trim(),
      audioUrl = htmlContent.audioUrl,
    )
  }

  protected fun readMediaGroup(parser: XmlPullParser): MediaGroupResult {
    var image: String? = null
    var description: String? = null

    forEachChildTag(parser, TAG_MEDIA_GROUP) { name ->
      when (name) {
        TAG_MEDIA_THUMBNAIL -> {
          val imageUrl = readMediaImageUrl(parser)
          if (image.isNullOrBlank()) {
            image = imageUrl
          }
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

    val postLink = XmlFeedParser.cleanText(link)!!

    return PostPayload(
      title = XmlFeedParser.cleanText(title).orEmpty().decodeHTMLString(),
      link = postLink,
      description = description.orEmpty().decodeHTMLString(),
      rawContent = rawContent,
      fullContent = null,
      imageUrl = UrlUtils.safeUrl(hostLink, imageUrl) ?: UrlUtils.youTubeThumbnail(postLink),
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
    val audioUrl: String? = null,
  )

  protected data class MediaGroupResult(val image: String?, val description: String?)
}
