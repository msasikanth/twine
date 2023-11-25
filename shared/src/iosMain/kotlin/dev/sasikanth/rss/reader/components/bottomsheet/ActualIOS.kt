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
package dev.sasikanth.rss.reader.components.bottomsheet

import kotlinx.atomicfu.atomic

actual class InternalAtomicReference<V> actual constructor(value: V) {

  private val atomicReference = atomic(value)

  actual fun get(): V {
    return atomicReference.value
  }

  actual fun set(value: V) {
    atomicReference.value = value
  }

  actual fun compareAndSet(expect: V, newValue: V): Boolean {
    return atomicReference.compareAndSet(expect, newValue)
  }
}
