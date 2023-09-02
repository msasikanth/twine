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
package dev.sasikanth.rss.reader.resources

import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight

// Copied from : https://github.com/chrisbanes/tivi

@Composable
internal expect fun font(
  fontName: String,
  resourceId: String,
  weight: FontWeight,
  style: FontStyle = FontStyle.Normal,
): Font

internal val GolosFontFamily: FontFamily
  @Composable
  get() =
    FontFamily(
      font(fontName = "Golos", resourceId = "golos_regular", weight = FontWeight.Normal),
      font(fontName = "Golos", resourceId = "golos_medium", weight = FontWeight.Medium),
    )
