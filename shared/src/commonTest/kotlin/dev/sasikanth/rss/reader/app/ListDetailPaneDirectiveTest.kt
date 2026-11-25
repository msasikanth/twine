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

import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.layout.PaneScaffoldDirective
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.unit.dp
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
class ListDetailPaneDirectiveTest {

  private val hingeBounds = listOf(Rect(left = 530f, top = 0f, right = 550f, bottom = 1000f))

  private val twoPaneBaseDirective =
    PaneScaffoldDirective(
      maxHorizontalPartitions = 2,
      horizontalPartitionSpacerSize = 24.dp,
      maxVerticalPartitions = 1,
      verticalPartitionSpacerSize = 0.dp,
      defaultPanePreferredWidth = 360.dp,
      excludedBounds = hingeBounds,
    )

  @Test
  fun calculateReaderPaneScaffoldDirective_whenExpanded_switchesToSingleFullWidthPane() {
    val result =
      calculateReaderPaneScaffoldDirective(
        baseDirective = twoPaneBaseDirective,
        windowWidth = 900.dp,
        listPaneWidth = 420.dp,
        minReaderPaneWidth = 360.dp,
        isReaderExpanded = true,
      )

    assertEquals(1, result.maxHorizontalPartitions)
    assertEquals(0.dp, result.horizontalPartitionSpacerSize)
    assertTrue(result.excludedBounds.isEmpty())
  }

  @Test
  fun calculateReaderPaneScaffoldDirective_whenCollapsedAndWideEnough_keepsBaseDirective() {
    val result =
      calculateReaderPaneScaffoldDirective(
        baseDirective = twoPaneBaseDirective,
        windowWidth = 900.dp,
        listPaneWidth = 420.dp,
        minReaderPaneWidth = 360.dp,
        isReaderExpanded = false,
      )

    assertEquals(twoPaneBaseDirective, result)
    assertEquals(2, result.maxHorizontalPartitions)
    assertEquals(24.dp, result.horizontalPartitionSpacerSize)
    assertEquals(hingeBounds, result.excludedBounds)
  }

  @Test
  fun calculateReaderPaneScaffoldDirective_whenTooNarrowForTwoPanes_usesSinglePartition() {
    val result =
      calculateReaderPaneScaffoldDirective(
        baseDirective = twoPaneBaseDirective,
        windowWidth = 780.dp,
        listPaneWidth = 420.dp,
        minReaderPaneWidth = 360.dp,
        isReaderExpanded = false,
      )

    assertEquals(1, result.maxHorizontalPartitions)
  }

  @Test
  fun shouldUseListDetailForSinglePane_returnsFalseForCompactNonDesktop() {
    assertFalse(
      shouldUseListDetailForSinglePane(
        isDesktop = false,
        isSplitCapableWindow = false,
        hasEnteredListDetailScaffold = false,
        isReaderExpanded = false,
      )
    )
  }

  @Test
  fun shouldUseListDetailForSinglePane_returnsTrueForSplitCapableWindowCollapsedOrExpanded() {
    assertTrue(
      shouldUseListDetailForSinglePane(
        isDesktop = false,
        isSplitCapableWindow = true,
        hasEnteredListDetailScaffold = false,
        isReaderExpanded = false,
      )
    )
    assertTrue(
      shouldUseListDetailForSinglePane(
        isDesktop = false,
        isSplitCapableWindow = true,
        hasEnteredListDetailScaffold = true,
        isReaderExpanded = true,
      )
    )
  }

  @Test
  fun shouldUseListDetailForSinglePane_returnsTrueForDesktopEvenWhenNarrow() {
    assertTrue(
      shouldUseListDetailForSinglePane(
        isDesktop = true,
        isSplitCapableWindow = false,
        hasEnteredListDetailScaffold = false,
        isReaderExpanded = false,
      )
    )
  }

  @Test
  fun shouldUseListDetailForSinglePane_staysTrueAfterEnteringListDetailScaffold() {
    assertTrue(
      shouldUseListDetailForSinglePane(
        isDesktop = false,
        isSplitCapableWindow = false,
        hasEnteredListDetailScaffold = true,
        isReaderExpanded = false,
      )
    )
  }
}
