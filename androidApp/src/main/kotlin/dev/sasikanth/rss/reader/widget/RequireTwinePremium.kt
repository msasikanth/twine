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

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.LocalContext
import androidx.glance.appwidget.cornerRadius
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.padding
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextAlign
import androidx.glance.text.TextStyle
import dev.sasikanth.rss.reader.R

@Composable
fun RequireTwinePremium(modifier: GlanceModifier = GlanceModifier) {
  val context = LocalContext.current
  Box(
    modifier =
      modifier
        .fillMaxSize()
        .background(GlanceTheme.colors.background)
        .cornerRadius(16.dp)
        .padding(16.dp),
    contentAlignment = Alignment.Center,
  ) {
    Text(
      text = context.getString(R.string.widget_required_premium),
      style =
        TextStyle(fontWeight = FontWeight.Medium, fontSize = 16.sp, textAlign = TextAlign.Center),
    )
  }
}
