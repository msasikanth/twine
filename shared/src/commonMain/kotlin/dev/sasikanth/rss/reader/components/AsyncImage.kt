package dev.sasikanth.rss.reader.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale

@Composable
expect fun AsyncImage(
  url: String,
  contentDescription: String?,
  contentScale: ContentScale = ContentScale.Fit,
  modifier: Modifier = Modifier,
)
