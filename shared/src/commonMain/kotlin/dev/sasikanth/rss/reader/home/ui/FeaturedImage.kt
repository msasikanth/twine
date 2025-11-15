/*
 * Copyright 2025 Sasikanth Miriyampalli
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
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.sasikanth.rss.reader.components.image.AsyncImage
import dev.sasikanth.rss.reader.ui.AppTheme
import dev.sasikanth.rss.reader.utils.LocalWindowSizeClass

private const val PARALLAX_MULTIPLIER = 350f

@Composable
fun FeaturedImage(
  imageUrl: String?,
  modifier: Modifier = Modifier,
  isComicStrip: Boolean = false,
  parallaxProgress: (() -> Float)? = null,
) {
  val sizeClass = LocalWindowSizeClass.current.widthSizeClass
  val imageMaxHeight =
    when {
      sizeClass >= WindowWidthSizeClass.Expanded -> 360.dp
      sizeClass >= WindowWidthSizeClass.Medium -> 250.dp
      else -> Dp.Unspecified
    }

  val comicStripImageModifier =
    if (isComicStrip) {
      Modifier.fillMaxWidth().clip(RectangleShape)
    } else {
      Modifier.aspectRatio(16f / 9f)
        .heightIn(max = imageMaxHeight)
        .clip(MaterialTheme.shapes.extraLarge)
    }
  val contentScale =
    if (isComicStrip) {
      ContentScale.FillWidth
    } else {
      ContentScale.Crop
    }

  imageUrl?.let { imageUrl ->
    AsyncImage(
      url = imageUrl,
      modifier =
        Modifier.then(comicStripImageModifier)
          .background(AppTheme.colorScheme.surfaceContainerLowest)
          .graphicsLayer { translationX = (parallaxProgress?.invoke() ?: 0f) * PARALLAX_MULTIPLIER }
          .then(modifier),
      contentDescription = null,
      contentScale = contentScale,
    )
  }
}
