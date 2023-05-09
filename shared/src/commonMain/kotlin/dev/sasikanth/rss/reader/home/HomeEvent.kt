package dev.sasikanth.rss.reader.home

import dev.sasikanth.rss.reader.database.Feed
import dev.sasikanth.rss.reader.database.Post

sealed interface HomeEvent {

  object LoadContent : HomeEvent

  object OnSwipeToRefresh : HomeEvent

  data class OnFeedSelected(val feed: Feed) : HomeEvent

  object OnHomeSelected : HomeEvent

  object OnAddClicked : HomeEvent

  data class OnPostClicked(val post: Post) : HomeEvent
}
