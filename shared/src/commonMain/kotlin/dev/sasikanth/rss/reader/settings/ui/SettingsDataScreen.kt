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
package dev.sasikanth.rss.reader.settings.ui

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
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import dev.sasikanth.rss.reader.components.SimpleTopAppBar
import dev.sasikanth.rss.reader.components.SubHeader
import dev.sasikanth.rss.reader.components.image.FeedIcon
import dev.sasikanth.rss.reader.core.model.local.FeedReadCount
import dev.sasikanth.rss.reader.core.model.local.ReadingTrend
import dev.sasikanth.rss.reader.settings.SettingsViewModel
import dev.sasikanth.rss.reader.statistics.StatisticsViewModel
import dev.sasikanth.rss.reader.ui.AppTheme
import dev.sasikanth.rss.reader.utils.LocalShowFeedFavIconSetting
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
import twine.shared.generated.resources.settingsYourInsights
import twine.shared.generated.resources.statisticsDailyAverage
import twine.shared.generated.resources.statisticsFirstPlace
import twine.shared.generated.resources.statisticsLess
import twine.shared.generated.resources.statisticsMore
import twine.shared.generated.resources.statisticsNoData
import twine.shared.generated.resources.statisticsTotalRead
import twine.shared.generated.resources.statisticsYourTopFeeds

@Composable
internal fun SettingsDataScreen(
  settingsViewModel: SettingsViewModel,
  statisticsViewModel: StatisticsViewModel,
  goBack: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val state by statisticsViewModel.state.collectAsState()
  val layoutDirection = LocalLayoutDirection.current

  Scaffold(
    modifier = modifier,
    topBar = {
      SimpleTopAppBar(title = stringResource(Res.string.settingsYourInsights), onBackClick = goBack)
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
        val statistics = state.statistics
        LazyColumn(
          modifier = Modifier.fillMaxSize(),
          contentPadding =
            PaddingValues(
              start = padding.calculateStartPadding(layoutDirection),
              top = padding.calculateTopPadding() + 8.dp,
              end = padding.calculateEndPadding(layoutDirection),
              bottom = padding.calculateBottomPadding() + 80.dp,
            ),
        ) {
          if (statistics == null || statistics.totalReadCount == 0L) {
            item {
              Box(
                modifier =
                  Modifier.fillMaxWidth()
                    .requiredHeight(120.dp)
                    .padding(horizontal = settingsItemHorizontalPadding),
                contentAlignment = Alignment.Center,
              ) {
                Text(
                  text = stringResource(Res.string.statisticsNoData),
                  style = MaterialTheme.typography.bodyLarge,
                  color = AppTheme.colorScheme.onSurfaceVariant,
                )
              }
            }
          } else {
            item {
              Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(24.dp),
              ) {
                OverviewStat(
                  label = stringResource(Res.string.statisticsTotalRead),
                  value = statistics.totalReadCount.toString(),
                  modifier = Modifier.weight(1f),
                )
                OverviewStat(
                  label = stringResource(Res.string.statisticsDailyAverage),
                  value = statistics.dailyAverage.toString(),
                  modifier = Modifier.weight(1f),
                )
              }
            }

            item { ReadingHeatmap(readingTrends = statistics.readingTrends) }

            if (statistics.topFeeds.isNotEmpty()) {
              item { SubHeader(text = stringResource(Res.string.statisticsYourTopFeeds)) }

              itemsIndexed(statistics.topFeeds.take(5)) { index, feedReadCount ->
                FeedReadCountItem(
                  feedReadCount = feedReadCount,
                  isFirstPlace = index == 0,
                  modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
                )
              }
            }
          }
        }
      }
    },
    containerColor = AppTheme.colorScheme.backdrop,
    contentColor = Color.Unspecified,
  )
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
  val monthLabels =
    remember(weeks) {
      val labels = mutableMapOf<Int, String>()
      weeks.forEachIndexed { index, week ->
        val firstDay = week.first()
        val prevMonth = weeks.getOrNull(index - 1)?.first()?.month
        val isMonthTransition = index > 0 && firstDay.month != prevMonth

        if (index == 0 || isMonthTransition) {
          val monthName =
            firstDay.month.name.take(3).lowercase().replaceFirstChar { it.uppercase() }

          // If this is the first week, check if the next month transition is too close
          if (index == 0) {
            val nextMonthTransitionIndex =
              weeks.indices.firstOrNull { i -> i > 0 && weeks[i].first().month != firstDay.month }
            if (nextMonthTransitionIndex == null || nextMonthTransitionIndex > 2) {
              labels[index] = monthName
            }
          } else {
            labels[index] = monthName
          }
        }
      }
      labels
    }

  Column(
    modifier =
      modifier
        .fillMaxWidth()
        .background(AppTheme.colorScheme.surfaceContainerLow)
        .horizontalScroll(rememberScrollState(), reverseScrolling = true)
        .padding(horizontal = 32.dp, vertical = 12.dp)
  ) {
    Column {
      Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        weeks.forEachIndexed { index, week ->
          val monthName = monthLabels[index]

          Box(modifier = Modifier.width(12.dp)) {
            if (monthName != null) {
              Text(
                modifier = Modifier.requiredWidth(40.dp),
                text = monthName,
                style = MaterialTheme.typography.labelSmall,
                color = AppTheme.colorScheme.onSurfaceVariant,
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
      count == 0L -> AppTheme.colorScheme.onSurface.copy(alpha = 0.1f)
      count < 3L -> AppTheme.colorScheme.primary.copy(alpha = 0.3f)
      count < 6L -> AppTheme.colorScheme.primary.copy(alpha = 0.5f)
      count < 10L -> AppTheme.colorScheme.primary.copy(alpha = 0.8f)
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
      color = AppTheme.colorScheme.onSurfaceVariant,
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
      color = AppTheme.colorScheme.onSurfaceVariant,
    )
  }
}

@Composable
private fun OverviewStat(label: String, value: String, modifier: Modifier = Modifier) {
  Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
    Text(
      text = value,
      style = MaterialTheme.typography.displayLarge,
      color = AppTheme.colorScheme.onSurface,
    )
    Text(
      text = label,
      style = MaterialTheme.typography.bodyLarge,
      color = AppTheme.colorScheme.onSurfaceVariant,
    )
  }
}

@Composable
private fun FeedReadCountItem(
  feedReadCount: FeedReadCount,
  modifier: Modifier = Modifier,
  isFirstPlace: Boolean = false,
) {
  Row(modifier = modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
    val iconSize = if (isFirstPlace) 48.dp else 24.dp
    val shape = RoundedCornerShape(25)

    FeedIcon(
      icon = feedReadCount.feedIcon,
      homepageLink = feedReadCount.homepageLink,
      showFeedFavIcon = LocalShowFeedFavIconSetting.current,
      contentDescription = null,
      modifier = Modifier.requiredSize(iconSize),
    )

    Spacer(modifier = Modifier.width(16.dp))

    Column(modifier = Modifier.weight(1f)) {
      if (isFirstPlace) {
        Text(
          text = stringResource(Res.string.statisticsFirstPlace),
          style = MaterialTheme.typography.titleSmall,
          color = AppTheme.colorScheme.primary,
        )
      }

      Text(
        text = feedReadCount.feedName,
        style =
          if (isFirstPlace) MaterialTheme.typography.titleLarge
          else MaterialTheme.typography.titleSmall,
        color = AppTheme.colorScheme.onSurface,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
      )
    }

    Spacer(modifier = Modifier.width(16.dp))

    val badgeColor =
      if (isFirstPlace) AppTheme.colorScheme.primary
      else AppTheme.colorScheme.onSurface.copy(alpha = 0.08f)
    val badgeContentColor =
      if (isFirstPlace) AppTheme.colorScheme.onPrimary else AppTheme.colorScheme.onSurface

    Box(
      modifier =
        Modifier.requiredHeight(if (isFirstPlace) 32.dp else 24.dp)
          .clip(CircleShape)
          .background(badgeColor),
      contentAlignment = Alignment.Center,
    ) {
      Text(
        modifier =
          Modifier.padding(vertical = 4.dp, horizontal = if (isFirstPlace) 12.dp else 8.dp),
        text = feedReadCount.readCount.toString(),
        style =
          if (isFirstPlace) MaterialTheme.typography.titleMedium
          else MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.Bold,
        color = badgeContentColor,
      )
    }
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
      color = AppTheme.colorScheme.onSurface,
    )
    Text(
      text = trend.count.toString(),
      style = MaterialTheme.typography.bodyMedium,
      fontWeight = FontWeight.Medium,
      color = AppTheme.colorScheme.onSurface,
    )
  }
}
