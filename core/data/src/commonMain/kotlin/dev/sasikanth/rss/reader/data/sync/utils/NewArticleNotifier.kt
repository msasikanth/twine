/*
 * Copyright 2026 Sasikanth Miriyampalli
 *
 * Licensed under the GPL, Version 3.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.gnu.org/licenses/gpl-3.0.en.html
 *
 */

package dev.sasikanth.rss.reader.data.sync.utils

import dev.sasikanth.rss.reader.data.refreshpolicy.RefreshPolicy
import dev.sasikanth.rss.reader.data.repository.RssRepository
import dev.sasikanth.rss.reader.data.repository.SettingsRepository
import dev.sasikanth.rss.reader.di.scopes.AppScope
import dev.sasikanth.rss.reader.notifications.Notifier
import kotlin.time.Clock
import kotlinx.coroutines.flow.first
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import me.tatarka.inject.annotations.Inject

@Inject
@AppScope
class NewArticleNotifier(
  private val rssRepository: RssRepository,
  private val settingsRepository: SettingsRepository,
  private val refreshPolicy: RefreshPolicy,
  private val notifier: Notifier,
) {

  suspend fun notifyIfNewArticles(
    title: (count: Int) -> String,
    content: () -> String,
  ) {
    if (settingsRepository.enableNotifications.first()) {
      val lastRefreshedAtDateTime = refreshPolicy.dateTimeFlow.first()
      val now = Clock.System.now()
      val tz = TimeZone.Companion.currentSystemDefault()

      val today = now.toLocalDateTime(tz).date
      val startOfDay = today.atStartOfDayIn(tz)

      val unreadSinceLastSync =
        rssRepository
          .unreadSinceLastSync(
            sources = emptyList(),
            postsAfter = startOfDay,
            lastSyncedAt =
              lastRefreshedAtDateTime.toInstant(TimeZone.Companion.currentSystemDefault())
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
