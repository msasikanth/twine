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

import dev.sasikanth.rss.reader.repository.Period
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimePeriod
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.minus
import kotlinx.datetime.toLocalDateTime

fun Period.calculateInstantBeforePeriod(): Instant {
  val period =
    when (this) {
      Period.ONE_WEEK -> DateTimePeriod(days = 7)
      Period.ONE_MONTH -> DateTimePeriod(months = 1)
      Period.THREE_MONTHS -> DateTimePeriod(months = 3)
      Period.SIX_MONTHS -> DateTimePeriod(months = 6)
      Period.ONE_YEAR -> DateTimePeriod(years = 1)
    }
  val currentMoment = Clock.System.now()

  return currentMoment.minus(period, TimeZone.currentSystemDefault())
}

internal fun getTodayStartInstant() =
  Clock.System.now()
    .toLocalDateTime(TimeZone.currentSystemDefault())
    .date
    .atStartOfDayIn(TimeZone.currentSystemDefault())
