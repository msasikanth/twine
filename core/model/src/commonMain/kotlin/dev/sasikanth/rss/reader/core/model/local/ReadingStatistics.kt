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

package dev.sasikanth.rss.reader.core.model.local

import androidx.compose.runtime.Immutable

@Immutable
data class FeedReadCount(
  val feedId: String,
  val feedName: String,
  val feedIcon: String,
  val homepageLink: String,
  val readCount: Long
)

@Immutable data class ReadingTrend(val date: String, val count: Long)

@Immutable
data class ReadingStatistics(
  val totalReadCount: Long,
  val topFeeds: List<FeedReadCount>,
  val readingTrends: List<ReadingTrend>
)
