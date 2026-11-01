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

package dev.sasikanth.rss.reader.core.network.utils

import dev.sasikanth.rss.reader.util.DispatchersProvider
import kotlin.coroutines.CoroutineContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Runnable
import kotlinx.coroutines.test.UnconfinedTestDispatcher

@OptIn(ExperimentalCoroutinesApi::class)
class TestDispatchersProvider : DispatchersProvider {

  private val dispatcher =
    object : CoroutineDispatcher() {
      private val delegate = UnconfinedTestDispatcher()

      override fun isDispatchNeeded(context: CoroutineContext): Boolean =
        delegate.isDispatchNeeded(context)

      override fun dispatch(context: CoroutineContext, block: Runnable) =
        delegate.dispatch(context, block)

      override fun limitedParallelism(parallelism: Int, name: String?): CoroutineDispatcher = this
    }

  override val main: CoroutineDispatcher = dispatcher

  override val io: CoroutineDispatcher = dispatcher

  override val default: CoroutineDispatcher = dispatcher

  override val databaseRead: CoroutineDispatcher = dispatcher

  override val databaseWrite: CoroutineDispatcher = dispatcher
}
