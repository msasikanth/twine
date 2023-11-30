/*
 * Copyright 2023 Sasikanth Miriyampalli
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package dev.sasikanth.rss.reader.di

import dev.sasikanth.rss.reader.components.image.ImageLoader
import dev.sasikanth.rss.reader.di.scopes.AppScope
import dev.sasikanth.rss.reader.filemanager.FileManagerComponent
import dev.sasikanth.rss.reader.initializers.Initializer
import dev.sasikanth.rss.reader.logging.LoggingComponent
import dev.sasikanth.rss.reader.network.NetworkComponent
import dev.sasikanth.rss.reader.refresh.LastUpdatedAt
import dev.sasikanth.rss.reader.sentry.SentryComponent
import dev.sasikanth.rss.reader.util.DefaultDispatchersProvider
import dev.sasikanth.rss.reader.util.DispatchersProvider
import me.tatarka.inject.annotations.Provides

abstract class SharedApplicationComponent :
  DataComponent,
  ImageLoaderComponent,
  SentryComponent,
  NetworkComponent,
  LoggingComponent,
  FileManagerComponent {

  abstract val imageLoader: ImageLoader

  abstract val initializers: Set<Initializer>

  abstract val lastUpdatedAt: LastUpdatedAt

  @Provides @AppScope fun DefaultDispatchersProvider.bind(): DispatchersProvider = this
}
