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

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.compose.LocalPlatformContext
import coil3.request.ImageRequest
import coil3.size.Size
import com.skydoves.landscapist.ImageOptions
import com.skydoves.landscapist.coil3.CoilImage
import com.skydoves.landscapist.components.rememberImageComponent
import com.skydoves.landscapist.zoomable.ZoomableConfig
import com.skydoves.landscapist.zoomable.ZoomablePlugin
import com.skydoves.landscapist.zoomable.rememberZoomableState
import dev.sasikanth.rss.reader.app.LocalSnackbarHostState
import dev.sasikanth.rss.reader.components.CircularIconButton
import dev.sasikanth.rss.reader.markdown.inlineImageMemoryCacheKey
import dev.sasikanth.rss.reader.platform.ImageDownloadResult
import dev.sasikanth.rss.reader.platform.LocalImageDownloader
import dev.sasikanth.rss.reader.resources.icons.ArrowBack
import dev.sasikanth.rss.reader.resources.icons.Download
import dev.sasikanth.rss.reader.resources.icons.TwineIcons
import dev.sasikanth.rss.reader.ui.AppTheme
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import twine.shared.generated.resources.Res
import twine.shared.generated.resources.buttonGoBack
import twine.shared.generated.resources.downloadImage
import twine.shared.generated.resources.imageSaveFailed
import twine.shared.generated.resources.imageSavePermissionDenied
import twine.shared.generated.resources.imageSaved

private val ImageViewerMaxSize = Size(2048, 2048)
private val ImageViewerZoomConfig = ZoomableConfig(enableSubSampling = false)
private val ImageViewerOptions =
  ImageOptions(contentScale = ContentScale.Fit, alignment = Alignment.Center)
private val ImageViewerButtonScrim = Color.Black.copy(alpha = 0.65f)

@Composable
internal fun ImageViewerScreen(
  imageUrl: String,
  onBack: () -> Unit,
  toggleLightStatusBar: (Boolean) -> Unit,
  toggleLightNavBar: (Boolean) -> Unit,
  modifier: Modifier = Modifier,
) {
  val imageDownloader = LocalImageDownloader.current
  val snackbarHostState = LocalSnackbarHostState.current
  val coroutineScope = rememberCoroutineScope()
  val imageSavedMessage = stringResource(Res.string.imageSaved)
  val imageSaveFailedMessage = stringResource(Res.string.imageSaveFailed)
  val imageSavePermissionDeniedMessage = stringResource(Res.string.imageSavePermissionDenied)
  var isDownloading by remember { mutableStateOf(false) }

  SideEffect(Unit) {
    toggleLightStatusBar(false)
    toggleLightNavBar(false)
  }

  ImageViewerContent(
    imageUrl = imageUrl,
    isDownloading = isDownloading,
    onBack = onBack,
    onDownloadClick = {
      if (isDownloading || imageUrl.isBlank()) return@ImageViewerContent
      isDownloading = true
      coroutineScope.launch {
        val result =
          try {
            imageDownloader.download(imageUrl)
          } finally {
            isDownloading = false
          }
        val message =
          when (result) {
            ImageDownloadResult.Success -> imageSavedMessage
            ImageDownloadResult.PermissionDenied -> imageSavePermissionDeniedMessage
            ImageDownloadResult.Failure -> imageSaveFailedMessage
            ImageDownloadResult.Cancelled -> null
          }
        if (message != null) {
          snackbarHostState.showSnackbar(message)
        }
      }
    },
    modifier = modifier,
  )
}

@Composable
private fun ImageViewerContent(
  imageUrl: String,
  isDownloading: Boolean,
  onBack: () -> Unit,
  onDownloadClick: () -> Unit,
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
            backgroundColor = ImageViewerButtonScrim,
            contentColor = Color.White,
            onClick = onBack,
          )
        },
        actions = {
          if (isDownloading) {
            Box(
              modifier = Modifier.size(40.dp).background(ImageViewerButtonScrim, CircleShape),
              contentAlignment = Alignment.Center,
            ) {
              CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                color = Color.White,
                strokeWidth = 2.dp,
              )
            }
          } else {
            CircularIconButton(
              icon = TwineIcons.Download,
              label = stringResource(Res.string.downloadImage),
              enabled = imageUrl.isNotBlank(),
              backgroundColor = ImageViewerButtonScrim,
              contentColor = Color.White,
              onClick = onDownloadClick,
            )
          }
        },
        contentPadding = PaddingValues(horizontal = 12.dp),
        colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
      )
    },
  ) { _ ->
    val zoomableState = rememberZoomableState(config = ImageViewerZoomConfig)
    val platformContext = LocalPlatformContext.current
    val imageRequest =
      remember(imageUrl, platformContext) {
        ImageRequest.Builder(platformContext)
          .data(imageUrl)
          .diskCacheKey(imageUrl)
          .placeholderMemoryCacheKey(inlineImageMemoryCacheKey(imageUrl))
          .size(ImageViewerMaxSize)
          .build()
      }

    Box(modifier = Modifier.fillMaxSize()) {
      CoilImage(
        modifier = Modifier.fillMaxSize(),
        imageRequest = { imageRequest },
        imageOptions = ImageViewerOptions,
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
  AppTheme {
    ImageViewerContent(imageUrl = "", isDownloading = false, onBack = {}, onDownloadClick = {})
  }
}
