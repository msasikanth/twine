package dev.sasikanth.rss.reader.home

import dev.sasikanth.rss.reader.database.Post

sealed interface HomeEffect {

  object NavigateToAddScreen : HomeEffect

  data class OpenPost(val post: Post) : HomeEffect
}
