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

package dev.sasikanth.rss.reader.notifications

import kotlinx.coroutines.CompletableDeferred

object PermissionRequestBridge {
  private var requestAction: ((CompletableDeferred<PermissionResult>) -> Unit)? = null

  fun register(action: (CompletableDeferred<PermissionResult>) -> Unit) {
    requestAction = action
  }

  fun unregister() {
    requestAction = null
  }

  fun requestPermission(): CompletableDeferred<PermissionResult>? {
    val deferred = CompletableDeferred<PermissionResult>()
    val action = requestAction
    return if (action != null) {
      action(deferred)
      deferred
    } else {
      null
    }
  }

  sealed interface PermissionResult {
    data object Granted : PermissionResult

    data object Denied : PermissionResult

    data object PermanentlyDenied : PermissionResult
  }
}
