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

package dev.sasikanth.rss.reader.utils

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

object ExternalUriHandler {

  val uri: StateFlow<String?>
    field = MutableStateFlow<String?>(null)

  private var cached: String? = null

  private val listeners = mutableSetOf<(String) -> Unit>()

  fun addListener(listener: (String) -> Unit) {
    listeners.add(listener)
    cached?.let {
      listener.invoke(it)
      cached = null
    }
  }

  fun removeListener(listener: (String) -> Unit) {
    listeners.remove(listener)
  }

  fun onNewUri(uri: String) {
    this.uri.value = uri
    cached = uri
    if (listeners.isNotEmpty()) {
      listeners.forEach { it.invoke(uri) }
      cached = null
    }
  }

  fun consume() {
    this.uri.value = null
  }
}
