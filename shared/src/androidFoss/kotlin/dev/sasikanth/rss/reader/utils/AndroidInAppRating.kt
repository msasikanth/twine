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
import dev.sasikanth.rss.reader.di.scopes.ActivityScope
import me.tatarka.inject.annotations.Inject

@Inject
@ActivityScope
class AndroidInAppRating(private val activity: ComponentActivity) : InAppRating {

  override suspend fun request() {
    // No-op
  }
}
