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

package dev.sasikanth.rss.reader.home.ui

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.util.fastForEach
import androidx.compose.ui.util.fastMaxBy

@Composable
internal fun HomeScreenContentScaffold(
  homeTopAppBar: @Composable () -> Unit,
  body: @Composable (PaddingValues) -> Unit,
  modifier: Modifier = Modifier,
) {
  SubcomposeLayout(
    modifier = Modifier.fillMaxSize().then(modifier),
  ) { constraints ->
    val layoutWidth = constraints.maxWidth
    val layoutHeight = constraints.maxHeight
    val looseConstraints = constraints.copy(minWidth = 0, minHeight = 0)

    val topBarPlaceables =
      subcompose("topBar") { homeTopAppBar() }.map { it.measure(looseConstraints) }
    val topBarHeight = topBarPlaceables.fastMaxBy { it.height }?.height ?: 0

    val bodyConstraints = looseConstraints.copy(maxHeight = layoutHeight)
    val bodyPlaceables =
      subcompose("body") { body(PaddingValues(top = topBarHeight.toDp())) }
        .map { it.measure(bodyConstraints) }

    layout(layoutWidth, layoutHeight) {
      bodyPlaceables.fastForEach { it.placeRelative(0, 0) }
      topBarPlaceables.fastForEach { it.placeRelative(0, 0) }
    }
  }
}
