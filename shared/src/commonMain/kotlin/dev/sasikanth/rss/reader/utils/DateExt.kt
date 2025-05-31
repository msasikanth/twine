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

package dev.sasikanth.rss.reader.utils

import dev.sasikanth.rss.reader.data.repository.Period
import kotlin.time.Duration.Companion.hours
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimePeriod
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.minus
import kotlinx.datetime.todayIn

fun Period.calculateInstantBeforePeriod(): Instant {
  if (this == Period.NEVER) return Instant.DISTANT_PAST

  val period =
    when (this) {
      Period.ONE_WEEK -> DateTimePeriod(days = 7)
      Period.ONE_MONTH -> DateTimePeriod(months = 1)
      Period.THREE_MONTHS -> DateTimePeriod(months = 3)
      Period.SIX_MONTHS -> DateTimePeriod(months = 6)
      Period.ONE_YEAR -> DateTimePeriod(years = 1)
      Period.NEVER -> throw IllegalArgumentException("Period.NEVER should've been returned early")
    }
  val currentMoment = Clock.System.now()

  return currentMoment.minus(period, TimeZone.currentSystemDefault())
}

internal fun getTodayStartInstant(): Instant {
  return Clock.System.todayIn(TimeZone.currentSystemDefault())
    .atStartOfDayIn(TimeZone.currentSystemDefault())
}

internal fun getLast24HourStart(): Instant {
  return Clock.System.now().minus(24.hours)
}
