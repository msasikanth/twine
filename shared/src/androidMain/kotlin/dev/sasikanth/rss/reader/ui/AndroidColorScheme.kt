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

import android.content.Context
import androidx.annotation.ColorRes
import androidx.annotation.DoNotInline
import androidx.compose.ui.graphics.Color
import dev.sasikanth.rss.reader.common.R

class AndroidColorScheme(context: Context) :
  AppColorScheme(
    tintedBackground = ColorResourceHelper.getColor(context, R.color.material_dynamic_primary10),
    tintedSurface = ColorResourceHelper.getColor(context, R.color.material_dynamic_primary20),
    tintedForeground = ColorResourceHelper.getColor(context, R.color.material_dynamic_primary80),
    surfaceContainer =
      ColorResourceHelper.getColor(context, R.color.m3_ref_palette_dyanmic_neutral12),
    surfaceContainerLowest =
      ColorResourceHelper.getColor(context, R.color.m3_ref_palette_dyanmic_neutral4)
  )

private object ColorResourceHelper {
  @DoNotInline
  fun getColor(context: Context, @ColorRes id: Int): Color {
    return Color(context.resources.getColor(id, context.theme))
  }
}
