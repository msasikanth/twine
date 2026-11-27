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

import androidx.compose.animation.EnterExitState
import androidx.compose.animation.core.animateDp
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Size as ComposeSize
import androidx.compose.ui.geometry.isSpecified
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.SingletonImageLoader
import coil3.compose.AsyncImagePainter
import coil3.compose.LocalPlatformContext
import coil3.compose.asPainter
import coil3.compose.rememberAsyncImagePainter
import coil3.memory.MemoryCache
import coil3.request.ImageRequest
import coil3.size.Size as CoilSize
import com.skydoves.landscapist.zoomable.ZoomableConfig
import com.skydoves.landscapist.zoomable.ZoomablePlugin
import com.skydoves.landscapist.zoomable.rememberZoomableState
import dev.sasikanth.rss.reader.app.LocalSnackbarHostState
import dev.sasikanth.rss.reader.components.CircularIconButton
import dev.sasikanth.rss.reader.core.network.utils.UrlUtils
import dev.sasikanth.rss.reader.markdown.inlineImageMemoryCacheKey
import dev.sasikanth.rss.reader.platform.ImageDownloadResult
import dev.sasikanth.rss.reader.platform.LocalImageDownloader
import dev.sasikanth.rss.reader.resources.icons.ArrowBack
import dev.sasikanth.rss.reader.resources.icons.Download
import dev.sasikanth.rss.reader.resources.icons.TwineIcons
import dev.sasikanth.rss.reader.ui.AppTheme
import dev.sasikanth.rss.reader.ui.ImageViewerBoundsTransform
import dev.sasikanth.rss.reader.ui.ImageViewerCornerRadiusSpec
import dev.sasikanth.rss.reader.ui.LocalNavAnimatedContentScopeOrNull
import dev.sasikanth.rss.reader.ui.LocalSharedTransitionScope
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import twine.shared.generated.resources.Res
import twine.shared.generated.resources.buttonGoBack
import twine.shared.generated.resources.downloadImage
import twine.shared.generated.resources.imageSaveFailed
import twine.shared.generated.resources.imageSavePermissionDenied
import twine.shared.generated.resources.imageSaved

private val ImageViewerMaxSize = CoilSize(2048, 2048)
private val ImageViewerZoomConfig = ZoomableConfig(enableSubSampling = false)
private val ImageViewerButtonScrim = Color.Black.copy(alpha = 0.65f)
private val DisableAnimationCacheExtras = mapOf("disable_animation" to "true")

@Composable
internal fun ImageViewerScreen(
  imageUrl: String,
  onBack: () -> Unit,
  toggleLightStatusBar: (Boolean) -> Unit,
  toggleLightNavBar: (Boolean) -> Unit,
  modifier: Modifier = Modifier,
  sharedElementKey: String = imageUrl,
  memoryCacheKey: String? = null,
  sourceCornerRadiusDp: Float = 0f,
  sourceAspectRatio: Float = 0f,
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
    sharedElementKey = sharedElementKey,
    memoryCacheKey = memoryCacheKey,
    sourceCornerRadiusDp = sourceCornerRadiusDp,
    sourceAspectRatio = sourceAspectRatio,
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
  sharedElementKey: String = imageUrl,
  memoryCacheKey: String? = null,
  sourceCornerRadiusDp: Float = 0f,
  sourceAspectRatio: Float = 0f,
) {
  val sharedTransitionScope = LocalSharedTransitionScope.current
  val animatedVisibilityScope = LocalNavAnimatedContentScopeOrNull.current
  val isSharedTransitionActive = sharedTransitionScope?.isTransitionActive == true

  val topBarModifier =
    if (sharedTransitionScope != null && animatedVisibilityScope != null) {
      with(sharedTransitionScope) {
        with(animatedVisibilityScope) {
          Modifier.renderInSharedTransitionScopeOverlay(zIndexInOverlay = 1f)
            .animateEnterExit(
              enter = fadeIn(animationSpec = tween(durationMillis = 250)),
              exit = fadeOut(animationSpec = tween(durationMillis = 150)),
            )
        }
      }
    } else {
      Modifier
    }

  val zoomableState = rememberZoomableState(config = ImageViewerZoomConfig)
  val platformContext = LocalPlatformContext.current
  val fallbackMemoryCacheKey = memoryCacheKey ?: inlineImageMemoryCacheKey(imageUrl)

  val cachedEntry =
    remember(imageUrl, memoryCacheKey, platformContext) {
      if (imageUrl.isBlank()) {
        null
      } else {
        val memoryCache = SingletonImageLoader.get(platformContext).memoryCache
        if (memoryCache == null) {
          null
        } else {
          val candidateKeys =
            listOfNotNull(
                memoryCacheKey,
                inlineImageMemoryCacheKey(imageUrl),
                imageUrl,
                UrlUtils.youTubeThumbnailFallback(imageUrl),
              )
              .distinct()
          candidateKeys.firstNotNullOfOrNull { key ->
            val directKey = MemoryCache.Key(key)
            memoryCache[directKey]?.let { directKey to it }
              ?: MemoryCache.Key(key, DisableAnimationCacheExtras).let { extraKey ->
                memoryCache[extraKey]?.let { extraKey to it }
              }
              ?: memoryCache.keys
                .firstOrNull { it.key == key }
                ?.let { matchedKey -> memoryCache[matchedKey]?.let { matchedKey to it } }
          }
        }
      }
    }
  val cachedKey = cachedEntry?.first
  val cachedImage = cachedEntry?.second?.image
  val cachedPainter =
    remember(cachedImage, platformContext) { cachedImage?.asPainter(platformContext) }

  val imageRequest =
    remember(imageUrl, cachedKey, fallbackMemoryCacheKey, platformContext) {
      ImageRequest.Builder(platformContext)
        .data(imageUrl)
        .diskCacheKey(imageUrl)
        .apply {
          if (cachedKey != null) {
            placeholderMemoryCacheKey(cachedKey)
          } else {
            placeholderMemoryCacheKey(fallbackMemoryCacheKey)
          }
        }
        .size(ImageViewerMaxSize)
        .build()
    }

  val asyncPainter = rememberAsyncImagePainter(model = imageRequest)
  val painterState by asyncPainter.state.collectAsState()
  val isAsyncSuccess = painterState is AsyncImagePainter.State.Success
  val activePainter =
    if (cachedPainter != null && (isSharedTransitionActive || !isAsyncSuccess)) {
      cachedPainter
    } else {
      asyncPainter
    }
  val activeIntrinsicSize =
    when {
      isAsyncSuccess && !isSharedTransitionActive ->
        (painterState as AsyncImagePainter.State.Success).painter.intrinsicSize
      cachedPainter != null -> cachedPainter.intrinsicSize
      else -> ComposeSize.Unspecified
    }
  val aspectRatio =
    when {
      activeIntrinsicSize.isSpecified &&
        activeIntrinsicSize.width > 0f &&
        activeIntrinsicSize.height > 0f -> activeIntrinsicSize.width / activeIntrinsicSize.height
      cachedImage != null && cachedImage.width > 0 && cachedImage.height > 0 ->
        cachedImage.width.toFloat() / cachedImage.height.toFloat()
      sourceAspectRatio > 0f -> sourceAspectRatio
      else -> null
    }

  val (sharedElementModifier, clipModifier) =
    if (sharedTransitionScope != null && animatedVisibilityScope != null) {
      with(sharedTransitionScope) {
        val sharedContentState = rememberSharedContentState(key = sharedElementKey)
        val cornerRadius by
          animatedVisibilityScope.transition.animateDp(
            transitionSpec = { ImageViewerCornerRadiusSpec },
            label = "imageViewerCornerRadius",
          ) { state ->
            if (state == EnterExitState.PreEnter) {
              sourceCornerRadiusDp.dp
            } else {
              0.dp
            }
          }
        val sharedModifier =
          Modifier.sharedElement(
            sharedContentState = sharedContentState,
            animatedVisibilityScope = animatedVisibilityScope,
            boundsTransform = ImageViewerBoundsTransform,
          )
        val shapeModifier =
          if (cornerRadius > 0.dp) {
            Modifier.clip(RoundedCornerShape(cornerRadius))
          } else {
            Modifier
          }
        sharedModifier to shapeModifier
      }
    } else {
      Modifier to Modifier
    }

  val sizeModifier =
    if (aspectRatio != null) {
      Modifier.aspectRatio(aspectRatio)
    } else {
      Modifier.fillMaxSize()
    }

  val zoomablePlugin =
    remember(zoomableState, isSharedTransitionActive) {
      ZoomablePlugin(state = zoomableState, enabled = !isSharedTransitionActive)
    }

  Box(modifier = modifier.fillMaxSize().background(Color.Black)) {
    Box(modifier = Modifier.fillMaxSize()) {
      zoomablePlugin.compose {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
          Image(
            painter = activePainter,
            contentDescription = null,
            contentScale = if (aspectRatio != null) ContentScale.Crop else ContentScale.Fit,
            alignment = Alignment.Center,
            modifier = sizeModifier.then(sharedElementModifier).then(clipModifier),
          )
        }
      }

      if (cachedPainter == null && painterState is AsyncImagePainter.State.Loading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
          CircularProgressIndicator()
        }
      }
    }

    TopAppBar(
      modifier = topBarModifier.align(Alignment.TopCenter),
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
  }
}

@Preview(locale = "en")
@Composable
private fun ImageViewerPreview() {
  AppTheme {
    ImageViewerContent(imageUrl = "", isDownloading = false, onBack = {}, onDownloadClick = {})
  }
}
