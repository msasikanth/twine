package dev.sasikanth.rss.reader.models.local

import kotlinx.datetime.Instant

data class Feed(
  val name: String,
  val icon: String,
  val description: String,
  val homepageLink: String,
  val createdAt: Instant,
  val link: String,
  val pinnedAt: Instant?,
)
