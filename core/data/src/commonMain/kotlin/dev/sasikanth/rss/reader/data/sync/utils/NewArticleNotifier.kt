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

package dev.sasikanth.rss.reader.data.sync.utils

import dev.sasikanth.rss.reader.data.refreshpolicy.RefreshPolicy
import dev.sasikanth.rss.reader.data.repository.RssRepository
import dev.sasikanth.rss.reader.data.repository.SettingsRepository
import dev.sasikanth.rss.reader.di.scopes.AppScope
import dev.sasikanth.rss.reader.notifications.Notifier
import dev.sasikanth.rss.reader.util.nameBasedUuidOf
import kotlin.time.Clock
import kotlin.time.Duration.Companion.hours
import kotlin.time.Instant
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import me.tatarka.inject.annotations.Inject

@Inject
@AppScope
class NewArticleNotifier(
  private val rssRepository: RssRepository,
  private val settingsRepository: SettingsRepository,
  private val refreshPolicy: RefreshPolicy,
  private val notifier: Notifier,
) {

  private companion object {
    private const val NOTIFICATION_GROUP_ID = "twine_new_articles"
  }

  suspend fun notifyIfNewArticles(
    lastRefreshedAt: Instant? = null,
    title: (count: Int) -> String,
    content: () -> String,
    perFeedTitle: (feedName: String, count: Int) -> String,
  ) {
    if (settingsRepository.enableNotifications.first()) {
      val lastRefreshedAtInstant =
        lastRefreshedAt
          ?: refreshPolicy.lastRefreshedAtFlow.first().toInstant(TimeZone.currentSystemDefault())
      val now = Clock.System.now()
      val tz = TimeZone.currentSystemDefault()

      val postsAfter = now.minus(24.hours)

      val postsUpperBound = lastRefreshedAtInstant

      if (settingsRepository.groupByFeedNotifications.first()) {
        val unreadSinceLastSyncPerFeed =
          rssRepository
            .unreadSinceLastSyncPerFeed(
              sources = emptyList(),
              postsAfter = postsAfter,
              postsUpperBound = postsUpperBound,
            )
            .first()

        if (unreadSinceLastSyncPerFeed.isNotEmpty()) {
          coroutineScope {
            unreadSinceLastSyncPerFeed.forEach { unread ->
              launch {
                notifier.show(
                  title = perFeedTitle(unread.feedName, unread.newArticleCount.toInt()),
                  content = content(),
                  notificationId = nameBasedUuidOf(unread.feedId).hashCode(),
                  groupId = NOTIFICATION_GROUP_ID,
                )
              }
            }

            val totalNewArticles = unreadSinceLastSyncPerFeed.sumOf { it.newArticleCount }
            launch {
              notifier.show(
                title = title(totalNewArticles.toInt()),
                content = content(),
                notificationId = 1,
                groupId = NOTIFICATION_GROUP_ID,
                isSummary = true,
              )
            }
          }
        }
      } else {
        val unreadSinceLastSync =
          rssRepository
            .unreadSinceLastSync(
              sources = emptyList(),
              postsAfter = postsAfter,
              postsUpperBound = postsUpperBound,
            )
            .first()

        if (unreadSinceLastSync.hasNewArticles) {
          notifier.show(
            title = title(unreadSinceLastSync.newArticleCount.toInt()),
            content = content(),
          )
        }
      }
    }
  }
}
