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
package dev.sasikanth.rss.reader.home.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.requiredWidthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.BlurredEdgeTreatment
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import dev.sasikanth.rss.reader.components.AsyncImage
import dev.sasikanth.rss.reader.database.PostWithMetadata

@Composable
internal fun FeaturedPostItem(item: PostWithMetadata, onClick: () -> Unit) {
  Column(modifier = Modifier.clip(MaterialTheme.shapes.extraLarge).clickable(onClick = onClick)) {
    Box {
      AsyncImage(
        url = item.imageUrl!!,
        modifier =
          Modifier.clip(MaterialTheme.shapes.extraLarge)
            .aspectRatio(1.77f)
            .background(MaterialTheme.colorScheme.surface),
        contentDescription = null,
        contentScale = ContentScale.Crop
      )

      PostMetadata(post = item, modifier = Modifier.align(Alignment.BottomStart))
    }

    Spacer(modifier = Modifier.height(16.dp))

    Text(
      modifier = Modifier.padding(horizontal = 16.dp),
      text = item.title,
      style = MaterialTheme.typography.headlineSmall,
      color = MaterialTheme.colorScheme.onSurface,
      maxLines = 2,
      overflow = TextOverflow.Ellipsis
    )

    if (item.description.isNotBlank()) {
      Spacer(modifier = Modifier.height(8.dp))

      Text(
        modifier = Modifier.padding(horizontal = 16.dp),
        text = item.description,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurface,
        maxLines = 3,
        overflow = TextOverflow.Ellipsis,
      )
    }

    Spacer(modifier = Modifier.height(16.dp))
  }
}

@Composable
internal fun FeaturedPostItemBackground(modifier: Modifier = Modifier, imageUrl: String?) {
  BoxWithConstraints(modifier = modifier) {
    AsyncImage(
      url = imageUrl!!,
      modifier = Modifier.aspectRatio(1.1f).blur(100.dp, BlurredEdgeTreatment.Unbounded),
      contentDescription = null,
      contentScale = ContentScale.Crop
    )

    Box(
      modifier =
        Modifier.matchParentSize()
          .background(
            brush =
              Brush.radialGradient(
                colors =
                  listOf(
                    Color.Black,
                    Color.Black.copy(alpha = 0.0f),
                    Color.Black.copy(alpha = 0.0f)
                  ),
                center = Offset(x = constraints.maxWidth.toFloat(), y = 40f)
              )
          )
    )

    Box(
      modifier =
        Modifier.matchParentSize()
          .background(
            brush =
              Brush.verticalGradient(
                colors = listOf(Color.Black, Color.Black.copy(alpha = 0.0f)),
              )
          )
    )
  }
}

@Composable
private fun PostMetadata(post: PostWithMetadata, modifier: Modifier = Modifier) {
  val feedName = post.feedName ?: "Unknown"

  Row(
    modifier =
      Modifier.padding(start = 12.dp, bottom = 12.dp)
        .background(color = Color.Black, shape = RoundedCornerShape(50))
        .padding(start = 8.dp, top = 8.dp, end = 16.dp, bottom = 8.dp)
        .then(modifier),
    horizontalArrangement = Arrangement.spacedBy(8.dp),
    verticalAlignment = Alignment.CenterVertically
  ) {
    post.feedIcon?.let { url ->
      AsyncImage(
        url = url,
        contentDescription = null,
        contentScale = ContentScale.Crop,
        modifier = Modifier.requiredSize(16.dp)
      )
    }

    Text(
      modifier = Modifier.requiredWidthIn(max = 64.dp),
      style = MaterialTheme.typography.bodySmall,
      maxLines = 1,
      text = feedName.uppercase(),
      color = MaterialTheme.colorScheme.onSurface,
      textAlign = TextAlign.Left
    )
  }
}
