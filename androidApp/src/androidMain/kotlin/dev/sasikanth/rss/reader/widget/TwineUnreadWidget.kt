import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.components.Scaffold
import androidx.glance.appwidget.lazy.LazyColumn
import androidx.glance.appwidget.lazy.items
import androidx.glance.appwidget.provideContent
import androidx.glance.layout.fillMaxSize
import androidx.glance.text.Text
import dev.sasikanth.rss.reader.ReaderApplication
import dev.sasikanth.rss.reader.data.repository.WidgetDataRepository

/*
 * Copyright 2025 Sasikanth Miriyampalli
 *
 * Licensed under the GPL, Version 3.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.gnu.org/licenses/gpl-3.0.en.html
 *
 */
class TwineUnreadWidget : GlanceAppWidget() {

  override val sizeMode: SizeMode = SizeMode.Single

  override suspend fun provideGlance(context: Context, id: GlanceId) {
    val applicationComponent = (context.applicationContext as ReaderApplication).appComponent
    val widgetDataRepository = applicationComponent.widgetDataRepository

    provideContent { GlanceTheme { WidgetContent(widgetDataRepository) } }
  }

  @Composable
  private fun WidgetContent(widgetDataRepository: WidgetDataRepository) {
    Scaffold(modifier = GlanceModifier.fillMaxSize()) {
      val unreadCount by widgetDataRepository.unreadPostsCount.collectAsState(initial = 0L)

      val unreadPosts by widgetDataRepository.unreadPosts.collectAsState(initial = emptyList())

      LazyColumn {
        item {
          if (unreadCount > 0L) {
            Text("Unread posts count: $unreadCount")
          } else {
            Text("No unread posts")
          }
        }

        items(unreadPosts) { post ->
          Text(
            text = post.title ?: "No title",
            maxLines = 2,
          )
        }
      }
    }
  }
}
