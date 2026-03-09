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

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.pager.PagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.graphics.shapes.Morph
import co.touchlab.kermit.Logger
import kotlin.time.Clock
import kotlin.time.Instant
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.format.MonthNames
import kotlinx.datetime.format.Padding
import kotlinx.datetime.format.char
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.compose.resources.stringResource
import twine.shared.generated.resources.Res
import twine.shared.generated.resources.month_apr
import twine.shared.generated.resources.month_aug
import twine.shared.generated.resources.month_dec
import twine.shared.generated.resources.month_feb
import twine.shared.generated.resources.month_jan
import twine.shared.generated.resources.month_jul
import twine.shared.generated.resources.month_jun
import twine.shared.generated.resources.month_mar
import twine.shared.generated.resources.month_may
import twine.shared.generated.resources.month_nov
import twine.shared.generated.resources.month_oct
import twine.shared.generated.resources.month_sep
import twine.shared.generated.resources.unit_days
import twine.shared.generated.resources.unit_hours
import twine.shared.generated.resources.unit_minutes
import twine.shared.generated.resources.unit_seconds

expect fun String.toClipEntry(): ClipEntry

fun Modifier.ignoreHorizontalParentPadding(horizontal: Dp): Modifier {
  return this.layout { measurable, constraints ->
    val updatedMaxWidth = constraints.maxWidth + (2 * horizontal.roundToPx())
    val placeable = measurable.measure(constraints.copy(maxWidth = updatedMaxWidth))
    layout(placeable.width, placeable.height) { placeable.place(0, 0) }
  }
}

@Composable
fun Instant.formatRelativeTime(): String {
  val now = Clock.System.now()
  val duration = now - this
  val seconds = duration.inWholeSeconds
  val days = duration.inWholeDays

  return when {
    seconds < 60 -> stringResource(Res.string.unit_seconds)
    seconds < 3600 -> stringResource(Res.string.unit_minutes, duration.inWholeMinutes)
    seconds < 86400 -> stringResource(Res.string.unit_hours, duration.inWholeHours)
    days < 7 -> stringResource(Res.string.unit_days, days)
    else -> {
      val monthNames =
        listOf(
          stringResource(Res.string.month_jan),
          stringResource(Res.string.month_feb),
          stringResource(Res.string.month_mar),
          stringResource(Res.string.month_apr),
          stringResource(Res.string.month_may),
          stringResource(Res.string.month_jun),
          stringResource(Res.string.month_jul),
          stringResource(Res.string.month_aug),
          stringResource(Res.string.month_sep),
          stringResource(Res.string.month_oct),
          stringResource(Res.string.month_nov),
          stringResource(Res.string.month_dec),
        )
      val numericDateFormatter =
        LocalDateTime.Format {
          day(Padding.ZERO)
          char(' ')
          monthName(MonthNames(monthNames))
          char(' ')
          yearTwoDigits(baseYear = 1950)
        }

      numericDateFormatter.format(this.toLocalDateTime(TimeZone.currentSystemDefault()))
    }
  }
}

@Composable
@ReadOnlyComposable
internal fun Dp.toSp() = with(LocalDensity.current) { this@toSp.toSp() }

/**
 * Idea is to take a value of a animation/transition progress, like if progress is going from 0-1,
 * this function will switch the values to map to 1-0 instead.
 */
internal fun Float.inverse() = 1f - this

internal enum class KeyboardState {
  Opened,
  Closed,
}

@Composable
internal fun keyboardVisibilityAsState(): State<KeyboardState> {
  return rememberUpdatedState(
    if (WindowInsets.ime.getBottom(LocalDensity.current) > 0) KeyboardState.Opened
    else KeyboardState.Closed
  )
}

internal class Ref(var value: Int)

// Note the inline function below which ensures that this function is essentially
// copied at the call site to ensure that its logging only recompositions from the
// original call site.
@Composable
internal fun LogCompositions(msg: String) {
  val ref = remember { Ref(0) }
  SideEffect { ref.value++ }
  Logger.i { "Compositions: $msg ${ref.value}" }
}

fun PagerState.getOffsetFractionForPage(page: Int): Float {
  return (currentPage - page) + currentPageOffsetFraction
}

@Composable
fun <T> PagerState.CollectItemTransition(
  vararg key: Any?,
  itemProvider: (Int) -> T?,
  onTransition: suspend (from: T?, to: T?, progress: Float) -> Unit,
) {
  val currentItemProvider by rememberUpdatedState(itemProvider)
  val currentOnTransition by rememberUpdatedState(onTransition)

  LaunchedEffect(this, key) {
    snapshotFlow {
        val settledPage = settledPage
        val offset = getOffsetFractionForPage(settledPage)

        // Quantize offset to 2 decimal places to throttle transitions
        val quantizedOffset = (offset * 100).toInt() / 100f
        settledPage to quantizedOffset
      }
      .distinctUntilChanged()
      .collect { (settledPage, offset) ->
        val activePost = currentItemProvider(settledPage)
        if (activePost == null) return@collect

        val fromItem =
          if (offset < -Constants.EPSILON) {
            currentItemProvider(settledPage - 1) ?: activePost
          } else {
            activePost
          }

        val toItem =
          if (offset > Constants.EPSILON) {
            currentItemProvider(settledPage + 1) ?: activePost
          } else {
            activePost
          }

        currentOnTransition(fromItem, toItem, offset)
      }
  }
}

fun Morph.toShape(progress: Float): Shape {
  return object : Shape {
    private val path = Path()

    override fun createOutline(
      size: androidx.compose.ui.geometry.Size,
      layoutDirection: LayoutDirection,
      density: Density,
    ): Outline {
      toComposePath(progress = progress, scale = size.minDimension, path = path)
      return Outline.Generic(path)
    }
  }
}

/**
 * Transforms the morph at a given progress into a [Path]. It can optionally be scaled, using the
 * origin (0,0) as pivot point.
 */
fun Morph.toComposePath(progress: Float, scale: Float = 1f, path: Path = Path()): Path {
  var first = true
  path.rewind()
  forEachCubic(progress) { bezier ->
    if (first) {
      path.moveTo(bezier.anchor0X * scale, bezier.anchor0Y * scale)
      first = false
    }
    path.cubicTo(
      bezier.control0X * scale,
      bezier.control0Y * scale,
      bezier.control1X * scale,
      bezier.control1Y * scale,
      bezier.anchor1X * scale,
      bezier.anchor1Y * scale,
    )
  }
  path.close()
  return path
}
