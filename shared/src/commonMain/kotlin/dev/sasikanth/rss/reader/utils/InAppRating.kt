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

import kotlin.time.Duration.Companion.days
import kotlin.time.Instant

interface InAppRating {
  suspend fun request()

  fun canShowReviewPrompt(
    currentTime: Instant,
    installDate: Instant,
    lastPromptDate: Instant,
    sessionCount: Int
  ): Boolean {
    val daysSinceInstall = currentTime - installDate
    val daysSinceLastPrompt = currentTime - lastPromptDate

    if (daysSinceInstall < 7.days) return false

    if (daysSinceLastPrompt == 0.days || daysSinceLastPrompt < 30.days) return false

    if (sessionCount < 5) return false

    return true
  }
}
