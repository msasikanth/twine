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
    buttonConfirm = "Confirm",
    feedEntryLinkHint = "Feed link",
    feedEntryTitleHint = "Title (Optional)",
    share = "Share",
    scrollToTop = "Scroll to top",
    noFeeds = "No feeds present!",
    swipeUpGetStarted = "Swipe up to get started",
    feedNameHint = "Feed name",
    editFeedName = "Edit",
    errorUnsupportedFeed = "Link doesn't contain any RSS/Atom feed.",
    errorMalformedXml = "Provided link doesn\'t contain valid RSS/Atom feed",
    errorRequestTimeout = "Timeout, check your network connection and try again later",
    errorFeedNotFound = { "($it): No content found at the given link." },
    errorServer = {
      "($it): Server error. Please try again later or contact the website administrator."
    },
    errorTooManyRedirects = "The given URL has too many redirects. Please use a different URL.",
    errorUnAuthorized = { "($it): You are not authorized to access content at this link." },
    errorUnknownHttpStatus = { "Failed to load content with HTTP code: ($it)" },
    postsSearchHint = "Search posts",
    searchSortNewest = "Newest",
    searchSortNewestFirst = "Newest first",
    searchSortOldest = "Oldest",
    searchSortOldestFirst = "Oldest first",
    searchResultsCount = { count ->
      when (count) {
        1 -> "$count result"
        else -> "$count results"
      }
    },
    bookmark = "Bookmark",
    unBookmark = "Un-bookmark",
    bookmarks = "Bookmarks",
    bookmarksPlaceholder = "Bookmarked posts will appear here 🔖",
    settings = "Settings",
    moreMenuOptions = "More menu options",
    settingsHeaderBehaviour = "Behavior",
    settingsHeaderOpml = "OPML",
    settingsHeaderFeedback = "Feedback & bug reports",
    settingsHeaderTheme = "Theme",
    settingsBrowserTypeTitle = "Use in-app browser",
    settingsBrowserTypeSubtitle = "When turned off, links will open in your default browser.",
    settingsEnableBlurTitle = "Enable blur in homepage",
    settingsEnableBlurSubtitle =
      "When disabled, displays gradients instead of blurred images. May improve performance.",
    settingsShowUnreadCountTitle = "Show unread posts count",
    settingsShowUnreadCountSubtitle = "Displays a bubble above feed selections",
    settingsReportIssue = "Report an issue",
    settingsVersion = { versionName, versionCode -> "$versionName ($versionCode)" },
    settingsAboutTitle = "About Twine",
    settingsAboutSubtitle = "Get to know the authors",
    settingsOpmlImport = "Import",
    settingsOpmlExport = "Export",
    settingsOpmlImporting = { progress -> "Importing.. $progress%" },
    settingsOpmlExporting = { progress -> "Exporting.. $progress%" },
    settingsOpmlCancel = "Cancel",
    settingsPostsDeletionPeriodTitle = "Delete read posts older than",
    settingsPostsDeletionPeriodOneWeek = "1 week",
    settingsPostsDeletionPeriodOneMonth = "1 month",
    settingsPostsDeletionPeriodThreeMonths = "3 months",
    settingsPostsDeletionPeriodSixMonths = "6 months",
    settingsPostsDeletionPeriodOneYear = "1 year",
    settingsShowReaderViewTitle = "Enable reader view",
    settingsShowReaderViewSubtitle = "Articles will be shown in a simplified, easy-to-read view",
    settingsThemeAuto = "Auto",
    settingsThemeLight = "Light",
    settingsThemeDark = "Dark",
    feeds = "Feeds",
    editFeeds = "Edit feeds",
    comments = "Comments",
    about = "About",
    aboutRoleDeveloper = "Developer",
    aboutRoleDesigner = "Designer",
    aboutSocialTwitter = "Twitter",
    aboutSocialThreads = "Threads",
    aboutSocialGitHub = "GitHub",
    aboutSocialWebsite = "Website",
    feedsSearchHint = "Filter",
    allFeeds = "Feeds",
    pinnedFeeds = "Pinned",
    markAllAsRead = "Mark All as Read",
    openWebsite = "Open Website",
    noNewPosts = "No new content",
    noNewPostsSubtitle = "Check back later or pull down to check for new content now",
    postsAll = "All articles",
    postsUnread = "Unread",
    postsToday = "Today",
    postsLast24Hours = "Last 24 hours",
    openSource = "Support Open Source",
    openSourceDesc =
      "Twine is an open source project and is available for free to use. Click here to know more on how to support this project or, view source code of Twine or some of my other popular projects.",
    markAsRead = "Mark as read",
    markAsUnRead = "Mark as unread",
    removeFeed = "Remove feed",
    delete = "Delete",
    removeFeedDesc = { "Do you want to remove \"${it}\"?" },
    alwaysFetchSourceArticle = "Always fetch full articles in reader view",
    getFeedInfo = "Get Info",
    newTag = "New tag",
    tags = "Tags",
    addTagTitle = "Add tag",
    tagNameHint = "Name",
    tagSaveButton = "Save",
    deleteTagTitle = "Delete tag?",
    deleteTagDesc =
      "Tag will be deleted and removed from all the assigned feeds. Your feeds won't be deleted",
    feedOptionShare = "Share",
    feedOptionWebsite = "Website",
    feedOptionRemove = "Remove",
    feedTitleHint = "Title",
    noUnreadPostsInFeed = "No unread articles",
    numberOfUnreadPostsInFeed = { numberOfUnreadPosts ->
      when (numberOfUnreadPosts) {
        1L -> "$numberOfUnreadPosts unread article"
        else -> "$numberOfUnreadPosts unread articles"
      }
    },
    feedsSortLatest = "Last added",
    feedsSortOldest = "First added",
    feedsSortAlphabetical = "A-Z",
    feedsBottomBarNewGroup = "New group",
    feedsBottomBarNewFeed = "New feed",
    actionPin = "Pin",
    actionUnpin = "Unpin",
    actionDelete = "Delete",
    actionAddTo = "Add to",
    actionMoveTo = "Move to",
    actionUngroup = "Ungroup",
    createGroup = "Create group",
    createFeed = "Add feed",
    groupNameHint = "Name",
    feedGroupNoFeeds = "No feeds",
    feedGroupFeeds = { numberOfFeeds ->
      when (numberOfFeeds) {
        1 -> "$numberOfFeeds feed"
        else -> "$numberOfFeeds feeds"
      }
    },
    actionGroupsTooltip = "Groups cannot be inside other groups.",
    groupAddNew = "Add new",
    appBarAllFeeds = "All feeds",
    edit = "Edit",
    buttonAddToGroup = "Add to group...",
    removeSources = "Delete sources",
    removeSourcesDesc = "Do you want to delete selected sources?",
    noPinnedSources = "No pinned feeds/groups",
    databaseMaintainenceTitle = "Please wait...",
    databaseMaintainenceSubtitle = "Performing database maintainence, don't close the app",
    cdLoadFullArticle = "Load full article",
    enableAutoSyncTitle = "Enable auto sync",
    enableAutoSyncDesc = "When turned-on, feeds will be updated in the background",
    showFeedFavIconTitle = "Show feed fav icon",
    showFeedFavIconDesc =
      "When turned-off, the feed icon will be displayed instead of the website's favicon",
    blockedWords = "Blocked words",
    blockedWordsHint = "Enter a word",
    blockedWordsDesc =
      "Post can be hidden from the home screen based on their text. We recommend avoiding common words that appear in many posts, since it can result in no posts being shown or negatively impacting app performance. \n\nHidden posts will still be displayed in search & bookmarks.",
    blockedWordsEmpty = "You haven't blocked any words yet",
    markArticleAsRead = "Mark article as read",
    markArticleAsReadOnOpen = "On Open",
    markArticleAsReadOnScroll = "On Scroll",
    noReaderContent =
      "No content to display in the reader, please try fetching article or visiting the website.",
  )
