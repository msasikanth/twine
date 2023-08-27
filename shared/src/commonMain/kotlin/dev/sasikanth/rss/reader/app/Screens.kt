package dev.sasikanth.rss.reader.app

import dev.sasikanth.rss.reader.home.HomePresenter

internal sealed interface Screen {
  class Home(val presenter: HomePresenter) : Screen
}
