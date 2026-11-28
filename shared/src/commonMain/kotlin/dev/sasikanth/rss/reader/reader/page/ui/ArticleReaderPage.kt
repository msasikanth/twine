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

package dev.sasikanth.rss.reader.reader.page.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.text.selection.DisableSelection
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.mikepenz.markdown.compose.components.MarkdownComponents
import com.mikepenz.markdown.model.State
import dev.sasikanth.rss.reader.components.TranslucentButton
import dev.sasikanth.rss.reader.core.model.local.ResolvedPost
import dev.sasikanth.rss.reader.core.model.local.ThemeVariant
import dev.sasikanth.rss.reader.reader.ReaderScreenArgs
import dev.sasikanth.rss.reader.reader.page.ReaderProcessingProgress
import dev.sasikanth.rss.reader.reader.ui.ImageClickEvent
import dev.sasikanth.rss.reader.reader.ui.READER_BACKDROP_SCROLL_FADE_DISTANCE
import dev.sasikanth.rss.reader.reader.ui.resolveReaderBackdropScrollVisibility
import dev.sasikanth.rss.reader.resources.icons.OpenBrowser
import dev.sasikanth.rss.reader.resources.icons.TwineIcons
import dev.sasikanth.rss.reader.ui.AppTheme
import org.jetbrains.compose.resources.stringResource
import twine.shared.generated.resources.Res
import twine.shared.generated.resources.noReaderContent
import twine.shared.generated.resources.openWebsite

@Composable
internal fun ArticleReaderPage(
  readerPost: ResolvedPost,
  showFullArticle: Boolean,
  page: Int,
  pagerState: PagerState,
  excerpt: String,
  markdownContentState: State,
  contentParsingProgress: ReaderProcessingProgress,
  markdownComponents: MarkdownComponents,
  isDarkTheme: Boolean,
  themeVariant: ThemeVariant,
  fromScreen: ReaderScreenArgs.FromScreen,
  contentPaddingValues: PaddingValues,
  onBookmarkClick: () -> Unit,
  onCommentsClick: () -> Unit,
  onMarkAsUnread: () -> Unit,
  onOpenWebsite: () -> Unit,
  onImageClick: (ImageClickEvent) -> Unit,
  onContentWidthChanged: (Dp) -> Unit,
  modifier: Modifier = Modifier,
  onBackdropVisibilityProviderChanged: ((() -> Float)?) -> Unit = {},
) {
  val listState = rememberLazyListState()
  val fadeDistancePx = with(LocalDensity.current) { READER_BACKDROP_SCROLL_FADE_DISTANCE.toPx() }
  val backdropVisibilityProvider =
    remember(listState, fadeDistancePx) {
      {
        resolveReaderBackdropScrollVisibility(
          firstVisibleItemIndex = listState.firstVisibleItemIndex,
          firstVisibleItemScrollOffset = listState.firstVisibleItemScrollOffset,
          fadeDistancePx = fadeDistancePx,
        )
      }
    }
  DisposableEffect(readerPost.id, backdropVisibilityProvider) {
    onBackdropVisibilityProviderChanged(backdropVisibilityProvider)
    onDispose { onBackdropVisibilityProviderChanged(null) }
  }

  val topPadding = contentPaddingValues.calculateTopPadding()
  val bottomPadding = contentPaddingValues.calculateBottomPadding()
  val lazyColumnContentPadding =
    remember(topPadding, bottomPadding) {
      PaddingValues(top = topPadding, bottom = bottomPadding + 24.dp)
    }

  LazyColumn(
    modifier =
      modifier
        .fillMaxSize()
        .readerContentWidthAndScrims(
          listState = listState,
          topPadding = topPadding,
          onContentWidthChanged = onContentWidthChanged,
        ),
    state = listState,
    overscrollEffect = null,
    contentPadding = lazyColumnContentPadding,
  ) {
    item(key = "reader-header", contentType = "reader-header") {
      PostHeader(
        modifier = Modifier.padding(bottom = 36.dp),
        readerPost = readerPost,
        showFullArticle = showFullArticle,
        page = page,
        pagerState = pagerState,
        excerpt = excerpt,
        darkTheme = isDarkTheme,
        themeVariant = themeVariant,
        fromScreen = fromScreen,
        onBookmarkClick = onBookmarkClick,
        onCommentsClick = onCommentsClick,
        onMarkAsUnread = onMarkAsUnread,
        onImageClick = onImageClick,
      )
    }

    if (contentParsingProgress == ReaderProcessingProgress.Loading) {
      item(key = "progress-indicator", contentType = "progress-indicator") {
        ReaderProgressIndicator()
      }
    }

    when (markdownContentState) {
      is State.Success -> {
        readerMarkdownItems(
          postId = readerPost.id,
          markdownContentState = markdownContentState,
          markdownComponents = markdownComponents,
        )
      }
      else -> {
        if (contentParsingProgress == ReaderProcessingProgress.Idle) {
          item(key = "no-reader-content", contentType = "no-reader-content") {
            DisableSelection {
              Column(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp),
              ) {
                Text(
                  text = stringResource(Res.string.noReaderContent),
                  style = MaterialTheme.typography.bodyMedium,
                  color = AppTheme.colorScheme.onSurfaceVariant,
                  textAlign = TextAlign.Center,
                )

                if (readerPost.link.isNotBlank()) {
                  TranslucentButton(
                    text = stringResource(Res.string.openWebsite),
                    leadingIcon = TwineIcons.OpenBrowser,
                    onClick = onOpenWebsite,
                  )
                }
              }
            }
          }
        }
      }
    }
  }
}
