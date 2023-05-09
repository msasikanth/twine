package dev.sasikanth.rss.reader.home.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.BlurredEdgeTreatment
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import dev.sasikanth.rss.reader.components.AsyncImage
import dev.sasikanth.rss.reader.database.Post

@Composable
internal fun FeaturedPostItem(
  item: Post,
  onClick: () -> Unit
) {
  Column {
    Box {
      AsyncImage(
        url = item.imageUrl!!,
        modifier = Modifier
          .clip(MaterialTheme.shapes.extraLarge)
          .aspectRatio(1.77f)
          .background(MaterialTheme.colorScheme.surface)
          .clickable(onClick = onClick),
        contentDescription = null,
        contentScale = ContentScale.Crop
      )
    }

    Spacer(modifier = Modifier.height(16.dp))

    Text(
      text = item.title,
      style = MaterialTheme.typography.headlineSmall,
      color = MaterialTheme.colorScheme.onSurface,
      modifier = Modifier.padding(horizontal = 16.dp),
      maxLines = 3,
      overflow = TextOverflow.Ellipsis
    )

    if (item.description.isNotBlank()) {
      Spacer(modifier = Modifier.height(8.dp))

      Text(
        text = item.description,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurface,
        maxLines = 3,
        overflow = TextOverflow.Ellipsis,
        modifier = Modifier.padding(horizontal = 16.dp)
      )
    }

    Spacer(modifier = Modifier.height(16.dp))
  }
}

@Composable
internal fun FeaturedPostItemBackground(
  modifier: Modifier = Modifier,
  imageUrl: String?
) {
  BoxWithConstraints(modifier = modifier) {
    AsyncImage(
      url = imageUrl!!,
      modifier = Modifier
        .aspectRatio(0.81f)
        .blur(100.dp, BlurredEdgeTreatment.Unbounded),
      contentDescription = null,
      contentScale = ContentScale.Crop
    )

    Box(
      modifier = Modifier
        .matchParentSize()
        .background(
          brush = Brush.radialGradient(
            colors = listOf(
              Color.Black,
              Color.Black.copy(alpha = 0.0f),
              Color.Black.copy(alpha = 0.0f)
            ),
            center = Offset(
              x = constraints.maxWidth.toFloat(),
              y = 40f
            )
          )
        )
    )

    Box(
      modifier = Modifier
        .matchParentSize()
        .background(
          brush = Brush.verticalGradient(
            colors = listOf(Color.Black, Color.Black.copy(alpha = 0.0f)),
          )
        )
    )
  }
}
