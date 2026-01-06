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

package dev.sasikanth.rss.reader.data.sync

import dev.sasikanth.rss.reader.data.repository.RssRepository
import dev.sasikanth.rss.reader.data.repository.SettingsRepository
import dev.sasikanth.rss.reader.data.time.LastRefreshedAt
import dev.sasikanth.rss.reader.data.utils.PostsFilterUtils
import dev.sasikanth.rss.reader.di.scopes.AppScope
import dev.sasikanth.rss.reader.notifications.Notifier
import kotlinx.coroutines.flow.first
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import me.tatarka.inject.annotations.Inject

@Inject
@AppScope
class NewArticleNotifier(
  private val rssRepository: RssRepository,
  private val settingsRepository: SettingsRepository,
  private val lastRefreshedAt: LastRefreshedAt,
  private val notifier: Notifier,
) {

  suspend fun notifyIfNewArticles(
    title: (count: String) -> String,
    content: () -> String,
  ) {
    if (settingsRepository.enableNotifications.first()) {
      val lastRefreshedAtDateTime = lastRefreshedAt.dateTimeFlow.first()
      val postsType = settingsRepository.postsType.first()
      val postsAfter =
        PostsFilterUtils.postsThresholdTime(
          postsType = postsType,
          dateTime = lastRefreshedAtDateTime
        )

      val unreadSinceLastSync =
        rssRepository
          .unreadSinceLastSync(
            sources = emptyList(),
            postsAfter = postsAfter,
            lastSyncedAt = lastRefreshedAtDateTime.toInstant(TimeZone.currentSystemDefault())
          )
          .first()

      if (unreadSinceLastSync.hasNewArticles) {
        notifier.show(
          title = title(unreadSinceLastSync.newArticleCount.toString()),
          content = content(),
        )
      }
    }
  }
}
