package dev.sasikanth.rss.reader.models.mappers

import dev.sasikanth.rss.reader.database.Post
import dev.sasikanth.rss.reader.models.PostPayload

fun PostPayload.toPost(feedLink: String) = Post(
  title = title,
  description = description,
  imageUrl = imageUrl,
  date = date,
  feedLink = feedLink,
  link = link
)
