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

package dev.sasikanth.rss.reader.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.sasikanth.rss.reader.components.PageIndicatorDefaults.MaxNumberOfIndicators
import dev.sasikanth.rss.reader.ui.AppTheme
import kotlin.math.abs

/**
 * A horizontal indicator for a Pager, representing the currently active page and total pages drawn
 * using a [Shape]. It shows up to 6 pages on the screen and doesn't represent the exact page index
 * if there are more than 6 pages. Instead of showing the exact position, [HorizontalPageIndicators]
 * shows a half-size indicator on the left or on the right if there are more pages.
 *
 * Here's how different positions 0..10 might be visually represented: "X" is selected item, "O" and
 * "o" full and half size items respectively.
 *
 * O X O O O o - 2nd position out of 10. There are no more items on the left but more on the right o
 * O O O X o - might be 6, 7 or 8 out of 10, as there are more possible items on the left and on the
 * right o O O O X O - is 9 out of 10, as there're no more items on the right
 *
 * This component also allows customising the [indicatorShape], which defines how the indicator is
 * visually represented.
 *
 * @param pageIndicatorState The state object of a [HorizontalPageIndicators] to be used to observe
 *   the Pager's state.
 * @param modifier Modifier to be applied to the [HorizontalPageIndicators]
 * @param selectedColor The color of the selected [HorizontalPageIndicators] item
 * @param unselectedColor The color of unselected [HorizontalPageIndicators] items
 * @param indicatorSize The size of each [HorizontalPageIndicators] item in [Dp]
 * @param spacing The spacing between indicator items in [Dp]
 * @param indicatorShape The shape of each [HorizontalPageIndicators] item. Defaults to
 *   [CircleShape]
 *
 * copied from:
 * https://cs.android.com/androidx/platform/frameworks/support/+/androidx-main:wear/compose/compose-material/src/main/java/androidx/wear/compose/material/HorizontalPageIndicator.kt
 */
@Composable
fun HorizontalPageIndicators(
  pageIndicatorState: PageIndicatorState,
  modifier: Modifier = Modifier,
  selectedColor: Color = AppTheme.colorScheme.onSurface,
  unselectedColor: Color = AppTheme.colorScheme.outline,
  indicatorSize: Dp = 8.dp,
  spacing: Dp = 8.dp,
  indicatorShape: Shape = CircleShape
) {
  // We want to bring offset to 0..1 range.
  // However, it can come in any range. It might be for example selectedPage = 1, with offset 2.5
  // We can't work with these offsets, thus should normalize them so that offset will be
  // in 0..1 range. For example selectedPage = 3, offset = -1.5 -> could transform it
  // to selectedPage = 1, offset = 0.5
  // Other example: selectedPage = 1, offset = 2.5 -> selectedPage = 3, offset = 0.5
  val normalizedSelectedPage: Int =
    (pageIndicatorState.selectedPage + pageIndicatorState.pageOffset).toInt()
  val normalizedOffset =
    pageIndicatorState.selectedPage + pageIndicatorState.pageOffset - normalizedSelectedPage

  val horizontalPadding = spacing / 2
  val spacerDefaultSize = (indicatorSize + spacing).value

  val pagesOnScreen = minOf(MaxNumberOfIndicators, pageIndicatorState.pageCount)
  val pagesState =
    remember(pageIndicatorState.pageCount) {
      PagesState(totalPages = pageIndicatorState.pageCount, pagesOnScreen = pagesOnScreen)
    }
  pagesState.recalculateState(normalizedSelectedPage, normalizedOffset)

  val indicatorFactory: @Composable (Int) -> Unit = { page ->
    // An external box with a fixed indicatorSize - let us remain the same size for
    // an indicator even if it's shrinked for smooth animations
    Box(modifier = Modifier.padding(horizontal = horizontalPadding).size(indicatorSize)) {
      Box(
        modifier =
          Modifier.fillMaxSize()
            .align(Alignment.Center)
            .scale(pagesState.sizeRatio(page))
            .clip(indicatorShape)
            .alpha(pagesState.alpha(page))
            // Interpolation between unselected and selected colors depending
            // on selectedPageRatio
            .background(
              lerp(
                unselectedColor,
                selectedColor,
                pagesState.calculateSelectedRatio(page, normalizedOffset)
              )
            )
      )
    }
  }

  val spacerLeft =
    @Composable {
      Spacer(
        Modifier.width((pagesState.leftSpacerSizeRatio * spacerDefaultSize).dp)
          .height(indicatorSize)
      )
    }
  val spacerRight =
    @Composable {
      Spacer(
        Modifier.width((pagesState.rightSpacerSizeRatio * spacerDefaultSize).dp)
          .height(indicatorSize)
      )
    }

  LinearPageIndicator(
    modifier = modifier,
    pagesOnScreen = pagesOnScreen,
    indicatorFactory = indicatorFactory,
    spacerLeft = spacerLeft,
    spacerRight = spacerRight
  )
}

object PageIndicatorDefaults {
  internal const val MaxNumberOfIndicators = 5
}

/** An interface for connection between Pager and [HorizontalPageIndicators]. */
interface PageIndicatorState {
  /**
   * The current offset from the start of [selectedPage], as a ratio of the page width.
   *
   * Changes when a scroll (drag, swipe or fling) between pages happens in Pager.
   */
  val pageOffset: Float

  /** The currently selected page index */
  val selectedPage: Int

  /** Total number of pages */
  val pageCount: Int
}

@Composable
private fun LinearPageIndicator(
  modifier: Modifier,
  pagesOnScreen: Int,
  indicatorFactory: @Composable (Int) -> Unit,
  spacerLeft: @Composable () -> Unit,
  spacerRight: @Composable () -> Unit
) {
  Row(
    modifier = modifier,
    horizontalArrangement = Arrangement.Center,
    verticalAlignment = Alignment.Bottom
  ) {
    // drawing 1 extra spacer for transition
    spacerLeft()
    for (page in 0..pagesOnScreen) {
      indicatorFactory(page)
    }
    spacerRight()
  }
}

/**
 * Represents an internal state of pageIndicator. This state is responsible for keeping and
 * recalculating alpha and size parameters of each indicator, and selected indicators as well.
 */
class PagesState(val totalPages: Int, val pagesOnScreen: Int) {
  // Sizes and alphas of first and last indicators on the screen. Used to show that there're more
  // pages on the left or on the right, and also for smooth transitions
  private var firstAlpha = 1f
  private var lastAlpha = 0f
  private var firstSize = 1f
  private var secondSize = 1f
  private var lastSize = 1f
  private var lastButOneSize = 1f

  private var smoothProgress = 0f

  // An offset in pages, basically meaning how many pages are hidden to the left.
  private var hiddenPagesToTheLeft = 0

  // A default size of spacers - invisible items to the left and to the right of
  // visible indicators, used for smooth transitions

  // Current visible position on the screen.
  private var visibleDotIndex: Int = 0
    private set

  // A size of a left spacer used for smooth transitions
  val leftSpacerSizeRatio: Float
    get() = 1 - smoothProgress

  // A size of a right spacer used for smooth transitions
  val rightSpacerSizeRatio: Float
    get() = smoothProgress

  /**
   * Depending on the page index, return an alpha for this indicator
   *
   * @param page Page index
   * @return An alpha of page index- in range 0..1
   */
  fun alpha(page: Int): Float =
    when (page) {
      0 -> firstAlpha
      pagesOnScreen -> lastAlpha
      else -> 1f
    }

  /**
   * Depending on the page index, return a size ratio for this indicator
   *
   * @param page Page index
   * @return An size ratio for page index - in range 0..1
   */
  fun sizeRatio(page: Int): Float =
    when (page) {
      0 -> firstSize
      1 -> secondSize
      pagesOnScreen - 1 -> lastButOneSize
      pagesOnScreen -> lastSize
      else -> 1f
    }

  /**
   * Returns a value in the range 0..1 where 0 is unselected state, and 1 is selected. Used to show
   * a smooth transition between page indicator items.
   */
  fun calculateSelectedRatio(targetPage: Int, offset: Float): Float =
    (1 - abs(visibleDotIndex + offset - targetPage)).coerceAtLeast(0f)

  // Main function responsible for recalculation of all parameters regarding
  // to the [selectedPage] and [offset]
  fun recalculateState(selectedPage: Int, offset: Float) {
    val pageWithOffset = selectedPage + offset

    // Calculating offsetInPages relating to the [selectedPage].

    // For example, for [selectedPage] = 4 we will see this picture :
    // O O O O X o. [offsetInPages] will be 0.
    // But when [selectedPage] will be incremented to 5, it will be seen as
    // o O O O X o, with [offsetInPages] = 1
    if (selectedPage > hiddenPagesToTheLeft + pagesOnScreen - 3) {
      // Set an offset as a difference between current page and pages on the screen,
      // except if this is not the last page - then offsetInPages is not changed
      hiddenPagesToTheLeft =
        (selectedPage - (pagesOnScreen - 3)).coerceAtMost(totalPages - pagesOnScreen)
    } else if (pageWithOffset <= hiddenPagesToTheLeft) {
      hiddenPagesToTheLeft = (selectedPage - 1).coerceAtLeast(0)
    }

    // Condition for scrolling to the right. A smooth scroll to the right is only triggered
    // when we have more than 2 pages to the right, and currently we're on the right edge.
    // For example -> o O O O X o -> a small "o" shows that there're more pages to the right
    val scrolledToTheRight =
      pageWithOffset > hiddenPagesToTheLeft + pagesOnScreen - 3 && pageWithOffset < totalPages - 3

    // Condition for scrolling to the left. A smooth scroll to the left is only triggered
    // when we have more than 2 pages to the left, and currently we're on the left edge.
    // For example -> o X O O O o -> a small "o" shows that there're more pages to the left
    val scrolledToTheLeft = pageWithOffset > 1 && pageWithOffset < hiddenPagesToTheLeft + 1

    smoothProgress = if (scrolledToTheLeft || scrolledToTheRight) offset else 0f

    // Calculating exact parameters for border indicators like [firstAlpha], [lastSize], etc.
    firstAlpha = 1 - smoothProgress
    lastAlpha = smoothProgress
    secondSize = 1 - 0.5f * smoothProgress

    // Depending on offsetInPages we'll either show a shrinked first indicator, or full-size
    firstSize =
      if (hiddenPagesToTheLeft == 0 || hiddenPagesToTheLeft == 1 && scrolledToTheLeft) {
        1 - smoothProgress
      } else {
        0.5f * (1 - smoothProgress)
      }

    // Depending on offsetInPages and other parameters, we'll either show a shrinked
    // last indicator, or full-size
    lastSize =
      if (
        hiddenPagesToTheLeft == totalPages - pagesOnScreen - 1 && scrolledToTheRight ||
          hiddenPagesToTheLeft == totalPages - pagesOnScreen && scrolledToTheLeft
      ) {
        smoothProgress
      } else {
        0.5f * smoothProgress
      }

    lastButOneSize =
      if (scrolledToTheRight || scrolledToTheLeft) {
        0.5f * (1 + smoothProgress)
      } else if (hiddenPagesToTheLeft < totalPages - pagesOnScreen) 0.5f else 1f

    // A visibleDot represents a currently selected page on the screen
    // As we scroll to the left, we add an invisible indicator to the left, shifting all other
    // indicators to the right. The shift is only possible when a visibleDot = 1,
    // thus we have to leave it at 1 as we always add a positive offset
    visibleDotIndex = if (scrolledToTheLeft) 1 else selectedPage - hiddenPagesToTheLeft
  }
}
