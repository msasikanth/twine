package dev.sasikanth.rss.reader.models

data class PostPayload(
  val title: String,
  val link: String,
  val description: String,
  val imageUrl: String?,
  val date: Long
) {
  companion object
}
