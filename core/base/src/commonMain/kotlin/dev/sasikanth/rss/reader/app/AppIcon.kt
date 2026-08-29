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

package dev.sasikanth.rss.reader.app

enum class AppIcon(val isPremium: Boolean = true) {
  Default(isPremium = false),
  Solarized,
  Amber,
  Coral,
  Raspberry,
  Skyline,
  Lavender,
  Parchment,
  Slate,
  Sepia,
}

/**
 * Icons shipped before the set was aligned with the app's theme variants. Their Android
 * activity-aliases stay declared in the manifest so that installs still pointing at one keep a
 * working launcher entry until [replacement] is applied.
 */
enum class LegacyAppIcon(val replacement: AppIcon) {
  AntiqueGold(AppIcon.Amber),
  Cranberry(AppIcon.Coral),
  DarkJade(AppIcon.Default),
  DeepIce(AppIcon.Skyline),
  DeepTeal(AppIcon.Default),
  DustyRose(AppIcon.Raspberry),
  RoyalPlum(AppIcon.Raspberry),
  SlateBlue(AppIcon.Slate),
  SoftSage(AppIcon.Default),
  StormySky(AppIcon.Slate),
}
