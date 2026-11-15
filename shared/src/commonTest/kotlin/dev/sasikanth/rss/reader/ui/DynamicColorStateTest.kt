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

package dev.sasikanth.rss.reader.ui

import androidx.compose.ui.graphics.Color
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain

class DynamicColorStateTest {

  @BeforeTest
  fun setUp() {
    Dispatchers.setMain(UnconfinedTestDispatcher())
  }

  @AfterTest
  fun tearDown() {
    Dispatchers.resetMain()
  }

  @Test
  fun appColorScheme_keepsSettledSecondaryStableWhileAnimating() {
    val initialValues = darkAppColorScheme()
    val updatedValues = initialValues.copy(secondary = Color.Red, onSurface = Color.Cyan)
    val colorScheme = AppColorScheme(initialValues)

    var observedValues: AppColorValues? = null
    var observedAnimating: Boolean? = null
    val dispose =
      colorScheme.observeValues { values, isAnimating ->
        observedValues = values
        observedAnimating = isAnimating
      }

    colorScheme.updateFromWithAnimationState(values = updatedValues, isAnimating = true)

    assertEquals(Color.Red, colorScheme.secondary)
    assertEquals(initialValues.secondary, colorScheme.settledSecondary)
    assertEquals(updatedValues, colorScheme.peekValues())
    assertEquals(updatedValues, observedValues)
    assertEquals(true, observedAnimating)

    colorScheme.updateFromWithAnimationState(values = updatedValues, isAnimating = false)

    assertEquals(Color.Red, colorScheme.settledSecondary)
    assertEquals(false, observedAnimating)

    dispose()
  }

  @Test
  fun appColorScheme_amoledOverrideAppliesToPeekValuesAndSurfaceStates() {
    val initialValues = darkAppColorScheme()
    val colorScheme = AppColorScheme(initialValues)

    colorScheme.updateFromWithAnimationState(
      values = initialValues,
      amoled = true,
      isAnimating = false,
    )

    assertEquals(Color.Black, colorScheme.surface)
    assertEquals(Color.Black, colorScheme.backdrop)
    assertEquals(Color.Black, colorScheme.bottomSheet)
    assertEquals(Color.Black, colorScheme.peekValues().surface)
    assertEquals(Color.Black, colorScheme.peekValues().backdrop)
  }

  @Test
  fun translucentStyle_lazyGettersReflectUpdatedColorSchemeWithoutReinstantiation() {
    val colorScheme = AppColorScheme(darkAppColorScheme())
    val translucentStyle =
      TranslucentStyle(
        backgroundGetter = { colorScheme.onSurface.copy(alpha = 0.08f) },
        foregroundGetter = { colorScheme.onSurface },
        outlineGetter = { colorScheme.onSurface.copy(alpha = 0.16f) },
      )

    val nextValues = colorScheme.peekValues().copy(onSurface = Color.Magenta)
    colorScheme.updateFrom(nextValues)

    assertEquals(Color.Magenta, translucentStyle.foreground)
    assertEquals(Color.Magenta.copy(alpha = 0.08f), translucentStyle.background)
    assertEquals(Color.Magenta.copy(alpha = 0.16f), translucentStyle.outline)
  }

  @Test
  fun dynamicColorState_updatesSettledSecondaryOnlyWhenSwipeSettles() = runTest {
    val defaultLight = lightAppColorScheme()
    val defaultDark = darkAppColorScheme()
    val state =
      DynamicColorState(
        defaultLightValues = defaultLight,
        defaultDarkValues = defaultDark,
        scheme = TwineDynamicColors.Scheme.TonalSpot,
      )

    val fromSeed = Color(0xFF1B5E20)
    val toSeed = Color(0xFFB71C1C)

    state.animate(fromSeedColor = fromSeed, toSeedColor = toSeed, progress = 0f)
    val startSettledSecondary = state.darkAppColorScheme.settledSecondary

    state.animate(fromSeedColor = fromSeed, toSeedColor = toSeed, progress = 0.5f)
    assertEquals(startSettledSecondary, state.darkAppColorScheme.settledSecondary)
    assertNotEquals(startSettledSecondary, state.darkAppColorScheme.secondary)
    assertTrue(state.darkAppColorScheme.isAnimating)

    state.animate(fromSeedColor = fromSeed, toSeedColor = toSeed, progress = 0.99f)
    state.animate(fromSeedColor = fromSeed, toSeedColor = toSeed, progress = 1f)
    assertEquals(state.darkAppColorScheme.secondary, state.darkAppColorScheme.settledSecondary)
    assertTrue(!state.darkAppColorScheme.isAnimating)
  }
}
