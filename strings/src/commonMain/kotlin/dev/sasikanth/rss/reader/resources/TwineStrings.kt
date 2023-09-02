package dev.sasikanth.rss.reader.resources

data class TwineStrings(
  val appName: String,
  val postSourceUnknown: String,
  val buttonAll: String,
  val buttonAddFeed: String,
  val buttonGoBack: String,
  val buttonCancel: String,
  val buttonAdd: String,
  val buttonChange: String,
  val feedEntryHint: String,
  val share: String,
  val scrollToTop: String,
  val noFeeds: String,
  val swipeUpGetStarted: String,
  val feedNameHint: String,
  val editFeedName: String,
  val errorUnsupportedFeed: String,
  val errorMalformedXml: String,
  val errorRequestTimeout: String,
  val searchHint: String,
  val searchSortNewest: String,
  val searchSortNewestFirst: String,
  val searchSortOldest: String,
  val searchSortOldestFirst: String
)

object Locales {
  const val EN = "en"
}
