/*
 * Copyright 2025 Sasikanth Miriyampalli
 *
 * Licensed under the GPL, Version 3.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.gnu.org/licenses/gpl-3.0.en.html
 *
 */

package dev.sasikanth.rss.reader.data.database.adapter

import app.cash.sqldelight.ColumnAdapter
import kotlin.time.Duration

internal object DurationAdapter : ColumnAdapter<Duration, String> {

  override fun decode(databaseValue: String): Duration {
    return Duration.parse(databaseValue)
  }

  override fun encode(value: Duration): String {
    return value.toString()
  }
}
