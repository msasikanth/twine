package dev.sasikanth.rss.reader.home

import dev.sasikanth.rss.reader.database.Feed
import dev.sasikanth.rss.reader.database.Post
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

data class HomeState(
  val feeds: Flow<List<Feed>>,
  val posts: Flow<List<Post>>,
  val selectedFeed: Feed?,
  val loadingState: HomeLoadingState
) {

  companion object {

    val DEFAULT = HomeState(
      feeds = emptyFlow(),
      posts = emptyFlow(),
      selectedFeed = null,
      loadingState = HomeLoadingState.Idle
    )
  }
}

sealed interface HomeLoadingState {
  object Idle : HomeLoadingState
  object Loading : HomeLoadingState
  data class Error(val errorMessage: String) : HomeLoadingState
}
