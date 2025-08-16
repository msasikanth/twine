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

package dev.sasikanth.rss.reader.core.network.utils

import dev.sasikanth.rss.reader.util.DispatchersProvider
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher

class TestDispatchersProvider : DispatchersProvider {

  override val main: CoroutineDispatcher
    get() = UnconfinedTestDispatcher()

  override val io: CoroutineDispatcher
    get() = UnconfinedTestDispatcher()

  override val default: CoroutineDispatcher
    get() = UnconfinedTestDispatcher()

  override val databaseRead: CoroutineDispatcher
    get() = UnconfinedTestDispatcher()

  override val databaseWrite: CoroutineDispatcher
    get() = UnconfinedTestDispatcher()
}
