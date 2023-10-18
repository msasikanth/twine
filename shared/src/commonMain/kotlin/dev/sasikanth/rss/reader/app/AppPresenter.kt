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
import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.router.stack.childStack
import com.arkivanov.decompose.router.stack.pop
import com.arkivanov.decompose.router.stack.push
import com.arkivanov.decompose.value.Value
import com.arkivanov.essenty.instancekeeper.InstanceKeeper
import com.arkivanov.essenty.instancekeeper.getOrCreate
import com.arkivanov.essenty.lifecycle.doOnStart
import com.arkivanov.essenty.parcelable.Parcelable
import com.arkivanov.essenty.parcelable.Parcelize
import dev.sasikanth.rss.reader.bookmarks.BookmarksPresenter
import dev.sasikanth.rss.reader.di.scopes.ActivityScope
import dev.sasikanth.rss.reader.home.HomePresenter
import dev.sasikanth.rss.reader.refresh.LastUpdatedAt
import dev.sasikanth.rss.reader.repository.RssRepository
import dev.sasikanth.rss.reader.search.SearchPresenter
import dev.sasikanth.rss.reader.settings.SettingsPresenter
import dev.sasikanth.rss.reader.utils.DispatchersProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import me.tatarka.inject.annotations.Inject

private typealias HomePresenterFactory =
  (
    ComponentContext, openSearch: () -> Unit, openBookmarks: () -> Unit, openSettings: () -> Unit
  ) -> HomePresenter

private typealias SearchPresentFactory =
  (
    ComponentContext,
    goBack: () -> Unit,
  ) -> SearchPresenter

private typealias BookmarkPresenterFactory =
  (
    ComponentContext,
    goBack: () -> Unit,
  ) -> BookmarksPresenter

private typealias SettingsPresenterFactory =
  (
    ComponentContext,
    goBack: () -> Unit,
  ) -> SettingsPresenter

@Inject
@ActivityScope
class AppPresenter(
  componentContext: ComponentContext,
  dispatchersProvider: DispatchersProvider,
  private val homePresenter: HomePresenterFactory,
  private val searchPresenter: SearchPresentFactory,
  private val bookmarksPresenter: BookmarkPresenterFactory,
  private val settingsPresenter: SettingsPresenterFactory,
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

  internal val screenStack: Value<ChildStack<*, Screen>> =
    childStack(
      source = navigation,
      initialConfiguration = Config.Home,
      handleBackButton = true,
      childFactory = ::createScreen,
    )

  init {
    lifecycle.doOnStart { presenterInstance.refreshFeedsIfExpired() }
  }

  fun onBackClicked() {
    navigation.pop()
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
              { navigation.push(Config.Settings) }
            )
        )
      }
      Config.Search -> {
        Screen.Search(presenter = searchPresenter(componentContext) { navigation.pop() })
      }
      Config.Bookmarks -> {
        Screen.Bookmarks(presenter = bookmarksPresenter(componentContext) { navigation.pop() })
      }
      Config.Settings -> {
        Screen.Settings(presenter = settingsPresenter(componentContext) { navigation.pop() })
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

  sealed interface Config : Parcelable {
    @Parcelize object Home : Config

    @Parcelize object Search : Config

    @Parcelize object Bookmarks : Config

    @Parcelize object Settings : Config
  }
}
