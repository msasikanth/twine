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

package dev.sasikanth.rss.reader.widget

import android.os.Build
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.glance.GlanceModifier
import androidx.glance.LocalContext
import androidx.glance.appwidget.cornerRadius

/**
 * Applies corner radius for views that are visually positioned [widgetPadding]dp inside of the
 * widget background.
 */
@Composable
fun GlanceModifier.appWidgetInnerCornerRadius(widgetPadding: Dp): GlanceModifier {

  if (Build.VERSION.SDK_INT < 31) {
    return GlanceModifier.cornerRadius(28.dp)
  }

  val resources = LocalContext.current.resources
  // get dimension in float (without rounding).
  val px = resources.getDimension(android.R.dimen.system_app_widget_background_radius)
  val widgetBackgroundRadiusDpValue = px / resources.displayMetrics.density
  if (widgetBackgroundRadiusDpValue < widgetPadding.value) {
    return this
  }
  return this.cornerRadius(Dp(widgetBackgroundRadiusDpValue - widgetPadding.value))
}
