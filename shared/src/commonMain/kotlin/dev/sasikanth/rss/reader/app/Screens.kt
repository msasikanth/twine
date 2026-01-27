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

import dev.sasikanth.rss.reader.reader.ReaderScreenArgs
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

sealed interface Screen {

  @Serializable data object Placeholder : Screen

  @Serializable data object Onboarding : Screen

  @Serializable data object Main : Screen

  @Serializable data object Home : Screen

  @Serializable
  data class Reader(val readerScreenArgs: ReaderScreenArgs) : Screen {

    companion object {
      const val ROUTE = "twine://reader"
    }

    fun toRoute(): String {
      return "$ROUTE/${Json.encodeToString(readerScreenArgs)}"
    }
  }

  @Serializable data object Search : Screen

  @Serializable data object Bookmarks : Screen

  @Serializable data object Settings : Screen

  @Serializable data object About : Screen

  @Serializable
  data object AddFeed : Screen {

    const val ROUTE = "twine://add"
  }

  @Serializable data class FeedGroup(val groupId: String) : Screen

  @Serializable data object BlockedWords : Screen

  @Serializable data object FreshRssLogin : Screen

  @Serializable data object MinifluxLogin : Screen

  @Serializable data object Paywall : Screen

  data object None : Screen
}
