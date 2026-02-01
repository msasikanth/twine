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

package dev.sasikanth.rss.reader.data.di

import app.cash.sqldelight.db.SqlDriver
import dev.sasikanth.rss.reader.data.database.DriverFactory
import dev.sasikanth.rss.reader.di.scopes.AppScope
import me.tatarka.inject.annotations.Provides

actual interface SqlDriverPlatformComponent {

  @Provides
  @AppScope
  fun providesJvmSqlDriver(driverFactory: DriverFactory): SqlDriver {
    return driverFactory.createDriver()
  }
}
