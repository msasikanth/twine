/*
 * Copyright 2024 Sasikanth Miriyampalli
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

package dev.sasikanth.rss.reader.utils

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import dev.sasikanth.material.color.utilities.utils.ColorUtils
import korlibs.util.format
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

val String?.asJSString: String
  get() {
    val data = Json.encodeToString(this.orEmpty())
    return data
  }

/**
 * Hex string representing color, ex. #ff0000 for red.
 *
 * @param argb ARGB representation of a color.
 */
fun Color.hexString(): String {
  val argb = toArgb()
  val red: Int = ColorUtils.redFromArgb(argb)
  val blue: Int = ColorUtils.blueFromArgb(argb)
  val green: Int = ColorUtils.greenFromArgb(argb)
  return "#%02x%02x%02x".format(red, green, blue)
}
