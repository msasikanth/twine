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

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import dev.icerock.moko.resources.compose.fontFamilyResource
import dev.sasikanth.rss.reader.CommonRes

@Composable
fun AppTheme(content: @Composable () -> Unit) {
  val appColorScheme = AppColorScheme()
  val fontFamily = fontFamilyResource(CommonRes.fonts.golos.medium)
  CompositionLocalProvider(LocalAppColorScheme provides appColorScheme) {
    MaterialTheme(
      colorScheme = darkColorScheme(),
      typography = typography(fontFamily),
      content = content
    )
  }
}

internal object AppTheme {

  val colorScheme: AppColorScheme
    @Composable @ReadOnlyComposable get() = LocalAppColorScheme.current
}
