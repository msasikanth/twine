/*
 * Copyright 2026 Sasikanth Miriyampalli
 *
 * Licensed under the GPL, Version 3.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.gnu.org/licenses/gpl-3.0.en.html
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package dev.sasikanth.rss.reader.reader.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.skydoves.landscapist.ImageOptions
import com.skydoves.landscapist.coil3.CoilImage
import com.skydoves.landscapist.components.rememberImageComponent
import com.skydoves.landscapist.zoomable.ZoomableConfig
import com.skydoves.landscapist.zoomable.ZoomablePlugin
import com.skydoves.landscapist.zoomable.rememberZoomableState
import dev.sasikanth.rss.reader.components.CircularIconButton
import dev.sasikanth.rss.reader.resources.icons.ArrowBack
import dev.sasikanth.rss.reader.resources.icons.TwineIcons
import dev.sasikanth.rss.reader.ui.AppTheme
import org.jetbrains.compose.resources.stringResource
import twine.shared.generated.resources.Res
import twine.shared.generated.resources.buttonGoBack

@Composable
internal fun ImageViewerScreen(
  imageUrl: String,
  onBack: () -> Unit,
  toggleLightStatusBar: (Boolean) -> Unit,
  toggleLightNavBar: (Boolean) -> Unit,
  modifier: Modifier = Modifier,
) {
  DisposableEffect(Unit) {
    toggleLightStatusBar(false)
    toggleLightNavBar(false)
    onDispose {}
  }

  ImageViewerContent(imageUrl = imageUrl, onBack = onBack, modifier = modifier)
}

@Composable
private fun ImageViewerContent(
  imageUrl: String,
  onBack: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Scaffold(
    modifier = modifier.fillMaxSize(),
    containerColor = Color.Black,
    topBar = {
      TopAppBar(
        title = {},
        navigationIcon = {
          CircularIconButton(
            icon = TwineIcons.ArrowBack,
            label = stringResource(Res.string.buttonGoBack),
            backgroundColor = Color.Black.copy(alpha = 0.65f),
            contentColor = Color.White,
            onClick = onBack,
          )
        },
        contentPadding = PaddingValues(horizontal = 12.dp),
        colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
      )
    },
  ) { _ ->
    val zoomableState = rememberZoomableState(config = ZoomableConfig(enableSubSampling = false))

    Box(modifier = Modifier.fillMaxSize()) {
      CoilImage(
        modifier = Modifier.fillMaxSize(),
        imageModel = { imageUrl },
        imageOptions = ImageOptions(contentScale = ContentScale.Fit, alignment = Alignment.Center),
        component = rememberImageComponent { +ZoomablePlugin(zoomableState) },
        loading = {
          Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
          }
        },
      )
    }
  }
}

@Preview(locale = "en")
@Composable
private fun ImageViewerPreview() {
  AppTheme { ImageViewerContent(imageUrl = "", onBack = {}) }
}
