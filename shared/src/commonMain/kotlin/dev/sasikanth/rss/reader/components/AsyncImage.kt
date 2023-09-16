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
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.layout.ContentScale
import com.seiko.imageloader.model.ImageRequest
import com.seiko.imageloader.option.SizeResolver
import com.seiko.imageloader.rememberImagePainter

@Composable
fun AsyncImage(
  url: String,
  contentDescription: String?,
  contentScale: ContentScale = ContentScale.Crop,
  modifier: Modifier = Modifier,
  size: Size = Size(1024f, 1024f)
) {
  val request =
    remember(url) {
      ImageRequest {
        data(url)
        size(SizeResolver(size))
      }
    }
  val painter = rememberImagePainter(request)
  Box(modifier) {
    Image(
      modifier = Modifier.matchParentSize(),
      painter = painter,
      contentDescription = contentDescription,
      contentScale = contentScale,
    )
  }
}
