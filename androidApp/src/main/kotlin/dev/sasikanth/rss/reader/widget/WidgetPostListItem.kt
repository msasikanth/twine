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
import android.graphics.Bitmap
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalContext
import androidx.glance.action.clickable
import androidx.glance.appwidget.cornerRadius
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.ContentScale
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import coil3.imageLoader
import coil3.request.CachePolicy
import coil3.request.ErrorResult
import coil3.request.ImageRequest
import coil3.request.SuccessResult
import coil3.toBitmap
import dev.sasikanth.rss.reader.R
import dev.sasikanth.rss.reader.core.model.local.WidgetPost
import dev.sasikanth.rss.reader.util.relativeDurationString

@Composable
fun WidgetPostListItem(
  post: WidgetPost,
  onClick: () -> Unit,
  modifier: GlanceModifier = GlanceModifier,
  showDivider: Boolean = true,
) {
  Box(
    modifier =
      GlanceModifier.fillMaxWidth()
        .clickable { onClick() }
        .padding(horizontal = 16.dp)
        .then(modifier),
    contentAlignment = Alignment.BottomStart,
  ) {
    val context = LocalContext.current
    Column(modifier = GlanceModifier.padding(vertical = 12.dp)) {
      Row(
        modifier = GlanceModifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Column(modifier = GlanceModifier.defaultWeight()) {
          Text(
            text = post.title ?: context.getString(R.string.widget_no_title),
            maxLines = 2,
            style =
              TextStyle(
                fontSize = 14.sp,
                color = GlanceTheme.colors.onSurface,
              )
          )

          Spacer(GlanceModifier.height(4.dp))

          Text(
            text = post.description ?: context.getString(R.string.widget_no_title),
            maxLines = 3,
            style =
              TextStyle(
                fontSize = 12.sp,
                color = GlanceTheme.colors.onSurfaceVariant,
              )
          )

          Spacer(GlanceModifier.height(12.dp))
        }

        var postImage by remember(post.image) { mutableStateOf<Bitmap?>(null) }

        LaunchedEffect(post.image) { postImage = context.getImage(url = post.image) }

        if (postImage != null) {
          Spacer(GlanceModifier.width(16.dp))

          Image(
            provider = ImageProvider(postImage!!),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = GlanceModifier.width(80.dp).height(80.dp).cornerRadius(16.dp)
          )
        }
      }

      Row(
        modifier = GlanceModifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
      ) {
        var feedImage by remember(post.image) { mutableStateOf<Bitmap?>(null) }

        LaunchedEffect(post.image) { feedImage = context.getImage(url = post.feedIcon) }

        if (feedImage != null) {
          Image(
            provider = ImageProvider(feedImage!!),
            contentDescription = null,
            contentScale = ContentScale.Fit,
            modifier = GlanceModifier.size(16.dp).cornerRadius(4.dp)
          )

          Spacer(GlanceModifier.width(8.dp))
        } else {
          // TODO: Placeholder icon?
        }

        Text(
          modifier = GlanceModifier.defaultWeight(),
          text = post.feedName.orEmpty() + " \u2022 " + post.postedOn.relativeDurationString(),
          maxLines = 1,
          style =
            TextStyle(
              fontSize = 12.sp,
              color = GlanceTheme.colors.onSurfaceVariant,
            )
        )
      }
    }

    if (showDivider) {
      Box(
        modifier =
          GlanceModifier.fillMaxWidth().height(1.dp).background(Color.Black.copy(alpha = 0.16f))
      ) {}
    }
  }
}

private suspend fun Context.getImage(url: String?, force: Boolean = false): Bitmap? {
  if (url.isNullOrBlank()) return null

  val request =
    ImageRequest.Builder(this)
      .size(250)
      .data(url)
      .apply {
        if (force) {
          memoryCachePolicy(CachePolicy.DISABLED)
          diskCachePolicy(CachePolicy.DISABLED)
        }
      }
      .build()

  return when (val result = imageLoader.execute(request)) {
    is ErrorResult -> null
    is SuccessResult -> result.image.toBitmap()
  }
}
