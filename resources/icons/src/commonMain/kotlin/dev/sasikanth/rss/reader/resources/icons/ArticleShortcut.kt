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
import androidx.compose.ui.graphics.PathFillType.Companion.NonZero
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap.Companion.Butt
import androidx.compose.ui.graphics.StrokeJoin.Companion.Miter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.ImageVector.Builder
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

public val TwineIcons.ArticleShortcut: ImageVector
  get() {
    if (_fetchArticle != null) {
      return _fetchArticle!!
    }
    _fetchArticle =
      Builder(
          name = "FetchArticle",
          defaultWidth = 20.0.dp,
          defaultHeight = 20.0.dp,
          viewportWidth = 20.0f,
          viewportHeight = 20.0f
        )
        .apply {
          path(
            fill = SolidColor(Color(0xFFC2C9BD)),
            stroke = null,
            strokeLineWidth = 0.0f,
            strokeLineCap = Butt,
            strokeLineJoin = Miter,
            strokeLineMiter = 4.0f,
            pathFillType = NonZero
          ) {
            moveTo(9.25f, 14.875f)
            verticalLineTo(5.417f)
            curveTo(8.667f, 5.111f, 8.063f, 4.882f, 7.438f, 4.729f)
            curveTo(6.813f, 4.576f, 6.165f, 4.5f, 5.495f, 4.5f)
            curveTo(4.982f, 4.5f, 4.473f, 4.545f, 3.967f, 4.635f)
            curveTo(3.461f, 4.726f, 2.972f, 4.875f, 2.5f, 5.083f)
            verticalLineTo(14.5f)
            curveTo(2.986f, 14.319f, 3.478f, 14.191f, 3.975f, 14.115f)
            curveTo(4.473f, 14.038f, 4.981f, 14.0f, 5.5f, 14.0f)
            curveTo(6.158f, 14.0f, 6.798f, 14.083f, 7.42f, 14.25f)
            curveTo(8.043f, 14.417f, 8.653f, 14.625f, 9.25f, 14.875f)
            close()
            moveTo(1.0f, 15.229f)
            verticalLineTo(4.75f)
            curveTo(1.0f, 4.556f, 1.052f, 4.375f, 1.156f, 4.208f)
            curveTo(1.26f, 4.042f, 1.403f, 3.917f, 1.583f, 3.833f)
            curveTo(2.208f, 3.556f, 2.847f, 3.347f, 3.498f, 3.208f)
            curveTo(4.149f, 3.069f, 4.814f, 3.0f, 5.491f, 3.0f)
            curveTo(6.455f, 3.0f, 7.292f, 3.094f, 8.0f, 3.281f)
            curveTo(8.708f, 3.469f, 9.458f, 3.764f, 10.25f, 4.167f)
            curveTo(10.403f, 4.25f, 10.524f, 4.365f, 10.615f, 4.51f)
            curveTo(10.705f, 4.656f, 10.75f, 4.819f, 10.75f, 5.0f)
            verticalLineTo(14.875f)
            curveTo(11.347f, 14.597f, 11.957f, 14.382f, 12.58f, 14.229f)
            curveTo(13.202f, 14.076f, 13.842f, 14.0f, 14.5f, 14.0f)
            curveTo(15.014f, 14.0f, 15.524f, 14.035f, 16.031f, 14.104f)
            curveTo(16.538f, 14.174f, 17.028f, 14.306f, 17.5f, 14.5f)
            verticalLineTo(4.229f)
            curveTo(17.5f, 4.017f, 17.576f, 3.839f, 17.729f, 3.695f)
            curveTo(17.882f, 3.551f, 18.064f, 3.479f, 18.275f, 3.479f)
            curveTo(18.486f, 3.479f, 18.663f, 3.551f, 18.806f, 3.695f)
            curveTo(18.949f, 3.839f, 19.021f, 4.017f, 19.021f, 4.229f)
            verticalLineTo(15.229f)
            curveTo(19.021f, 15.576f, 18.913f, 15.854f, 18.698f, 16.063f)
            curveTo(18.483f, 16.271f, 18.257f, 16.319f, 18.021f, 16.208f)
            curveTo(17.465f, 15.958f, 16.892f, 15.778f, 16.302f, 15.667f)
            curveTo(15.713f, 15.556f, 15.112f, 15.5f, 14.5f, 15.5f)
            curveTo(13.806f, 15.5f, 13.139f, 15.601f, 12.5f, 15.802f)
            curveTo(11.861f, 16.003f, 11.243f, 16.271f, 10.646f, 16.604f)
            curveTo(10.535f, 16.66f, 10.427f, 16.701f, 10.323f, 16.729f)
            curveTo(10.219f, 16.757f, 10.111f, 16.771f, 10.0f, 16.771f)
            curveTo(9.889f, 16.771f, 9.781f, 16.76f, 9.677f, 16.74f)
            curveTo(9.573f, 16.719f, 9.464f, 16.675f, 9.351f, 16.608f)
            curveTo(8.756f, 16.258f, 8.139f, 15.986f, 7.5f, 15.792f)
            curveTo(6.861f, 15.597f, 6.194f, 15.5f, 5.5f, 15.5f)
            curveTo(4.972f, 15.5f, 4.444f, 15.552f, 3.917f, 15.656f)
            curveTo(3.389f, 15.76f, 2.882f, 15.917f, 2.396f, 16.125f)
            curveTo(2.063f, 16.264f, 1.747f, 16.239f, 1.448f, 16.052f)
            curveTo(1.149f, 15.864f, 1.0f, 15.59f, 1.0f, 15.229f)
            close()
            moveTo(13.0f, 11.167f)
            verticalLineTo(3.5f)
            curveTo(13.0f, 3.346f, 13.042f, 3.207f, 13.125f, 3.083f)
            curveTo(13.208f, 2.958f, 13.326f, 2.868f, 13.479f, 2.813f)
            lineTo(14.729f, 2.313f)
            curveTo(14.924f, 2.243f, 15.101f, 2.264f, 15.26f, 2.375f)
            curveTo(15.42f, 2.486f, 15.5f, 2.639f, 15.5f, 2.833f)
            verticalLineTo(10.5f)
            curveTo(15.5f, 10.654f, 15.458f, 10.793f, 15.375f, 10.917f)
            curveTo(15.292f, 11.042f, 15.174f, 11.132f, 15.021f, 11.188f)
            lineTo(13.771f, 11.688f)
            curveTo(13.576f, 11.757f, 13.399f, 11.736f, 13.24f, 11.625f)
            curveTo(13.08f, 11.514f, 13.0f, 11.361f, 13.0f, 11.167f)
            close()
          }
        }
        .build()
    return _fetchArticle!!
  }

private var _fetchArticle: ImageVector? = null
