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

package dev.sasikanth.rss.reader.widget

import dev.sasikanth.rss.reader.core.model.local.ReadingStatistics
import dev.sasikanth.rss.reader.core.model.local.WidgetPost
import kotlin.time.Clock
import kotlinx.collections.immutable.persistentListOf

object WidgetMockData {

  val posts =
    persistentListOf(
      WidgetPost(
        id = "1",
        title = "Phones are going to get weird next week",
        description =
          "Expect rotating camera rings, robot arms, and magnetic modules at MWC 2026...",
        image =
          "https://platform.theverge.com/wp-content/uploads/sites/2/2026/02/honor-robot-phone.jpg",
        postedOn = Clock.System.now(),
        feedName = "The Verge",
        feedIcon = "https://theverge.com/icon.png",
        readingTimeEstimate = 2,
      ),
      WidgetPost(
        id = "2",
        title = "Android 16: Everything we know so far",
        description =
          "From improved multitasking to new privacy features, here is what to expect from the next major Android release.",
        image = null,
        postedOn = Clock.System.now(),
        feedName = "Android Police",
        feedIcon = null,
        readingTimeEstimate = 5,
      ),
      WidgetPost(
        id = "3",
        title = "The future of Kotlin Multiplatform",
        description =
          "Kotlin Multiplatform is evolving rapidly. Let's look at the latest updates and what they mean for developers.",
        image = null,
        postedOn = Clock.System.now(),
        feedName = "JetBrains Blog",
        feedIcon = null,
        readingTimeEstimate = 8,
      ),
    )

  val readerStatistics =
    ReadingStatistics(
      totalReadCount = 1240,
      dailyAverage = 12,
      topFeeds = persistentListOf(),
      readingTrends = persistentListOf(),
    )
}
