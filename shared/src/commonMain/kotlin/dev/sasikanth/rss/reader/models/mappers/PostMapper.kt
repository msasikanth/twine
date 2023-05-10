package dev.sasikanth.rss.reader.models.mappers

import dev.sasikanth.rss.reader.database.Post
import dev.sasikanth.rss.reader.models.PostPayload
import kotlinx.datetime.Instant

fun PostPayload.toPost(feedLink: String) = Post(
  title = title,
  description = description,
  imageUrl = imageUrl,
  date = Instant.fromEpochMilliseconds(date),
  feedLink = feedLink,
  link = link
)
