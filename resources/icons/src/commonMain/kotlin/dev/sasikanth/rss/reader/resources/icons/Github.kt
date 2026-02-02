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
import androidx.compose.ui.graphics.PathFillType.Companion.EvenOdd
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap.Companion.Butt
import androidx.compose.ui.graphics.StrokeJoin.Companion.Miter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.ImageVector.Builder
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val TwineIcons.GitHub: ImageVector
  get() {
    if (github != null) {
      return github!!
    }
    github =
      Builder(
          name = "Github",
          defaultWidth = 20.0.dp,
          defaultHeight = 20.0.dp,
          viewportWidth = 20.0f,
          viewportHeight = 20.0f,
        )
        .apply {
          path(
            fill = SolidColor(Color(0xFF000000)),
            stroke = null,
            strokeLineWidth = 0.0f,
            strokeLineCap = Butt,
            strokeLineJoin = Miter,
            strokeLineMiter = 4.0f,
            pathFillType = EvenOdd,
          ) {
            moveTo(8.6786f, 1.9716f)
            curveTo(10.4173f, 1.5589f, 12.2286f, 1.5589f, 13.9673f, 1.9716f)
            curveTo(14.968f, 1.3384f, 15.7396f, 1.0427f, 16.3086f, 0.9164f)
            curveTo(16.6217f, 0.8469f, 16.8724f, 0.8289f, 17.0637f, 0.8342f)
            curveTo(17.1592f, 0.8368f, 17.2389f, 0.8452f, 17.3034f, 0.8555f)
            curveTo(17.3355f, 0.8606f, 17.3637f, 0.8662f, 17.388f, 0.8717f)
            curveTo(17.4002f, 0.8745f, 17.4113f, 0.8772f, 17.4215f, 0.8799f)
            lineTo(17.436f, 0.8838f)
            lineTo(17.4427f, 0.8857f)
            lineTo(17.4459f, 0.8866f)
            lineTo(17.4474f, 0.8871f)
            curveTo(17.4482f, 0.8873f, 17.449f, 0.8875f, 17.2226f, 1.6508f)
            lineTo(17.449f, 0.8875f)
            curveTo(17.6759f, 0.9548f, 17.8611f, 1.1196f, 17.9544f, 1.3372f)
            curveTo(18.3811f, 2.3327f, 18.4592f, 3.4382f, 18.1846f, 4.4766f)
            curveTo(18.5056f, 4.9066f, 18.7505f, 5.3965f, 18.9249f, 5.8646f)
            curveTo(19.1515f, 6.473f, 19.2847f, 7.1138f, 19.2847f, 7.6381f)
            curveTo(19.2847f, 9.9838f, 18.5671f, 11.5571f, 17.3747f, 12.5597f)
            curveTo(16.5735f, 13.2334f, 15.6164f, 13.5977f, 14.6727f, 13.8057f)
            curveTo(14.6849f, 13.8369f, 14.6968f, 13.8683f, 14.7082f, 13.8999f)
            curveTo(14.8717f, 14.3541f, 14.939f, 14.8372f, 14.9057f, 15.3185f)
            verticalLineTo(18.3705f)
            curveTo(14.9057f, 18.8102f, 14.5493f, 19.1667f, 14.1095f, 19.1667f)
            curveTo(13.6698f, 19.1667f, 13.3134f, 18.8102f, 13.3134f, 18.3705f)
            verticalLineTo(15.2893f)
            curveTo(13.3134f, 15.2685f, 13.3142f, 15.2476f, 13.3158f, 15.2269f)
            curveTo(13.3368f, 14.9599f, 13.3008f, 14.6915f, 13.21f, 14.4395f)
            curveTo(13.1193f, 14.1875f, 12.9759f, 13.9577f, 12.7895f, 13.7655f)
            curveTo(12.5781f, 13.5474f, 12.5091f, 13.2279f, 12.6119f, 12.9421f)
            curveTo(12.7146f, 12.6562f, 12.971f, 12.4537f, 13.2729f, 12.42f)
            curveTo(14.4782f, 12.2857f, 15.5649f, 12.001f, 16.3499f, 11.341f)
            curveTo(17.0962f, 10.7134f, 17.6923f, 9.6394f, 17.6923f, 7.6381f)
            curveTo(17.6923f, 7.3502f, 17.6116f, 6.9007f, 17.4327f, 6.4205f)
            curveTo(17.2546f, 5.9424f, 17.0047f, 5.503f, 16.7176f, 5.2014f)
            curveTo(16.5075f, 4.9807f, 16.4418f, 4.6595f, 16.5483f, 4.374f)
            curveTo(16.7771f, 3.761f, 16.8135f, 3.0972f, 16.6597f, 2.4696f)
            curveTo(16.6577f, 2.47f, 16.6557f, 2.4704f, 16.6537f, 2.4709f)
            curveTo(16.2654f, 2.5571f, 15.578f, 2.8033f, 14.5529f, 3.4905f)
            curveTo(14.3614f, 3.6188f, 14.1237f, 3.6579f, 13.9013f, 3.5976f)
            curveTo(12.2128f, 3.14f, 10.433f, 3.14f, 8.7446f, 3.5976f)
            curveTo(8.5221f, 3.6579f, 8.2845f, 3.6188f, 8.093f, 3.4905f)
            curveTo(7.0679f, 2.8033f, 6.3805f, 2.5571f, 5.9922f, 2.4709f)
            curveTo(5.9902f, 2.4704f, 5.9882f, 2.47f, 5.9862f, 2.4696f)
            curveTo(5.8323f, 3.0972f, 5.8688f, 3.761f, 6.0975f, 4.374f)
            curveTo(6.2041f, 4.6595f, 6.1384f, 4.9807f, 5.9283f, 5.2014f)
            curveTo(5.6382f, 5.5061f, 5.3887f, 5.9453f, 5.2117f, 6.4239f)
            curveTo(5.0342f, 6.9039f, 4.9535f, 7.359f, 4.9535f, 7.662f)
            curveTo(4.9535f, 9.6448f, 5.5481f, 10.7121f, 6.296f, 11.341f)
            curveTo(7.0838f, 12.0034f, 8.1736f, 12.2949f, 9.3829f, 12.4451f)
            curveTo(9.6824f, 12.4823f, 9.9352f, 12.6857f, 10.0356f, 12.9703f)
            curveTo(10.136f, 13.255f, 10.0668f, 13.5719f, 9.8569f, 13.7888f)
            curveTo(9.6727f, 13.9792f, 9.5306f, 14.2062f, 9.4399f, 14.4551f)
            curveTo(9.3492f, 14.704f, 9.312f, 14.9692f, 9.3305f, 15.2334f)
            curveTo(9.3319f, 15.252f, 9.3325f, 15.2707f, 9.3325f, 15.2893f)
            verticalLineTo(18.3705f)
            curveTo(9.3325f, 18.8102f, 8.976f, 19.1667f, 8.5363f, 19.1667f)
            curveTo(8.0966f, 19.1667f, 7.7401f, 18.8102f, 7.7401f, 18.3705f)
            verticalLineTo(16.9806f)
            curveTo(7.0983f, 17.0788f, 6.5335f, 17.0619f, 6.0286f, 16.9438f)
            curveTo(5.2604f, 16.764f, 4.7119f, 16.3686f, 4.291f, 15.9478f)
            curveTo(4.0851f, 15.7419f, 3.9077f, 15.5278f, 3.7544f, 15.3357f)
            curveTo(3.7023f, 15.2705f, 3.6547f, 15.21f, 3.6098f, 15.1529f)
            curveTo(3.5139f, 15.0312f, 3.4299f, 14.9246f, 3.3398f, 14.8188f)
            curveTo(3.0786f, 14.5122f, 2.9166f, 14.4025f, 2.77f, 14.3658f)
            curveTo(2.3434f, 14.2592f, 2.0841f, 13.8269f, 2.1907f, 13.4003f)
            curveTo(2.2973f, 12.9738f, 2.7296f, 12.7144f, 3.1562f, 12.821f)
            curveTo(3.8058f, 12.9834f, 4.2409f, 13.4211f, 4.552f, 13.7862f)
            curveTo(4.6627f, 13.9163f, 4.7772f, 14.0615f, 4.8819f, 14.1945f)
            curveTo(4.9226f, 14.2461f, 4.9619f, 14.2959f, 4.9988f, 14.3423f)
            curveTo(5.141f, 14.5203f, 5.2746f, 14.6794f, 5.417f, 14.8218f)
            curveTo(5.6929f, 15.0977f, 5.9902f, 15.2994f, 6.3914f, 15.3933f)
            curveTo(6.7101f, 15.4679f, 7.1416f, 15.4854f, 7.7401f, 15.3635f)
            verticalLineTo(15.3154f)
            curveTo(7.7107f, 14.838f, 7.78f, 14.3596f, 7.9438f, 13.91f)
            curveTo(7.9549f, 13.8793f, 7.9665f, 13.8488f, 7.9786f, 13.8185f)
            curveTo(7.0335f, 13.603f, 6.0738f, 13.2346f, 5.2712f, 12.5597f)
            curveTo(4.0803f, 11.5584f, 3.3612f, 9.9943f, 3.3612f, 7.662f)
            curveTo(3.3612f, 7.1289f, 3.4915f, 6.4847f, 3.7182f, 5.8717f)
            curveTo(3.8926f, 5.3998f, 4.1382f, 4.9083f, 4.4612f, 4.4762f)
            curveTo(4.1867f, 3.4379f, 4.2649f, 2.3326f, 4.6915f, 1.3372f)
            curveTo(4.7847f, 1.1196f, 4.97f, 0.9548f, 5.1969f, 0.8875f)
            lineTo(5.4233f, 1.6508f)
            curveTo(5.1969f, 0.8875f, 5.1976f, 0.8873f, 5.1984f, 0.8871f)
            lineTo(5.2f, 0.8866f)
            lineTo(5.2032f, 0.8857f)
            lineTo(5.2099f, 0.8838f)
            lineTo(5.2244f, 0.8799f)
            curveTo(5.2345f, 0.8772f, 5.2457f, 0.8745f, 5.2578f, 0.8717f)
            curveTo(5.2821f, 0.8662f, 5.3103f, 0.8606f, 5.3425f, 0.8555f)
            curveTo(5.4069f, 0.8452f, 5.4867f, 0.8368f, 5.5821f, 0.8342f)
            curveTo(5.7735f, 0.8289f, 6.0242f, 0.8469f, 6.3373f, 0.9164f)
            curveTo(6.9063f, 1.0427f, 7.6779f, 1.3384f, 8.6786f, 1.9716f)
            close()
          }
        }
        .build()
    return github!!
  }

private var github: ImageVector? = null
