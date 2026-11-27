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
import dev.sasikanth.rss.reader.core.network.parser.xml.XmlFeedParser.Companion.ATTR_MEDIUM
import dev.sasikanth.rss.reader.core.network.parser.xml.XmlFeedParser.Companion.ATTR_TYPE
import dev.sasikanth.rss.reader.core.network.parser.xml.XmlFeedParser.Companion.ATTR_URL
import dev.sasikanth.rss.reader.core.network.parser.xml.XmlFeedParser.Companion.TAG_MEDIA_CONTENT
import dev.sasikanth.rss.reader.core.network.parser.xml.XmlFeedParser.Companion.TAG_MEDIA_DESCRIPTION
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

  internal companion object {
    private const val FALLBACK_SCAN_LIMIT = 64 * 1024
    private const val FALLBACK_PREVIEW_LIMIT = 8 * 1024

    private val imgSrcRegex =
      Regex("""<img\b[^>]*?\bsrc\s*=\s*["']([^"']+)["']""", RegexOption.IGNORE_CASE)

    private val srcsetSplitRegex = Regex(",\\s+|,(?=https?://)", RegexOption.IGNORE_CASE)

    internal fun parseSrcsetHighestResUrl(srcset: String?): String? {
      if (srcset.isNullOrBlank()) return null

      var bestUrl: String? = null
      var bestScore = -1.0

      srcset.split(srcsetSplitRegex).forEach { entry ->
        val parts = entry.trim().split(Regex("\\s+")).filter { it.isNotBlank() }
        val url = parts.firstOrNull() ?: return@forEach
        val descriptor = parts.getOrNull(1)?.lowercase()
        val score =
          when {
            descriptor == null -> 0.0
            descriptor.endsWith("w") -> descriptor.removeSuffix("w").toDoubleOrNull() ?: 0.0
            descriptor.endsWith("x") ->
              (descriptor.removeSuffix("x").toDoubleOrNull() ?: 0.0) * 1000.0
            else -> 0.0
          }
        if (score > bestScore) {
          bestScore = score
          bestUrl = url
        }
      }

      return bestUrl
    }
  }

  protected abstract val articleHtmlParser: ArticleHtmlParser

  abstract suspend fun parse(feedUrl: String, parser: XmlPullParser): FeedPayload

  /**
   * Advances to [containerTag]'s start tag, skipping over any sibling elements that precede it.
   * Some providers put unrelated elements, like a `<script>` for their XSLT stylesheet, between the
   * feed root and its container tag. Depth is tracked here instead of using `skipSubTree`, which
   * loops forever when the document ends inside the subtree being skipped.
   */
  protected fun advanceToContainerTag(parser: XmlPullParser, containerTag: String) {
    var depth = 0

    while (true) {
      when (parser.next()) {
        EventType.END_DOCUMENT -> break
        EventType.START_TAG -> {
          if (depth == 0 && parser.name == containerTag) return
          depth++
        }
        EventType.END_TAG -> if (depth > 0) depth--
        else -> {}
      }
    }

    parser.require(EventType.START_TAG, parser.namespace, containerTag)
  }

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

  protected fun readMediaContent(parser: XmlPullParser): MediaContentResult {
    val medium = parser.getAttributeValue(parser.namespace, ATTR_MEDIUM)?.trim()?.lowercase()
    val type = parser.getAttributeValue(parser.namespace, ATTR_TYPE)?.trim()?.lowercase()
    val urlFromAttribute =
      parser.getAttributeValue(parser.namespace, ATTR_URL)?.trim()?.ifBlank { null }
    val rawUrl =
      if (!urlFromAttribute.isNullOrBlank()) {
        parser.skipSubTree()
        urlFromAttribute
      } else {
        readTextContentAndSkipSubTree(parser)
      }

    if (rawUrl.isNullOrBlank()) return MediaContentResult()

    val isAudio = medium == "audio" || type?.startsWith("audio/") == true
    val isVideo = medium == "video" || type?.startsWith("video/") == true

    return when {
      isAudio -> MediaContentResult(audioUrl = rawUrl)
      isVideo -> MediaContentResult()
      else -> MediaContentResult(imageUrl = rawUrl)
    }
  }

  protected fun createFeedPayload(
    name: String?,
    description: String?,
    icon: String?,
    coverArtUrl: String? = null,
    homepageLink: String?,
    link: String,
    posts: Flow<PostPayload>,
  ): FeedPayload {
    val resolvedHomepageLink = homepageLink?.ifBlank { null } ?: link
    val host = UrlUtils.extractHost(resolvedHomepageLink)
    val resolvedCoverArtUrl =
      coverArtUrl?.trim()?.ifBlank { null }?.let { UrlUtils.safeUrl(host, it) }
    val resolvedIcon = icon?.trim()?.ifBlank { null }?.let { UrlUtils.safeUrl(host, it) }
    val finalIcon = resolvedIcon ?: resolvedCoverArtUrl ?: UrlUtils.fallbackFeedIcon(host)

    return FeedPayload(
      name = XmlFeedParser.cleanText(name ?: resolvedHomepageLink)!!.decodeHTMLString(),
      description = XmlFeedParser.cleanText(description).orEmpty().decodeHTMLString(),
      icon = finalIcon,
      homepageLink = homepageLink ?: link,
      link = link,
      posts = posts,
      coverArtUrl = resolvedCoverArtUrl,
    )
  }

  protected fun parsePostContent(parser: XmlPullParser): PostContent {
    return parsePostContentText(parser.nextText().trimIndent())
  }

  protected open fun parsePostContentText(postHtmlContent: String): PostContent {
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

  protected fun resolveDeferredPostContent(
    primaryHtml: String?,
    secondaryHtml: String?,
  ): PostContent? {
    val nonBlankPrimary = primaryHtml?.ifBlank { null }
    val nonBlankSecondary = secondaryHtml?.ifBlank { null }
    val selectedHtml = nonBlankPrimary ?: nonBlankSecondary ?: return null
    val parsedPrimary = parsePostContentText(selectedHtml)

    if (
      nonBlankPrimary != null &&
        nonBlankSecondary != null &&
        ((parsedPrimary.heroImage == null &&
          nonBlankSecondary.contains("<img", ignoreCase = true)) ||
          (parsedPrimary.audioUrl == null &&
            (nonBlankSecondary.contains("<audio", ignoreCase = true) ||
              nonBlankSecondary.contains("<source", ignoreCase = true))))
    ) {
      val parsedSecondary = parsePostContentText(nonBlankSecondary)
      return PostContent(
        rawContent = parsedPrimary.rawContent,
        heroImage = parsedPrimary.heroImage ?: parsedSecondary.heroImage,
        textContent = parsedPrimary.textContent,
        audioUrl = parsedPrimary.audioUrl ?: parsedSecondary.audioUrl,
      )
    }

    return parsedPrimary
  }

  protected fun readMediaGroup(parser: XmlPullParser): MediaGroupResult {
    var image: String? = null
    var description: String? = null
    var audioUrl: String? = null

    forEachChildTag(parser, TAG_MEDIA_GROUP) { name ->
      when (name) {
        TAG_MEDIA_THUMBNAIL -> {
          val imageUrl = readMediaImageUrl(parser)
          if (image.isNullOrBlank()) {
            image = imageUrl
          }
        }
        TAG_MEDIA_CONTENT -> {
          val mediaContent = readMediaContent(parser)
          if (image.isNullOrBlank() && !mediaContent.imageUrl.isNullOrBlank()) {
            image = mediaContent.imageUrl
          }
          if (audioUrl.isNullOrBlank() && !mediaContent.audioUrl.isNullOrBlank()) {
            audioUrl = mediaContent.audioUrl
          }
        }
        TAG_MEDIA_DESCRIPTION -> {
          description = parser.nextText()
        }
        else -> parser.skipSubTree()
      }
    }

    return MediaGroupResult(image = image, description = description, audioUrl = audioUrl)
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
      imageUrl =
        UrlUtils.upgradeYouTubeThumbnail(UrlUtils.safeUrl(hostLink, imageUrl))
          ?: UrlUtils.youTubeThumbnail(postLink),
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

  protected data class MediaContentResult(
    val imageUrl: String? = null,
    val audioUrl: String? = null,
  )

  protected data class MediaGroupResult(
    val image: String?,
    val description: String?,
    val audioUrl: String? = null,
  )
}
