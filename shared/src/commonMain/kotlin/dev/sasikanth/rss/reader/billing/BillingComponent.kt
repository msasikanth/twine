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

package dev.sasikanth.rss.reader.billing

import dev.sasikanth.rss.reader.di.scopes.AppScope
import dev.sasikanth.rss.reader.initializers.Initializer
import me.tatarka.inject.annotations.IntoSet
import me.tatarka.inject.annotations.Provides

interface BillingComponent {

  @IntoSet
  @Provides
  @AppScope
  fun providesBillingInitializer(bind: BillingInitializer): Initializer = bind
}
