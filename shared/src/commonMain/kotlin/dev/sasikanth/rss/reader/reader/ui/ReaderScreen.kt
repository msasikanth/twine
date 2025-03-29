@file:Suppress(
  "CANNOT_OVERRIDE_INVISIBLE_MEMBER",
  "INVISIBLE_MEMBER",
  "INVISIBLE_REFERENCE",
)
/*
 * Copyright 2024 Sasikanth Miriyampalli
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

package dev.sasikanth.rss.reader.reader.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.requiredHeightIn
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.requiredSizeIn
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ChevronLeft
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberSheetState
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.BlurEffect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.UriHandler
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.isTraversalGroup
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.lerp
import androidx.compose.ui.util.lerp
import app.cash.paging.compose.collectAsLazyPagingItems
import app.cash.paging.compose.itemKey
import coil3.size.Size
import com.mikepenz.markdown.coil3.Coil3ImageTransformerImpl
import com.mikepenz.markdown.compose.LocalImageTransformer
import com.mikepenz.markdown.compose.components.markdownComponents
import com.mikepenz.markdown.compose.elements.MarkdownHighlightedCodeBlock
import com.mikepenz.markdown.compose.elements.MarkdownHighlightedCodeFence
import com.mikepenz.markdown.m3.Markdown
import com.mikepenz.markdown.m3.markdownColor
import com.mikepenz.markdown.m3.markdownTypography
import com.mikepenz.markdown.utils.findChildOfTypeRecursive
import com.mikepenz.markdown.utils.getUnescapedTextInNode
import dev.sasikanth.rss.reader.components.image.AsyncImage
import dev.sasikanth.rss.reader.components.image.FeedIcon
import dev.sasikanth.rss.reader.core.model.local.PostWithMetadata
import dev.sasikanth.rss.reader.home.ui.PostMetadataConfig
import dev.sasikanth.rss.reader.platform.LocalLinkHandler
import dev.sasikanth.rss.reader.reader.ReaderEvent
import dev.sasikanth.rss.reader.reader.ReaderPresenter
import dev.sasikanth.rss.reader.resources.icons.ArticleShortcut
import dev.sasikanth.rss.reader.resources.icons.Bookmark
import dev.sasikanth.rss.reader.resources.icons.Bookmarked
import dev.sasikanth.rss.reader.resources.icons.Comments
import dev.sasikanth.rss.reader.resources.icons.OpenBrowser
import dev.sasikanth.rss.reader.resources.icons.Share
import dev.sasikanth.rss.reader.resources.icons.TwineIcons
import dev.sasikanth.rss.reader.resources.strings.LocalStrings
import dev.sasikanth.rss.reader.share.LocalShareHandler
import dev.sasikanth.rss.reader.ui.AppTheme
import dev.sasikanth.rss.reader.util.canBlurImage
import dev.sasikanth.rss.reader.util.readerDateTimestamp
import dev.sasikanth.rss.reader.utils.LocalShowFeedFavIconSetting
import dev.sasikanth.rss.reader.utils.getOffsetFractionForPage
import dev.snipme.highlights.Highlights
import dev.snipme.highlights.model.SyntaxThemes
import kotlin.math.absoluteValue
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.intellij.markdown.MarkdownElementTypes

private val json = Json {
  ignoreUnknownKeys = true
  isLenient = true
  explicitNulls = false
}

@OptIn(FlowPreview::class)
@Composable
internal fun ReaderScreen(
  darkTheme: Boolean,
  presenter: ReaderPresenter,
  modifier: Modifier = Modifier
) {
  val state by presenter.state.collectAsState()
  val posts = state.posts.collectAsLazyPagingItems()
  val pagerState = rememberPagerState { posts.itemCount }
  val sheetState =
    rememberSheetState(skipPartiallyExpanded = true, velocityThreshold = Float.POSITIVE_INFINITY.dp)
  var sheetGesturesEnabled by remember { mutableStateOf(true) }

  ModalBottomSheet(
    modifier = modifier,
    sheetState = sheetState,
    containerColor = AppTheme.colorScheme.backdrop,
    dragHandle = null,
    shape = RectangleShape,
    sheetGesturesEnabled = sheetGesturesEnabled,
    onDismissRequest = { presenter.dispatch(ReaderEvent.BackClicked) },
  ) {
    HorizontalPager(
      state = pagerState,
      key = posts.itemKey { it.id },
      overscrollEffect = null,
    ) { page ->
      val readerPost = posts[page]
      val coroutineScope = rememberCoroutineScope()
      val listState = rememberLazyListState()

      LaunchedEffect(page) {
        val postId = readerPost?.id
        if (!postId.isNullOrBlank()) {
          presenter.dispatch(ReaderEvent.PostPageChanged(postId))
        }
      }

      if (readerPost != null) {
        LaunchedEffect(listState, pagerState) {
          snapshotFlow { listState.firstVisibleItemIndex }
            .filter { !pagerState.isScrollInProgress }
            .debounce(500)
            .collectLatest { index -> sheetGesturesEnabled = index == 0 }
        }

        val pageOffset = pagerState.getOffsetFractionForPage(page).absoluteValue
        ReaderPage(
          modifier =
            Modifier.graphicsLayer {
              val scale = lerp(1f, 0.75f, pageOffset)
              scaleX = scale
              scaleY = scale
            },
          listState = listState,
          readerPost = readerPost,
          presenter = presenter,
          darkTheme = darkTheme,
          canNavigateNext = pagerState.canScrollForward,
          canNavigatePrev = pagerState.canScrollBackward,
          goNext = {
            coroutineScope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) }
          },
          goPrev = {
            coroutineScope.launch { pagerState.animateScrollToPage(pagerState.currentPage - 1) }
          },
        )
      }
    }
  }
}

@Composable
private fun ReaderPage(
  listState: LazyListState,
  readerPost: PostWithMetadata,
  presenter: ReaderPresenter,
  darkTheme: Boolean,
  canNavigateNext: Boolean,
  canNavigatePrev: Boolean,
  goNext: () -> Unit,
  goPrev: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val linkHandler = LocalLinkHandler.current
  val sharedHandler = LocalShareHandler.current
  val coroutineScope = rememberCoroutineScope()

  var readerProcessingProgress by
    remember(readerPost.id) { mutableStateOf(ReaderProcessingProgress.Loading) }
  var parsedContent by remember(readerPost.id) { mutableStateOf(ReaderContent("", "")) }
  var loadFullArticleClicked by remember(readerPost.id) { mutableStateOf(false) }

  val content = readerPost.rawContent ?: readerPost.description
  val fetchFullArticle =
    readerPost.alwaysFetchFullArticle ||
      content.isBlank() ||
      loadFullArticleClicked ||
      (parsedContent.content.isNullOrBlank() &&
        readerProcessingProgress != ReaderProcessingProgress.Loading)

  Box {
    // Dummy view to parse the reader content using JS
    ReaderWebView(
      modifier = Modifier.requiredSize(0.dp),
      link = readerPost.link,
      content = content,
      postImage = readerPost.imageUrl,
      fetchFullArticle = fetchFullArticle,
      contentLoaded = {
        readerProcessingProgress = ReaderProcessingProgress.Idle
        parsedContent = json.decodeFromString(it)
      },
    )

    Scaffold(
      modifier = modifier.fillMaxSize(),
      bottomBar = {
        BottomBar(
          openInBrowserClick = { coroutineScope.launch { linkHandler.openLink(readerPost.link) } },
          loadFullArticleClick = { loadFullArticleClicked = true }
        )
      },
      containerColor = AppTheme.colorScheme.backdrop,
      contentColor = Color.Unspecified
    ) { paddingValues ->
      val layoutDirection = LocalLayoutDirection.current
      LazyColumn(
        modifier = Modifier.fillMaxSize(),
        state = listState,
        overscrollEffect = null,
        contentPadding =
          PaddingValues(
            start = paddingValues.calculateStartPadding(layoutDirection),
            top = 0.dp,
            end = paddingValues.calculateEndPadding(layoutDirection),
            bottom = paddingValues.calculateBottomPadding()
          )
      ) {
        item(key = "top-bar") {
          CenterAlignedTopAppBar(
            colors =
              TopAppBarDefaults.topAppBarColors(
                containerColor = Color.Transparent,
                scrolledContainerColor = Color.Transparent,
              ),
            navigationIcon = {
              IconButton(enabled = canNavigatePrev, onClick = goPrev) {
                Icon(
                  imageVector = Icons.Rounded.ChevronLeft,
                  contentDescription = null,
                  tint = AppTheme.colorScheme.onSurface,
                )
              }
            },
            title = {
              Box(
                modifier =
                  Modifier.background(
                      color = AppTheme.colorScheme.primary.copy(alpha = 0.08f),
                      shape = RoundedCornerShape(50)
                    )
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                contentAlignment = Alignment.Center,
              ) {
                Text(
                  text = LocalStrings.current.pullToClose,
                  style = MaterialTheme.typography.labelSmall,
                  color = AppTheme.colorScheme.onSurfaceVariant
                )
              }
            },
            actions = {
              IconButton(enabled = canNavigateNext, onClick = goNext) {
                Icon(
                  imageVector = Icons.Rounded.ChevronRight,
                  contentDescription = null,
                  tint = AppTheme.colorScheme.onSurface,
                )
              }
            }
          )
        }

        item(key = "reader-header") {
          val postImage = readerPost.imageUrl

          Box {
            if (canBlurImage) {
              BannerImageBlurred(postImage = postImage, darkTheme = darkTheme)
            }

            PostInfo(
              readerPost = readerPost,
              parsedContent = parsedContent,
              onCommentsClick = {
                coroutineScope.launch { linkHandler.openLink(readerPost.commentsLink) }
              },
              onShareClick = { sharedHandler.share(readerPost.link) },
              onBookmarkClick = {
                presenter.dispatch(
                  ReaderEvent.TogglePostBookmark(
                    postId = readerPost.id,
                    currentBookmarkStatus = readerPost.bookmarked
                  )
                )
              }
            )
          }
        }

        item(key = "divider") {
          HorizontalDivider(
            modifier = Modifier.padding(horizontal = 32.dp).padding(top = 20.dp, bottom = 24.dp),
            color = AppTheme.colorScheme.outlineVariant
          )
        }

        item(
          key = "reader-content",
        ) {
          val readerLinkHandler = remember {
            object : UriHandler {
              override fun openUri(uri: String) {
                coroutineScope.launch { linkHandler.openLink(uri) }
              }
            }
          }
          CompositionLocalProvider(LocalUriHandler provides readerLinkHandler) {
            val highlightsBuilder =
              remember(darkTheme) {
                Highlights.Builder().theme(SyntaxThemes.atom(darkMode = darkTheme))
              }

            parsedContent.content?.let {
              Markdown(
                modifier = Modifier.fillMaxSize().padding(horizontal = 32.dp),
                content = it,
                typography =
                  markdownTypography(
                    link =
                      MaterialTheme.typography.bodyLarge.copy(
                        color = AppTheme.colorScheme.tintedForeground,
                        fontWeight = FontWeight.Bold,
                        textDecoration = TextDecoration.Underline
                      )
                  ),
                colors =
                  markdownColor(
                    text = AppTheme.colorScheme.onSurface,
                  ),
                imageTransformer = Coil3ImageTransformerImpl,
                components =
                  markdownComponents(
                    codeBlock = { cm ->
                      MarkdownHighlightedCodeBlock(
                        content = cm.content,
                        node = cm.node,
                        highlights = highlightsBuilder
                      )
                    },
                    codeFence = { cm ->
                      MarkdownHighlightedCodeFence(
                        content = cm.content,
                        node = cm.node,
                        highlights = highlightsBuilder
                      )
                    },
                    image = {
                      val link =
                        it.node
                          .findChildOfTypeRecursive(MarkdownElementTypes.LINK_DESTINATION)
                          ?.getUnescapedTextInNode(it.content)
                          ?: return@markdownComponents

                      LocalImageTransformer.current.transform(link)?.let { imageData ->
                        Image(
                          painter = imageData.painter,
                          contentDescription = imageData.contentDescription,
                          modifier = imageData.modifier.clip(MaterialTheme.shapes.extraLarge),
                          alignment = imageData.alignment,
                          contentScale = imageData.contentScale,
                          alpha = imageData.alpha,
                          colorFilter = imageData.colorFilter
                        )
                      }
                    }
                  )
              )
            }
          }

          when {
            readerProcessingProgress == ReaderProcessingProgress.Loading -> {
              Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                LinearProgressIndicator(
                  trackColor = AppTheme.colorScheme.tintedSurface,
                  color = AppTheme.colorScheme.tintedForeground,
                )
              }
            }
            content.isBlank() -> {
              Text(LocalStrings.current.noReaderContent)
            }
          }
        }
      }
    }
  }
}

@Composable
private fun BottomBar(
  openInBrowserClick: () -> Unit,
  loadFullArticleClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val navBarScrimColor = AppTheme.colorScheme.backdrop

  Box(
    modifier =
      Modifier.fillMaxWidth().wrapContentHeight().navigationBarsPadding().drawBehind {
        drawRect(brush = Brush.verticalGradient(listOf(Color.Transparent, navBarScrimColor)))
      },
    contentAlignment = Alignment.Center
  ) {
    Row(
      modifier =
        Modifier.padding(bottom = 16.dp, top = 16.dp)
          .clipToBounds()
          .background(color = AppTheme.colorScheme.bottomSheet, shape = RoundedCornerShape(50))
          .border(
            width = 1.dp,
            color = AppTheme.colorScheme.bottomSheetBorder,
            shape = RoundedCornerShape(50)
          )
          .padding(all = 12.dp)
          .then(modifier),
      verticalAlignment = Alignment.CenterVertically,
    ) {
      BottomBarIconButton(
        label = LocalStrings.current.openWebsite,
        icon = TwineIcons.OpenBrowser,
        onClick = openInBrowserClick
      )

      BottomBarIconButton(
        label = LocalStrings.current.cdLoadFullArticle,
        icon = TwineIcons.ArticleShortcut,
        onClick = loadFullArticleClick
      )
    }
  }
}

@Composable
private fun BottomBarIconButton(label: String, icon: ImageVector, onClick: () -> Unit) {
  AppTheme(useDarkTheme = true) {
    Box(
      modifier =
        Modifier.requiredSizeIn(minWidth = 64.dp, minHeight = 40.dp)
          .semantics {
            role = Role.Button
            contentDescription = label
          }
          .clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = ripple(bounded = false),
          ) {
            onClick()
          },
      contentAlignment = Alignment.Center,
    ) {
      Icon(
        modifier = Modifier.requiredSize(20.dp),
        imageVector = icon,
        contentDescription = null,
        tint = AppTheme.colorScheme.onSurfaceVariant,
      )
    }
  }
}

@Composable
private fun PostInfo(
  readerPost: PostWithMetadata,
  parsedContent: ReaderContent,
  onCommentsClick: () -> Unit,
  onShareClick: () -> Unit,
  onBookmarkClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Column(
    modifier = Modifier.fillMaxWidth().padding(horizontal = 32.dp).then(modifier),
  ) {
    val title = readerPost.title
    val description = readerPost.description
    val postImage = readerPost.imageUrl

    if (!postImage.isNullOrBlank()) {
      AsyncImage(
        url = postImage,
        modifier =
          Modifier.clip(MaterialTheme.shapes.extraLarge)
            .requiredHeightIn(max = 198.dp)
            .aspectRatio(ratio = 16f / 9f)
            .background(AppTheme.colorScheme.surfaceContainerLowest)
            .align(Alignment.CenterHorizontally),
        contentDescription = null,
        contentScale = ContentScale.Crop,
      )

      Spacer(modifier = Modifier.requiredHeight(8.dp))
    }

    Text(
      modifier = Modifier.padding(top = 20.dp),
      text = readerPost.date.readerDateTimestamp(),
      style = MaterialTheme.typography.bodyMedium,
      color = AppTheme.colorScheme.outline,
      maxLines = 1,
    )

    Text(
      modifier = Modifier.padding(top = 12.dp),
      text = title.ifBlank { description },
      style = MaterialTheme.typography.headlineSmall,
      color = AppTheme.colorScheme.onSurface,
      maxLines = 3,
      overflow = TextOverflow.Ellipsis,
    )

    if (!parsedContent.excerpt.isNullOrBlank()) {
      Spacer(Modifier.requiredHeight(8.dp))

      Text(
        text = parsedContent.excerpt,
        style = MaterialTheme.typography.bodyMedium,
        color = AppTheme.colorScheme.secondary,
        maxLines = 3,
        overflow = TextOverflow.Ellipsis,
      )
    }

    Spacer(Modifier.requiredHeight(12.dp))

    Row(verticalAlignment = Alignment.CenterVertically) {
      val showFeedFavIcon = LocalShowFeedFavIconSetting.current
      val feedIconUrl = if (showFeedFavIcon) readerPost.feedHomepageLink else readerPost.feedIcon

      PostSourcePill(
        modifier = Modifier.weight(1f).clearAndSetSemantics {},
        feedName = readerPost.feedName,
        feedIcon = feedIconUrl,
        config =
          PostMetadataConfig(
            showUnreadIndicator = false,
            showToggleReadUnreadOption = true,
            enablePostSource = false
          ),
        onSourceClick = {
          // no-op
        },
      )

      PostOptionsButtonRow(
        postBookmarked = readerPost.bookmarked,
        commentsLink = readerPost.commentsLink,
        onCommentsClick = onCommentsClick,
        onShareClick = onShareClick,
        onBookmarkClick = onBookmarkClick,
      )
    }
  }
}

@Composable
private fun BannerImageBlurred(postImage: String?, darkTheme: Boolean) {
  if (!postImage.isNullOrBlank()) {
    val gradientOverlayModifier =
      if (darkTheme) {
        Modifier.drawWithCache {
          val gradientColor = Color.Black
          val radialGradient =
            Brush.radialGradient(
              colors =
                listOf(
                  gradientColor,
                  gradientColor.copy(alpha = 0.0f),
                  gradientColor.copy(alpha = 0.0f)
                ),
              center = Offset(x = this.size.width, y = 40f)
            )

          val linearGradient =
            Brush.verticalGradient(
              colors = listOf(gradientColor, gradientColor.copy(alpha = 0.0f)),
            )

          onDrawWithContent {
            drawContent()
            drawRect(radialGradient)
            drawRect(linearGradient)
          }
        }
      } else {
        Modifier
      }

    val overlayColor = AppTheme.colorScheme.inversePrimary
    val colorMatrix = remember {
      ColorMatrix().apply {
        val sat = if (darkTheme) 1f else 5f
        setToSaturation(sat)
      }
    }

    val blurModifier =
      if (canBlurImage) {
        Modifier.graphicsLayer {
          val blurRadiusInPx = 100.dp.toPx()
          renderEffect = BlurEffect(blurRadiusInPx, blurRadiusInPx, TileMode.Decal)
          shape = RectangleShape
          clip = false
        }
      } else {
        Modifier
      }

    Box(
      modifier =
        Modifier.requiredHeightIn(max = 800.dp)
          .aspectRatio(1f)
          .then(blurModifier)
          .drawWithContent {
            drawContent()

            drawRect(
              color = overlayColor,
              blendMode = BlendMode.Luminosity,
            )
          }
          .then(gradientOverlayModifier)
    ) {
      AsyncImage(
        modifier = Modifier.matchParentSize(),
        url = postImage,
        contentDescription = null,
        contentScale = ContentScale.Crop,
        size = Size(128, 128),
        backgroundColor = AppTheme.colorScheme.surface,
        colorFilter = ColorFilter.colorMatrix(colorMatrix)
      )
    }
  }
}

@Composable
private fun PostSourcePill(
  feedIcon: String,
  feedName: String,
  config: PostMetadataConfig,
  onSourceClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  Box(modifier = modifier) {
    val postSourceTextColor =
      if (config.enablePostSource) {
        AppTheme.colorScheme.onSurface
      } else {
        AppTheme.colorScheme.onSurfaceVariant
      }

    Row(
      modifier =
        Modifier.background(
            MaterialTheme.colorScheme.secondary.copy(alpha = 0.08f),
            RoundedCornerShape(50)
          )
          .border(
            1.dp,
            MaterialTheme.colorScheme.secondary.copy(alpha = 0.16f),
            RoundedCornerShape(50)
          )
          .clip(RoundedCornerShape(50))
          .clickable(onClick = onSourceClick, enabled = config.enablePostSource)
          .padding(vertical = 6.dp)
          .padding(start = 8.dp, end = 12.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      FeedIcon(
        modifier = Modifier.requiredSize(16.dp).clip(RoundedCornerShape(4.dp)),
        url = feedIcon,
        contentDescription = null,
      )

      Spacer(Modifier.requiredWidth(6.dp))

      Text(
        style = MaterialTheme.typography.labelMedium,
        maxLines = 1,
        text = feedName,
        color = postSourceTextColor,
        overflow = TextOverflow.Ellipsis
      )
    }
  }
}

@Composable
private fun PostOptionsButtonRow(
  postBookmarked: Boolean,
  commentsLink: String?,
  onCommentsClick: () -> Unit,
  onShareClick: () -> Unit,
  onBookmarkClick: () -> Unit,
) {
  Row(modifier = Modifier.semantics { isTraversalGroup = true }) {
    if (!commentsLink.isNullOrBlank()) {
      val commentsLabel = LocalStrings.current.comments
      PostOptionIconButton(
        modifier =
          Modifier.semantics {
            role = Role.Button
            contentDescription = commentsLabel
          },
        icon = TwineIcons.Comments,
        iconTint = AppTheme.colorScheme.onSurfaceVariant,
        onClick = onCommentsClick
      )
    }

    val sharedLabel = LocalStrings.current.share
    PostOptionIconButton(
      modifier =
        Modifier.semantics {
          role = Role.Button
          contentDescription = sharedLabel
        },
      icon = TwineIcons.Share,
      iconTint = AppTheme.colorScheme.onSurfaceVariant,
      onClick = onShareClick
    )

    val bookmarkLabel =
      if (postBookmarked) {
        LocalStrings.current.unBookmark
      } else {
        LocalStrings.current.bookmark
      }
    PostOptionIconButton(
      modifier =
        Modifier.semantics {
          role = Role.Button
          contentDescription = bookmarkLabel
        },
      icon =
        if (postBookmarked) {
          TwineIcons.Bookmarked
        } else {
          TwineIcons.Bookmark
        },
      iconTint =
        if (postBookmarked) {
          AppTheme.colorScheme.tintedForeground
        } else {
          AppTheme.colorScheme.onSurfaceVariant
        },
      onClick = onBookmarkClick
    )
  }
}

@Composable
private fun PostOptionIconButton(
  icon: ImageVector,
  modifier: Modifier = Modifier,
  iconTint: Color = AppTheme.colorScheme.textEmphasisHigh,
  onClick: () -> Unit,
) {
  Box(
    modifier =
      Modifier.requiredSize(40.dp)
        .clip(MaterialTheme.shapes.small)
        .clickable(onClick = onClick)
        .then(modifier),
    contentAlignment = Alignment.Center
  ) {
    Icon(
      imageVector = icon,
      contentDescription = null,
      tint = iconTint,
      modifier = Modifier.size(20.dp)
    )
  }
}

@Serializable
enum class ReaderProcessingProgress {
  Loading,
  Idle
}

@Serializable
data class ReaderContent(
  val excerpt: String?,
  val content: String?,
)
