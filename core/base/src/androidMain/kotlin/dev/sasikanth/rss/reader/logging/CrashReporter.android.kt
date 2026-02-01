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

package dev.sasikanth.rss.reader.logging

import co.touchlab.crashkios.bugsnag.BugsnagKotlin

actual object CrashReporter {
  actual fun log(exception: Throwable) {
    BugsnagKotlin.sendHandledException(exception)
  }

  actual fun setCustomValue(section: String, key: String, value: String?) {
    if (value != null) {
      BugsnagKotlin.setCustomValue(section, key, value)
    }
  }
}
