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

package dev.sasikanth.rss.reader.ui

import androidx.compose.runtime.Composable

/**
 * Returns true if the platform supports system dynamic colors (Material You). This is only
 * available on Android 12+ (API 31+).
 */
internal expect val isSystemDynamicColorSupported: Boolean

/**
 * Returns the system dynamic color scheme based on the Android system wallpaper colors (Material
 * You). On non-Android platforms, this returns a fallback color scheme.
 */
@Composable internal expect fun systemDynamicColorScheme(isDark: Boolean): AppColorScheme
