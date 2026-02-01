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

package dev.sasikanth.rss.reader.di

import dev.sasikanth.rss.reader.app.App
import dev.sasikanth.rss.reader.di.scopes.ActivityScope
import dev.sasikanth.rss.reader.platform.PlatformComponent
import dev.sasikanth.rss.reader.share.ShareComponent
import me.tatarka.inject.annotations.Component

@ActivityScope
@Component
abstract class DesktopComponent(@Component val applicationComponent: ApplicationComponent) :
  PlatformComponent, ShareComponent {

  abstract val app: App

  companion object
}
