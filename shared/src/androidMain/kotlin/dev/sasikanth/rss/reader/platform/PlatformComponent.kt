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

package dev.sasikanth.rss.reader.platform

import dev.sasikanth.rss.reader.app.AndroidAppIconManager
import dev.sasikanth.rss.reader.app.AppIconManager
import dev.sasikanth.rss.reader.media.AndroidAudioPlayer
import dev.sasikanth.rss.reader.media.AudioPlayer
import dev.sasikanth.rss.reader.utils.AndroidInAppRating
import dev.sasikanth.rss.reader.utils.InAppRating
import me.tatarka.inject.annotations.Provides

actual interface PlatformComponent {

  @Provides fun AndroidLinkHandler.bind(): LinkHandler = this

  @Provides fun AndroidInAppRating.bind(): InAppRating = this

  @Provides fun AndroidAppIconManager.bind(): AppIconManager = this

  @Provides fun AndroidAudioPlayer.bind(): AudioPlayer = this
}
