import android.app.PendingIntent
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
import androidx.core.app.TaskStackBuilder
import androidx.core.net.toUri
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.ImageProvider
import androidx.glance.LocalContext
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetManager
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
import dev.sasikanth.rss.reader.billing.BillingHandler.SubscriptionResult.Subscribed
import dev.sasikanth.rss.reader.data.repository.WidgetDataRepository
import dev.sasikanth.rss.reader.reader.ReaderScreenArgs
import dev.sasikanth.rss.reader.widget.NoPosts
import dev.sasikanth.rss.reader.widget.RequireTwinePremium
import dev.sasikanth.rss.reader.widget.WidgetPostListItem
import kotlinx.coroutines.launch

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
    val imageLoader = applicationComponent.imageLoader
    val billingHandler = applicationComponent.billingHandler

    provideContent {
      val widgetId = GlanceAppWidgetManager(context).getAppWidgetId(id)

      setSingletonImageLoaderFactory { imageLoader }

      GlanceTheme {
        var isSubscribed: Boolean? by remember { mutableStateOf(null) }

        LaunchedEffect(Unit) { isSubscribed = billingHandler.customerResult() is Subscribed }

        when (isSubscribed) {
          true -> {
            WidgetContent(widgetDataRepository = widgetDataRepository, widgetId = widgetId)
          }
          false -> {
            RequireTwinePremium()
          }
          null -> {
            // no-op
          }
        }
      }
    }
  }

  @Composable
  private fun WidgetContent(
    widgetDataRepository: WidgetDataRepository,
    widgetId: Int,
  ) {
    val context = LocalContext.current
    val unreadCount by
      remember { widgetDataRepository.unreadPostsCount }.collectAsState(initial = 0L)

    Scaffold(
      modifier = GlanceModifier.fillMaxSize().cornerRadius(16.dp),
      titleBar = { TitleBar(unreadPostsCount = unreadCount, context = context) },
      horizontalPadding = 0.dp,
    ) {
      val unreadPosts by
        remember { widgetDataRepository.unreadPosts(NUMBER_OF_UNREAD_POSTS_IN_WIDGET) }
          .collectAsState(initial = emptyList())

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
                      fromScreen = ReaderScreenArgs.FromScreen.UnreadWidget
                    )
                  val uri = Screen.Reader(readerScreenArgs).toRoute().toUri()

                  val deepLinkIntent =
                    Intent(Intent.ACTION_VIEW, uri, context, MainActivity::class.java)

                  val deepLinkPendingIntent: PendingIntent? =
                    TaskStackBuilder.create(context).run {
                      addNextIntentWithParentStack(deepLinkIntent)
                      getPendingIntent(0, PendingIntent.FLAG_IMMUTABLE)
                    }
                  deepLinkPendingIntent?.send()
                }
              )
            }
          }

          if (unreadCount > NUMBER_OF_UNREAD_POSTS_IN_WIDGET) {
            item {
              Box(
                modifier = GlanceModifier.padding(vertical = 16.dp),
              ) {
                OutlineButton(
                  text = context.getString(R.string.widget_see_more),
                  contentColor = GlanceTheme.colors.primary,
                  onClick = {
                    val intent =
                      Intent(context, MainActivity::class.java).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                      }
                    context.startActivity(intent)
                  }
                )
              }
            }
          }
        }
      }
    }
  }

  @Composable
  private fun TitleBar(
    unreadPostsCount: Long,
    context: Context,
  ) {
    val coroutineScope = rememberCoroutineScope()
    Row(
      GlanceModifier.fillMaxWidth().padding(vertical = 8.dp),
      verticalAlignment = Alignment.Vertical.CenterVertically
    ) {
      val title =
        context.resources.getQuantityString(
          R.plurals.widget_unread_posts,
          unreadPostsCount.toInt(),
          unreadPostsCount
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
        modifier = GlanceModifier.defaultWeight().padding(start = 16.dp)
      )

      CircleIconButton(
        imageProvider = ImageProvider(R.drawable.ic_add),
        backgroundColor = GlanceTheme.colors.widgetBackground,
        contentDescription = context.getString(R.string.widget_unread_add),
        onClick = {
          val deepLinkIntent =
            Intent(
              Intent.ACTION_VIEW,
              Screen.AddFeed.ROUTE.toUri(),
              context,
              MainActivity::class.java
            )

          val deepLinkPendingIntent: PendingIntent? =
            TaskStackBuilder.create(context).run {
              addNextIntentWithParentStack(deepLinkIntent)
              getPendingIntent(0, PendingIntent.FLAG_IMMUTABLE)
            }
          deepLinkPendingIntent?.send()
        }
      )

      Spacer(GlanceModifier.width(4.dp))

      CircleIconButton(
        imageProvider = ImageProvider(R.drawable.ic_refresh),
        backgroundColor = GlanceTheme.colors.widgetBackground,
        contentDescription = context.getString(R.string.widget_unread_refresh),
        onClick = { coroutineScope.launch { this@TwineUnreadWidget.updateAll(context) } }
      )

      Spacer(GlanceModifier.width(16.dp))
    }
  }
}

private const val NUMBER_OF_UNREAD_POSTS_IN_WIDGET = 15
