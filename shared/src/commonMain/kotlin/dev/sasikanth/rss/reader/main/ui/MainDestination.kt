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

package dev.sasikanth.rss.reader.main.ui

import androidx.compose.ui.graphics.vector.ImageVector
import dev.sasikanth.rss.reader.resources.icons.Bookmark
import dev.sasikanth.rss.reader.resources.icons.BookmarkFilled
import dev.sasikanth.rss.reader.resources.icons.Home
import dev.sasikanth.rss.reader.resources.icons.HomeFilled
import dev.sasikanth.rss.reader.resources.icons.Search
import dev.sasikanth.rss.reader.resources.icons.Settings
import dev.sasikanth.rss.reader.resources.icons.SettingsFilled
import dev.sasikanth.rss.reader.resources.icons.TwineIcons
import org.jetbrains.compose.resources.StringResource
import twine.shared.generated.resources.Res
import twine.shared.generated.resources.bookmarks
import twine.shared.generated.resources.discoveryTitle
import twine.shared.generated.resources.screenHome
import twine.shared.generated.resources.settings

internal enum class MainDestination(
  val icon: ImageVector,
  val selectedIcon: ImageVector,
  val label: StringResource,
) {
  Home(icon = TwineIcons.Home, selectedIcon = TwineIcons.HomeFilled, label = Res.string.screenHome),
  Discovery(
    icon = TwineIcons.Search,
    selectedIcon = TwineIcons.Search,
    label = Res.string.discoveryTitle,
  ),
  Bookmarks(
    icon = TwineIcons.Bookmark,
    selectedIcon = TwineIcons.BookmarkFilled,
    label = Res.string.bookmarks,
  ),
  Settings(
    icon = TwineIcons.Settings,
    selectedIcon = TwineIcons.SettingsFilled,
    label = Res.string.settings,
  ),
}
