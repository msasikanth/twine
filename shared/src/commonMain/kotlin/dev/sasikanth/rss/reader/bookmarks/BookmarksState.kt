package dev.sasikanth.rss.reader.bookmarks

import dev.sasikanth.rss.reader.database.PostWithMetadata
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

data class BookmarksState(val bookmarks: ImmutableList<PostWithMetadata>) {

  companion object {

    val DEFAULT = BookmarksState(bookmarks = persistentListOf())
  }
}
