/*
 * Copyright 2024 Sasikanth Miriyampalli
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

@LyricistStrings(languageTag = Locales.DE, default = false)
val DeTwineStrings =
  TwineStrings(
    appName = "Twine",
    postSourceUnknown = "Unbekannt",
    buttonAll = "Alle",
    buttonAddFeed = "Feed hinzufügen",
    buttonGoBack = "Zurückgehen",
    buttonCancel = "Abbrechen",
    buttonAdd = "Hinzufügen",
    buttonChange = "Erledigt",
    feedEntryHint = "Feed-Link eingeben",
    share = "Aktie",
    scrollToTop = "Nach oben scrollen",
    noFeeds = "Keine Feeds vorhanden!",
    swipeUpGetStarted = "Wischen Sie nach oben, um zu beginnen",
    feedNameHint = "Feedname",
    editFeedName = "Bearbeiten",
    errorUnsupportedFeed = "Der Link enthält keinen RSS/Atom-Feed.",
    errorMalformedXml = "Der bereitgestellte Link enthält keinen gültigen RSS/Atom-Feed",
    errorRequestTimeout =
      "Zeitüberschreitung, überprüfen Sie Ihre Netzwerkverbindung und versuchen Sie es später erneut",
    errorFeedNotFound = { "($it): Unter dem angegebenen Link wurde kein Inhalt gefunden." },
    errorServer = {
      "($it): Serverfehler. Bitte versuchen Sie es später noch einmal oder wenden Sie sich an den Website-Administrator."
    },
    errorTooManyRedirects =
      "Die angegebene URL enthält zu viele Weiterleitungen. Bitte verwenden Sie eine andere URL.",
    errorUnAuthorized = {
      "($it): Sie sind nicht berechtigt, auf Inhalte unter diesem Link zuzugreifen."
    },
    errorUnknownHttpStatus = { "Inhalt konnte nicht mit HTTP-Code geladen werden: ($it)" },
    postsSearchHint = "Beiträge durchsuchen",
    searchSortNewest = "Neueste",
    searchSortNewestFirst = "Das neuste zuerst",
    searchSortOldest = "Älteste",
    searchSortOldestFirst = "Die ältesten zu erst",
    searchResultsCount = { count ->
      when (count) {
        1 -> "$count Ergebnis"
        else -> "$count Ergebnisse"
      }
    },
    bookmark = "Lesezeichen",
    bookmarks = "Lesezeichen",
    bookmarksPlaceholder = "Mit Lesezeichen versehene Beiträge werden hier angezeigt 🔖",
    settings = "Einstellungen",
    moreMenuOptions = "Weitere Menüoptionen",
    settingsHeaderBehaviour = "Verhalten",
    settingsHeaderFeedback = "Feedback und Fehlerberichte",
    settingsHeaderOpml = "OPML",
    settingsBrowserTypeTitle = "Verwenden Sie den In-App-Browser",
    settingsBrowserTypeSubtitle =
      "Wenn diese Option deaktiviert ist, werden Links in Ihrem Standardbrowser geöffnet.",
    settingsEnableBlurTitle = "Aktivieren Sie die Unschärfe auf der Startseite",
    settingsEnableBlurSubtitle =
      "Wenn deaktiviert, werden Farbverläufe statt unscharfer Bilder angezeigt. Kann die Leistung verbessern.",
    settingsShowUnreadCountTitle = "Anzahl der ungelesenen Beiträge anzeigen",
    settingsShowUnreadCountSubtitle =
      "Wenn diese Option deaktiviert ist, wird die Anzahl der ungelesenen Beiträge nicht im Feed-Blatt angezeigt",
    settingsReportIssue = "Ein Problem melden",
    settingsVersion = { versionName, versionCode -> "$versionName ($versionCode)" },
    settingsAboutTitle = "Um Twine",
    settingsAboutSubtitle = "Lernen Sie die Autoren kennen",
    settingsOpmlImport = "Importieren",
    settingsOpmlExport = "Export",
    settingsOpmlImporting = { progress -> "Importieren.. $progress%" },
    settingsOpmlExporting = { progress -> "Exportieren.. $progress%" },
    settingsOpmlCancel = "Stornieren",
    settingsPostsDeletionPeriodTitle = "Gelesene Beiträge löschen",
    settingsPostsDeletionPeriodSubtitle =
      "Die App löscht automatisch gelesene Beiträge, die älter als der ausgewählte Zeitraum sind",
    settingsPostsDeletionPeriodOneWeek = "1 Woche",
    settingsPostsDeletionPeriodOneMonth = "1 Monat",
    settingsPostsDeletionPeriodThreeMonths = "3 Monate",
    settingsPostsDeletionPeriodSixMonths = "6 Monate",
    settingsPostsDeletionPeriodOneYear = "1 Jahr",
    feeds = "Einspeisungen",
    editFeeds = "Feeds bearbeiten",
    comments = "Kommentare",
    about = "Um",
    aboutRoleDeveloper = "Entwickler",
    aboutRoleDesigner = "Designerr",
    aboutSocialTwitter = "Twitter",
    aboutSocialThreads = "Threads",
    aboutSocialGitHub = "GitHub",
    aboutSocialWebsite = "Webseite",
    feedsSearchHint = "Such-Feeds",
    allFeeds = "Alle Feeds",
    pinnedFeeds = "Angepinnt",
    openWebsite = "Website öffnen",
    markAllAsRead = "Alles als gelesen markieren",
    noNewPosts = "Keine neuen Inhalte",
    noNewPostsSubtitle =
      "Schauen Sie später noch einmal vorbei oder klicken Sie nach unten, um jetzt nach neuen Inhalten zu suchen",
    postsAll = "Alle",
    postsUnread = "Ungelesen",
    postsToday = "Heute",
    openSource = "Open Source",
    openSourceDesc =
      "Twine basiert auf Open-Source-Technologien und ist völlig kostenlos. Den Quellcode von Twine und einigen meiner anderen beliebten Projekte finden Sie auf GitHub. Klicken Sie hier, um dorthin zu gelangen.",
    markAsRead = "Als gelesen markieren",
    markAsUnRead = "Als ungelesen markieren",
    removeFeed = "Futter entfernen",
    delete = "Löschen",
    removeFeedDesc = { "Möchten Sie entfernen? \"${it}\"?" }
  )
