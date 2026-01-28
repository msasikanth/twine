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

package dev.sasikanth.rss.reader.home.ui

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastForEach
import androidx.compose.ui.util.fastMaxBy

@Composable
internal fun HomeScreenContentScaffold(
  homeTopAppBar: @Composable () -> Unit,
  body: @Composable (PaddingValues) -> Unit,
  modifier: Modifier = Modifier,
) {
  val latestHomeTopAppBar by rememberUpdatedState(homeTopAppBar)
  val latestBody by rememberUpdatedState(body)
  val paddingValues = remember { MutablePaddingValues() }

  val homeTopAppBarContent = remember { @Composable { latestHomeTopAppBar() } }
  val bodyContent = remember { @Composable { latestBody(paddingValues) } }

  SubcomposeLayout(
    modifier = Modifier.fillMaxSize().then(modifier),
  ) { constraints ->
    val layoutWidth = constraints.maxWidth
    val layoutHeight = constraints.maxHeight
    val looseConstraints = constraints.copy(minWidth = 0, minHeight = 0)

    val topBarPlaceables =
      subcompose("topBar", homeTopAppBarContent).map { it.measure(looseConstraints) }
    val topBarHeight = topBarPlaceables.fastMaxBy { it.height }?.height ?: 0

    paddingValues.top = topBarHeight.toDp()

    val bodyConstraints = looseConstraints.copy(maxHeight = layoutHeight)
    val bodyPlaceables = subcompose("body", bodyContent).map { it.measure(bodyConstraints) }

    layout(layoutWidth, layoutHeight) {
      bodyPlaceables.fastForEach { it.placeRelative(0, 0) }
      topBarPlaceables.fastForEach { it.placeRelative(0, 0) }
    }
  }
}

@Stable
private class MutablePaddingValues : PaddingValues {

  var top: Dp by mutableStateOf(0.dp)
  var bottom: Dp by mutableStateOf(0.dp)
  var start: Dp by mutableStateOf(0.dp)
  var end: Dp by mutableStateOf(0.dp)

  override fun calculateLeftPadding(layoutDirection: LayoutDirection): Dp =
    if (layoutDirection == LayoutDirection.Ltr) start else end

  override fun calculateTopPadding(): Dp = top

  override fun calculateRightPadding(layoutDirection: LayoutDirection): Dp =
    if (layoutDirection == LayoutDirection.Ltr) end else start

  override fun calculateBottomPadding(): Dp = bottom
}
