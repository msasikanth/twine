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

package dev.sasikanth.rss.reader.resources.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val TwineIcons.Changelog: ImageVector
  get() {
    if (_Changelog != null) {
      return _Changelog!!
    }
    _Changelog =
      ImageVector.Builder(
          name = "Changelog",
          defaultWidth = 20.dp,
          defaultHeight = 20.dp,
          viewportWidth = 20f,
          viewportHeight = 20f,
        )
        .apply {
          path(fill = SolidColor(Color(0xFF161D1C))) {
            moveTo(12.373f, 2f)
            curveTo(13.138f, 2f, 13.87f, 2.153f, 14.537f, 2.432f)
            curveTo(14.768f, 2.528f, 14.936f, 2.733f, 14.984f, 2.979f)
            curveTo(15.033f, 3.224f, 14.956f, 3.478f, 14.779f, 3.654f)
            lineTo(12.748f, 5.685f)
            verticalLineTo(7.25f)
            horizontalLineTo(14.314f)
            lineTo(16.344f, 5.219f)
            curveTo(16.521f, 5.042f, 16.774f, 4.965f, 17.02f, 5.014f)
            curveTo(17.265f, 5.062f, 17.47f, 5.23f, 17.566f, 5.461f)
            curveTo(17.845f, 6.128f, 17.998f, 6.86f, 17.998f, 7.625f)
            curveTo(17.998f, 10.731f, 15.479f, 13.25f, 12.373f, 13.25f)
            curveTo(11.893f, 13.25f, 11.428f, 13.188f, 10.984f, 13.074f)
            lineTo(6.4f, 17.657f)
            curveTo(5.279f, 18.778f, 3.462f, 18.778f, 2.341f, 17.657f)
            curveTo(1.22f, 16.536f, 1.219f, 14.718f, 2.341f, 13.597f)
            lineTo(6.924f, 9.014f)
            curveTo(6.81f, 8.57f, 6.748f, 8.105f, 6.748f, 7.625f)
            curveTo(6.748f, 4.519f, 9.267f, 2f, 12.373f, 2f)
            close()
            moveTo(12.373f, 3.5f)
            curveTo(10.095f, 3.5f, 8.248f, 5.347f, 8.248f, 7.625f)
            curveTo(8.248f, 8.1f, 8.329f, 8.556f, 8.478f, 8.981f)
            curveTo(8.572f, 9.253f, 8.504f, 9.555f, 8.301f, 9.759f)
            lineTo(3.401f, 14.658f)
            curveTo(2.866f, 15.194f, 2.866f, 16.061f, 3.401f, 16.597f)
            curveTo(3.937f, 17.132f, 4.804f, 17.132f, 5.34f, 16.597f)
            lineTo(10.239f, 11.697f)
            curveTo(10.443f, 11.494f, 10.745f, 11.426f, 11.017f, 11.521f)
            curveTo(11.442f, 11.669f, 11.898f, 11.75f, 12.373f, 11.75f)
            curveTo(14.651f, 11.75f, 16.498f, 9.903f, 16.498f, 7.625f)
            curveTo(16.498f, 7.484f, 16.491f, 7.344f, 16.478f, 7.207f)
            lineTo(15.153f, 8.53f)
            curveTo(15.013f, 8.671f, 14.822f, 8.75f, 14.623f, 8.75f)
            horizontalLineTo(11.998f)
            curveTo(11.584f, 8.75f, 11.248f, 8.414f, 11.248f, 8f)
            verticalLineTo(5.375f)
            curveTo(11.248f, 5.176f, 11.327f, 4.985f, 11.468f, 4.845f)
            lineTo(12.791f, 3.521f)
            curveTo(12.654f, 3.507f, 12.514f, 3.5f, 12.373f, 3.5f)
            close()
          }
        }
        .build()

    return _Changelog!!
  }

@Suppress("ObjectPropertyName") private var _Changelog: ImageVector? = null
