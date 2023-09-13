package dev.sasikanth.rss.reader.models.local

import kotlinx.datetime.Instant

data class PostWithMetadata(
  val title: String,
  val description: String,
  val imageUrl: String?,
  val date: Instant,
  val link: String,
  val bookmarked: Boolean,
  val feedName: String,
  val feedIcon: String,
  val feedLink: String,
)
