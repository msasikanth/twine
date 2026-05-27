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
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import dev.sasikanth.rss.reader.components.SimpleTopAppBar
import dev.sasikanth.rss.reader.components.image.FeedIcon
import dev.sasikanth.rss.reader.core.model.local.FeedHealthInfo
import dev.sasikanth.rss.reader.feedhealth.FeedHealthEvent
import dev.sasikanth.rss.reader.feedhealth.FeedHealthState
import dev.sasikanth.rss.reader.feedhealth.FeedHealthViewModel
import dev.sasikanth.rss.reader.resources.icons.RemoveFeed
import dev.sasikanth.rss.reader.resources.icons.TwineIcons
import dev.sasikanth.rss.reader.ui.AppTheme
import kotlin.time.Instant
import kotlinx.coroutines.launch
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.compose.resources.stringResource
import twine.shared.generated.resources.Res
import twine.shared.generated.resources.buttonCancel
import twine.shared.generated.resources.feedHealthLastPostDate
import twine.shared.generated.resources.feedHealthNeverUpdated
import twine.shared.generated.resources.feedHealthNoHighVolumeFeeds
import twine.shared.generated.resources.feedHealthNoLeastReadFeeds
import twine.shared.generated.resources.feedHealthNoStaleFeeds
import twine.shared.generated.resources.feedHealthReadRatio
import twine.shared.generated.resources.feedHealthTabHighVolume
import twine.shared.generated.resources.feedHealthTabLeastRead
import twine.shared.generated.resources.feedHealthTabStale
import twine.shared.generated.resources.feedHealthTitle
import twine.shared.generated.resources.feedHealthTotalPosts
import twine.shared.generated.resources.feedHealthUnsubscribeConfirm
import twine.shared.generated.resources.feedHealthUnsubscribeDialogDesc
import twine.shared.generated.resources.feedHealthUnsubscribeDialogTitle

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

@Composable
private fun FeedHealthContent(
  state: FeedHealthState,
  dispatch: (FeedHealthEvent) -> Unit,
  goBack: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val layoutDirection = LocalLayoutDirection.current
  val pagerState = rememberPagerState(pageCount = { 3 })
  val coroutineScope = rememberCoroutineScope()
  var feedToUnsubscribe by remember { mutableStateOf<FeedHealthInfo?>(null) }

  Scaffold(
    modifier = modifier,
    topBar = {
      SimpleTopAppBar(
        title = stringResource(Res.string.feedHealthTitle),
        onBackClick = goBack,
        showDivider = false,
      )
    },
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
          Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            val tabs =
              listOf(
                stringResource(Res.string.feedHealthTabStale),
                stringResource(Res.string.feedHealthTabHighVolume),
                stringResource(Res.string.feedHealthTabLeastRead),
              )

            SecondaryTabRow(
              selectedTabIndex = pagerState.currentPage,
              modifier = Modifier,
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
            ) {
              tabs.forEachIndexed { index, title ->
                Tab(
                  modifier = Modifier.height(56.dp),
                  selected = pagerState.currentPage == index,
                  onClick = { coroutineScope.launch { pagerState.animateScrollToPage(index) } },
                  text = {
                    Text(
                      text = title,
                      style = MaterialTheme.typography.titleSmall,
                      fontWeight = FontWeight.Medium,
                    )
                  },
                )
              }
            }

            HorizontalPager(state = pagerState, modifier = Modifier.fillMaxSize()) { page ->
              val currentTabFeeds =
                when (page) {
                  0 -> healthData.staleFeeds
                  1 -> healthData.highVolumeFeeds
                  else -> healthData.leastReadFeeds
                }

              val emptyMessage =
                when (page) {
                  0 -> stringResource(Res.string.feedHealthNoStaleFeeds)
                  1 -> stringResource(Res.string.feedHealthNoHighVolumeFeeds)
                  else -> stringResource(Res.string.feedHealthNoLeastReadFeeds)
                }

              LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding =
                  PaddingValues(
                    start = padding.calculateStartPadding(layoutDirection) + 24.dp,
                    top = 16.dp,
                    end = padding.calculateEndPadding(layoutDirection) + 24.dp,
                    bottom = padding.calculateBottomPadding() + 80.dp,
                  ),
              ) {
                if (currentTabFeeds.isNotEmpty()) {
                  items(currentTabFeeds) { feed ->
                    FeedHealthItem(
                      feed = feed,
                      tabIndex = page,
                      onUnsubscribeClick = { feedToUnsubscribe = feed },
                      modifier = Modifier.padding(vertical = 8.dp),
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

  // Unsubscribe Confirmation Dialog
  feedToUnsubscribe?.let { feed ->
    AlertDialog(
      onDismissRequest = { feedToUnsubscribe = null },
      title = { Text(text = stringResource(Res.string.feedHealthUnsubscribeDialogTitle)) },
      text = {
        Text(
          text = stringResource(Res.string.feedHealthUnsubscribeDialogDesc, feed.name),
          color = AppTheme.colorScheme.onSurfaceVariant,
        )
      },
      confirmButton = {
        TextButton(
          onClick = {
            dispatch(FeedHealthEvent.UnsubscribeFeed(feed.id))
            feedToUnsubscribe = null
          },
          colors = ButtonDefaults.textButtonColors(contentColor = AppTheme.colorScheme.error),
        ) {
          Text(text = stringResource(Res.string.feedHealthUnsubscribeConfirm))
        }
      },
      dismissButton = {
        TextButton(
          onClick = { feedToUnsubscribe = null },
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

@Composable
private fun FeedHealthItem(
  feed: FeedHealthInfo,
  tabIndex: Int,
  onUnsubscribeClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Row(modifier = modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
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

      val subtitle =
        when (tabIndex) {
          0 -> {
            val dateStr = feed.lastPostDate?.let { formatInstant(it) }
            if (dateStr != null) {
              stringResource(Res.string.feedHealthLastPostDate, dateStr)
            } else {
              stringResource(Res.string.feedHealthNeverUpdated)
            }
          }
          1 -> stringResource(Res.string.feedHealthTotalPosts, feed.totalPostsCount.toInt())
          else ->
            stringResource(
              Res.string.feedHealthReadRatio,
              feed.readPostsCount.toInt(),
              feed.totalPostsCount.toInt(),
            )
        }

      Text(
        text = subtitle,
        style = MaterialTheme.typography.bodyMedium,
        color = AppTheme.colorScheme.onSurfaceVariant,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
      )
    }

    Spacer(modifier = Modifier.width(16.dp))

    IconButton(onClick = onUnsubscribeClick) {
      Icon(
        imageVector = TwineIcons.RemoveFeed,
        contentDescription = stringResource(Res.string.feedHealthUnsubscribeConfirm),
        tint = AppTheme.colorScheme.error,
      )
    }
  }
}

private fun formatInstant(instant: Instant): String {
  val localDateTime = instant.toLocalDateTime(TimeZone.currentSystemDefault())
  val month = localDateTime.month.name.take(3).lowercase().replaceFirstChar { it.uppercase() }
  return "$month ${localDateTime.day}, ${localDateTime.year}"
}
