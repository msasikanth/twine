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

import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.LocalContext
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.fillMaxSize
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import dev.sasikanth.rss.reader.MainActivity
import dev.sasikanth.rss.reader.R
import dev.sasikanth.rss.reader.ReaderApplication
import dev.sasikanth.rss.reader.data.repository.WidgetDataRepository

class TwineUnreadCountWidget : GlanceAppWidget() {

  override suspend fun provideGlance(context: Context, id: GlanceId) {
    val applicationComponent = (context.applicationContext as ReaderApplication).appComponent
    val widgetDataRepository = applicationComponent.widgetDataRepository

    provideContent { GlanceTheme { WidgetContent(widgetDataRepository = widgetDataRepository) } }
  }

  @Composable
  private fun WidgetContent(widgetDataRepository: WidgetDataRepository) {
    val context = LocalContext.current
    val unreadCount by
      remember { widgetDataRepository.unreadPostsCount }.collectAsState(initial = 1L)

    Box(
      modifier =
        GlanceModifier.fillMaxSize()
          .background(GlanceTheme.colors.widgetBackground)
          .cornerRadius(16.dp)
          .clickable {
            val intent =
              Intent(context, MainActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
              }
            context.startActivity(intent)
          },
      contentAlignment = Alignment.Center,
    ) {
      Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
          text = unreadCount.toString(),
          style =
            TextStyle(
              color = GlanceTheme.colors.primary,
              fontWeight = FontWeight.Bold,
              fontSize = 32.sp,
            ),
        )
        Text(
          text = context.getString(R.string.widget_unread_count_title),
          style =
            TextStyle(
              color = GlanceTheme.colors.onSurfaceVariant,
              fontWeight = FontWeight.Medium,
              fontSize = 12.sp,
            ),
        )
      }
    }
  }
}
