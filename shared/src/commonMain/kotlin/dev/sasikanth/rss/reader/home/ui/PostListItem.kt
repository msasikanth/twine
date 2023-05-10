package dev.sasikanth.rss.reader.home.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.requiredWidthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import dev.sasikanth.rss.reader.components.AsyncImage
import dev.sasikanth.rss.reader.database.PostWithMetadata
import dev.sasikanth.rss.reader.utils.relativeDurationString

@Composable
internal fun PostListItem(
  item: PostWithMetadata,
  onClick: () -> Unit
) {
  Row(
    modifier = Modifier
      .clickable(onClick = onClick)
      .padding(24.dp),
    horizontalArrangement = Arrangement.spacedBy(16.dp),
    verticalAlignment = Alignment.CenterVertically
  ) {
    Column(
      modifier = Modifier.weight(1f)
    ) {
      Text(
        style = MaterialTheme.typography.titleSmall,
        text = item.title,
        color = MaterialTheme.colorScheme.onSurface,
        maxLines = 2
      )
      PostMetadata(post = item)
    }

    item.imageUrl?.let { url ->
      AsyncImage(
        url = url,
        modifier = Modifier
          .requiredSize(width = 128.dp, height = 72.dp)
          .clip(RoundedCornerShape(12.dp)),
        contentDescription = null,
        contentScale = ContentScale.Crop
      )
    }
  }
}

@Composable
private fun PostMetadata(post: PostWithMetadata) {
  val feedName = post.feedName ?: "Unknown"
  val postPublishedAt = post.date.relativeDurationString()

  Row(
    horizontalArrangement = Arrangement.spacedBy(8.dp)
  ) {
    Text(
      modifier = Modifier.requiredWidthIn(max = 72.dp),
      style = MaterialTheme.typography.bodySmall,
      maxLines = 1,
      text = feedName,
      color = MaterialTheme.colorScheme.onSurface,
      overflow = TextOverflow.Ellipsis
    )

    Text(
      style = MaterialTheme.typography.bodySmall,
      maxLines = 1,
      text = "•",
      color = MaterialTheme.colorScheme.onSurface
    )

    Text(
      modifier = Modifier.weight(1f),
      style = MaterialTheme.typography.bodySmall,
      maxLines = 1,
      text = postPublishedAt,
      color = MaterialTheme.colorScheme.onSurface,
      textAlign = TextAlign.Left
    )
  }
}
