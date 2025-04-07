/*
 * Copyright 2025 Sasikanth Miriyampalli
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

package dev.sasikanth.rss.reader.resources.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType.Companion.EvenOdd
import androidx.compose.ui.graphics.PathFillType.Companion.NonZero
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap.Companion.Butt
import androidx.compose.ui.graphics.StrokeJoin.Companion.Miter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.ImageVector.Builder
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val TwineIcons.Settings: ImageVector
  get() {
    if (_settings != null) {
      return _settings!!
    }
    _settings =
      Builder(
          name = "Settings",
          defaultWidth = 20.0.dp,
          defaultHeight = 20.0.dp,
          viewportWidth = 20.0f,
          viewportHeight = 20.0f
        )
        .apply {
          path(
            fill = SolidColor(Color(0xFF181D18)),
            stroke = null,
            strokeLineWidth = 0.0f,
            strokeLineCap = Butt,
            strokeLineJoin = Miter,
            strokeLineMiter = 4.0f,
            pathFillType = NonZero
          ) {
            moveTo(10.0f, 12.5f)
            curveTo(11.381f, 12.5f, 12.5f, 11.381f, 12.5f, 10.0f)
            curveTo(12.5f, 8.619f, 11.381f, 7.5f, 10.0f, 7.5f)
            curveTo(8.619f, 7.5f, 7.5f, 8.619f, 7.5f, 10.0f)
            curveTo(7.5f, 11.381f, 8.619f, 12.5f, 10.0f, 12.5f)
            close()
          }
          path(
            fill = SolidColor(Color(0xFF181D18)),
            stroke = null,
            strokeLineWidth = 0.0f,
            strokeLineCap = Butt,
            strokeLineJoin = Miter,
            strokeLineMiter = 4.0f,
            pathFillType = EvenOdd
          ) {
            moveTo(7.706f, 16.683f)
            curveTo(7.799f, 17.435f, 8.437f, 18.0f, 9.195f, 18.0f)
            horizontalLineTo(10.8f)
            curveTo(11.552f, 18.0f, 12.189f, 17.442f, 12.287f, 16.696f)
            lineTo(12.451f, 15.452f)
            curveTo(12.642f, 15.366f, 12.828f, 15.27f, 13.01f, 15.164f)
            curveTo(13.18f, 15.064f, 13.346f, 14.957f, 13.505f, 14.842f)
            lineTo(14.675f, 15.326f)
            curveTo(15.371f, 15.615f, 16.174f, 15.341f, 16.55f, 14.687f)
            lineTo(17.342f, 13.306f)
            curveTo(17.718f, 12.651f, 17.548f, 11.818f, 16.945f, 11.363f)
            lineTo(15.949f, 10.61f)
            curveTo(15.969f, 10.399f, 15.976f, 10.193f, 15.976f, 10.0f)
            curveTo(15.976f, 9.806f, 15.969f, 9.602f, 15.949f, 9.39f)
            lineTo(16.945f, 8.637f)
            curveTo(17.548f, 8.182f, 17.718f, 7.35f, 17.342f, 6.694f)
            lineTo(16.55f, 5.313f)
            curveTo(16.172f, 4.654f, 15.361f, 4.382f, 14.663f, 4.679f)
            lineTo(13.513f, 5.166f)
            curveTo(13.351f, 5.048f, 13.184f, 4.937f, 13.01f, 4.836f)
            curveTo(12.831f, 4.731f, 12.648f, 4.637f, 12.461f, 4.552f)
            lineTo(12.309f, 3.317f)
            curveTo(12.217f, 2.565f, 11.578f, 2.0f, 10.82f, 2.0f)
            horizontalLineTo(9.216f)
            curveTo(8.463f, 2.0f, 7.827f, 2.558f, 7.729f, 3.304f)
            lineTo(7.564f, 4.548f)
            curveTo(7.373f, 4.634f, 7.187f, 4.73f, 7.006f, 4.836f)
            curveTo(6.835f, 4.936f, 6.67f, 5.043f, 6.51f, 5.158f)
            lineTo(5.341f, 4.674f)
            curveTo(4.644f, 4.385f, 3.841f, 4.659f, 3.466f, 5.313f)
            lineTo(2.674f, 6.694f)
            curveTo(2.296f, 7.353f, 2.47f, 8.191f, 3.079f, 8.644f)
            lineTo(4.067f, 9.379f)
            curveTo(4.047f, 9.586f, 4.039f, 9.792f, 4.039f, 10.0f)
            curveTo(4.039f, 10.2f, 4.047f, 10.403f, 4.067f, 10.609f)
            lineTo(3.071f, 11.363f)
            curveTo(2.468f, 11.818f, 2.298f, 12.651f, 2.674f, 13.306f)
            lineTo(3.466f, 14.687f)
            curveTo(3.844f, 15.345f, 4.654f, 15.618f, 5.353f, 15.321f)
            lineTo(6.502f, 14.834f)
            curveTo(6.664f, 14.952f, 6.832f, 15.063f, 7.006f, 15.164f)
            curveTo(7.184f, 15.269f, 7.367f, 15.363f, 7.555f, 15.448f)
            lineTo(7.706f, 16.683f)
            close()
            moveTo(12.252f, 13.87f)
            curveTo(11.894f, 14.079f, 11.504f, 14.238f, 11.084f, 14.347f)
            lineTo(10.8f, 16.5f)
            horizontalLineTo(9.195f)
            lineTo(8.931f, 14.347f)
            curveTo(8.511f, 14.238f, 8.122f, 14.079f, 7.763f, 13.87f)
            curveTo(7.404f, 13.66f, 7.076f, 13.399f, 6.778f, 13.087f)
            lineTo(4.767f, 13.941f)
            lineTo(3.975f, 12.559f)
            lineTo(5.722f, 11.239f)
            curveTo(5.586f, 10.833f, 5.539f, 10.433f, 5.539f, 10.0f)
            curveTo(5.539f, 9.54f, 5.586f, 9.147f, 5.722f, 8.741f)
            lineTo(3.975f, 7.441f)
            lineTo(4.767f, 6.059f)
            lineTo(6.778f, 6.892f)
            curveTo(7.076f, 6.594f, 7.404f, 6.34f, 7.763f, 6.13f)
            curveTo(8.122f, 5.921f, 8.511f, 5.761f, 8.931f, 5.653f)
            lineTo(9.216f, 3.5f)
            horizontalLineTo(10.82f)
            lineTo(11.084f, 5.653f)
            curveTo(11.504f, 5.761f, 11.894f, 5.921f, 12.252f, 6.13f)
            curveTo(12.611f, 6.34f, 12.94f, 6.601f, 13.238f, 6.912f)
            lineTo(15.248f, 6.059f)
            lineTo(16.041f, 7.441f)
            lineTo(14.294f, 8.761f)
            curveTo(14.429f, 9.14f, 14.476f, 9.567f, 14.476f, 10.0f)
            curveTo(14.476f, 10.433f, 14.429f, 10.86f, 14.294f, 11.239f)
            lineTo(16.041f, 12.559f)
            lineTo(15.248f, 13.941f)
            lineTo(13.238f, 13.108f)
            curveTo(12.94f, 13.406f, 12.611f, 13.66f, 12.252f, 13.87f)
            close()
          }
        }
        .build()
    return _settings!!
  }

private var _settings: ImageVector? = null
