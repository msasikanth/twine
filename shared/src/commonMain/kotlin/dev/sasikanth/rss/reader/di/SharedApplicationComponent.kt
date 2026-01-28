/*
 * Copyright 2026 Sasikanth Miriyampalli
 *
 * Licensed under the GPL, Version 3.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.gnu.org/licenses/gpl-3.0.en.html
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package dev.sasikanth.rss.reader.di

import dev.sasikanth.rss.reader.billing.BillingComponent
import dev.sasikanth.rss.reader.core.network.di.NetworkComponent
import dev.sasikanth.rss.reader.data.di.DataComponent
import dev.sasikanth.rss.reader.data.refreshpolicy.RefreshPolicy
import dev.sasikanth.rss.reader.di.scopes.AppScope
import dev.sasikanth.rss.reader.initializers.Initializer
import dev.sasikanth.rss.reader.logging.LoggingComponent
import dev.sasikanth.rss.reader.notifications.di.NotificationsComponent
import dev.sasikanth.rss.reader.util.DefaultDispatchersProvider
import dev.sasikanth.rss.reader.util.DispatchersProvider
import me.tatarka.inject.annotations.Provides

abstract class SharedApplicationComponent :
  DataComponent,
  NetworkComponent,
  LoggingComponent,
  ImageLoaderComponent,
  BillingComponent,
  NotificationsComponent {

  abstract val initializers: Set<Initializer>

  abstract val refreshPolicy: RefreshPolicy

  @Provides @AppScope fun DefaultDispatchersProvider.bind(): DispatchersProvider = this
}
