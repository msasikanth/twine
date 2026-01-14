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

package dev.sasikanth.rss.reader.utils

import androidx.activity.ComponentActivity
import com.google.android.play.core.review.ReviewManagerFactory
import dev.sasikanth.rss.reader.data.repository.SettingsRepository
import dev.sasikanth.rss.reader.di.scopes.ActivityScope
import kotlin.coroutines.resume
import kotlin.time.Clock
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.suspendCancellableCoroutine
import me.tatarka.inject.annotations.Inject

@Inject
@ActivityScope
class AndroidInAppRating(
  private val activity: ComponentActivity,
  private val settingsRepository: SettingsRepository,
) : InAppRating {

  override suspend fun request() {
    val manager = ReviewManagerFactory.create(activity)
    val request = suspendCancellableCoroutine { continuation ->
      manager.requestReviewFlow().addOnCompleteListener { task ->
        if (task.isSuccessful) {
          continuation.resume(task.result)
        } else {
          continuation.resume(null)
        }
      }
    }

    val now = Clock.System.now()
    val installDate = settingsRepository.installDate.firstOrNull() ?: now
    val lastPromptDate = settingsRepository.lastReviewPromptDate.firstOrNull() ?: now
    val sessionCount = settingsRepository.userSessionCount.first()
    val canShowReviewPrompt =
      canShowReviewPrompt(
        currentTime = Clock.System.now(),
        installDate = installDate,
        lastPromptDate = lastPromptDate,
        sessionCount = sessionCount,
      )

    if (request != null && canShowReviewPrompt) {
      manager.launchReviewFlow(activity, request)
      settingsRepository.updateLastReviewPromptDate(now)
    }
  }
}
