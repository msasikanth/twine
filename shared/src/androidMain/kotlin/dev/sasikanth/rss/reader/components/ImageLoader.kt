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

import android.content.Context
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.core.graphics.drawable.toBitmap
import coil.imageLoader
import coil.request.ImageRequest
import coil.request.SuccessResult
import coil.size.Scale
import dev.sasikanth.rss.reader.di.scopes.AppScope
import me.tatarka.inject.annotations.Inject

@Inject
@AppScope
class AndroidImageLoader(private val context: Context) : ImageLoader {

  override suspend fun getImage(url: String, size: Int?): ImageBitmap? {
    val requestBuilder =
      ImageRequest.Builder(context)
        .data(url)
        .scale(Scale.FILL)
        .allowHardware(false)
        .memoryCacheKey("$url.dynamic_colors")

    if (size != null) {
      requestBuilder.size(size)
    }

    return when (val result = context.imageLoader.execute(requestBuilder.build())) {
      is SuccessResult -> result.drawable.toBitmap().asImageBitmap()
      else -> null
    }
  }
}
