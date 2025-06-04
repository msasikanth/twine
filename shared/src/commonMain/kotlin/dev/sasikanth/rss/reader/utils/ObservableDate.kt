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
package dev.sasikanth.rss.reader.utils

import dev.sasikanth.rss.reader.di.scopes.AppScope
import kotlin.time.Duration.Companion.hours
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.onStart
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import me.tatarka.inject.annotations.Inject

@Inject
@AppScope
class ObservableDate {

  private val refreshFlow = MutableSharedFlow<Unit>(replay = 1)

  val dateTimeFlow: Flow<LocalDateTime> =
    merge(
      flow {
        while (true) {
          emit(currentDateTime())
          delay(1.hours)
        }
      },
      refreshFlow.onStart { emit(Unit) }.map { currentDateTime() }
    )

  private fun currentDateTime() =
    Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())

  fun refresh() {
    refreshFlow.tryEmit(Unit)
  }
}
