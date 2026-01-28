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

import dev.sasikanth.rss.reader.di.scopes.AppScope
import kotlin.coroutines.resume
import kotlinx.coroutines.suspendCancellableCoroutine
import me.tatarka.inject.annotations.Inject
import platform.Foundation.NSURL
import platform.UIKit.UIApplication
import platform.UIKit.UIApplicationOpenNotificationSettingsURLString
import platform.UserNotifications.UNAuthorizationOptionAlert
import platform.UserNotifications.UNAuthorizationOptionBadge
import platform.UserNotifications.UNAuthorizationOptionSound
import platform.UserNotifications.UNAuthorizationStatusAuthorized
import platform.UserNotifications.UNAuthorizationStatusDenied
import platform.UserNotifications.UNAuthorizationStatusProvisional
import platform.UserNotifications.UNMutableNotificationContent
import platform.UserNotifications.UNNotificationRequest
import platform.UserNotifications.UNNotificationSound
import platform.UserNotifications.UNUserNotificationCenter

@Inject
@AppScope
class IosNotifier : Notifier {

  override fun show(title: String, content: String, notificationId: Int) {
    val notificationContent =
      UNMutableNotificationContent().apply {
        setTitle(title)
        setBody(content)
        setSound(UNNotificationSound.defaultSound())
      }

    val request =
      UNNotificationRequest.requestWithIdentifier(
        identifier = notificationId.toString(),
        content = notificationContent,
        trigger = null
      )

    UNUserNotificationCenter.currentNotificationCenter().addNotificationRequest(request) { error ->
      if (error != null) {
        // no-op
      }
    }
  }

  override suspend fun requestPermission(): Boolean {
    return suspendCancellableCoroutine { continuation ->
      val container = UNUserNotificationCenter.currentNotificationCenter()
      container.getNotificationSettingsWithCompletionHandler { settings ->
        when (settings?.authorizationStatus) {
          UNAuthorizationStatusAuthorized,
          UNAuthorizationStatusProvisional -> {
            continuation.resume(true)
          }
          UNAuthorizationStatusDenied -> {
            openSettings()
            continuation.resume(false)
          }
          else -> {
            container.requestAuthorizationWithOptions(
              options =
                UNAuthorizationOptionAlert or
                  UNAuthorizationOptionBadge or
                  UNAuthorizationOptionSound
            ) { granted, _ ->
              continuation.resume(granted)
            }
          }
        }
      }
    }
  }

  override fun openSettings() {
    val url = NSURL(string = UIApplicationOpenNotificationSettingsURLString)
    if (UIApplication.sharedApplication.canOpenURL(url)) {
      UIApplication.sharedApplication.openURL(
        url = url,
        options = emptyMap<Any?, Any>(),
        completionHandler = null
      )
    }
  }
}
