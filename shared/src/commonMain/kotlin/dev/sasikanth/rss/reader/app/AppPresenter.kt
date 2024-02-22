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
import com.arkivanov.decompose.router.stack.childStack
import com.arkivanov.decompose.router.stack.pop
import com.arkivanov.decompose.router.stack.push
import com.arkivanov.decompose.value.Value
import com.arkivanov.essenty.instancekeeper.InstanceKeeper
import com.arkivanov.essenty.instancekeeper.getOrCreate
import com.arkivanov.essenty.lifecycle.doOnStart
import dev.sasikanth.rss.reader.about.AboutPresenter
import dev.sasikanth.rss.reader.bookmarks.BookmarksPresenter
import dev.sasikanth.rss.reader.di.scopes.ActivityScope
import dev.sasikanth.rss.reader.feed.FeedPresenter
import dev.sasikanth.rss.reader.home.HomePresenter
import dev.sasikanth.rss.reader.reader.ReaderPresenter
import dev.sasikanth.rss.reader.refresh.LastUpdatedAt
import dev.sasikanth.rss.reader.repository.RssRepository
import dev.sasikanth.rss.reader.search.SearchPresenter
import dev.sasikanth.rss.reader.settings.SettingsPresenter
import dev.sasikanth.rss.reader.util.DispatchersProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import me.tatarka.inject.annotations.Inject

private typealias HomePresenterFactory =
  (
    ComponentContext,
    openSearch: () -> Unit,
    openBookmarks: () -> Unit,
    openSettings: () -> Unit,
    openPost: (String) -> Unit,
    openFeedInfo: (String) -> Unit,
  ) -> HomePresenter

private typealias SearchPresentFactory =
  (
    ComponentContext,
    goBack: () -> Unit,
    openPost: (String) -> Unit,
  ) -> SearchPresenter

private typealias BookmarkPresenterFactory =
  (
    ComponentContext,
    goBack: () -> Unit,
    openReaderView: (String) -> Unit,
  ) -> BookmarksPresenter

private typealias SettingsPresenterFactory =
  (
    ComponentContext,
    goBack: () -> Unit,
    openAbout: () -> Unit,
  ) -> SettingsPresenter

private typealias AboutPresenterFactory =
  (
    ComponentContext,
    goBack: () -> Unit,
  ) -> AboutPresenter

private typealias ReaderPresenterFactory =
  (
    postLink: String,
    ComponentContext,
    goBack: () -> Unit,
  ) -> ReaderPresenter

private typealias FeedPresenterFactory =
  (
    feedLink: String,
    ComponentContext,
    dismiss: () -> Unit,
  ) -> FeedPresenter

@Inject
@ActivityScope
class AppPresenter(
  componentContext: ComponentContext,
  dispatchersProvider: DispatchersProvider,
  private val homePresenter: HomePresenterFactory,
  private val searchPresenter: SearchPresentFactory,
  private val bookmarksPresenter: BookmarkPresenterFactory,
  private val settingsPresenter: SettingsPresenterFactory,
  private val aboutPresenter: AboutPresenterFactory,
  private val readerPresenter: ReaderPresenterFactory,
  private val feedPresenter: FeedPresenterFactory,
  private val lastUpdatedAt: LastUpdatedAt,
  private val rssRepository: RssRepository
) : ComponentContext by componentContext {

  private val presenterInstance =
    instanceKeeper.getOrCreate {
      PresenterInstance(
        dispatchersProvider = dispatchersProvider,
        lastUpdatedAt = lastUpdatedAt,
        rssRepository = rssRepository
      )
    }

  private val navigation = StackNavigation<Config>()
  private val modalNavigation = SlotNavigation<ModalConfig>()

  internal val screenStack: Value<ChildStack<*, Screen>> =
    childStack(
      source = navigation,
      serializer = Config.serializer(),
      initialConfiguration = Config.Home,
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

  init {
    lifecycle.doOnStart { presenterInstance.refreshFeedsIfExpired() }
  }

  fun onBackClicked() {
    navigation.pop()
  }

  private fun createModal(modalConfig: ModalConfig, componentContext: ComponentContext): Modals =
    when (modalConfig) {
      is ModalConfig.FeedInfo -> {
        Modals.FeedInfo(
          presenter =
            feedPresenter(modalConfig.feedLink, componentContext) { modalNavigation.dismiss() }
        )
      }
    }

  private fun createScreen(config: Config, componentContext: ComponentContext): Screen =
    when (config) {
      Config.Home -> {
        Screen.Home(
          presenter =
            homePresenter(
              componentContext,
              { navigation.push(Config.Search) },
              { navigation.push(Config.Bookmarks) },
              { navigation.push(Config.Settings) },
              { navigation.push(Config.Reader(it)) },
              { modalNavigation.activate(ModalConfig.FeedInfo(it)) }
            )
        )
      }
      Config.Search -> {
        Screen.Search(
          presenter =
            searchPresenter(
              componentContext,
              { navigation.pop() },
              { navigation.push(Config.Reader(it)) }
            )
        )
      }
      Config.Bookmarks -> {
        Screen.Bookmarks(
          presenter =
            bookmarksPresenter(
              componentContext,
              { navigation.pop() },
              { navigation.push(Config.Reader(it)) }
            )
        )
      }
      Config.Settings -> {
        Screen.Settings(
          presenter =
            settingsPresenter(
              componentContext,
              { navigation.pop() },
              { navigation.push(Config.About) }
            )
        )
      }
      Config.About -> {
        Screen.About(presenter = aboutPresenter(componentContext) { navigation.pop() })
      }
      is Config.Reader -> {
        Screen.Reader(
          presenter = readerPresenter(config.postLink, componentContext) { navigation.pop() }
        )
      }
    }

  private class PresenterInstance(
    dispatchersProvider: DispatchersProvider,
    private val lastUpdatedAt: LastUpdatedAt,
    private val rssRepository: RssRepository
  ) : InstanceKeeper.Instance {

    private val coroutineScope = CoroutineScope(SupervisorJob() + dispatchersProvider.main)

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

    @Serializable data object Home : Config

    @Serializable data object Search : Config

    @Serializable data object Bookmarks : Config

    @Serializable data object Settings : Config

    @Serializable data object About : Config

    @Serializable data class Reader(val postLink: String) : Config
  }

  @Serializable
  sealed interface ModalConfig {
    @Serializable data class FeedInfo(val feedLink: String) : ModalConfig
  }
}
