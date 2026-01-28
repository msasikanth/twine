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

package dev.sasikanth.rss.reader.data.opml

import co.touchlab.crashkios.bugsnag.BugsnagKotlin
import kotlinx.serialization.serializer
import me.tatarka.inject.annotations.Inject
import nl.adaptivity.xmlutil.serialization.XML

@Inject
class SourcesOpml {

  private val xml = XML {
    autoPolymorphic = true
    indentString = "  "
    defaultPolicy {
      pedantic = false
      ignoreUnknownChildren()
    }
  }

  fun encode(sources: List<OpmlSource>): String {
    try {
      val opml =
        Opml(
          version = "2.0",
          head = Head("Twine RSS Feeds"),
          body = Body(outlines = sources.map(::mapSourceToOutline))
        )

      val xmlString = xml.encodeToString(serializer<Opml>(), opml)

      return StringBuilder(xmlString)
        .insert(0, "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n")
        .appendLine()
        .toString()
    } catch (e: Exception) {
      BugsnagKotlin.sendHandledException(e)
    }

    return ""
  }

  fun decode(content: String): List<OpmlSource> {
    try {
      val opml = xml.decodeFromString(serializer<Opml>(), content)
      return opml.body.outlines.mapNotNull(::mapOutlineToSource)
    } catch (e: Exception) {
      BugsnagKotlin.sendHandledException(e)
    }

    return emptyList()
  }

  private fun mapSourceToOutline(source: OpmlSource): Outline {
    return when (source) {
      is OpmlFeed -> {
        Outline(
          text = source.title,
          title = source.title,
          type = "rss",
          xmlUrl = source.link,
          outlines = null
        )
      }
      is OpmlFeedGroup -> {
        Outline(
          text = source.title,
          title = source.title,
          type = null,
          xmlUrl = null,
          outlines = source.feeds.map(::mapSourceToOutline)
        )
      }
    }
  }

  private fun mapOutlineToSource(outline: Outline): OpmlSource? {
    if (!outline.xmlUrl.isNullOrBlank()) {
      return mapOutlineToOpmlFeed(outline)
    } else if (outline.outlines.isNullOrEmpty().not()) {
      return OpmlFeedGroup(
        title = outline.title ?: outline.text ?: "Group",
        feeds = outline.outlines.mapNotNull(::mapOutlineToOpmlFeed)
      )
    }

    return null
  }

  private fun mapOutlineToOpmlFeed(outline: Outline): OpmlFeed? {
    return if (!outline.xmlUrl.isNullOrBlank()) {
      OpmlFeed(title = outline.title ?: outline.text, link = outline.xmlUrl)
    } else {
      null
    }
  }
}
