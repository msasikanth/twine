/*
 * Copyright 2026 Sasikanth Miriyampalli
 *
 * Licensed under the GPL, Version 3.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.gnu.org/licenses/gpl-3.0.en.html
 *
 */

package dev.sasikanth.rss.reader.platform

import dev.sasikanth.rss.reader.app.AppIconManager
import dev.sasikanth.rss.reader.app.JvmAppIconManager
import dev.sasikanth.rss.reader.media.AudioPlayer
import dev.sasikanth.rss.reader.media.JvmAudioPlayer
import dev.sasikanth.rss.reader.utils.InAppRating
import dev.sasikanth.rss.reader.utils.JvmInAppRating
import me.tatarka.inject.annotations.Provides

actual interface PlatformComponent {

  @Provides fun JvmLinkHandler.bind(): LinkHandler = this

  @Provides fun JvmInAppRating.bind(): InAppRating = this

  @Provides fun JvmAppIconManager.bind(): AppIconManager = this

  @Provides fun JvmAudioPlayer.bind(): AudioPlayer = this
}
