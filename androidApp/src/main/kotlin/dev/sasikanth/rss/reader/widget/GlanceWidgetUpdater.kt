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

package dev.sasikanth.rss.reader.widget

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.updateAll
import dev.sasikanth.rss.reader.ReaderApplication
import kotlin.time.Clock
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

object GlanceWidgetUpdater {

  private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

  fun update(context: Context) {
    val applicationComponent = (context.applicationContext as ReaderApplication).appComponent
    val settingsRepository = applicationComponent.settingsRepository

    scope.launch {
      TwineUnreadWidget().updateAll(context)
      TwineUnreadSmallWidget().updateAll(context)
      TwineBookmarkWidget().updateAll(context)

      val lastUpdate = settingsRepository.lastWidgetPreviewUpdateTime.first()
      val now = Clock.System.now()
      if (lastUpdate == null || (now - lastUpdate).inWholeHours >= 1L) {
        updatePreviews(context)
        settingsRepository.updateLastWidgetPreviewUpdateTime(now)
      }
    }
  }

  @SuppressLint("CheckResult")
  suspend fun updatePreviews(context: Context) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM) {
      GlanceAppWidgetManager(context).apply {
        setWidgetPreviews(TwineWidgetReceiver::class)
        setWidgetPreviews(TwineUnreadSmallWidgetReceiver::class)
        setWidgetPreviews(TwineBookmarkWidgetReceiver::class)
      }
    }
  }
}
