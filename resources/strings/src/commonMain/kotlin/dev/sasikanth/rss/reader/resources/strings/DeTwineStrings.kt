package dev.sasikanth.rss.reader.resources.strings

import cafe.adriel.lyricist.LyricistStrings

@LyricistStrings(languageTag = Locales.DE, default = false)
val DeTwineStrings =
  TwineStrings(
    appName = "Twine",
    postSourceUnknown = "Unbekannt",
    buttonAll = "ALLE",
    buttonAddFeed = "Feed hinzufügen",
    buttonGoBack = "Zurück",
    buttonCancel = "Abbrechen",
    buttonAdd = "Hinzufügen",
    buttonChange = "Fertig",
    buttonConfirm = "Bestätigen",
    feedEntryLinkHint = "Feed-Link",
    feedEntryTitleHint = "Titel (Optional)",
    share = "Teilen",
    scrollToTop = "Nach oben scrollen",
    noFeeds = "Keine Feeds vorhanden!",
    swipeUpGetStarted = "Wischen Sie nach oben, um zu beginnen",
    feedNameHint = "Feed-Name",
    editFeedName = "Bearbeiten",
    errorUnsupportedFeed = "Der Link enthält keinen RSS-/Atom-Feed.",
    errorMalformedXml = "Der bereitgestellte Link enthält keinen gültigen RSS-/Atom-Feed",
    errorRequestTimeout =
      "Zeitüberschreitung, überprüfen Sie Ihre Netzwerkverbindung und versuchen Sie es später erneut",
    errorFeedNotFound = { "($it): Unter dem angegebenen Link wurde kein Inhalt gefunden." },
    errorServer = {
      "($it): Serverfehler. Bitte versuchen Sie es später erneut oder kontaktieren Sie den Website-Administrator."
    },
    errorTooManyRedirects =
      "Die angegebene URL enthält zu viele Weiterleitungen. Bitte verwenden Sie eine andere URL.",
    errorUnAuthorized = {
      "($it): Sie sind nicht berechtigt, auf Inhalte unter diesem Link zuzugreifen."
    },
    errorUnknownHttpStatus = { "Inhalt konnte nicht mit HTTP-Code geladen werden: ($it)" },
    postsSearchHint = "Beiträge durchsuchen",
    searchSortNewest = "Neueste",
    searchSortNewestFirst = "Neueste zuerst",
    searchSortOldest = "Älteste",
    searchSortOldestFirst = "Älteste zuerst",
    searchResultsCount = { count ->
      when (count) {
        1 -> "$count Ergebnis"
        else -> "$count Ergebnisse"
      }
    },
    bookmark = "Lesezeichen",
    unBookmark = "Un-bookmark",
    bookmarks = "Lesezeichen",
    bookmarksPlaceholder = "Mit Lesezeichen versehene Beiträge werden hier angezeigt 🔖",
    settings = "Einstellungen",
    moreMenuOptions = "Weitere Menüoptionen",
    settingsHeaderBehaviour = "Verhalten",
    settingsHeaderOpml = "OPML",
    settingsHeaderFeedback = "Feedback & Fehlerberichte",
    settingsHeaderTheme = "Thema",
    settingsBrowserTypeTitle = "In-App-Browser verwenden",
    settingsBrowserTypeSubtitle =
      "Wenn ausgeschaltet, werden Links in Ihrem Standardbrowser geöffnet.",
    settingsEnableBlurTitle = "Unschärfe auf der Startseite aktivieren",
    settingsEnableBlurSubtitle =
      "Wenn deaktiviert, werden Farbverläufe statt unscharfer Bilder angezeigt. Kann die Leistung verbessern.",
    settingsShowUnreadCountTitle = "Anzahl ungelesener Beiträge anzeigen",
    settingsShowUnreadCountSubtitle = "Zeigt eine Blase über der Feed-Auswahl an",
    settingsReportIssue = "Ein Problem melden",
    settingsVersion = { versionName, versionCode -> "$versionName ($versionCode)" },
    settingsAboutTitle = "Über Twine",
    settingsAboutSubtitle = "Lernen Sie die Autoren kennen",
    settingsOpmlImport = "Importieren",
    settingsOpmlExport = "Exportieren",
    settingsOpmlImporting = { progress -> "Wird importiert.. $progress%" },
    settingsOpmlExporting = { progress -> "Wird exportiert.. $progress%" },
    settingsOpmlCancel = "Abbrechen",
    settingsPostsDeletionPeriodTitle = "Gelesene Beiträge löschen, die älter sind als",
    settingsPostsDeletionPeriodOneWeek = "1 Woche",
    settingsPostsDeletionPeriodOneMonth = "1 Monat",
    settingsPostsDeletionPeriodThreeMonths = "3 Monate",
    settingsPostsDeletionPeriodSixMonths = "6 Monate",
    settingsPostsDeletionPeriodOneYear = "1 Jahr",
    settingsPostsDeletionPeriodNever = "Never",
    settingsShowReaderViewTitle = "Leseansicht aktivieren",
    settingsShowReaderViewSubtitle =
      "Beiträge werden in einer vereinfachten, leicht lesbaren Ansicht angezeigt.",
    settingsThemeAuto = "Automatisch",
    settingsThemeLight = "Hell",
    settingsThemeDark = "Dunkel",
    feeds = "Feeds",
    editFeeds = "Feeds bearbeiten",
    comments = "Kommentare",
    about = "Über",
    aboutRoleDeveloper = "Entwickler",
    aboutRoleDesigner = "Designer",
    aboutSocialTwitter = "Twitter",
    aboutSocialThreads = "Threads",
    aboutSocialGitHub = "GitHub",
    aboutSocialWebsite = "Website",
    feedsSearchHint = "Filter",
    allFeeds = "Feeds",
    pinnedFeeds = "Angepinnt",
    markAllAsRead = "Alle als gelesen markieren",
    openWebsite = "Website öffnen",
    noNewPosts = "Keine neuen Beiträge",
    noNewPostsSubtitle =
      "Schauen Sie später wieder vorbei oder ziehen Sie nach unten, um jetzt nach neuen Inhalten zu suchen",
    postsAll = "Alle Beiträge",
    postsUnread = "Ungelesen",
    postsToday = "Heute",
    postsLast24Hours = "Letzte 24 Stunden",
    openSource = "Open-Source unterstützen",
    openSourceDesc =
      "Twine ist ein Open-Source-Projekt und kann kostenlos genutzt werden. Klicken Sie hier, um mehr darüber zu erfahren, wie Sie dieses Projekt unterstützen können, oder um den Quellcode von Twine oder einigen meiner anderen beliebten Projekte anzusehen.",
    markAsRead = "Als gelesen markieren",
    markAsUnRead = "Als ungelesen markieren",
    removeFeed = "Feed entfernen",
    delete = "Löschen",
    removeFeedDesc = { "Möchten Sie \"${it}\" entfernen?" },
    alwaysFetchSourceArticle = "Immer vollständige Beiträge in der Leseansicht abrufen",
    getFeedInfo = "Informationen bekommen",
    newTag = "Neuer Tag",
    tags = "Tags",
    addTagTitle = "Tag hinzufügen",
    tagNameHint = "Name",
    tagSaveButton = "Speichern",
    deleteTagTitle = "Tag löschen?",
    deleteTagDesc =
      "Das Tag wird gelöscht und aus allen zugewiesenen Feeds entfernt. Ihre Feeds werden nicht gelöscht",
    feedOptionShare = "Teilen",
    feedOptionWebsite = "Website",
    feedOptionRemove = "Entfernen",
    feedTitleHint = "Titel",
    noUnreadPostsInFeed = "Keine ungelesenen Beiträge",
    numberOfUnreadPostsInFeed = { numberOfUnreadPosts ->
      when (numberOfUnreadPosts) {
        1L -> "$numberOfUnreadPosts ungelesener Beitrag"
        else -> "$numberOfUnreadPosts ungelesene Beiträge"
      }
    },
    feedsSortLatest = "Zuletzt hinzugefügt",
    feedsSortOldest = "Zuerst hinzugefügt",
    feedsSortAlphabetical = "A-Z",
    feedsBottomBarNewGroup = "Neue Gruppe",
    feedsBottomBarNewFeed = "Neuer Feed",
    actionPin = "Anpinnen",
    actionUnpin = "Loslösen",
    actionDelete = "Löschen",
    actionAddTo = "Hinzufügen zu",
    actionMoveTo = "Verschieben nach",
    actionUngroup = "Entgruppieren",
    createGroup = "Gruppe erstellen",
    createFeed = "Feed hinzufügen",
    groupNameHint = "Name",
    feedGroupNoFeeds = "Keine Feeds",
    feedGroupFeeds = { numberOfFeeds ->
      when (numberOfFeeds) {
        1 -> "$numberOfFeeds Feed"
        else -> "$numberOfFeeds Feeds"
      }
    },
    actionGroupsTooltip = "Gruppen können nicht innerhalb anderer Gruppen sein.",
    groupAddNew = "Neue hinzufügen",
    appBarAllFeeds = "Alle Feeds",
    edit = "Bearbeiten",
    buttonAddToGroup = "Zu Gruppe hinzufügen...",
    removeSources = "Quellen löschen",
    removeSourcesDesc = "Möchten Sie die ausgewählten Quellen löschen?",
    noPinnedSources = "Keine angepinnten Feeds/Gruppen",
    databaseMaintainenceTitle = "Bitte warten...",
    databaseMaintainenceSubtitle =
      "Datenbankwartung wird durchgeführt, schließen Sie die App nicht",
    cdLoadFullArticle = "Vollständigen Beitrag laden",
    enableAutoSyncTitle = "Automatische Synchronisierung aktivieren",
    enableAutoSyncDesc = "Wenn eingeschaltet, werden die Feeds im Hintergrund aktualisiert",
    showFeedFavIconTitle = "Feed-Favoriten-Symbol anzeigen",
    showFeedFavIconDesc =
      "Wenn ausgeschaltet, wird das Feed-Symbol anstelle des Favoriten-Symbols der Website angezeigt",
    blockedWords = "Blockierte Wörter",
    blockedWordsHint = "Geben Sie ein Wort ein",
    blockedWordsDesc =
      "Beiträge können aufgrund ihres Textes auf dem Startbildschirm ausgeblendet werden. Wir empfehlen, häufige Wörter, die in vielen Beiträgen vorkommen, zu vermeiden, da dies dazu führen kann, dass keine Beiträge angezeigt werden oder die Leistung der App negativ beeinflusst wird. \n\nAusgeblendete Beiträge werden weiterhin in der Suche und in den Lesezeichen angezeigt.",
    blockedWordsEmpty = "Sie haben noch keine Wörter blockiert",
    markArticleAsRead = "Beitrag als gelesen markieren",
    markArticleAsReadOnOpen = "Beim Öffnen",
    markArticleAsReadOnScroll = "Beim Scrollen",
    noReaderContent =
      "No content to display in the reader, please try fetching article or visiting the website.",
    pullToClose = "Pull down to close",
    readerSettings = "Reader screen settings",
    comingSoon = "Coming soon",
    homeViewMode = "View modes",
    homeViewModeDefault = "Default",
    homeViewModeSimple = "Simple",
    homeViewModeCompact = "Compact",
  )
