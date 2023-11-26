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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.unit.IntSize

@Composable
internal fun rememberImageLoaderState(url: String?, size: IntSize?): State<ImageLoaderState> {
  val initialState =
    if (url.isNullOrBlank()) {
      ImageLoaderState.Error
    } else {
      ImageLoaderState.Loading
    }
  val imageLoader = LocalImageLoader.current
  val result = remember(url, imageLoader) { mutableStateOf(initialState) }

  LaunchedEffect(url) {
    val imageLoaderState =
      try {
        ImageLoaderState.Loaded(imageLoader?.getImage(url!!, size = size)!!)
      } catch (e: Exception) {
        ImageLoaderState.Error
      }

    result.value = imageLoaderState
  }

  return result
}

interface ImageLoader {
  suspend fun getImage(url: String, size: IntSize?): ImageBitmap?
}

internal sealed interface ImageLoaderState {
  data object Idle : ImageLoaderState

  data object Loading : ImageLoaderState

  data class Loaded(val image: ImageBitmap) : ImageLoaderState

  data object Error : ImageLoaderState
}

internal val LocalImageLoader = staticCompositionLocalOf<ImageLoader?> { null }
