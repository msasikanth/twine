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

package dev.sasikanth.rss.reader.app

import androidx.compose.ui.unit.dp
import kotlin.test.Test
import kotlin.test.assertEquals

class ReaderPaneSizeClassTest {

  @Test
  fun calculateReaderPaneWidth_subtractsSpacerAndListPaneWidth() {
    assertEquals(
      456.dp,
      calculateReaderPaneWidth(
        windowWidth = 900.dp,
        partitionSpacerWidth = 24.dp,
        listPaneWidth = 420.dp,
        minReaderPaneWidth = 360.dp,
      ),
    )
  }

  @Test
  fun calculateReaderPaneWidth_coercesAtLeastMinimumReaderPaneWidth() {
    assertEquals(
      360.dp,
      calculateReaderPaneWidth(
        windowWidth = 780.dp,
        partitionSpacerWidth = 24.dp,
        listPaneWidth = 420.dp,
        minReaderPaneWidth = 360.dp,
      ),
    )
  }
}
