/*
 * Copyright 2025 Sasikanth Miriyampalli
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
package dev.sasikanth.twine

import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import com.arkivanov.essenty.lifecycle.create
import dev.sasikanth.rss.reader.di.ApplicationComponent
import dev.sasikanth.rss.reader.di.DesktopAppComponent
import dev.sasikanth.rss.reader.di.create

fun main() {
  val lifecycle = LifecycleRegistry().also {
    it.create()
  }
  val appComponent = ApplicationComponent::class.create()
  val desktopAppComponent = DesktopAppComponent::class.create(
    componentContext = DefaultComponentContext(
      lifecycle = lifecycle
    ),
    applicationComponent = appComponent,
  )

  desktopAppComponent.desktopApp()
}
