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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.ImageProvider
import androidx.glance.LocalContext
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.components.CircleIconButton
import androidx.glance.appwidget.components.OutlineButton
import androidx.glance.appwidget.components.Scaffold
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.lazy.LazyColumn
import androidx.glance.appwidget.lazy.itemsIndexed
import androidx.glance.appwidget.provideContent
import androidx.glance.appwidget.updateAll
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.padding
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import coil3.compose.setSingletonImageLoaderFactory
import dev.sasikanth.rss.reader.MainActivity
import dev.sasikanth.rss.reader.R
import dev.sasikanth.rss.reader.ReaderApplication
import dev.sasikanth.rss.reader.app.Screen
import dev.sasikanth.rss.reader.core.model.local.WidgetPost
import dev.sasikanth.rss.reader.reader.ReaderScreenArgs
import kotlinx.coroutines.launch

class TwineUnreadWidget : GlanceAppWidget() {

  override val sizeMode: SizeMode = SizeMode.Single

  override suspend fun provideGlance(context: Context, id: GlanceId) {
    val applicationComponent = (context.applicationContext as ReaderApplication).appComponent
    val widgetDataRepository = applicationComponent.widgetDataRepository
    val imageLoader = applicationComponent.imageLoader
    val billingHandler = applicationComponent.billingHandler

    provideContent {
      val unreadCount by
        remember { widgetDataRepository.unreadPostsCount }.collectAsState(initial = 1L)
      val unreadPosts by
        remember { widgetDataRepository.unreadPosts(NUMBER_OF_UNREAD_POSTS_IN_WIDGET) }
          .collectAsState(initial = emptyList())

      var isSubscribed: Boolean? by remember { mutableStateOf(null) }
      LaunchedEffect(Unit) { isSubscribed = billingHandler.isSubscribed() }

      setSingletonImageLoaderFactory { imageLoader }

      GlanceTheme {
        WidgetContent(
          unreadCount = unreadCount,
          unreadPosts = unreadPosts,
          isSubscribed = isSubscribed,
        )
      }
    }
  }

  override suspend fun providePreview(context: Context, widgetCategory: Int) {
    val applicationComponent = (context.applicationContext as ReaderApplication).appComponent
    val imageLoader = applicationComponent.imageLoader

    provideContent {
      setSingletonImageLoaderFactory { imageLoader }

      GlanceTheme {
        WidgetContent(
          unreadCount = WidgetMockData.posts.size.toLong(),
          unreadPosts = WidgetMockData.posts,
          isSubscribed = true,
        )
      }
    }
  }

  @Composable
  private fun WidgetContent(
    unreadCount: Long,
    unreadPosts: List<WidgetPost>,
    isSubscribed: Boolean?,
  ) {
    val context = LocalContext.current

    when (isSubscribed) {
      true,
      null -> {
        Scaffold(
          modifier = GlanceModifier.fillMaxSize().cornerRadius(16.dp),
          titleBar = { TitleBar(unreadPostsCount = unreadCount, context = context) },
          horizontalPadding = 0.dp,
        ) {
          if (unreadPosts.isEmpty()) {
            NoPosts()
          } else {
            LazyColumn(horizontalAlignment = Alignment.CenterHorizontally) {
              itemsIndexed(unreadPosts) { index, post ->
                Column {
                  WidgetPostListItem(
                    post = post,
                    showDivider = index < unreadPosts.lastIndex,
                    onClick = {
                      val readerScreenArgs =
                        ReaderScreenArgs(
                          postIndex = index,
                          postId = post.id,
                          fromScreen = ReaderScreenArgs.FromScreen.UnreadWidget,
                        )
                      val uri = Screen.Reader(readerScreenArgs).toRoute().toUri()

                      val deepLinkIntent =
                        Intent(Intent.ACTION_VIEW, uri, context, MainActivity::class.java).apply {
                          addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        }
                      context.startActivity(deepLinkIntent)
                    },
                  )
                }
              }

              if (unreadCount > NUMBER_OF_UNREAD_POSTS_IN_WIDGET) {
                item {
                  Box(modifier = GlanceModifier.padding(vertical = 16.dp)) {
                    OutlineButton(
                      text = context.getString(R.string.widget_see_more),
                      contentColor = GlanceTheme.colors.primary,
                      onClick = {
                        val intent =
                          Intent(context, MainActivity::class.java).apply {
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                          }
                        context.startActivity(intent)
                      },
                    )
                  }
                }
              }
            }
          }
        }
      }
      false -> {
        RequireTwinePremium()
      }
    }
  }

  @Composable
  private fun TitleBar(unreadPostsCount: Long, context: Context) {
    val coroutineScope = rememberCoroutineScope()
    Row(
      GlanceModifier.fillMaxWidth().padding(vertical = 8.dp),
      verticalAlignment = Alignment.Vertical.CenterVertically,
    ) {
      val title =
        context.resources.getQuantityString(
          R.plurals.widget_unread_posts,
          unreadPostsCount.toInt(),
          unreadPostsCount,
        )
      Text(
        text = title,
        style =
          TextStyle(
            color = GlanceTheme.colors.onSurface,
            fontWeight = FontWeight.Medium,
            fontSize = 16.sp,
          ),
        maxLines = 1,
        modifier = GlanceModifier.defaultWeight().padding(start = 16.dp),
      )

      CircleIconButton(
        imageProvider = ImageProvider(R.drawable.ic_refresh),
        backgroundColor = GlanceTheme.colors.widgetBackground,
        contentDescription = context.getString(R.string.widget_unread_refresh),
        onClick = { coroutineScope.launch { this@TwineUnreadWidget.updateAll(context) } },
      )

      Spacer(GlanceModifier.width(16.dp))
    }
  }

  companion object {
    private const val NUMBER_OF_UNREAD_POSTS_IN_WIDGET = 15
  }
}
