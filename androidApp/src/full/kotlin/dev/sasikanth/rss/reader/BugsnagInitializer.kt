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
package dev.sasikanth.rss.reader

import android.app.Application
import co.touchlab.crashkios.bugsnag.enableBugsnag
import com.bugsnag.android.Bugsnag

object BugsnagInitializer {
  fun start(application: Application) {
    if (!BuildConfig.DEBUG) {
      Bugsnag.start(application)
      enableBugsnag()
    }
  }
}
