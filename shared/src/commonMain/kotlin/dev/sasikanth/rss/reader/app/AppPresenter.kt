/*
 * Copyright 2023 Sasikanth Miriyampalli
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package dev.sasikanth.rss.reader.app

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.slot.ChildSlot
import com.arkivanov.decompose.router.slot.SlotNavigation
import com.arkivanov.decompose.router.slot.activate
import com.arkivanov.decompose.router.slot.childSlot
import com.arkivanov.decompose.router.slot.dismiss
import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.router.stack.active
import com.arkivanov.decompose.router.stack.childStack
import com.arkivanov.decompose.router.stack.pop
import com.arkivanov.decompose.router.stack.pushNew
import com.arkivanov.decompose.router.stack.replaceAll
import com.arkivanov.decompose.value.Value
import com.arkivanov.essenty.instancekeeper.InstanceKeeper
import com.arkivanov.essenty.instancekeeper.getOrCreate
import com.arkivanov.essenty.lifecycle.coroutines.coroutineScope
import com.arkivanov.essenty.lifecycle.doOnStart
import dev.sasikanth.rss.reader.about.AboutPresenterFactory
import dev.sasikanth.rss.reader.addfeed.AddFeedEvent
import dev.sasikanth.rss.reader.addfeed.AddFeedPresenterFactory
import dev.sasikanth.rss.reader.blockedwords.BlockedWordsPresenterFactory
import dev.sasikanth.rss.reader.bookmarks.BookmarksPresenterFactory
import dev.sasikanth.rss.reader.core.model.local.PostWithMetadata
import dev.sasikanth.rss.reader.data.repository.RssRepository
import dev.sasikanth.rss.reader.data.repository.SettingsRepository
import dev.sasikanth.rss.reader.di.scopes.ActivityScope
import dev.sasikanth.rss.reader.feed.FeedPresenterFactory
import dev.sasikanth.rss.reader.feeds.FeedsEvent
import dev.sasikanth.rss.reader.group.GroupEvent
import dev.sasikanth.rss.reader.group.GroupPresenterFactory
import dev.sasikanth.rss.reader.groupselection.GroupSelectionPresenterFactory
import dev.sasikanth.rss.reader.home.HomePresenterFactory
import dev.sasikanth.rss.reader.platform.LinkHandler
import dev.sasikanth.rss.reader.reader.ReaderPresenterFactory
import dev.sasikanth.rss.reader.refresh.LastUpdatedAt
import dev.sasikanth.rss.reader.search.SearchPresentFactory
import dev.sasikanth.rss.reader.settings.SettingsPresenterFactory
import dev.sasikanth.rss.reader.util.DispatchersProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import me.tatarka.inject.annotations.Inject

@Inject
@ActivityScope
class AppPresenter(
  componentContext: ComponentContext,
  private val dispatchersProvider: DispatchersProvider,
  private val homePresenter: HomePresenterFactory,
  private val searchPresenter: SearchPresentFactory,
  private val bookmarksPresenter: BookmarksPresenterFactory,
  private val settingsPresenter: SettingsPresenterFactory,
  private val aboutPresenter: AboutPresenterFactory,
  private val readerPresenter: ReaderPresenterFactory,
  private val feedPresenter: FeedPresenterFactory,
  private val groupSelectionPresenter: GroupSelectionPresenterFactory,
  private val addFeedPresenter: AddFeedPresenterFactory,
  private val groupPresenter: GroupPresenterFactory,
  private val blockedWordsPresenter: BlockedWordsPresenterFactory,
  private val lastUpdatedAt: LastUpdatedAt,
  private val rssRepository: RssRepository,
  private val settingsRepository: SettingsRepository,
  private val linkHandler: LinkHandler,
) : ComponentContext by componentContext {

  private val presenterInstance =
    instanceKeeper.getOrCreate {
      PresenterInstance(
        dispatchersProvider = dispatchersProvider,
        lastUpdatedAt = lastUpdatedAt,
        rssRepository = rssRepository,
        settingsRepository = settingsRepository,
      )
    }

  private val navigation = StackNavigation<Config>()
  private val modalNavigation = SlotNavigation<ModalConfig>()

  internal val screenStack: Value<ChildStack<*, Screen>> =
    childStack(
      source = navigation,
      serializer = Config.serializer(),
      initialConfiguration = Config.Placeholder,
      handleBackButton = true,
      childFactory = ::createScreen,
    )

  internal val modalStack: Value<ChildSlot<*, Modals>> =
    childSlot(
      source = modalNavigation,
      serializer = ModalConfig.serializer(),
      handleBackButton = true,
      childFactory = ::createModal,
    )

  internal val state = presenterInstance.state

  private val scope = coroutineScope(dispatchersProvider.main + SupervisorJob())

  init {
    lifecycle.doOnStart { presenterInstance.refreshFeedsIfExpired() }

    // Loading feed count to make sure all database maintainence operations
    // are finished and we can navigate to next screen
    scope.launch {
      withContext(dispatchersProvider.io) { rssRepository.numberOfFeeds().firstOrNull() }
      if (screenStack.active.instance is Screen.Placeholder) {
        navigation.replaceAll(Config.Home)
      }
    }
  }

  fun onBackClicked() {
    navigation.pop()
  }

  private fun createModal(modalConfig: ModalConfig, componentContext: ComponentContext): Modals =
    when (modalConfig) {
      is ModalConfig.FeedInfo -> {
        Modals.FeedInfo(
          presenter =
            feedPresenter(modalConfig.feedId, componentContext) { modalNavigation.dismiss() }
        )
      }
      ModalConfig.GroupSelection -> {
        Modals.GroupSelection(
          presenter =
            groupSelectionPresenter(
              componentContext,
              { selectedGroupIds ->
                modalNavigation.dismiss {
                  when (val activeInstance = screenStack.active.instance) {
                    is Screen.Home -> {
                      activeInstance.presenter.feedsPresenter.dispatch(
                        FeedsEvent.OnGroupsSelected(selectedGroupIds)
                      )
                    }
                    is Screen.AddFeed -> {
                      activeInstance.presenter.dispatch(
                        AddFeedEvent.OnGroupsSelected(selectedGroupIds)
                      )
                    }
                    is Screen.GroupDetails -> {
                      activeInstance.presenter.dispatch(
                        GroupEvent.OnGroupsSelected(selectedGroupIds)
                      )
                    }
                    else -> {
                      throw IllegalArgumentException("Unhandled active instance: $activeInstance")
                    }
                  }
                }
              },
              { modalNavigation.dismiss() }
            )
        )
      }
    }

  private fun createScreen(config: Config, componentContext: ComponentContext): Screen =
    when (config) {
      Config.Placeholder -> {
        Screen.Placeholder
      }
      Config.Home -> {
        Screen.Home(
          presenter =
            homePresenter(
              componentContext,
              { navigation.pushNew(Config.Search) },
              { navigation.pushNew(Config.Bookmarks) },
              { navigation.pushNew(Config.Settings) },
              { openPost(it) },
              { modalNavigation.activate(ModalConfig.GroupSelection) },
              { modalNavigation.activate(ModalConfig.FeedInfo(it)) },
              { navigation.pushNew(Config.AddFeed) },
              { navigation.pushNew(Config.GroupDetails(it)) }
            )
        )
      }
      Config.Search -> {
        Screen.Search(
          presenter = searchPresenter(componentContext, { navigation.pop() }, { openPost(it) })
        )
      }
      Config.Bookmarks -> {
        Screen.Bookmarks(
          presenter = bookmarksPresenter(componentContext, { navigation.pop() }, { openPost(it) })
        )
      }
      Config.Settings -> {
        Screen.Settings(
          presenter =
            settingsPresenter(
              componentContext,
              { navigation.pop() },
              { navigation.pushNew(Config.About) }
            )
        )
      }
      Config.About -> {
        Screen.About(presenter = aboutPresenter(componentContext) { navigation.pop() })
      }
      is Config.Reader -> {
        Screen.Reader(
          presenter = readerPresenter(config.postId, componentContext) { navigation.pop() }
        )
      }
      is Config.AddFeed -> {
        Screen.AddFeed(
          presenter =
            addFeedPresenter(
              componentContext,
              { navigation.pop() },
              { modalNavigation.activate(ModalConfig.GroupSelection) }
            )
        )
      }
      is Config.GroupDetails -> {
        Screen.GroupDetails(
          presenter =
            groupPresenter(
              config.groupId,
              componentContext,
              { navigation.pop() },
              { modalNavigation.activate(ModalConfig.GroupSelection) }
            )
        )
      }
      is Config.BlockedWords -> {
        Screen.BlockedWords(
          presenter = blockedWordsPresenter(componentContext, { navigation.pop() })
        )
      }
    }

  private fun openPost(post: PostWithMetadata) {
    scope.launch {
      val showReaderView =
        withContext(dispatchersProvider.io) { settingsRepository.showReaderView.first() }

      if (showReaderView) {
        navigation.pushNew(Config.Reader(post.id))
      } else {
        linkHandler.openLink(post.link)
        rssRepository.updatePostReadStatus(read = true, id = post.id)
      }
    }
  }

  private class PresenterInstance(
    dispatchersProvider: DispatchersProvider,
    private val lastUpdatedAt: LastUpdatedAt,
    private val rssRepository: RssRepository,
    private val settingsRepository: SettingsRepository,
  ) : InstanceKeeper.Instance {

    private val coroutineScope = CoroutineScope(SupervisorJob() + dispatchersProvider.main)
    private val _state = MutableStateFlow(AppState.DEFAULT)
    val state: StateFlow<AppState> =
      _state.stateIn(
        scope = coroutineScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = AppState.DEFAULT
      )

    init {
      combine(
          settingsRepository.appThemeMode,
          settingsRepository.showFeedFavIcon,
        ) { appThemeMode, showFeedFavIcon ->
          Pair(appThemeMode, showFeedFavIcon)
        }
        .onEach { (appThemeMode, showFeedFavIcon) ->
          _state.update { it.copy(appThemeMode = appThemeMode, showFeedFavIcon = showFeedFavIcon) }
        }
        .launchIn(coroutineScope)
    }

    fun refreshFeedsIfExpired() {
      coroutineScope.launch {
        if (lastUpdatedAt.hasExpired()) {
          rssRepository.updateFeeds()
          lastUpdatedAt.refresh()
        }
      }
    }

    override fun onDestroy() {
      coroutineScope.cancel()
    }
  }

  @Serializable
  sealed interface Config {

    @Serializable data object Placeholder : Config

    @Serializable data object Home : Config

    @Serializable data object Search : Config

    @Serializable data object Bookmarks : Config

    @Serializable data object Settings : Config

    @Serializable data object About : Config

    @Serializable data class Reader(val postId: String) : Config

    @Serializable data object AddFeed : Config

    @Serializable data class GroupDetails(val groupId: String) : Config

    @Serializable data object BlockedWords : Config
  }

  @Serializable
  sealed interface ModalConfig {
    @Serializable data class FeedInfo(val feedId: String) : ModalConfig

    @Serializable data object GroupSelection : ModalConfig
  }
}
