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
import com.arkivanov.essenty.lifecycle.coroutines.coroutineScope
import com.arkivanov.essenty.lifecycle.doOnStart
import dev.sasikanth.rss.reader.about.AboutPresenterFactory
import dev.sasikanth.rss.reader.bookmarks.BookmarksPresenterFactory
import dev.sasikanth.rss.reader.core.model.local.PostWithMetadata
import dev.sasikanth.rss.reader.di.scopes.ActivityScope
import dev.sasikanth.rss.reader.feed.FeedPresenterFactory
import dev.sasikanth.rss.reader.home.HomePresenterFactory
import dev.sasikanth.rss.reader.platform.LinkHandler
import dev.sasikanth.rss.reader.reader.ReaderPresenterFactory
import dev.sasikanth.rss.reader.refresh.LastUpdatedAt
import dev.sasikanth.rss.reader.repository.RssRepository
import dev.sasikanth.rss.reader.repository.SettingsRepository
import dev.sasikanth.rss.reader.search.SearchPresentFactory
import dev.sasikanth.rss.reader.settings.SettingsPresenterFactory
import dev.sasikanth.rss.reader.util.DispatchersProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
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

  private val scope = coroutineScope(dispatchersProvider.main + SupervisorJob())

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
              { openPost(it) },
              { modalNavigation.activate(ModalConfig.FeedInfo(it)) }
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
              { navigation.push(Config.About) }
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
    }

  private fun openPost(post: PostWithMetadata) {
    scope.launch {
      val showReaderView =
        withContext(dispatchersProvider.io) { settingsRepository.showReaderView.first() }

      if (showReaderView) {
        navigation.push(Config.Reader(post.id))
      } else {
        linkHandler.openLink(post.link)
        rssRepository.updatePostReadStatus(read = true, id = post.id)
      }
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

    @Serializable data class Reader(val postId: String) : Config
  }

  @Serializable
  sealed interface ModalConfig {
    @Serializable data class FeedInfo(val feedLink: String) : ModalConfig
  }
}
