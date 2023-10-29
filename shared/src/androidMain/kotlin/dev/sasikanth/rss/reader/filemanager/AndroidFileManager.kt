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

package dev.sasikanth.rss.reader.filemanager

import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import dev.sasikanth.rss.reader.di.scopes.AppScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import me.tatarka.inject.annotations.Inject

@Inject
@AppScope
class AndroidFileManager(context: Context) : FileManager {

  private val application = context as Application
  private val result = Channel<String?>()

  private lateinit var createDocumentLauncher: ActivityResultLauncher<String>
  private lateinit var openDocumentLauncher: ActivityResultLauncher<Array<String>>

  private var content: String? = null

  override suspend fun save(name: String, content: String) {
    this.content = content

    if (!this.content.isNullOrBlank()) {
      createDocumentLauncher.launch(name)
    }
  }

  override suspend fun read(): String? {
    openDocumentLauncher.launch(arrayOf("application/xml", "text/xml", "text/x-opml"))
    return result.receiveAsFlow().first()
  }

  internal fun registerActivityWatcher() {
    val callback =
      object : ActivityLifecycleCallbacksAdapter() {
        val launcherIntent =
          Intent(Intent.ACTION_MAIN, null).apply { addCategory(Intent.CATEGORY_LAUNCHER) }
        val appList = application.packageManager.queryIntentActivities(launcherIntent, 0)

        override fun onActivityCreated(activity: Activity, bundle: Bundle?) {
          if (
            activity is ComponentActivity &&
              appList.any { it.activityInfo.name == activity::class.qualifiedName }
          ) {
            registerDocumentCreateActivityResult(activity)
            registerDocumentOpenActivityResult(activity)
          }
        }
      }
    application.registerActivityLifecycleCallbacks(callback)
  }

  private fun registerDocumentCreateActivityResult(activity: ComponentActivity) {
    createDocumentLauncher =
      activity.registerForActivityResult(
        ActivityResultContracts.CreateDocument("application/xml")
      ) { uri ->
        if (uri == null) return@registerForActivityResult

        val outputStream = application.contentResolver.openOutputStream(uri)
        outputStream?.use { it.write(content?.toByteArray()) }

        content = null
      }
  }

  private fun registerDocumentOpenActivityResult(activity: ComponentActivity) {
    openDocumentLauncher =
      activity.registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri == null) return@registerForActivityResult

        val inputStream = application.contentResolver.openInputStream(uri)
        inputStream?.use {
          val content = it.bufferedReader().readText()
          result.trySend(content)
        }
      }
  }
}

private open class ActivityLifecycleCallbacksAdapter : Application.ActivityLifecycleCallbacks {
  override fun onActivityCreated(activity: Activity, bundle: Bundle?) = Unit

  override fun onActivityStarted(activity: Activity) = Unit

  override fun onActivityResumed(activity: Activity) = Unit

  override fun onActivityPaused(activity: Activity) = Unit

  override fun onActivityStopped(activity: Activity) = Unit

  override fun onActivitySaveInstanceState(activity: Activity, bundle: Bundle) = Unit

  override fun onActivityDestroyed(activity: Activity) = Unit
}
