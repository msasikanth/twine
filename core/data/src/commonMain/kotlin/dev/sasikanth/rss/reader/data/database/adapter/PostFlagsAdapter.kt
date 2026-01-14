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

package dev.sasikanth.rss.reader.data.database.adapter

import app.cash.sqldelight.ColumnAdapter
import dev.sasikanth.rss.reader.core.model.local.PostFlag

object PostFlagsAdapter : ColumnAdapter<Set<PostFlag>, Long> {
  override fun decode(databaseValue: Long): Set<PostFlag> {
    val flags = mutableSetOf<PostFlag>()
    PostFlag.entries.forEach { flag ->
      if ((databaseValue and (1L shl flag.ordinal)) != 0L) {
        flags.add(flag)
      }
    }
    return flags
  }

  override fun encode(value: Set<PostFlag>): Long {
    var databaseValue = 0L
    value.forEach { flag -> databaseValue = databaseValue or (1L shl flag.ordinal) }
    return databaseValue
  }
}
