package dev.sasikanth.rss.reader.bookmarks

import dev.sasikanth.rss.reader.database.PostWithMetadata

sealed interface BookmarksEvent {

  object Init : BookmarksEvent

  object BackClicked : BookmarksEvent

  data class OnPostBookmarkClick(val post: PostWithMetadata) : BookmarksEvent
}
