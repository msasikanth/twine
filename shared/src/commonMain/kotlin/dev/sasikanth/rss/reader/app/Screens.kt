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
package dev.sasikanth.rss.reader.app

import dev.sasikanth.rss.reader.about.AboutPresenter
import dev.sasikanth.rss.reader.bookmarks.BookmarksPresenter
import dev.sasikanth.rss.reader.home.HomePresenter
import dev.sasikanth.rss.reader.reader.ReaderPresenter
import dev.sasikanth.rss.reader.search.SearchPresenter
import dev.sasikanth.rss.reader.settings.SettingsPresenter

internal sealed interface Screen {
  class Home(val presenter: HomePresenter) : Screen

  class Search(val presenter: SearchPresenter) : Screen

  class Bookmarks(val presenter: BookmarksPresenter) : Screen

  class Settings(val presenter: SettingsPresenter) : Screen

  class About(val presenter: AboutPresenter) : Screen

  class Reader(val presenter: ReaderPresenter) : Screen
}
