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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.intPreferencesKey
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
import androidx.glance.appwidget.provideContent
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.background
import androidx.glance.currentState
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
import androidx.glance.unit.ColorProvider
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

class TwineUnreadSmallWidget : GlanceAppWidget() {

  override val sizeMode: SizeMode = SizeMode.Single

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

      GlanceTheme {
        val prefs = currentState<Preferences>()
        val currentIndex = prefs[CurrentIndexKey] ?: 0

        WidgetContent(
          unreadPosts = unreadPosts,
          currentIndex = currentIndex,
          isSubscribed = isSubscribed,
        )
      }
    }
  }

  @Composable
  private fun WidgetContent(
    unreadPosts: List<WidgetPost>,
    currentIndex: Int,
    isSubscribed: Boolean?,
  ) {
    val context = LocalContext.current

    when (isSubscribed) {
      true,
      null -> {
        Box(
          modifier =
            GlanceModifier.fillMaxSize()
              .background(GlanceTheme.colors.widgetBackground)
              .cornerRadius(28.dp)
              .padding(4.dp)
        ) {
          if (unreadPosts.isEmpty()) {
            NoPostsSmall()
          } else {
            val safeIndex = if (currentIndex < unreadPosts.size) currentIndex else 0
            val post = unreadPosts[safeIndex]

            PostContent(
              post = post,
              index = safeIndex,
              count = unreadPosts.size,
              onClick = {
                val readerScreenArgs =
                  ReaderScreenArgs(
                    postIndex = safeIndex,
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
      false -> {
        RequireTwinePremium()
      }
    }
  }

  @Composable
  private fun PostContent(post: WidgetPost, index: Int, count: Int, onClick: () -> Unit) {
    val context = LocalContext.current
    val hasImage = !post.image.isNullOrBlank()
    var postImage by remember(post.image) { mutableStateOf<Bitmap?>(null) }
    LaunchedEffect(post.image) { postImage = context.getImage(url = post.image) }

    Box(modifier = GlanceModifier.fillMaxSize().clickable(onClick)) {
      Column(modifier = GlanceModifier.fillMaxSize()) {
        if (hasImage && postImage != null) {
          Image(
            provider = ImageProvider(postImage!!),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = GlanceModifier.fillMaxWidth().height(100.dp).cornerRadius(24.dp),
          )
          Spacer(GlanceModifier.height(8.dp))
          Text(
            text = post.title ?: "",
            style =
              TextStyle(
                color = GlanceTheme.colors.onSurface,
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium,
              ),
            maxLines = 2,
            modifier = GlanceModifier.padding(horizontal = 8.dp),
          )
        } else {
          Spacer(GlanceModifier.defaultWeight())
          Text(
            text = post.title ?: "",
            style =
              TextStyle(
                color = GlanceTheme.colors.onSurface,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
              ),
            maxLines = 4,
            modifier = GlanceModifier.padding(horizontal = 8.dp),
          )
        }

        Spacer(GlanceModifier.defaultWeight())

        Row(
          modifier = GlanceModifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp),
          verticalAlignment = Alignment.CenterVertically,
        ) {
          // Publisher Info
          Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = GlanceModifier.defaultWeight(),
          ) {
            Box(modifier = GlanceModifier.size(10.dp)) {
              var feedIcon by remember(post.feedIcon) { mutableStateOf<Bitmap?>(null) }
              LaunchedEffect(post.feedIcon) { feedIcon = context.getImage(url = post.feedIcon) }

              if (feedIcon != null) {
                Image(
                  provider = ImageProvider(feedIcon!!),
                  contentDescription = null,
                  modifier = GlanceModifier.fillMaxSize().cornerRadius(2.dp),
                )
              }
              Image(
                provider = ImageProvider(R.drawable.ic_unread_tick),
                contentDescription = null,
                modifier = GlanceModifier.size(6.dp).padding(start = 6.dp, top = 6.dp),
              )
            }
            Spacer(GlanceModifier.width(4.dp))
            Text(
              text = post.feedName ?: "",
              style =
                TextStyle(
                  color = GlanceTheme.colors.onSurface,
                  fontSize = 8.sp,
                  fontWeight = FontWeight.Medium,
                ),
              maxLines = 1,
            )
          }

          // Pagination Dots
          PaginationDots(index = index, count = count)
        }
      }

      // Next Button (Arrow)
      Box(
        modifier =
          GlanceModifier.padding(12.dp)
            .size(24.dp)
            .background(ColorProvider(Color(0xFFF0F0F0)))
            .cornerRadius(99.dp)
            .clickable(actionRunCallback<NextPostAction>()),
        contentAlignment = Alignment.Center,
      ) {
        Image(
          provider = ImageProvider(R.drawable.ic_chevron_right),
          contentDescription = null,
          modifier = GlanceModifier.size(12.dp),
        )
      }
    }
  }

  @Composable
  private fun PaginationDots(index: Int, count: Int) {
    Row(verticalAlignment = Alignment.CenterVertically) {
      for (i in 0 until minOf(count, 5)) {
        val isSelected = i == (index % 5)
        Box(
          modifier =
            GlanceModifier.size(4.dp)
              .padding(horizontal = 1.dp)
              .background(
                if (isSelected) GlanceTheme.colors.onSurface
                else GlanceTheme.colors.onSurfaceVariant
              )
              .cornerRadius(99.dp)
        ) {}
      }
    }
  }

  @Composable
  private fun NoPostsSmall() {
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
              fontSize = 12.sp,
              fontWeight = FontWeight.Medium,
            ),
        )
      }
    }
  }

  private suspend fun Context.getImage(url: String?): Bitmap? {
    if (url.isNullOrBlank()) return null
    val request =
      ImageRequest.Builder(this)
        .size(250)
        .data(url)
        .memoryCachePolicy(CachePolicy.ENABLED)
        .diskCachePolicy(CachePolicy.ENABLED)
        .build()

    return when (val result = imageLoader.execute(request)) {
      is ErrorResult -> null
      is SuccessResult -> result.image.toBitmap()
    }
  }

  companion object {
    private const val NUMBER_OF_UNREAD_POSTS_IN_WIDGET = 10
    val CurrentIndexKey = intPreferencesKey("current_index")
  }
}

class NextPostAction : ActionCallback {
  override suspend fun onAction(
    context: Context,
    glanceId: GlanceId,
    parameters: ActionParameters,
  ) {
    updateAppWidgetState(context, glanceId) { prefs ->
      val currentIndex = prefs[TwineUnreadSmallWidget.CurrentIndexKey] ?: 0
      prefs[TwineUnreadSmallWidget.CurrentIndexKey] = (currentIndex + 1) % 10
    }
    TwineUnreadSmallWidget().update(context, glanceId)
  }
}
