package dev.sasikanth.rss.reader.home

import dev.sasikanth.rss.reader.database.PostWithMetadata

sealed interface HomeEffect {

  object NavigateToAddScreen : HomeEffect

  data class OpenPost(val post: PostWithMetadata) : HomeEffect
}
