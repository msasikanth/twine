package dev.sasikanth.rss.reader.home

import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.arkivanov.decompose.value.update
import com.arkivanov.essenty.instancekeeper.InstanceKeeper
import com.arkivanov.essenty.lifecycle.Lifecycle
import com.arkivanov.essenty.lifecycle.doOnCreate
import com.arkivanov.essenty.lifecycle.doOnResume
import dev.sasikanth.rss.reader.database.Feed
import dev.sasikanth.rss.reader.database.PostWithMetadata
import dev.sasikanth.rss.reader.repository.RssRepository
import dev.sasikanth.rss.reader.utils.DispatchersProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.launch

internal class HomeViewModel(
  lifecycle: Lifecycle,
  dispatchersProvider: DispatchersProvider,
  private val rssRepository: RssRepository,
) : InstanceKeeper.Instance {

  private val viewModelScope = CoroutineScope(SupervisorJob() + dispatchersProvider.main)

  private val _state = MutableValue(HomeState.DEFAULT)
  val state: Value<HomeState> = _state

  private val _effects: Channel<HomeEffect> = Channel(
    capacity = 5,
    onBufferOverflow = BufferOverflow.DROP_OLDEST
  )
  val effects: Channel<HomeEffect> = Channel()

  init {
    lifecycle.doOnCreate {
      dispatch(HomeEvent.LoadContent)
    }

    lifecycle.doOnResume {
      viewModelScope.launch {
        _effects.consumeAsFlow().collect(effects::send)
      }
    }
  }

  fun dispatch(event: HomeEvent) {
    when (event) {
      HomeEvent.LoadContent -> loadContent()
      HomeEvent.OnSwipeToRefresh -> refreshContent()
      is HomeEvent.OnFeedSelected -> onFeedSelected(event.feed)
      HomeEvent.OnHomeSelected -> onHomeSelected()
      HomeEvent.OnAddClicked -> onAddClicked()
      is HomeEvent.OnPostClicked -> onPostClicked(event.post)
    }
  }

  private fun onPostClicked(post: PostWithMetadata) {
    viewModelScope.launch {
      _effects.send(HomeEffect.OpenPost(post))
    }
  }

  private fun onAddClicked() {
    viewModelScope.launch {
      _effects.send(HomeEffect.NavigateToAddScreen)
    }
  }

  private fun onHomeSelected() {
    val posts = rssRepository.allPosts()
    _state.update {
      it.copy(posts = posts, selectedFeed = null)
    }
  }

  private fun onFeedSelected(feed: Feed) {
    val posts = rssRepository.postsOfFeed(feed.link)
    _state.update {
      it.copy(
        posts = posts,
        selectedFeed = feed
      )
    }
  }

  private fun refreshContent() {
    viewModelScope.launch {
      updateLoadingState {
        rssRepository.updateFeeds()
      }
    }
  }

  private fun loadContent() {
    val feeds = rssRepository.allFeeds()
    val posts = rssRepository.allPosts()

    _state.update {
      it.copy(
        feeds = feeds,
        posts = posts
      )
    }
  }

  private suspend fun updateLoadingState(action: suspend () -> Unit) {
    _state.update { it.copy(loadingState = HomeLoadingState.Loading) }
    action()
    _state.update { it.copy(loadingState = HomeLoadingState.Idle) }
  }

  override fun onDestroy() {
    viewModelScope.cancel()
  }
}
