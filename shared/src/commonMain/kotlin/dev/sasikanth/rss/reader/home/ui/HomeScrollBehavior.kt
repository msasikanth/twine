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

import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.unit.Velocity
import dev.sasikanth.rss.reader.feeds.ui.pinned.PinnedSourcesBottomBarScrollBehavior
import kotlin.math.absoluteValue

@Stable
internal class HomeScrollBehavior(
  private val appBarScrollBehaviour: TopAppBarScrollBehavior,
  private val bottomBarScrollState: PinnedSourcesBottomBarScrollBehavior,
) {
  val nestedScrollConnection =
    object : NestedScrollConnection {
      override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
        val appBarConsumed =
          appBarScrollBehaviour.nestedScrollConnection.onPreScroll(available, source)
        val bottomBarConsumed =
          bottomBarScrollState.nestedScrollConnection.onPreScroll(available, source)

        return if (appBarConsumed.y.absoluteValue > bottomBarConsumed.y.absoluteValue) {
          appBarConsumed
        } else {
          bottomBarConsumed
        }
      }

      override fun onPostScroll(
        consumed: Offset,
        available: Offset,
        source: NestedScrollSource,
      ): Offset {
        val appBarConsumed =
          appBarScrollBehaviour.nestedScrollConnection.onPostScroll(consumed, available, source)
        val bottomBarConsumed =
          bottomBarScrollState.nestedScrollConnection.onPostScroll(consumed, available, source)

        return if (appBarConsumed.y.absoluteValue > bottomBarConsumed.y.absoluteValue) {
          appBarConsumed
        } else {
          bottomBarConsumed
        }
      }

      override suspend fun onPreFling(available: Velocity): Velocity {
        val appBarConsumed = appBarScrollBehaviour.nestedScrollConnection.onPreFling(available)
        val bottomBarConsumed = bottomBarScrollState.nestedScrollConnection.onPreFling(available)

        return if (appBarConsumed.y.absoluteValue > bottomBarConsumed.y.absoluteValue) {
          appBarConsumed
        } else {
          bottomBarConsumed
        }
      }

      override suspend fun onPostFling(consumed: Velocity, available: Velocity): Velocity {
        val appBarConsumed =
          appBarScrollBehaviour.nestedScrollConnection.onPostFling(consumed, available)
        val bottomBarConsumed =
          bottomBarScrollState.nestedScrollConnection.onPostFling(consumed, available)

        return if (appBarConsumed.y.absoluteValue > bottomBarConsumed.y.absoluteValue) {
          appBarConsumed
        } else {
          bottomBarConsumed
        }
      }
    }
}

@Composable
internal fun rememberHomeScrollBehavior(
  appBarScrollBehaviour: TopAppBarScrollBehavior,
  bottomBarScrollState: PinnedSourcesBottomBarScrollBehavior,
): HomeScrollBehavior =
  remember(appBarScrollBehaviour, bottomBarScrollState) {
    HomeScrollBehavior(
      appBarScrollBehaviour = appBarScrollBehaviour,
      bottomBarScrollState = bottomBarScrollState,
    )
  }
