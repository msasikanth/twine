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

import com.arkivanov.decompose.ComponentContext
import dev.sasikanth.rss.reader.database.DriverFactory
import dev.sasikanth.rss.reader.home.HomeViewModelFactory
import dev.sasikanth.rss.reader.utils.DefaultDispatchersProvider
import dev.sasikanth.rss.reader.utils.DispatchersProvider
import me.tatarka.inject.annotations.Component
import me.tatarka.inject.annotations.Provides
import me.tatarka.inject.annotations.Scope

@Component
@AppScope
abstract class DIAppComponent(
  @get:Provides val componentContext: ComponentContext,
  @get:Provides val driverFactory: DriverFactory
) : DIDataComponent {

  abstract val homeViewModelFactory: HomeViewModelFactory

  @Provides @AppScope fun DefaultDispatchersProvider.bind(): DispatchersProvider = this
}

@Scope
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY_GETTER)
annotation class AppScope
