/*
 * Copyright 2025 Sasikanth Miriyampalli
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

package dev.sasikanth.rss.reader.posts

import dev.sasikanth.rss.reader.core.model.local.PostsType
import kotlin.time.Duration.Companion.hours
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.toInstant

object PostsFilterUtils {

  fun postsThresholdTime(postsType: PostsType, dateTime: LocalDateTime): Instant {
    return when (postsType) {
      PostsType.ALL,
      PostsType.UNREAD -> Instant.DISTANT_PAST
      PostsType.TODAY -> {
        dateTime.date.atStartOfDayIn(TimeZone.currentSystemDefault())
      }
      PostsType.LAST_24_HOURS -> {
        dateTime.toInstant(TimeZone.currentSystemDefault()).minus(24.hours)
      }
    }
  }

  fun shouldGetUnreadPostsOnly(postsType: PostsType): Boolean? {
    return when (postsType) {
      PostsType.UNREAD -> true
      PostsType.ALL,
      PostsType.TODAY,
      PostsType.LAST_24_HOURS -> null
    }
  }
}
