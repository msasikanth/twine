package dev.sasikanth.rss.reader.models

data class FeedPayload(
  val name: String,
  val icon: String,
  val description: String,
  val link: String,
  val posts: List<PostPayload>,
) {
  companion object
}
