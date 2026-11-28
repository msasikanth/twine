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

package dev.sasikanth.rss.reader.components

import androidx.compose.ui.graphics.Color
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class AmbientBlurredBackdropTest {

  @Test
  fun shouldRenderBlurredBackdrop_requiresAllConditions() {
    assertTrue(
      shouldRenderBlurredBackdrop(
        canBlur = true,
        hasBlurEffect = true,
        imageUrl = "https://example.com/art.jpg",
        blockImages = false,
      )
    )
    assertFalse(
      shouldRenderBlurredBackdrop(
        canBlur = false,
        hasBlurEffect = true,
        imageUrl = "https://example.com/art.jpg",
        blockImages = false,
      )
    )
    assertFalse(
      shouldRenderBlurredBackdrop(
        canBlur = true,
        hasBlurEffect = false,
        imageUrl = "https://example.com/art.jpg",
        blockImages = false,
      )
    )
    assertFalse(
      shouldRenderBlurredBackdrop(
        canBlur = true,
        hasBlurEffect = true,
        imageUrl = null,
        blockImages = false,
      )
    )
    assertFalse(
      shouldRenderBlurredBackdrop(
        canBlur = true,
        hasBlurEffect = true,
        imageUrl = "   ",
        blockImages = false,
      )
    )
    assertFalse(
      shouldRenderBlurredBackdrop(
        canBlur = true,
        hasBlurEffect = true,
        imageUrl = "https://example.com/art.jpg",
        blockImages = true,
      )
    )
  }

  @Test
  fun resolveAmbientBackdropLayerAlphas_clampsAndCrossfadesWithinMaxAlpha() {
    assertEquals(0f to 1f, resolveAmbientBackdropLayerAlphas(maxAlpha = 1f, imageLayerAlpha = 0f))
    assertEquals(1f to 0f, resolveAmbientBackdropLayerAlphas(maxAlpha = 1f, imageLayerAlpha = 1f))
    assertEquals(
      0f to 0.45f,
      resolveAmbientBackdropLayerAlphas(maxAlpha = 0.45f, imageLayerAlpha = 0f),
    )
    assertEquals(
      0.45f to 0f,
      resolveAmbientBackdropLayerAlphas(maxAlpha = 0.45f, imageLayerAlpha = 1f),
    )
    assertEquals(
      0f to 0f,
      resolveAmbientBackdropLayerAlphas(maxAlpha = -0.2f, imageLayerAlpha = 1f),
    )
    assertEquals(1f to 0f, resolveAmbientBackdropLayerAlphas(maxAlpha = 2f, imageLayerAlpha = 1.5f))
  }

  @Test
  fun resolveAmbientLuminosityColor_returnsNeutralLightAndDarkGrays() {
    val light = resolveAmbientLuminosityColor(isDarkTheme = false)
    val dark = resolveAmbientLuminosityColor(isDarkTheme = true)

    assertEquals(Color(0xFFC6C6C6), light)
    assertEquals(Color(0xFF5E5E5E), dark)
    assertEquals(light.red, light.green)
    assertEquals(light.green, light.blue)
    assertEquals(dark.red, dark.green)
    assertEquals(dark.green, dark.blue)
  }
}
