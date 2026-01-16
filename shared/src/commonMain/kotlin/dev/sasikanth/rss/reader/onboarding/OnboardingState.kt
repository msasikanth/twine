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

package dev.sasikanth.rss.reader.onboarding

import androidx.compose.runtime.Immutable

@Immutable
data class OnboardingState(val isPrePopulating: Boolean) {

  companion object {

    val DEFAULT = OnboardingState(isPrePopulating = false)
  }
}
