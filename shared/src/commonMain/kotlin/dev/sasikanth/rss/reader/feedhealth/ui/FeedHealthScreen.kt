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

package dev.sasikanth.rss.reader.feedhealth.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SecondaryScrollableTabRow
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import dev.sasikanth.rss.reader.components.ContextActionItem
import dev.sasikanth.rss.reader.components.ContextActionsBottomBar
import dev.sasikanth.rss.reader.components.SimpleTopAppBar
import dev.sasikanth.rss.reader.components.image.FeedIcon
import dev.sasikanth.rss.reader.core.model.local.FeedHealthInfo
import dev.sasikanth.rss.reader.feedhealth.FeedHealthEvent
import dev.sasikanth.rss.reader.feedhealth.FeedHealthState
import dev.sasikanth.rss.reader.feedhealth.FeedHealthViewModel
import dev.sasikanth.rss.reader.feeds.ui.SelectedCheckIndicator
import dev.sasikanth.rss.reader.resources.icons.Delete
import dev.sasikanth.rss.reader.resources.icons.RemoveFeed
import dev.sasikanth.rss.reader.resources.icons.TwineIcons
import dev.sasikanth.rss.reader.ui.AppTheme
import dev.sasikanth.rss.reader.ui.LocalTranslucentStyles
import dev.sasikanth.rss.reader.utils.restrictContentWidth
import kotlin.math.abs
import kotlin.time.Clock
import kotlin.time.Instant
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import twine.shared.generated.resources.Res
import twine.shared.generated.resources.buttonCancel
import twine.shared.generated.resources.feedHealthBrokenDesc
import twine.shared.generated.resources.feedHealthBrokenFetchErrors
import twine.shared.generated.resources.feedHealthBulkUnsubscribeDialogDesc
import twine.shared.generated.resources.feedHealthBulkUnsubscribeDialogTitle
import twine.shared.generated.resources.feedHealthHighVolumeDesc
import twine.shared.generated.resources.feedHealthLastPostDate
import twine.shared.generated.resources.feedHealthLeastReadDesc
import twine.shared.generated.resources.feedHealthNeverUpdated
import twine.shared.generated.resources.feedHealthNoBrokenFeeds
import twine.shared.generated.resources.feedHealthNoHighVolumeFeeds
import twine.shared.generated.resources.feedHealthNoLeastReadFeeds
import twine.shared.generated.resources.feedHealthNoStaleFeeds
import twine.shared.generated.resources.feedHealthReadRatio
import twine.shared.generated.resources.feedHealthStaleDesc
import twine.shared.generated.resources.feedHealthTabBroken
import twine.shared.generated.resources.feedHealthTabHighVolume
import twine.shared.generated.resources.feedHealthTabLeastRead
import twine.shared.generated.resources.feedHealthTabStale
import twine.shared.generated.resources.feedHealthTitle
import twine.shared.generated.resources.feedHealthTotalPosts
import twine.shared.generated.resources.feedHealthUndoAction
import twine.shared.generated.resources.feedHealthUnsubscribeConfirm
import twine.shared.generated.resources.feedHealthUnsubscribeSelectedAction
import twine.shared.generated.resources.feedHealthUnsubscribedSnackbar

@Composable
internal fun FeedHealthScreen(
  viewModel: FeedHealthViewModel,
  goBack: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val state by viewModel.state.collectAsState()

  FeedHealthContent(
    state = state,
    dispatch = viewModel::dispatch,
    goBack = goBack,
    modifier = modifier,
  )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun FeedHealthContent(
  state: FeedHealthState,
  dispatch: (FeedHealthEvent) -> Unit,
  goBack: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val layoutDirection = LocalLayoutDirection.current
  val pagerState = rememberPagerState(pageCount = { 4 })
  val coroutineScope = rememberCoroutineScope()
  val snackbarHostState = remember { SnackbarHostState() }

  var showBulkUnsubscribeDialog by remember { mutableStateOf(false) }

  // Undo unsubscribe snackbar
  val pendingUnsubscribe = state.pendingUnsubscribe
  val unsubscribedMessage =
    pendingUnsubscribe?.let {
      stringResource(Res.string.feedHealthUnsubscribedSnackbar, it.feedName)
    }
  val undoLabel = stringResource(Res.string.feedHealthUndoAction)
  LaunchedEffect(pendingUnsubscribe) {
    if (pendingUnsubscribe != null && unsubscribedMessage != null) {
      val result =
        snackbarHostState.showSnackbar(
          message = unsubscribedMessage,
          actionLabel = undoLabel,
          duration = SnackbarDuration.Short,
        )
      if (result == SnackbarResult.ActionPerformed) {
        dispatch(FeedHealthEvent.UndoUnsubscribe)
      }
    }
  }

  Scaffold(
    modifier = modifier,
    topBar = {
      SimpleTopAppBar(
        title = stringResource(Res.string.feedHealthTitle),
        onBackClick = {
          if (state.isSelectionMode) {
            dispatch(FeedHealthEvent.ClearSelection)
          } else {
            goBack()
          }
        },
        showDivider = false,
      )
    },
    bottomBar = {
      AnimatedVisibility(
        visible = state.isSelectionMode,
        enter = slideInVertically { it } + fadeIn(),
        exit = slideOutVertically { it } + fadeOut(),
      ) {
        ContextActionsBottomBar(onCancel = { dispatch(FeedHealthEvent.ClearSelection) }) {
          ContextActionItem(
            modifier = Modifier.weight(1f),
            icon = TwineIcons.Delete,
            label = stringResource(Res.string.feedHealthUnsubscribeSelectedAction),
            onClick = { showBulkUnsubscribeDialog = true },
          )
        }
      }
    },
    snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
    content = { padding ->
      if (state.isLoading) {
        Box(
          modifier = Modifier.fillMaxSize().padding(padding),
          contentAlignment = Alignment.Center,
        ) {
          CircularProgressIndicator()
        }
      } else {
        val healthData = state.healthData
        if (healthData == null) {
          Box(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentAlignment = Alignment.Center,
          ) {
            CircularProgressIndicator()
          }
        } else {
          Column(
            modifier =
              Modifier.restrictContentWidth()
                .padding(
                  top = padding.calculateTopPadding(),
                  start = padding.calculateStartPadding(layoutDirection),
                  end = padding.calculateEndPadding(layoutDirection),
                )
          ) {
            val tabs =
              listOf(
                stringResource(Res.string.feedHealthTabStale) to healthData.staleFeeds.size,
                stringResource(Res.string.feedHealthTabHighVolume) to
                  healthData.highVolumeFeeds.size,
                stringResource(Res.string.feedHealthTabLeastRead) to healthData.leastReadFeeds.size,
                stringResource(Res.string.feedHealthTabBroken) to healthData.brokenFeeds.size,
              )

            val tabDescriptions =
              listOf(
                stringResource(Res.string.feedHealthStaleDesc),
                stringResource(Res.string.feedHealthHighVolumeDesc),
                stringResource(Res.string.feedHealthLeastReadDesc),
                stringResource(Res.string.feedHealthBrokenDesc),
              )

            SecondaryScrollableTabRow(
              selectedTabIndex = pagerState.currentPage,
              containerColor = AppTheme.colorScheme.backdrop,
              contentColor = AppTheme.colorScheme.onSurface,
              indicator = {
                TabRowDefaults.SecondaryIndicator(
                  modifier = Modifier.tabIndicatorOffset(pagerState.currentPage),
                  color = AppTheme.colorScheme.primary,
                )
              },
              divider =
                @Composable { HorizontalDivider(color = AppTheme.colorScheme.outlineVariant) },
              edgePadding = 24.dp,
            ) {
              tabs.forEachIndexed { index, (title, count) ->
                Tab(
                  modifier = Modifier.height(56.dp),
                  selected = pagerState.currentPage == index,
                  onClick = { coroutineScope.launch { pagerState.animateScrollToPage(index) } },
                  text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                      Text(
                        text = title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Medium,
                      )
                      if (count > 0) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Badge(
                          containerColor = AppTheme.colorScheme.primary,
                          contentColor = AppTheme.colorScheme.onPrimary,
                        ) {
                          Text(text = "$count", style = MaterialTheme.typography.labelSmall)
                        }
                      }
                    }
                  },
                )
              }
            }

            HorizontalPager(state = pagerState, modifier = Modifier.fillMaxSize()) { page ->
              val (currentTabFeeds, emptyMessage) =
                when (page) {
                  0 -> healthData.staleFeeds to stringResource(Res.string.feedHealthNoStaleFeeds)
                  1 ->
                    healthData.highVolumeFeeds to
                      stringResource(Res.string.feedHealthNoHighVolumeFeeds)
                  2 ->
                    healthData.leastReadFeeds to
                      stringResource(Res.string.feedHealthNoLeastReadFeeds)
                  else ->
                    healthData.brokenFeeds to stringResource(Res.string.feedHealthNoBrokenFeeds)
                }

              LazyColumn(
                modifier =
                  Modifier.fillMaxSize()
                    .graphicsLayer(compositingStrategy = CompositingStrategy.Offscreen)
                    .drawWithContent {
                      drawContent()
                      drawRect(
                        brush =
                          Brush.verticalGradient(
                            0f to Color.Black,
                            0.9f to Color.Black,
                            1f to Color.Transparent,
                          ),
                        blendMode = BlendMode.DstIn,
                      )
                    },
                contentPadding =
                  PaddingValues(
                    start = padding.calculateStartPadding(layoutDirection) + 24.dp,
                    top = 4.dp,
                    end = padding.calculateEndPadding(layoutDirection) + 24.dp,
                    bottom = padding.calculateBottomPadding() + 80.dp,
                  ),
              ) {
                if (currentTabFeeds.isNotEmpty()) {
                  item {
                    Text(
                      text = tabDescriptions[page],
                      style = MaterialTheme.typography.bodySmall,
                      color = AppTheme.colorScheme.onSurfaceVariant,
                      modifier = Modifier.padding(top = 12.dp, bottom = 12.dp),
                    )
                  }

                  items(currentTabFeeds, key = { it.id }) { feed ->
                    FeedHealthItem(
                      feed = feed,
                      tabIndex = page,
                      isSelected = feed.id in state.selectedFeedIds,
                      isSelectionMode = state.isSelectionMode,
                      onLongClick = { dispatch(FeedHealthEvent.ToggleFeedSelection(feed.id)) },
                      onItemClick = {
                        if (state.isSelectionMode) {
                          dispatch(FeedHealthEvent.ToggleFeedSelection(feed.id))
                        }
                      },
                      onUnsubscribeClick = { dispatch(FeedHealthEvent.UnsubscribeFeed(feed.id)) },
                      modifier = Modifier.padding(vertical = 12.dp).animateItem(),
                    )
                  }
                } else {
                  item {
                    Box(
                      modifier = Modifier.fillParentMaxHeight(0.7f).fillMaxWidth(),
                      contentAlignment = Alignment.Center,
                    ) {
                      Text(
                        text = emptyMessage,
                        style = MaterialTheme.typography.bodyLarge,
                        color = AppTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                      )
                    }
                  }
                }
              }
            }
          }
        }
      }
    },
    containerColor = AppTheme.colorScheme.backdrop,
    contentColor = Color.Unspecified,
  )

  // Bulk unsubscribe confirmation dialog
  if (showBulkUnsubscribeDialog) {
    val count = state.selectedFeedIds.size
    AlertDialog(
      onDismissRequest = { showBulkUnsubscribeDialog = false },
      title = {
        Text(text = stringResource(Res.string.feedHealthBulkUnsubscribeDialogTitle, count))
      },
      text = {
        Text(
          text = stringResource(Res.string.feedHealthBulkUnsubscribeDialogDesc),
          color = AppTheme.colorScheme.onSurfaceVariant,
        )
      },
      confirmButton = {
        TextButton(
          onClick = {
            showBulkUnsubscribeDialog = false
            dispatch(FeedHealthEvent.UnsubscribeSelectedFeeds)
          },
          colors = ButtonDefaults.textButtonColors(contentColor = AppTheme.colorScheme.error),
        ) {
          Text(text = stringResource(Res.string.feedHealthUnsubscribeSelectedAction))
        }
      },
      dismissButton = {
        TextButton(
          onClick = { showBulkUnsubscribeDialog = false },
          colors = ButtonDefaults.textButtonColors(contentColor = AppTheme.colorScheme.primary),
        ) {
          Text(text = stringResource(Res.string.buttonCancel))
        }
      },
      containerColor = AppTheme.colorScheme.surfaceContainerLow,
      titleContentColor = AppTheme.colorScheme.onSurface,
      textContentColor = AppTheme.colorScheme.onSurfaceVariant,
    )
  }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun FeedHealthItem(
  feed: FeedHealthInfo,
  tabIndex: Int,
  isSelected: Boolean,
  isSelectionMode: Boolean,
  onLongClick: () -> Unit,
  onItemClick: () -> Unit,
  onUnsubscribeClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val translucentStyle = LocalTranslucentStyles.current
  val backgroundColor by
    animateColorAsState(
      if (isSelected) {
        translucentStyle.default.background
      } else {
        Color.Transparent
      }
    )

  Row(
    modifier =
      modifier
        .fillMaxWidth()
        .clip(RoundedCornerShape(16.dp))
        .background(backgroundColor)
        .combinedClickable(onLongClick = onLongClick, onClick = onItemClick)
        .padding(start = 12.dp, top = 8.dp, end = 8.dp, bottom = 8.dp),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    FeedIcon(
      icon = feed.icon,
      homepageLink = feed.homepageLink,
      showFeedFavIcon = feed.showFeedFavIcon,
      contentDescription = null,
      modifier = Modifier.requiredSize(40.dp).clip(RoundedCornerShape(8.dp)),
    )

    Spacer(modifier = Modifier.width(16.dp))

    Column(modifier = Modifier.weight(1f)) {
      Text(
        text = feed.name,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Medium,
        color = AppTheme.colorScheme.onSurface,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
      )

      when (tabIndex) {
        0 -> {
          // Stale tab: relative timestamp
          val lastPostDate = feed.lastPostDate
          val subtitle =
            if (lastPostDate != null) {
              val relativeStr = formatRelativeTime(lastPostDate)
              stringResource(Res.string.feedHealthLastPostDate, relativeStr)
            } else {
              stringResource(Res.string.feedHealthNeverUpdated)
            }
          Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = AppTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
          )
        }
        1 -> {
          // High Volume tab: article count
          Text(
            text = stringResource(Res.string.feedHealthTotalPosts, feed.totalPostsCount.toInt()),
            style = MaterialTheme.typography.bodyMedium,
            color = AppTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
          )
        }
        2 -> {
          // Least Read tab: read ratio with 3-tier colour
          val readRatio =
            if (feed.totalPostsCount > 0)
              feed.readPostsCount.toFloat() / feed.totalPostsCount.toFloat()
            else 0f
          val percentage = (readRatio * 100).toInt()
          val percentageColor =
            when {
              readRatio < 0.2f -> AppTheme.colorScheme.error
              readRatio < 0.5f -> Color(0xFFFFA000) // Amber
              else -> AppTheme.colorScheme.primary
            }

          Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            Text(
              text = "$percentage% read",
              style = MaterialTheme.typography.bodyMedium,
              color = percentageColor,
              fontWeight = FontWeight.Medium,
              maxLines = 1,
            )
            Text(
              text = " • ",
              style = MaterialTheme.typography.bodyMedium,
              color = AppTheme.colorScheme.onSurfaceVariant,
              maxLines = 1,
            )
            Text(
              text =
                stringResource(
                  Res.string.feedHealthReadRatio,
                  feed.readPostsCount.toInt(),
                  feed.totalPostsCount.toInt(),
                ),
              style = MaterialTheme.typography.bodyMedium,
              color = AppTheme.colorScheme.onSurfaceVariant,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis,
              modifier = Modifier.weight(1f),
            )
          }
        }
        else -> {
          // Broken tab: consecutive error count
          Text(
            text =
              stringResource(Res.string.feedHealthBrokenFetchErrors, feed.totalPostsCount.toInt()),
            style = MaterialTheme.typography.bodyMedium,
            color = AppTheme.colorScheme.error,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
          )
        }
      }
    }

    if (isSelectionMode) {
      SelectedCheckIndicator(selected = isSelected, modifier = Modifier.requiredSize(40.dp))
    } else {
      IconButton(onClick = onUnsubscribeClick, modifier = Modifier.requiredSize(40.dp)) {
        Icon(
          imageVector = TwineIcons.RemoveFeed,
          contentDescription = stringResource(Res.string.feedHealthUnsubscribeConfirm),
          tint = AppTheme.colorScheme.error,
          modifier = Modifier.requiredSize(20.dp),
        )
      }
    }
  }
}

private fun formatRelativeTime(instant: Instant): String {
  val now = Clock.System.now()
  val totalDays = abs((now - instant).inWholeDays).toInt()
  return when {
    totalDays < 1 -> "today"
    totalDays < 7 -> "${totalDays}d ago"
    totalDays < 30 -> "${totalDays / 7}w ago"
    totalDays < 365 -> "${totalDays / 30}mo ago"
    else -> {
      val years = totalDays / 365
      if (years == 1) "1 year ago" else "$years years ago"
    }
  }
}
