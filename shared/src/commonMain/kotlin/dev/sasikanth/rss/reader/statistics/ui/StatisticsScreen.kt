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

package dev.sasikanth.rss.reader.statistics.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import dev.sasikanth.rss.reader.components.CircularIconButton
import dev.sasikanth.rss.reader.components.SubHeader
import dev.sasikanth.rss.reader.components.image.FeedIcon
import dev.sasikanth.rss.reader.core.model.local.FeedReadCount
import dev.sasikanth.rss.reader.core.model.local.ReadingTrend
import dev.sasikanth.rss.reader.resources.icons.ArrowBack
import dev.sasikanth.rss.reader.resources.icons.TwineIcons
import dev.sasikanth.rss.reader.statistics.StatisticsViewModel
import dev.sasikanth.rss.reader.ui.AppTheme
import dev.sasikanth.rss.reader.utils.formatReadingTrendDate
import kotlin.time.Clock
import kotlinx.collections.immutable.ImmutableList
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.datetime.todayIn
import org.jetbrains.compose.resources.stringResource
import twine.shared.generated.resources.Res
import twine.shared.generated.resources.buttonGoBack
import twine.shared.generated.resources.statistics
import twine.shared.generated.resources.statisticsLess
import twine.shared.generated.resources.statisticsMore
import twine.shared.generated.resources.statisticsNoData
import twine.shared.generated.resources.statisticsReadingTrends
import twine.shared.generated.resources.statisticsTopFeeds
import twine.shared.generated.resources.statisticsTotalRead

@Composable
fun StatisticsScreen(
  viewModel: StatisticsViewModel,
  goBack: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val state by viewModel.state.collectAsState()
  val layoutDirection = LocalLayoutDirection.current

  Scaffold(
    modifier = modifier,
    topBar = {
      Box {
        CenterAlignedTopAppBar(
          title = {
            Text(
              text = stringResource(Res.string.statistics),
              color = AppTheme.colorScheme.onSurface,
              style = MaterialTheme.typography.titleMedium,
            )
          },
          navigationIcon = {
            CircularIconButton(
              modifier = Modifier.padding(start = 12.dp),
              icon = TwineIcons.ArrowBack,
              label = stringResource(Res.string.buttonGoBack),
              onClick = goBack,
            )
          },
          colors =
            TopAppBarDefaults.topAppBarColors(
              containerColor = AppTheme.colorScheme.surface,
              navigationIconContentColor = AppTheme.colorScheme.onSurface,
              titleContentColor = AppTheme.colorScheme.onSurface,
              actionIconContentColor = AppTheme.colorScheme.onSurface,
            ),
        )

        HorizontalDivider(
          modifier = Modifier.fillMaxWidth().align(Alignment.BottomStart),
          color = AppTheme.colorScheme.outlineVariant,
        )
      }
    },
    containerColor = AppTheme.colorScheme.backdrop,
  ) { padding ->
    if (state.isLoading) {
      Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
      }
    } else {
      val statistics = state.statistics
      if (statistics == null || statistics.totalReadCount == 0L) {
        Box(
          modifier = Modifier.fillMaxSize().padding(padding),
          contentAlignment = Alignment.Center,
        ) {
          Text(
            text = stringResource(Res.string.statisticsNoData),
            style = MaterialTheme.typography.bodyLarge,
            color = AppTheme.colorScheme.onSurfaceVariant,
          )
        }
      } else {
        LazyColumn(
          modifier = Modifier.fillMaxSize(),
          contentPadding =
            PaddingValues(
              start = padding.calculateStartPadding(layoutDirection),
              end = padding.calculateEndPadding(layoutDirection),
              top = padding.calculateTopPadding(),
              bottom = padding.calculateBottomPadding() + 80.dp,
            ),
        ) {
          item {
            StatisticCard(
              title = stringResource(Res.string.statisticsTotalRead),
              value = statistics.totalReadCount.toString(),
            )
          }

          item { Spacer(modifier = Modifier.height(24.dp)) }

          item {
            ReadingHeatmap(
              readingTrends = statistics.readingTrends,
              modifier = Modifier.padding(horizontal = 24.dp),
            )
          }

          item { Spacer(modifier = Modifier.height(24.dp)) }

          if (statistics.topFeeds.isNotEmpty()) {
            item { SubHeader(text = stringResource(Res.string.statisticsTopFeeds)) }

            items(statistics.topFeeds.take(10)) { feedReadCount ->
              FeedReadCountItem(
                feedReadCount = feedReadCount,
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp),
              )
            }

            item { Spacer(modifier = Modifier.height(24.dp)) }
          }

          if (statistics.readingTrends.isNotEmpty()) {
            item { SubHeader(text = stringResource(Res.string.statisticsReadingTrends)) }

            items(statistics.readingTrends.take(30)) { trend ->
              ReadingTrendItem(
                trend = trend,
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
              )
            }
          }
        }
      }
    }
  }
}

@Composable
private fun ReadingHeatmap(
  readingTrends: ImmutableList<ReadingTrend>,
  modifier: Modifier = Modifier,
) {
  val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
  val sixMonthsAgo = today.minus(6, DateTimeUnit.MONTH)
  val startDay = sixMonthsAgo.minus((sixMonthsAgo.dayOfWeek.ordinal + 1) % 7, DateTimeUnit.DAY)
  val allDates =
    remember(startDay, today) {
      val dates = mutableListOf<LocalDate>()
      var currentDate = startDay
      while (currentDate <= today) {
        dates.add(currentDate)
        currentDate = currentDate.plus(1, DateTimeUnit.DAY)
      }
      dates
    }
  val trendsMap =
    remember(readingTrends) {
      readingTrends.associate {
        try {
          LocalDate.parse(it.date) to it.count
        } catch (_: Exception) {
          today to 0L
        }
      }
    }
  val weeks = remember(allDates) { allDates.chunked(7) }

  Column(
    modifier =
      modifier
        .fillMaxWidth()
        .clip(RoundedCornerShape(16.dp))
        .background(AppTheme.colorScheme.surface)
        .horizontalScroll(rememberScrollState(), reverseScrolling = true)
        .padding(16.dp)
  ) {
    Column {
      Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        weeks.forEachIndexed { index, week ->
          val firstDay = week.first()
          val prevMonth = weeks.getOrNull(index - 1)?.first()?.month
          val showMonth = index == 0 || firstDay.month != prevMonth

          Box(modifier = Modifier.width(12.dp)) {
            if (showMonth) {
              val monthName =
                firstDay.month.name.take(3).lowercase().replaceFirstChar { it.uppercase() }
              Text(
                modifier = Modifier.requiredWidth(40.dp),
                text = monthName,
                style = MaterialTheme.typography.labelSmall,
                color = AppTheme.colorScheme.textEmphasisMed,
                maxLines = 1,
              )
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(8.dp))

      Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        weeks.forEach { week ->
          Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            week.forEach { date ->
              val count = trendsMap[date] ?: 0L
              HeatmapCell(count = count)
            }
          }
        }
      }
    }

    Spacer(modifier = Modifier.height(16.dp))

    HeatmapLegend(modifier = Modifier.align(Alignment.End))
  }
}

@Composable
private fun HeatmapCell(count: Long) {
  val color =
    when {
      count == 0L -> AppTheme.colorScheme.surfaceContainerHighest
      count < 3L -> AppTheme.colorScheme.primary.copy(alpha = 0.2f)
      count < 6L -> AppTheme.colorScheme.primary.copy(alpha = 0.4f)
      count < 10L -> AppTheme.colorScheme.primary.copy(alpha = 0.7f)
      else -> AppTheme.colorScheme.primary
    }

  Box(modifier = Modifier.requiredSize(12.dp).clip(RoundedCornerShape(2.dp)).background(color))
}

@Composable
private fun HeatmapLegend(modifier: Modifier = Modifier) {
  Row(
    modifier = modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.End,
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Text(
      text = stringResource(Res.string.statisticsLess),
      style = MaterialTheme.typography.labelSmall,
      color = AppTheme.colorScheme.textEmphasisMed,
    )
    Spacer(modifier = Modifier.width(4.dp))
    HeatmapCell(count = 0)
    Spacer(modifier = Modifier.width(2.dp))
    HeatmapCell(count = 2)
    Spacer(modifier = Modifier.width(2.dp))
    HeatmapCell(count = 5)
    Spacer(modifier = Modifier.width(2.dp))
    HeatmapCell(count = 8)
    Spacer(modifier = Modifier.width(2.dp))
    HeatmapCell(count = 12)
    Spacer(modifier = Modifier.width(4.dp))
    Text(
      text = stringResource(Res.string.statisticsMore),
      style = MaterialTheme.typography.labelSmall,
      color = AppTheme.colorScheme.textEmphasisMed,
    )
  }
}

@Composable
private fun StatisticCard(title: String, value: String, modifier: Modifier = Modifier) {
  Column(
    modifier =
      modifier
        .fillMaxWidth()
        .background(AppTheme.colorScheme.surface)
        .padding(horizontal = 16.dp, vertical = 20.dp),
    horizontalAlignment = Alignment.CenterHorizontally,
  ) {
    Text(
      text = title,
      style = MaterialTheme.typography.titleMedium,
      color = AppTheme.colorScheme.onSurfaceVariant,
    )
    Spacer(modifier = Modifier.height(8.dp))
    Text(
      text = value,
      style = MaterialTheme.typography.displayMedium,
      fontWeight = FontWeight.Bold,
      color = AppTheme.colorScheme.primary,
    )
  }
}

@Composable
private fun FeedReadCountItem(feedReadCount: FeedReadCount, modifier: Modifier = Modifier) {
  Row(modifier = modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
    val shape = RoundedCornerShape(16.dp)

    FeedIcon(
      icon = feedReadCount.feedIcon,
      homepageLink = feedReadCount.homepageLink,
      showFeedFavIcon = true,
      shape = shape,
      contentDescription = null,
      modifier = Modifier.requiredSize(32.dp),
    )
    Spacer(modifier = Modifier.width(12.dp))
    Column(modifier = Modifier.weight(1f)) {
      Text(
        text = feedReadCount.feedName,
        style = MaterialTheme.typography.bodyLarge,
        fontWeight = FontWeight.Medium,
        color = AppTheme.colorScheme.textEmphasisHigh,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
      )
    }
    Spacer(modifier = Modifier.width(12.dp))
    Text(
      text = feedReadCount.readCount.toString(),
      style = MaterialTheme.typography.titleMedium,
      fontWeight = FontWeight.Bold,
      color = AppTheme.colorScheme.primary,
    )
  }
}

@Composable
private fun ReadingTrendItem(trend: ReadingTrend, modifier: Modifier = Modifier) {
  Row(
    modifier = modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Text(
      text = trend.date.formatReadingTrendDate(),
      style = MaterialTheme.typography.bodyMedium,
      color = AppTheme.colorScheme.textEmphasisHigh,
    )
    Text(
      text = trend.count.toString(),
      style = MaterialTheme.typography.bodyMedium,
      fontWeight = FontWeight.Medium,
      color = AppTheme.colorScheme.textEmphasisHigh,
    )
  }
}
