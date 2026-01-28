/*
 * Copyright 2024 Sasikanth Miriyampalli
 *
 * Licensed under the GPL, Version 3.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.gnu.org/licenses/gpl-3.0.en.html
 *
 */

package dev.sasikanth.rss.reader.statistics

import androidx.compose.runtime.Immutable
import dev.sasikanth.rss.reader.core.model.local.ReadingStatistics

@Immutable
data class StatisticsState(
  val statistics: ReadingStatistics? = null,
  val isLoading: Boolean = true
)
