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
package dev.sasikanth.rss.reader.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.IntSize

@Composable
actual fun AsyncImage(
  url: String,
  contentDescription: String?,
  contentScale: ContentScale,
  size: IntSize?,
  modifier: Modifier,
) {
  val imageLoader = LocalImageLoader.current
  var image: ImageBitmap? by remember { mutableStateOf(null) }

  LaunchedEffect(url) { image = imageLoader?.getImage(url, size) }

  Box(modifier) {
    image?.let { bitmap ->
      Image(
        modifier = Modifier.matchParentSize(),
        bitmap = bitmap,
        contentDescription = contentDescription,
        contentScale = ContentScale.Crop
      )
    }
  }
}
