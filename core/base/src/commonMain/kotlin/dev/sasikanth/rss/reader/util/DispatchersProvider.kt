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
package dev.sasikanth.rss.reader.util

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import me.tatarka.inject.annotations.Inject

interface DispatchersProvider {
  val main: CoroutineDispatcher
  val io: CoroutineDispatcher
  val default: CoroutineDispatcher
  val databaseRead: CoroutineDispatcher
  val databaseWrite: CoroutineDispatcher
}

@Inject
class DefaultDispatchersProvider : DispatchersProvider {
  override val main = Dispatchers.Main
  override val io = Dispatchers.IO
  override val default = Dispatchers.Default
  override val databaseRead = Dispatchers.IO.limitedParallelism(4)
  override val databaseWrite = Dispatchers.IO.limitedParallelism(1)
}
