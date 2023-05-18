/*
 * Copyright 2023 Sasikanth Miriyampalli
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
package dev.sasikanth.rss.reader.ui

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

private val darkYellowGreen = Color(0xFF1A1E00)
private val darkYellowGreenLight = Color(0xFF2E3300)
private val vibrantYellowGreen = Color(0xFFBFD100)
private val darkYellow = Color(0xFF0E0E0A)
private val darkGrayishYellow = Color(0xFF20201B)

open class AppColorScheme {

  open val tintedBackground: Color
    get() = darkYellowGreen

  open val tintedSurface: Color
    get() = darkYellowGreenLight

  open val tintedForeground: Color
    get() = vibrantYellowGreen

  open val surfaceContainer: Color
    get() = darkGrayishYellow

  open val surfaceContainerLowest: Color
    get() = darkYellow
}

internal val LocalAppColorScheme = staticCompositionLocalOf { AppColorScheme() }
