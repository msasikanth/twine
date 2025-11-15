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

import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import coil3.compose.LocalPlatformContext
import coil3.request.ImageRequest
import coil3.request.crossfade
import coil3.size.Dimension
import coil3.size.Size

@Composable
internal fun AsyncImage(
  url: String,
  contentDescription: String?,
  modifier: Modifier = Modifier,
  contentScale: ContentScale = ContentScale.Fit,
  size: Size = Size(Dimension.Undefined, 500),
  colorFilter: ColorFilter? = null,
  alignment: Alignment = Alignment.Center,
) {
  val context = LocalPlatformContext.current

  coil3.compose.AsyncImage(
    model =
      ImageRequest.Builder(context)
        .data(url)
        .size(size)
        .diskCacheKey(url)
        .memoryCacheKey(url)
        .crossfade(true)
        .build(),
    contentDescription = contentDescription,
    modifier = modifier,
    contentScale = contentScale,
    colorFilter = colorFilter,
    alignment = alignment,
  )
}
