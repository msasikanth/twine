package dev.sasikanth.rss.reader.parser

import dev.sasikanth.rss.reader.models.FeedPayload

internal interface FeedParser {

  companion object {
    private val htmlTag = Regex("<.+?>")
    private val blankLine = Regex("(?m)^[ \t]*\r?\n")

    val imageTags = setOf(
      "media:content",
      "media:thumbnail"
    )

    fun cleanText(text: String?): String? =
      text?.replace(htmlTag, "")
        ?.replace(blankLine, "")
        ?.trim()

    fun cleanTextCompact(text: String?) = cleanText(text)?.take(300)

    fun feedIcon(host: String): String {
      return "https://icon.horse/icon/$host"
    }
  }

  suspend fun parse(xmlContent: String): FeedPayload
}
