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
import com.arkivanov.essenty.parcelable.Parcelable
import com.arkivanov.essenty.parcelable.Parcelize
import dev.sasikanth.rss.reader.bookmarks.BookmarksPresenter
import dev.sasikanth.rss.reader.di.scopes.ActivityScope
import dev.sasikanth.rss.reader.home.HomePresenter
import dev.sasikanth.rss.reader.search.SearchPresenter
import me.tatarka.inject.annotations.Inject

@Inject
@ActivityScope
class AppPresenter(
  componentContext: ComponentContext,
  private val homePresenter: (ComponentContext, openSearch: () -> Unit) -> HomePresenter,
  private val searchPresenter:
    (
      ComponentContext,
      goBack: () -> Unit,
    ) -> SearchPresenter,
  private val bookmarksPresenter:
    (
      ComponentContext,
      goBack: () -> Unit,
    ) -> BookmarksPresenter
) : ComponentContext by componentContext {

  private val navigation = StackNavigation<Config>()

  internal val screenStack: Value<ChildStack<*, Screen>> =
    childStack(
      source = navigation,
      initialConfiguration = Config.Home,
      handleBackButton = true,
      childFactory = ::createScreen,
    )

  private fun createScreen(config: Config, componentContext: ComponentContext): Screen =
    when (config) {
      Config.Home -> {
        Screen.Home(presenter = homePresenter(componentContext) { navigation.push(Config.Search) })
      }
      Config.Search -> {
        Screen.Search(presenter = searchPresenter(componentContext) { navigation.pop() })
      }
      Config.Bookmarks -> {
        Screen.Bookmarks(presenter = bookmarksPresenter(componentContext) { navigation.pop() })
      }
    }

  sealed interface Config : Parcelable {
    @Parcelize object Home : Config

    @Parcelize object Search : Config

    @Parcelize object Bookmarks : Config
  }
}
