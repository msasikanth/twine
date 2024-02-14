/*
 * Copyright 2023 Sasikanth Miriyampalli
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
package dev.sasikanth.rss.reader.opml

import co.touchlab.crashkios.bugsnag.BugsnagKotlin
import dev.sasikanth.rss.reader.core.model.local.Feed
import dev.sasikanth.rss.reader.di.scopes.AppScope
import kotlinx.serialization.serializer
import me.tatarka.inject.annotations.Inject
import nl.adaptivity.xmlutil.serialization.XML

@Inject
@AppScope
class FeedsOpml {

  private val xml = XML {
    autoPolymorphic = true
    indentString = "  "
    defaultPolicy {
      pedantic = false
      ignoreUnknownChildren()
    }
  }

  fun encode(feeds: List<Feed>): String {
    return try {
      val opml =
        Opml(
          version = "2.0",
          head = Head("Twine RSS Feeds"),
          body = Body(outlines = feeds.map(::mapFeedToOutline))
        )

      val xmlString = xml.encodeToString(serializer<Opml>(), opml)

      StringBuilder(xmlString)
        .insert(0, "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n")
        .appendLine()
        .toString()
    } catch (e: Exception) {
      BugsnagKotlin.sendHandledException(e)
      ""
    }
  }

  fun decode(content: String): List<OpmlFeed> {
    return try {
      val opml = xml.decodeFromString(serializer<Opml>(), content)
      val opmlFeeds = mutableListOf<OpmlFeed>()

      fun flatten(outline: Outline) {
        if (outline.outlines.isNullOrEmpty() && !outline.xmlUrl.isNullOrBlank()) {
          opmlFeeds.add(mapOutlineToOpmlFeed(outline))
        }

        outline.outlines?.forEach { nestedOutline -> flatten(nestedOutline) }
      }

      opml.body.outlines.forEach { outline -> flatten(outline) }

      opmlFeeds.distinctBy { it.link }
    } catch (e: Exception) {
      BugsnagKotlin.sendHandledException(e)
      emptyList()
    }
  }

  private fun mapFeedToOutline(feed: Feed) =
    Outline(text = feed.name, title = feed.name, type = "rss", xmlUrl = feed.link, outlines = null)

  private fun mapOutlineToOpmlFeed(outline: Outline): OpmlFeed {
    return OpmlFeed(title = outline.title, link = outline.xmlUrl!!)
  }
}
