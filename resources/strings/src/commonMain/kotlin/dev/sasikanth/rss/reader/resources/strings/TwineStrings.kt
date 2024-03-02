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

data class TwineStrings(
  val appName: String,
  val postSourceUnknown: String,
  val buttonAll: String,
  val buttonAddFeed: String,
  val buttonGoBack: String,
  val buttonCancel: String,
  val buttonAdd: String,
  val buttonChange: String,
  val feedEntryHint: String,
  val share: String,
  val scrollToTop: String,
  val noFeeds: String,
  val swipeUpGetStarted: String,
  val feedNameHint: String,
  val editFeedName: String,
  val errorUnsupportedFeed: String,
  val errorMalformedXml: String,
  val errorRequestTimeout: String,
  val errorFeedNotFound: (Int) -> String,
  val errorServer: (Int) -> String,
  val errorTooManyRedirects: String,
  val errorUnAuthorized: (Int) -> String,
  val errorUnknownHttpStatus: (Int) -> String,
  val postsSearchHint: String,
  val searchSortNewest: String,
  val searchSortNewestFirst: String,
  val searchSortOldest: String,
  val searchSortOldestFirst: String,
  val searchResultsCount: (Int) -> String,
  val bookmark: String,
  val bookmarks: String,
  val bookmarksPlaceholder: String,
  val settings: String,
  val moreMenuOptions: String,
  val settingsHeaderBehaviour: String,
  val settingsHeaderOpml: String,
  val settingsHeaderFeedback: String,
  val settingsBrowserTypeTitle: String,
  val settingsBrowserTypeSubtitle: String,
  val settingsEnableBlurTitle: String,
  val settingsEnableBlurSubtitle: String,
  val settingsShowUnreadCountTitle: String,
  val settingsShowUnreadCountSubtitle: String,
  val settingsReportIssue: String,
  val settingsVersion: (String, Int) -> String,
  val settingsAboutTitle: String,
  val settingsAboutSubtitle: String,
  val settingsOpmlImport: String,
  val settingsOpmlExport: String,
  val settingsOpmlImporting: (Int) -> String,
  val settingsOpmlExporting: (Int) -> String,
  val settingsOpmlCancel: String,
  val settingsPostsDeletionPeriodTitle: String,
  val settingsPostsDeletionPeriodSubtitle: String,
  val settingsPostsDeletionPeriodOneWeek: String,
  val settingsPostsDeletionPeriodOneMonth: String,
  val settingsPostsDeletionPeriodThreeMonths: String,
  val settingsPostsDeletionPeriodSixMonths: String,
  val settingsPostsDeletionPeriodOneYear: String,
  val settingsShowReaderViewTitle: String,
  val settingsShowReaderViewSubtitle: String,
  val feeds: String,
  val editFeeds: String,
  val comments: String,
  val about: String,
  val aboutRoleDeveloper: String,
  val aboutRoleDesigner: String,
  // don't translate
  val aboutSocialTwitter: String,
  // don't translate
  val aboutSocialThreads: String,
  // don't translate
  val aboutSocialGitHub: String,
  // don't translate
  val aboutSocialWebsite: String,
  val feedsSearchHint: String,
  val allFeeds: String,
  val pinnedFeeds: String,
  val markAllAsRead: String,
  val openWebsite: String,
  val noNewPosts: String,
  val noNewPostsSubtitle: String,
  val postsAll: String,
  val postsUnread: String,
  val postsToday: String,
  val openSource: String,
  val openSourceDesc: String,
  val markAsRead: String,
  val markAsUnRead: String,
  val removeFeed: String,
  val delete: String,
  val removeFeedDesc: (String) -> String,
  val alwaysFetchSourceArticle: String,
  val getFeedInfo: String,
  val newTag: String,
  val tags: String,
  val addTagTitle: String,
  val tagNameHint: String,
  val tagSaveButton: String,
  val deleteTagTitle: String,
  val deleteTagDesc: String,
)

object Locales {
  const val EN = "en"
}

expect fun String.fmt(vararg args: Any?): String
