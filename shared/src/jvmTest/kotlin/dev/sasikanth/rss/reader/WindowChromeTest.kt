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

package dev.sasikanth.rss.reader.utils

import androidx.compose.ui.unit.dp
import kotlin.test.Test
import kotlin.test.assertEquals

class WindowChromeTest {

  @Test
  fun windowsResizeEdgeInsetIsOnlyAppliedOnWindows() {
    assertEquals(16.dp, windowsResizeEdgeInset("Windows 10 Pro"))
    assertEquals(16.dp, windowsResizeEdgeInset("Windows 11"))
    assertEquals(0.dp, windowsResizeEdgeInset("Mac OS X"))
    assertEquals(0.dp, windowsResizeEdgeInset("Linux"))
    assertEquals(0.dp, windowsResizeEdgeInset(""))
  }
}
