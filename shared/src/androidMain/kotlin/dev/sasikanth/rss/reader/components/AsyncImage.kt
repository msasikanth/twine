package dev.sasikanth.rss.reader.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale

@Composable
actual fun AsyncImage(
  url: String,
  contentDescription: String?,
  contentScale: ContentScale,
  modifier: Modifier,
) {
  coil.compose.AsyncImage(
    modifier = modifier,
    model = url,
    contentDescription = contentDescription,
    contentScale = ContentScale.Crop
  )
}
