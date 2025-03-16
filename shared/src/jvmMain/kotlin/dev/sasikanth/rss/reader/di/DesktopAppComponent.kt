/*
 * Copyright 2025 Sasikanth Miriyampalli
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

package dev.sasikanth.rss.reader.di

import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.essenty.backhandler.BackDispatcher
import dev.datlag.kcef.KCEF
import dev.datlag.kcef.KCEFBuilder
import dev.sasikanth.rss.reader.app.App
import dev.sasikanth.rss.reader.di.scopes.ActivityScope
import dev.sasikanth.rss.reader.platform.PlatformComponent
import dev.sasikanth.rss.reader.resources.strings.LocalStrings
import dev.sasikanth.rss.reader.share.ShareComponent
import dev.sasikanth.rss.reader.util.DispatchersProvider
import kotlinx.coroutines.withContext
import me.tatarka.inject.annotations.Component
import me.tatarka.inject.annotations.Inject
import me.tatarka.inject.annotations.Provides
import java.io.File

typealias DesktopApp = () -> Unit

@Inject
fun DesktopApp(
  app: App,
  dispatchersProvider: DispatchersProvider,
) {
  return application {
    Window(
      onCloseRequest = ::exitApplication,
      title = LocalStrings.current.appName,
      state = rememberWindowState(width = 550.dp, height = 750.dp),
    ) {
      var restartRequired by remember { mutableStateOf(false) }
      var downloading by remember { mutableStateOf(0F) }
      var initialized by remember { mutableStateOf(false) }
      val appConfigPath = File(System.getProperty("user.home"), ".twine")

      LaunchedEffect(Unit) {
        withContext(dispatchersProvider.io) {
          KCEF.init(builder = {
            installDir(appConfigPath.resolve("kcef-bundle"))

            progress {
              onInitialized {
                initialized = true
                println("Webview initialised")
              }
              onDownloading {
                downloading = it
                println("Downloading: $it")
              }
            }

            download {
              github {
                release("jbr-release-17.0.12b1207.37")
              }
            }
          },
            onError = {
              it?.printStackTrace()
            },
            onRestartRequired = {
              restartRequired = true
            })
        }
      }

      DisposableEffect(Unit) {
        onDispose {
          KCEF.disposeBlocking()
        }
      }

      app(
        {
          // no-op
        },
        {
          // no-op
        },
        {
          // no-op
        }
      )
    }
  }
}

@Component
@ActivityScope
abstract class DesktopAppComponent(
  @get:Provides val componentContext: ComponentContext,
  @Component val applicationComponent: ApplicationComponent,
) : PlatformComponent, ShareComponent {

  abstract val desktopApp: DesktopApp
}
