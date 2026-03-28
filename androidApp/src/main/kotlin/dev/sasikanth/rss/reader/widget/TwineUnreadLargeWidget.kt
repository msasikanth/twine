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
import android.graphics.Bitmap
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import androidx.glance.ColorFilter
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalContext
import androidx.glance.action.ActionParameters
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.lazy.LazyColumn
import androidx.glance.appwidget.lazy.itemsIndexed
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.ContentScale
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.state.GlanceStateDefinition
import androidx.glance.state.PreferencesGlanceStateDefinition
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import coil3.compose.setSingletonImageLoaderFactory
import coil3.imageLoader
import coil3.request.CachePolicy
import coil3.request.ErrorResult
import coil3.request.ImageRequest
import coil3.request.SuccessResult
import coil3.toBitmap
import dev.sasikanth.rss.reader.MainActivity
import dev.sasikanth.rss.reader.R
import dev.sasikanth.rss.reader.ReaderApplication
import dev.sasikanth.rss.reader.app.Screen
import dev.sasikanth.rss.reader.core.model.local.WidgetPost
import dev.sasikanth.rss.reader.reader.ReaderScreenArgs
import kotlin.time.Clock
import kotlin.time.Instant
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class TwineUnreadLargeWidget : GlanceAppWidget() {

  override val sizeMode: SizeMode = SizeMode.Exact

  override val stateDefinition: GlanceStateDefinition<*> = PreferencesGlanceStateDefinition

  override suspend fun provideGlance(context: Context, id: GlanceId) {
    val applicationComponent = (context.applicationContext as ReaderApplication).appComponent
    val widgetDataRepository = applicationComponent.widgetDataRepository
    val imageLoader = applicationComponent.imageLoader
    val billingHandler = applicationComponent.billingHandler

    provideContent {
      val unreadPosts by
        remember { widgetDataRepository.unreadPosts(NUMBER_OF_UNREAD_POSTS_IN_WIDGET) }
          .collectAsState(initial = emptyList())

      var isSubscribed: Boolean? by remember { mutableStateOf(null) }
      LaunchedEffect(Unit) { isSubscribed = billingHandler.isSubscribed() }

      setSingletonImageLoaderFactory { imageLoader }

      GlanceTheme { WidgetContent(unreadPosts = unreadPosts, isSubscribed = isSubscribed) }
    }
  }

  override suspend fun providePreview(context: Context, widgetCategory: Int) {
    val applicationComponent = (context.applicationContext as ReaderApplication).appComponent
    val imageLoader = applicationComponent.imageLoader

    provideContent {
      setSingletonImageLoaderFactory { imageLoader }

      GlanceTheme { WidgetContent(unreadPosts = WidgetMockData.posts, isSubscribed = true) }
    }
  }

  @Composable
  private fun WidgetContent(unreadPosts: List<WidgetPost>, isSubscribed: Boolean?) {
    val context = LocalContext.current

    when (isSubscribed) {
      true,
      null -> {
        Box(
          modifier =
            GlanceModifier.fillMaxSize()
              .background(GlanceTheme.colors.widgetBackground)
              .appWidgetInnerCornerRadius(28.dp)
              .padding(4.dp)
        ) {
          if (unreadPosts.isEmpty()) {
            NoPostsLarge()
          } else {
            Column(modifier = GlanceModifier.fillMaxSize()) {
              Header()

              LazyColumn {
                itemsIndexed(unreadPosts) { index, post ->
                  PostItem(
                    post = post,
                    index = index,
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
  private fun Header() {
    val context = LocalContext.current
    Row(
      modifier =
        GlanceModifier.fillMaxWidth().padding(start = 16.dp, end = 8.dp, top = 8.dp, bottom = 8.dp),
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Text(
        text = context.getString(R.string.widget_latest),
        style =
          TextStyle(
            color = GlanceTheme.colors.onSurface,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
          ),
      )

      Spacer(GlanceModifier.defaultWeight())

      Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
          modifier =
            GlanceModifier.cornerRadius(99.dp)
              .size(32.dp)
              .clickable(actionRunCallback<LargeRefreshAction>()),
          contentAlignment = Alignment.Center,
        ) {
          Box(
            modifier =
              GlanceModifier.size(32.dp).background(GlanceTheme.colors.surface).cornerRadius(99.dp),
            contentAlignment = Alignment.Center,
          ) {
            Image(
              provider = ImageProvider(R.drawable.ic_refresh),
              contentDescription = null,
              colorFilter = ColorFilter.tint(GlanceTheme.colors.onSurface),
              modifier = GlanceModifier.size(16.dp),
            )
          }
        }

        Spacer(GlanceModifier.width(8.dp))

        Box(
          modifier =
            GlanceModifier.cornerRadius(99.dp).size(32.dp).clickable {
              val deepLinkIntent =
                Intent(
                    Intent.ACTION_VIEW,
                    "twine://bookmarks".toUri(),
                    context,
                    MainActivity::class.java,
                  )
                  .apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) }
              context.startActivity(deepLinkIntent)
            },
          contentAlignment = Alignment.Center,
        ) {
          Box(
            modifier =
              GlanceModifier.size(32.dp).background(GlanceTheme.colors.surface).cornerRadius(99.dp),
            contentAlignment = Alignment.Center,
          ) {
            Image(
              provider = ImageProvider(R.drawable.ic_bookmark),
              contentDescription = null,
              colorFilter = ColorFilter.tint(GlanceTheme.colors.onSurface),
              modifier = GlanceModifier.size(16.dp),
            )
          }
        }
      }
    }
  }

  @Composable
  private fun PostItem(post: WidgetPost, index: Int, onClick: () -> Unit) {
    val context = LocalContext.current
    Row(
      modifier =
        GlanceModifier.fillMaxWidth()
          .padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 8.dp)
          .clickable(onClick),
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Column(modifier = GlanceModifier.defaultWeight()) {
        Text(
          text = post.title ?: "",
          style =
            TextStyle(
              color = GlanceTheme.colors.onSurface,
              fontSize = 12.sp,
              fontWeight = FontWeight.Medium,
            ),
          maxLines = 2,
          modifier = GlanceModifier.fillMaxWidth().height(32.dp),
        )
        Spacer(GlanceModifier.height(8.dp))
        PublisherInfo(post = post)
      }

      Spacer(GlanceModifier.width(12.dp))

      val hasImage = !post.image.isNullOrBlank()
      if (hasImage) {
        PostImage(url = post.image)
      } else {
        Box(modifier = GlanceModifier.size(56.dp)) {}
      }
    }
  }

  @Composable
  private fun PostImage(url: String?) {
    val context = LocalContext.current
    var postImage by remember(url) { mutableStateOf<Bitmap?>(null) }
    LaunchedEffect(url) { postImage = context.getImage(url = url) }

    if (postImage != null) {
      Image(
        provider = ImageProvider(postImage!!),
        contentDescription = null,
        contentScale = ContentScale.Crop,
        modifier = GlanceModifier.size(56.dp).cornerRadius(14.dp),
      )
    }
  }

  @Composable
  private fun PublisherInfo(post: WidgetPost) {
    val context = LocalContext.current
    Row(verticalAlignment = Alignment.CenterVertically) {
      // Feed Icon
      Box(modifier = GlanceModifier.size(12.dp)) {
        var feedIcon by remember(post.feedIcon) { mutableStateOf<Bitmap?>(null) }
        LaunchedEffect(post.feedIcon) { feedIcon = context.getImage(url = post.feedIcon) }

        if (feedIcon != null) {
          Image(
            provider = ImageProvider(feedIcon!!),
            contentDescription = null,
            modifier = GlanceModifier.fillMaxSize().cornerRadius(2.dp),
          )
        } else {
          Image(
            provider = ImageProvider(R.drawable.ic_newsstand),
            contentDescription = null,
            colorFilter = ColorFilter.tint(GlanceTheme.colors.onSurface),
            modifier = GlanceModifier.fillMaxSize(),
          )
        }
      }
      Spacer(GlanceModifier.width(4.dp))
      Text(
        text = post.feedName ?: "",
        style =
          TextStyle(
            color = GlanceTheme.colors.onSurface,
            fontSize = 10.sp,
            fontWeight = FontWeight.Medium,
          ),
        maxLines = 1,
      )

      val relativeTime = post.postedOn.formatRelativeTime(context)
      if (relativeTime.isNotBlank()) {
        Text(
          text = " \u2022 $relativeTime",
          style =
            TextStyle(
              color = GlanceTheme.colors.onSurfaceVariant,
              fontSize = 10.sp,
              fontWeight = FontWeight.Normal,
            ),
          maxLines = 1,
        )
      }

      if (post.readingTimeEstimate > 0) {
        Text(
          text =
            " \u2022 ${context.getString(R.string.unit_minutes, post.readingTimeEstimate)} read",
          style =
            TextStyle(
              color = GlanceTheme.colors.onSurfaceVariant,
              fontSize = 10.sp,
              fontWeight = FontWeight.Normal,
            ),
          maxLines = 1,
        )
      }
    }
  }

  @Composable
  private fun NoPostsLarge() {
    Box(modifier = GlanceModifier.fillMaxSize(), contentAlignment = Alignment.Center) {
      Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Image(
          provider = ImageProvider(R.drawable.ic_newsstand),
          contentDescription = null,
          modifier = GlanceModifier.size(48.dp),
        )
        Spacer(GlanceModifier.height(8.dp))
        Text(
          text = LocalContext.current.getString(R.string.widget_no_posts),
          style =
            TextStyle(
              color = GlanceTheme.colors.onSurface,
              fontSize = 14.sp,
              fontWeight = FontWeight.Medium,
            ),
        )
      }
    }
  }

  private fun Instant.formatRelativeTime(context: Context): String {
    val now = Clock.System.now()
    val duration = now - this
    val seconds = duration.inWholeSeconds
    val days = duration.inWholeDays

    return when {
      seconds < 60 -> context.getString(R.string.unit_seconds)
      seconds < 3600 -> context.getString(R.string.unit_minutes, duration.inWholeMinutes.toInt())
      seconds < 86400 -> context.getString(R.string.unit_hours, duration.inWholeHours.toInt())
      days < 7 -> context.getString(R.string.unit_days, days.toInt())
      else -> ""
    }
  }

  private suspend fun Context.getImage(url: String?): Bitmap? {
    if (url.isNullOrBlank()) return null

    val request =
      ImageRequest.Builder(this)
        .size(200)
        .data(url)
        .memoryCachePolicy(CachePolicy.ENABLED)
        .diskCachePolicy(CachePolicy.ENABLED)
        .build()

    return withContext(Dispatchers.IO) {
      when (val result = imageLoader.execute(request)) {
        is ErrorResult -> null
        is SuccessResult -> result.image.toBitmap()
      }
    }
  }

  companion object {
    private const val NUMBER_OF_UNREAD_POSTS_IN_WIDGET = 15
  }
}

class LargeRefreshAction : ActionCallback {
  override suspend fun onAction(
    context: Context,
    glanceId: GlanceId,
    parameters: ActionParameters,
  ) {
    val applicationComponent = (context.applicationContext as ReaderApplication).appComponent
    applicationComponent.syncCoordinator.triggerPull()
  }
}
