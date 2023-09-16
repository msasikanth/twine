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
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import io.ktor.client.request.get
import org.jetbrains.skia.Image

@Composable
actual fun AsyncImage(
  url: String,
  contentDescription: String?,
  contentScale: ContentScale,
  modifier: Modifier,
) {
  Box(modifier) {
    val imageState by rememberImageLoaderState(url)

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
