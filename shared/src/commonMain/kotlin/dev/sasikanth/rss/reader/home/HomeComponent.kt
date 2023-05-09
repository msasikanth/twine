package dev.sasikanth.rss.reader.home

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.essenty.instancekeeper.getOrCreate
import dev.sasikanth.rss.reader.repository.RssRepository
import dev.sasikanth.rss.reader.utils.DispatchersProvider

class HomeComponent(
  componentContext: ComponentContext,
  rssRepository: RssRepository,
  dispatchersProvider: DispatchersProvider
) : ComponentContext by componentContext {

  internal val viewModel = instanceKeeper.getOrCreate {
    HomeViewModel(
      lifecycle = lifecycle,
      dispatchersProvider = dispatchersProvider,
      rssRepository = rssRepository,
    )
  }
}
