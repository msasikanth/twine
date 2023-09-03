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
package dev.sasikanth.rss.reader.resources.strings

import cafe.adriel.lyricist.LyricistStrings

@LyricistStrings(languageTag = Locales.EN, default = true)
val EnTwineStrings =
  TwineStrings(
    appName = "Twine",
    postSourceUnknown = "Unknown",
    buttonAll = "ALL",
    buttonAddFeed = "Add feed",
    buttonGoBack = "Go back",
    buttonCancel = "Cancel",
    buttonAdd = "Add",
    buttonChange = "Done",
    feedEntryHint = "Enter feed link",
    share = "Share",
    scrollToTop = "Scroll to top",
    noFeeds = "No feeds present!",
    swipeUpGetStarted = "Swipe up to get started",
    feedNameHint = "Feed name",
    editFeedName = "Edit",
    errorUnsupportedFeed = "App currently doesn\'t support this link",
    errorMalformedXml = "Provided link doesn\'t contain valid RSS/Atom feed",
    errorRequestTimeout = "Timeout, check your network connection and try again later",
    searchHint = "Search posts",
    searchSortNewest = "Newest",
    searchSortNewestFirst = "Newest first",
    searchSortOldest = "Oldest",
    searchSortOldestFirst = "Oldest first",
    bookmark = "Bookmark",
    bookmarks = "Bookmarks",
    settings = "Settings",
  )
