/*
 * Copyright 2026 Sasikanth Miriyampalli
 *
 * Licensed under the GPL, Version 3.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.gnu.org/licenses/gpl-3.0.en.html
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package dev.sasikanth.rss.reader.app

import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import kotlinx.coroutines.flow.MutableStateFlow

class AppNavigator(val backStack: NavBackStack<NavKey>) {
  val results = MutableStateFlow<Map<String, Any>>(emptyMap())

  fun setResult(key: String, value: Any) {
    results.value = results.value + (key to value)
  }

  fun consumeResult(key: String): Any? {
    val res = results.value[key]
    if (res != null) {
      results.value = results.value - key
    }
    return res
  }

  fun navigate(route: NavKey) {
    if (backStack.lastOrNull() == route) return

    // In split mode the list stays interactive beside the reader; opening another post
    // swaps the detail pane instead of stacking reader entries.
    if (route is Screen.Reader && backStack.lastOrNull() is Screen.Reader) {
      backStack.removeLastOrNull()
    }
    backStack.add(route)
  }

  fun goBack(): Boolean {
    if (backStack.size > 1) {
      backStack.removeLastOrNull()
      return true
    }
    return false
  }

  fun popUpTo(routeClass: kotlin.reflect.KClass<out NavKey>, inclusive: Boolean = false): Boolean {
    val index = backStack.indexOfLast { routeClass.isInstance(it) }
    if (index != -1) {
      val targetSize = if (inclusive) index else index + 1
      while (backStack.size > targetSize) {
        backStack.removeLast()
      }
      return true
    }
    return false
  }

  fun navigateToReader(readerScreenArgs: dev.sasikanth.rss.reader.reader.ReaderScreenArgs) {
    val route = Screen.Reader(readerScreenArgs)
    val mainIndex = backStack.indexOfLast { it is Screen.Main }
    if (mainIndex != -1) {
      // Pop to Main (exclusive)
      while (backStack.size > mainIndex + 1) {
        backStack.removeLast()
      }
    } else {
      // Clear and add Main
      backStack.clear()
      backStack.add(Screen.Main())
    }
    backStack.add(route)
  }

  fun navigateToMain(startTab: String? = null, triggerSync: Boolean = false) {
    backStack.clear()
    backStack.add(Screen.Main(triggerSync = triggerSync, startTab = startTab))
  }

  fun navigateToOnboarding() {
    backStack.clear()
    backStack.add(Screen.Onboarding)
  }
}
