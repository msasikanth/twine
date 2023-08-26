package dev.sasikanth.rss.reader.app

import dev.sasikanth.rss.reader.home.HomePresenterFactory

internal sealed interface Screen {
  class Home(val presenterFactory: HomePresenterFactory) : Screen
}
