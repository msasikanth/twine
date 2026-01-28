/*
 * Copyright 2024 Sasikanth Miriyampalli
 *
 * Licensed under the GPL, Version 3.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.gnu.org/licenses/gpl-3.0.en.html
 *
 */
package dev.sasikanth.rss.reader.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import dev.sasikanth.rss.reader.app.AppIcon
import dev.sasikanth.rss.reader.core.model.local.PostsSortOrder
import dev.sasikanth.rss.reader.core.model.local.PostsType
import dev.sasikanth.rss.reader.di.scopes.AppScope
import kotlin.time.Instant
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import me.tatarka.inject.annotations.Inject

@Inject
@AppScope
class SettingsRepository(
  private val dataStore: DataStore<Preferences>,
) {

  private val browserTypeKey = stringPreferencesKey("pref_browser_type")
  private val showUnreadPostsCountKey = booleanPreferencesKey("show_unread_posts_count")
  private val postsDeletionPeriodKey = stringPreferencesKey("posts_cleanup_frequency")
  private val postsTypeKey = stringPreferencesKey("posts_type")
  private val postsSortOrderKey = stringPreferencesKey("posts_sort_order")
  private val showReaderViewKey = booleanPreferencesKey("pref_show_reader_view")
  private val feedsSortOrderKey = stringPreferencesKey("pref_feeds_sort_order")
  private val appThemeModeKey = stringPreferencesKey("pref_app_theme_mode_v2")
  private val useAmoledKey = booleanPreferencesKey("use_amoled")
  private val enableAutoSyncKey = booleanPreferencesKey("enable_auto_sync")
  private val showFeedFavIconKey = booleanPreferencesKey("show_feed_fav_icon")
  private val markPostsAsReadOnKey = stringPreferencesKey("mark_posts_as_read_on")
  private val homeViewModeKey = stringPreferencesKey("home_view_mode")
  private val readerFontScaleFactorKey = floatPreferencesKey("reader_font_scale")
  private val readerLineHeightScaleFactorKey = floatPreferencesKey("reader_line_height_scale")
  private val readerFontStyleKey = stringPreferencesKey("reader_font_style")
  private val blockImagesKey = booleanPreferencesKey("block_images")
  private val enableNotificationsKey = booleanPreferencesKey("enable_notifications")
  private val downloadFullContentKey = booleanPreferencesKey("download_full_content")
  private val lastReviewPromptDateKey = longPreferencesKey("last_review_prompt_date")
  private val installDateKey = longPreferencesKey("install_date")
  private val userSessionCountKey = intPreferencesKey("user_session_count")
  private val appIconKey = stringPreferencesKey("app_icon")
  private val isOnboardingDoneKey = booleanPreferencesKey("is_onboarding_done")
  private val dynamicColorEnabledKey = booleanPreferencesKey("dynamic_color_enabled")

  val browserType: Flow<BrowserType> =
    dataStore.data.map { preferences ->
      mapToBrowserType(preferences[browserTypeKey]) ?: BrowserType.Default
    }

  val showUnreadPostsCount: Flow<Boolean> =
    dataStore.data.map { preferences -> preferences[showUnreadPostsCountKey] ?: true }

  val postsDeletionPeriod: Flow<Period> =
    dataStore.data.map { preferences ->
      mapToPostsDeletionPeriod(preferences[postsDeletionPeriodKey]) ?: Period.ONE_MONTH
    }

  val showReaderView: Flow<Boolean> =
    dataStore.data.map { preferences -> preferences[showReaderViewKey] ?: true }

  val postsType: Flow<PostsType> =
    dataStore.data.map { preferences -> mapToPostsType(preferences[postsTypeKey]) ?: PostsType.ALL }

  val postsSortOrder: Flow<PostsSortOrder> =
    dataStore.data.map { preferences ->
      mapToPostsSortOrder(preferences[postsSortOrderKey]) ?: PostsSortOrder.Latest
    }

  val feedsSortOrder: Flow<FeedsOrderBy> =
    dataStore.data.map { preferences ->
      mapToFeedsOrderBy(preferences[feedsSortOrderKey]) ?: FeedsOrderBy.Latest
    }

  val appThemeMode: Flow<AppThemeMode> =
    dataStore.data.map { preferences ->
      mapToAppThemeMode(preferences[appThemeModeKey]) ?: AppThemeMode.Auto
    }

  val useAmoled: Flow<Boolean> =
    dataStore.data.map { preferences -> preferences[useAmoledKey] ?: false }

  val isOnboardingDone: Flow<Boolean> =
    dataStore.data.map { preferences -> preferences[isOnboardingDoneKey] ?: false }

  val dynamicColorEnabled: Flow<Boolean> =
    dataStore.data.map { preferences -> preferences[dynamicColorEnabledKey] ?: true }

  val enableAutoSync: Flow<Boolean> =
    dataStore.data.map { preferences -> preferences[enableAutoSyncKey] ?: true }

  val showFeedFavIcon: Flow<Boolean> =
    dataStore.data.map { preferences -> preferences[showFeedFavIconKey] ?: true }

  val markAsReadOn: Flow<MarkAsReadOn> =
    dataStore.data.map { preferences -> mapToMarkAsReadOnType(preferences[markPostsAsReadOnKey]) }

  val homeViewMode: Flow<HomeViewMode> =
    dataStore.data.map { preferences -> mapToHomeViewMode(preferences[homeViewModeKey]) }

  val readerFontScaleFactor: Flow<Float> =
    dataStore.data.map { preferences -> preferences[readerFontScaleFactorKey] ?: 1f }

  val readerLineHeightScaleFactor: Flow<Float> =
    dataStore.data.map { preferences -> preferences[readerLineHeightScaleFactorKey] ?: 1f }

  val readerFontStyle: Flow<ReaderFont> =
    dataStore.data.map { preferences -> mapToReaderFont(preferences[readerFontStyleKey]) }

  val blockImages: Flow<Boolean> =
    dataStore.data.map { preferences -> preferences[blockImagesKey] ?: false }

  val enableNotifications: Flow<Boolean> =
    dataStore.data.map { preferences -> preferences[enableNotificationsKey] ?: false }

  val downloadFullContent: Flow<Boolean> =
    dataStore.data.map { preferences -> preferences[downloadFullContentKey] ?: false }

  val lastReviewPromptDate: Flow<Instant?> =
    dataStore.data.map { preferences ->
      preferences[lastReviewPromptDateKey]?.let(Instant::fromEpochMilliseconds)
    }

  val installDate: Flow<Instant?> =
    dataStore.data.map { preferences ->
      preferences[installDateKey]?.let(Instant::fromEpochMilliseconds)
    }

  val userSessionCount: Flow<Int> =
    dataStore.data.map { preferences -> preferences[userSessionCountKey] ?: 0 }

  val appIcon: Flow<AppIcon> =
    dataStore.data.map { preferences -> mapToAppIcon(preferences[appIconKey]) }

  suspend fun enableAutoSyncImmediate(): Boolean {
    return enableAutoSync.first()
  }

  suspend fun updateFeedsSortOrder(value: FeedsOrderBy) {
    dataStore.edit { preferences -> preferences[feedsSortOrderKey] = value.name }
  }

  suspend fun postsDeletionPeriodImmediate(): Period {
    return postsDeletionPeriod.first()
  }

  suspend fun updateBrowserType(browserType: BrowserType) {
    dataStore.edit { preferences -> preferences[browserTypeKey] = browserType.name }
  }

  suspend fun toggleShowUnreadPostsCount(value: Boolean) {
    dataStore.edit { preferences -> preferences[showUnreadPostsCountKey] = value }
  }

  suspend fun updatePostsDeletionPeriod(postsDeletionPeriod: Period) {
    dataStore.edit { preferences -> preferences[postsDeletionPeriodKey] = postsDeletionPeriod.name }
  }

  suspend fun updatePostsType(postsType: PostsType) {
    dataStore.edit { preferences -> preferences[postsTypeKey] = postsType.name }
  }

  suspend fun updatePostsSortOrder(postsSortOrder: PostsSortOrder) {
    dataStore.edit { preferences -> preferences[postsSortOrderKey] = postsSortOrder.name }
  }

  suspend fun toggleShowReaderView(value: Boolean) {
    dataStore.edit { preferences -> preferences[showReaderViewKey] = value }
  }

  suspend fun updateAppTheme(value: AppThemeMode) {
    dataStore.edit { preferences -> preferences[appThemeModeKey] = value.name }
  }

  suspend fun updateAppIcon(value: AppIcon) {
    dataStore.edit { preferences -> preferences[appIconKey] = value.name }
  }

  suspend fun toggleAmoled(value: Boolean) {
    dataStore.edit { preferences -> preferences[useAmoledKey] = value }
  }

  suspend fun toggleDynamicColor(value: Boolean) {
    dataStore.edit { preferences -> preferences[dynamicColorEnabledKey] = value }
  }

  suspend fun completeOnboarding() {
    dataStore.edit { preferences -> preferences[isOnboardingDoneKey] = true }
  }

  suspend fun toggleAutoSync(value: Boolean) {
    dataStore.edit { preferences -> preferences[enableAutoSyncKey] = value }
  }

  suspend fun toggleShowFeedFavIcon(value: Boolean) {
    dataStore.edit { preferences -> preferences[showFeedFavIconKey] = value }
  }

  suspend fun updateMarkAsReadOn(value: MarkAsReadOn) {
    dataStore.edit { preferences -> preferences[markPostsAsReadOnKey] = value.name }
  }

  suspend fun updateHomeViewMode(value: HomeViewMode) {
    dataStore.edit { preferences -> preferences[homeViewModeKey] = value.name }
  }

  suspend fun updateReaderFontScaleFactor(value: Float) {
    dataStore.edit { preferences -> preferences[readerFontScaleFactorKey] = value }
  }

  suspend fun updateReaderLineHeightScaleFactor(value: Float) {
    dataStore.edit { preferences -> preferences[readerLineHeightScaleFactorKey] = value }
  }

  suspend fun updateReaderFont(value: ReaderFont) {
    dataStore.edit { preferences -> preferences[readerFontStyleKey] = value.name }
  }

  suspend fun toggleBlockImages(value: Boolean) {
    dataStore.edit { preferences -> preferences[blockImagesKey] = value }
  }

  suspend fun toggleNotifications(value: Boolean) {
    dataStore.edit { preferences -> preferences[enableNotificationsKey] = value }
  }

  suspend fun toggleDownloadFullContent(value: Boolean) {
    dataStore.edit { preferences -> preferences[downloadFullContentKey] = value }
  }

  suspend fun updateLastReviewPromptDate(value: Instant) {
    dataStore.edit { preferences ->
      preferences[lastReviewPromptDateKey] = value.toEpochMilliseconds()
    }
  }

  suspend fun updateInstallDate(value: Instant) {
    dataStore.edit { preferences -> preferences[installDateKey] = value.toEpochMilliseconds() }
  }

  suspend fun updateUserSessionCount(value: Int) {
    dataStore.edit { preferences -> preferences[userSessionCountKey] = value }
  }

  suspend fun incrementUserSessionCount() {
    dataStore.edit { preferences ->
      val currentSessionCount = preferences[userSessionCountKey] ?: 0
      preferences[userSessionCountKey] = currentSessionCount + 1
    }
  }

  suspend fun clear() {
    dataStore.edit { it.clear() }
  }

  private fun mapToAppThemeMode(pref: String?): AppThemeMode? {
    if (pref.isNullOrBlank()) return null
    return AppThemeMode.valueOf(pref)
  }

  private fun mapToFeedsOrderBy(pref: String?): FeedsOrderBy? {
    if (pref.isNullOrBlank()) return null
    return FeedsOrderBy.valueOf(pref)
  }

  private fun mapToBrowserType(pref: String?): BrowserType? {
    if (pref.isNullOrBlank()) return null
    return BrowserType.valueOf(pref)
  }

  private fun mapToPostsDeletionPeriod(pref: String?): Period? {
    if (pref.isNullOrBlank()) return null
    return Period.valueOf(pref)
  }

  private fun mapToPostsType(pref: String?): PostsType? {
    if (pref.isNullOrBlank()) return null
    return PostsType.valueOf(pref)
  }

  private fun mapToPostsSortOrder(pref: String?): PostsSortOrder? {
    if (pref.isNullOrBlank()) return null
    return try {
      when (pref) {
        "UploadedLatest" -> PostsSortOrder.AddedLatest
        "UploadedOldest" -> PostsSortOrder.AddedOldest
        else -> PostsSortOrder.valueOf(pref)
      }
    } catch (e: Exception) {
      null
    }
  }

  private fun mapToMarkAsReadOnType(pref: String?): MarkAsReadOn {
    if (pref.isNullOrBlank()) return MarkAsReadOn.Open
    return MarkAsReadOn.valueOf(pref)
  }

  private fun mapToHomeViewMode(pref: String?): HomeViewMode {
    if (pref.isNullOrBlank()) return HomeViewMode.Default
    return HomeViewMode.valueOf(pref)
  }

  private fun mapToReaderFont(pref: String?): ReaderFont {
    if (pref.isNullOrBlank()) return ReaderFont.Golos
    return try {
      ReaderFont.valueOf(pref)
    } catch (e: Exception) {
      ReaderFont.Golos
    }
  }

  private fun mapToAppIcon(pref: String?): AppIcon {
    if (pref.isNullOrBlank()) return AppIcon.DarkJade
    return try {
      AppIcon.valueOf(pref)
    } catch (e: Exception) {
      AppIcon.DarkJade
    }
  }
}

enum class AppThemeMode {
  Light,
  Dark,
  Auto
}

enum class BrowserType {
  Default,
  InApp
}

enum class Period {
  ONE_WEEK,
  ONE_MONTH,
  THREE_MONTHS,
  SIX_MONTHS,
  ONE_YEAR,
  NEVER
}

enum class MarkAsReadOn {
  Open,
  Scroll
}

enum class HomeViewMode {
  Default,
  Simple,
  Compact
}

enum class ReaderFont(val value: String) {
  ComicNeue("Comic Neue"),
  GoogleSans("Google Sans"),
  Golos("Golos Text"),
  Lora("Lora"),
  Merriweather("Merriweather"),
  RethinkSans("Rethink Sans"),
  RobotoSerif("Roboto Serif"),
}
