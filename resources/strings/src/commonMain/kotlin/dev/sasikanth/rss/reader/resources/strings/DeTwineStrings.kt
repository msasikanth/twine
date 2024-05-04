package dev.sasikanth.rss.reader.resources.strings

import cafe.adriel.lyricist.LyricistStrings

@LyricistStrings(languageTag = Locales.DE, default = false)
val DeTwineStrings =
  TwineStrings(
    appName = "Twine",
    postSourceUnknown = "Unbekannt",
    buttonAll = "Alle",
    buttonAddFeed = "Feed hinzufügen",
    buttonGoBack = "Zurück",
    buttonCancel = "Abbrechen",
    buttonAdd = "Hinzufügen",
    buttonChange = "Fertig",
    buttonConfirm = "Confirm",
    feedEntryLinkHint = "Feed-Link",
    feedEntryTitleHint = "Title (Optional)",
    share = "Teilen",
    scrollToTop = "Hochscrollen",
    noFeeds = "Keine Feeds vorhanden!",
    swipeUpGetStarted = "Nach oben swipen, um zu beginnen",
    feedNameHint = "Feed Name",
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
    searchSortNewest = "Neuesten",
    searchSortNewestFirst = "Die Neuesten zuerst",
    searchSortOldest = "Ältesten",
    searchSortOldestFirst = "Die Ältesten zuerst",
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
    settingsBrowserTypeTitle = "Verwende den In-App-Browser",
    settingsBrowserTypeSubtitle =
      "Wenn diese Option deaktiviert ist, werden Links in Ihrem Standardbrowser geöffnet.",
    settingsEnableBlurTitle = "Aktiviere Unschärfe auf der Startseite",
    settingsEnableBlurSubtitle =
      "Wenn deaktiviert, werden Farbverläufe statt unscharfer Bilder angezeigt. Kann die Leistung verbessern.",
    settingsShowUnreadCountTitle = "Anzahl der ungelesenen Beiträge anzeigen",
    settingsShowUnreadCountSubtitle =
      "Wenn diese Option deaktiviert ist, wird die Anzahl der ungelesenen Beiträge nicht im Feed-Blatt angezeigt",
    settingsReportIssue = "Ein Problem melden",
    settingsVersion = { versionName, versionCode -> "$versionName ($versionCode)" },
    settingsAboutTitle = "Über Twine",
    settingsAboutSubtitle = "Lernen Sie die Autoren kennen",
    settingsOpmlImport = "Importieren",
    settingsOpmlExport = "Exportieren",
    settingsOpmlImporting = { progress -> "Importieren.. $progress%" },
    settingsOpmlExporting = { progress -> "Exportieren.. $progress%" },
    settingsOpmlCancel = "Abbrechen",
    settingsPostsDeletionPeriodTitle = "Gelesene Beiträge löschen",
    settingsPostsDeletionPeriodOneWeek = "1 Woche",
    settingsPostsDeletionPeriodOneMonth = "1 Monat",
    settingsPostsDeletionPeriodThreeMonths = "3 Monate",
    settingsPostsDeletionPeriodSixMonths = "6 Monate",
    settingsPostsDeletionPeriodOneYear = "1 Jahr",
    settingsShowReaderViewTitle = "Aktiviere Leseansicht",
    settingsShowReaderViewSubtitle =
      "Artikel werden in einer vereinfachten, leicht lesbaren Ansicht angezeigt.",
    feeds = "Feeds",
    editFeeds = "Feeds bearbeiten",
    comments = "Kommentare",
    about = "Über",
    aboutRoleDeveloper = "Entwickler",
    aboutRoleDesigner = "Designer",
    aboutSocialTwitter = "Twitter",
    aboutSocialThreads = "Threads",
    aboutSocialGitHub = "GitHub",
    aboutSocialWebsite = "Webseite",
    feedsSearchHint = "Filter",
    allFeeds = "Feeds",
    pinnedFeeds = "Angepinnt",
    openWebsite = "Website öffnen",
    markAllAsRead = "Alle als gelesen markieren",
    noNewPosts = "Keine neuen Beiträge",
    noNewPostsSubtitle =
      "Schauen Sie später noch einmal vorbei oder ziehen Sie nach unten, um nach neuen Inhalten zu suchen",
    postsAll = "Alle artikel",
    postsUnread = "Ungelesen",
    postsToday = "Heute",
    postsLast24Hours = "Letzte 24 Stunden",
    openSource = "Open Source",
    openSourceDesc =
      "Twine basiert auf Open-Source-Technologien und ist völlig kostenlos. Den Quellcode von Twine und einigen meiner anderen beliebten Projekte finden Sie auf GitHub. Klicken Sie hier, um dorthin zu gelangen.",
    markAsRead = "Als gelesen markieren",
    markAsUnRead = "Als ungelesen markieren",
    removeFeed = "Futter entfernen",
    delete = "Löschen",
    removeFeedDesc = { "Möchten Sie \"${it}\" entfernen?" },
    alwaysFetchSourceArticle = "Vollständige Artikel immer in der Leseransicht abrufen",
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
    feedOptionWebsite = "Webseite",
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
    actionMoveTo = "Ziehen nach",
    actionUngroup = "Ungroup",
    createGroup = "Gruppe erstellen",
    createFeed = "Feed hinzufügen",
    groupNameHint = "Name",
    feedGroupNoFeeds = "Keine Feeds",
    feedGroupFeeds = { numberOfFeeds ->
      when (numberOfFeeds) {
        1 -> "$numberOfFeeds feed"
        else -> "$numberOfFeeds feeds"
      }
    },
    actionGroupsTooltip = "Gruppen können nicht innerhalb anderer Gruppen sein.",
    groupAddNew = "Add new",
    appBarAllFeeds = "All feeds",
    edit = "Edit",
    buttonAddToGroup = "Add to group...",
    removeSources = "Delete sources",
    removeSourcesDesc = "Do you want to delete selected sources?",
  )
