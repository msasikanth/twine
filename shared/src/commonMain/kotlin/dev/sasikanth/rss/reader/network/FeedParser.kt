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
package dev.sasikanth.rss.reader.network

import dev.sasikanth.rss.reader.models.FeedPayload

internal interface FeedParser {

  companion object {
    private val htmlTag = Regex("<.+?>")
    private val blankLine = Regex("(?m)^[ \t]*\r?\n")

    val imageTags = setOf("media:content", "media:thumbnail")

    fun cleanText(text: String?): String? =
      text?.replace(htmlTag, "")?.replace(blankLine, "")?.trim()

    fun cleanTextCompact(text: String?) = cleanText(text)?.take(300)

    fun feedIcon(host: String): String {
      return "https://icon.horse/icon/$host"
    }
  }

  suspend fun parse(xmlContent: String, feedUrl: String): FeedPayload
}
