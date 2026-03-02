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

import co.touchlab.kermit.ExperimentalKermitApi
import co.touchlab.kermit.Logger
import co.touchlab.kermit.NoTagFormatter
import co.touchlab.kermit.Severity
import co.touchlab.kermit.platformLogWriter
import dev.sasikanth.rss.reader.app.AppInfo
import dev.sasikanth.rss.reader.initializers.Initializer
import me.tatarka.inject.annotations.Inject

@Inject
actual class LoggingInitializer actual constructor(private val appInfo: AppInfo) : Initializer {

  @OptIn(ExperimentalKermitApi::class)
  actual override fun initialize() {
    val loggers = listOf(platformLogWriter(NoTagFormatter))

    Logger.setLogWriters(loggers)
    Logger.setMinSeverity(Severity.Info)
  }
}
