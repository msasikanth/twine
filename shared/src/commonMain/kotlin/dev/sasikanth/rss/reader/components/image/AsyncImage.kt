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
package dev.sasikanth.rss.reader.components.image

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.IntSize

@Composable
fun AsyncImage(
  url: String,
  contentDescription: String?,
  modifier: Modifier = Modifier,
  contentScale: ContentScale = ContentScale.Fit,
  size: IntSize? = null
) {
  Box(modifier) {
    val imageState by rememberImageLoaderState(url, size)

    when (imageState) {
      is ImageLoaderState.Loaded -> {
        Image(
          modifier = Modifier.matchParentSize(),
          bitmap = (imageState as ImageLoaderState.Loaded).image,
          contentDescription = contentDescription,
          contentScale = contentScale
        )
      }
      else -> {
        // TODO: Handle other cases instead of just showing blank space?
      }
    }
  }
}
