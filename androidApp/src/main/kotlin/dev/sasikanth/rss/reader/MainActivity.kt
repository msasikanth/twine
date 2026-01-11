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
package dev.sasikanth.rss.reader

import android.Manifest
import android.app.Activity
import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.WindowCompat
import dev.sasikanth.rss.reader.app.App
import dev.sasikanth.rss.reader.di.ApplicationComponent
import dev.sasikanth.rss.reader.di.scopes.ActivityScope
import dev.sasikanth.rss.reader.notifications.PermissionRequestBridge
import dev.sasikanth.rss.reader.platform.PlatformComponent
import dev.sasikanth.rss.reader.share.ShareComponent
import dev.sasikanth.rss.reader.utils.ExternalUriHandler
import io.github.vinceglb.filekit.core.FileKit
import me.tatarka.inject.annotations.Component
import me.tatarka.inject.annotations.Provides

class MainActivity : ComponentActivity() {

  private val permissionLauncher =
    registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
      val result =
        if (granted) {
          PermissionRequestBridge.PermissionResult.Granted
        } else {
          val shouldShowRationale =
            shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS)
          if (shouldShowRationale) {
            PermissionRequestBridge.PermissionResult.Denied
          } else {
            PermissionRequestBridge.PermissionResult.PermanentlyDenied
          }
        }
      currentPermissionDeferred?.complete(result)
    }

  private var currentPermissionDeferred:
    kotlinx.coroutines.CompletableDeferred<PermissionRequestBridge.PermissionResult>? =
    null

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    PermissionRequestBridge.register { deferred ->
      currentPermissionDeferred = deferred
      permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
    }

    FileKit.init(this)

    enableEdgeToEdge(
      statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
      navigationBarStyle = SystemBarStyle.dark(Color.TRANSPARENT)
    )

    intent.data?.let { ExternalUriHandler.onNewUri(it.toString()) }

    val activityComponent = ActivityComponent::class.create(activity = this)

    setContent {
      activityComponent.app(
        { useDarkTheme ->
          WindowCompat.getInsetsController(window, window.decorView).isAppearanceLightStatusBars =
            !useDarkTheme
          WindowCompat.getInsetsController(window, window.decorView)
            .isAppearanceLightNavigationBars = !useDarkTheme
        },
        { isLightStatusBar ->
          WindowCompat.getInsetsController(window, window.decorView).isAppearanceLightStatusBars =
            isLightStatusBar
        },
        { isLightNavBar ->
          WindowCompat.getInsetsController(window, window.decorView)
            .isAppearanceLightNavigationBars = isLightNavBar
        }
      )
    }
  }

  override fun onNewIntent(intent: android.content.Intent) {
    super.onNewIntent(intent)
    intent.data?.let { ExternalUriHandler.onNewUri(it.toString()) }
  }

  override fun onDestroy() {
    PermissionRequestBridge.unregister()
    super.onDestroy()
  }
}

fun ApplicationComponent.Companion.from(activity: Activity) =
  (activity.applicationContext as ReaderApplication).appComponent

@Component
@ActivityScope
abstract class ActivityComponent(
  @get:Provides val activity: ComponentActivity,
  @Component val applicationComponent: ApplicationComponent = ApplicationComponent.from(activity)
) : PlatformComponent, ShareComponent {

  abstract val app: App
}
