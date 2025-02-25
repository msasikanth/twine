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

import android.app.Activity
import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.view.WindowCompat
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.defaultComponentContext
import dev.sasikanth.rss.reader.app.App
import dev.sasikanth.rss.reader.di.ApplicationComponent
import dev.sasikanth.rss.reader.di.scopes.ActivityScope
import dev.sasikanth.rss.reader.platform.PlatformComponent
import dev.sasikanth.rss.reader.share.ShareComponent
import io.github.vinceglb.filekit.core.FileKit
import me.tatarka.inject.annotations.Component
import me.tatarka.inject.annotations.Provides

class MainActivity : ComponentActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    FileKit.init(this)

    enableEdgeToEdge(
      statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
      navigationBarStyle = SystemBarStyle.dark(Color.TRANSPARENT)
    )

    val activityComponent = ActivityComponent::class.create(activity = this)

    setContent {
      activityComponent.app(
        { useDarkTheme ->
          WindowCompat.getInsetsController(window, window.decorView).isAppearanceLightStatusBars =
            useDarkTheme.not()
          WindowCompat.getInsetsController(window, window.decorView)
            .isAppearanceLightNavigationBars = false
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
}

fun ApplicationComponent.Companion.from(activity: Activity) =
  (activity.applicationContext as ReaderApplication).appComponent

@Component
@ActivityScope
abstract class ActivityComponent(
  @get:Provides val activity: ComponentActivity,
  @get:Provides val componentContext: ComponentContext = activity.defaultComponentContext(),
  @Component val applicationComponent: ApplicationComponent = ApplicationComponent.from(activity)
) : PlatformComponent, ShareComponent {

  abstract val app: App
}
