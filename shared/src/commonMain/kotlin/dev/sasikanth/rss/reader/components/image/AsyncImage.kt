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

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import coil3.compose.LocalPlatformContext
import coil3.request.ImageRequest
import coil3.size.Dimension
import coil3.size.Size

@Composable
internal fun AsyncImage(
  url: String,
  contentDescription: String?,
  modifier: Modifier = Modifier,
  contentScale: ContentScale = ContentScale.Fit,
  size: Size = Size(Dimension.Undefined, 500),
  backgroundColor: Color? = null,
  colorFilter: ColorFilter? = null,
) {
  val backgroundColorModifier =
    if (backgroundColor != null) {
      Modifier.background(color = backgroundColor)
    } else {
      Modifier
    }

  Box(modifier.then(backgroundColorModifier)) {
    val imageRequest =
      ImageRequest.Builder(LocalPlatformContext.current).data(url).size(size).build()

    coil3.compose.AsyncImage(
      model = imageRequest,
      contentDescription = contentDescription,
      modifier = Modifier.matchParentSize(),
      contentScale = contentScale,
      colorFilter = colorFilter,
    )
  }
}
