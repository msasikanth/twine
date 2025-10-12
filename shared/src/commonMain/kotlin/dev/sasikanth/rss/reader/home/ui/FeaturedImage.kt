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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.layer.GraphicsLayer
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.sasikanth.rss.reader.components.image.AsyncImage
import dev.sasikanth.rss.reader.ui.AppTheme
import dev.sasikanth.rss.reader.utils.LocalWindowSizeClass

@Composable
fun FeaturedImage(
  image: String?,
  modifier: Modifier = Modifier,
  imageGraphicsLayer: GraphicsLayer? = null,
) {
  val sizeClass = LocalWindowSizeClass.current.widthSizeClass
  val imageMaxHeight =
    when {
      sizeClass >= WindowWidthSizeClass.Expanded -> {
        360.dp
      }
      sizeClass >= WindowWidthSizeClass.Medium -> {
        250.dp
      }
      else -> Dp.Unspecified
    }

  if (!image.isNullOrBlank()) {
    AsyncImage(
      url = image,
      modifier =
        Modifier.aspectRatio(16f / 9f)
          .heightIn(max = imageMaxHeight)
          .drawWithContent {
            if (imageGraphicsLayer != null) {
              imageGraphicsLayer.record { this@drawWithContent.drawContent() }
              drawLayer(imageGraphicsLayer)
            } else {
              this.drawContent()
            }
          }
          .clip(MaterialTheme.shapes.extraLarge)
          .background(AppTheme.colorScheme.surfaceContainerLowest)
          .then(modifier),
      contentDescription = null,
      contentScale = ContentScale.Crop,
    )
  }
}
