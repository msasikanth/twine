package dev.sasikanth.rss.reader.models.mappers

import dev.sasikanth.rss.reader.database.Feed
import dev.sasikanth.rss.reader.models.FeedPayload

fun FeedPayload.toFeed() = Feed(
  name = name,
  icon = icon,
  description = description,
  link = link
)
