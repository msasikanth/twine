package dev.sasikanth.rss.reader.resources.strings

import cafe.adriel.lyricist.LyricistStrings

@LyricistStrings(languageTag = Locales.TR, default = false)
val TrTwineStrings =
  TwineStrings(
    appName = "Twine",
    postSourceUnknown = "Bilinmeyen",
    buttonAll = "TÜMÜ",
    buttonAddFeed = "Besleme ekle",
    buttonGoBack = "Geri dön",
    buttonCancel = "İptal",
    buttonAdd = "Ekle",
    buttonChange = "Tamam",
    buttonConfirm = "Onayla",
    feedEntryLinkHint = "Besleme bağlantısı",
    feedEntryTitleHint = "Başlık (İsteğe bağlı)",
    share = "Paylaş",
    scrollToTop = "Yukarı kaydır",
    noFeeds = "Hiç besleme yok!",
    swipeUpGetStarted = "Başlamak için yukarı kaydır",
    feedNameHint = "Besleme adı",
    editFeedName = "Düzenle",
    errorUnsupportedFeed = "Bağlantı herhangi bir RSS/Atom beslemesi içermiyor.",
    errorMalformedXml = "Sağlanan bağlantı geçerli bir RSS/Atom beslemesi içermiyor.",
    errorRequestTimeout = "Zaman aşımı, ağ bağlantınızı kontrol edin ve daha sonra tekrar deneyin.",
    errorFeedNotFound = { "($it): Verilen bağlantıda içerik bulunamadı." },
    errorServer = {
      "($it): Sunucu hatası. Lütfen daha sonra tekrar deneyin veya web sitesi yöneticisiyle iletişime geçin."
    },
    errorTooManyRedirects =
      "Verilen URL'de çok fazla yönlendirme var. Lütfen farklı bir URL kullanın.",
    errorUnAuthorized = { "($it): Bu bağlantıdaki içeriğe erişim yetkiniz yok." },
    errorUnknownHttpStatus = { "HTTP koduyla içerik yüklenemedi: ($it)" },
    postsSearchHint = "Gönderileri ara",
    searchSortNewest = "En yeni",
    searchSortNewestFirst = "Önce en yeni",
    searchSortOldest = "En eski",
    searchSortOldestFirst = "Önce en eski",
    searchResultsCount = { count ->
      when (count) {
        1 -> "$count sonuç"
        else -> "$count sonuç"
      }
    },
    bookmark = "Yer işareti ekle",
    unBookmark = "Yer işaretini kaldır",
    bookmarks = "Yer işaretleri",
    bookmarksPlaceholder = "Yer işaretli gönderiler burada görünecek 🔖",
    settings = "Ayarlar",
    moreMenuOptions = "Daha fazla menü seçeneği",
    settingsHeaderBehaviour = "Davranış",
    settingsHeaderOpml = "OPML",
    settingsHeaderFeedback = "Geri bildirim ve hata raporları",
    settingsHeaderTheme = "Tema",
    settingsBrowserTypeTitle = "Uygulama içi tarayıcıyı kullan",
    settingsBrowserTypeSubtitle = "Kapalı olduğunda, bağlantılar varsayılan tarayıcınızda açılır.",
    settingsEnableBlurTitle = "Ana sayfada bulanıklığı etkinleştir",
    settingsEnableBlurSubtitle =
      "Devre dışı bırakıldığında, bulanık görüntüler yerine gradyanlar gösterilir. Performansı artırabilir.",
    settingsShowUnreadCountTitle = "Okunmamış gönderi sayısını göster",
    settingsShowUnreadCountSubtitle = "Besleme seçimlerinin üzerinde bir baloncuk gösterir",
    settingsReportIssue = "Sorun bildir",
    settingsVersion = { versionName, versionCode -> "$versionName ($versionCode)" },
    settingsAboutTitle = "Twine Hakkında",
    settingsAboutSubtitle = "Yazarları tanıyın",
    settingsOpmlImport = "İçe aktar",
    settingsOpmlExport = "Dışa aktar",
    settingsOpmlImporting = { progress -> "İçe aktarılıyor.. $progress%" },
    settingsOpmlExporting = { progress -> "Dışa aktarılıyor.. $progress%" },
    settingsOpmlCancel = "İptal",
    settingsPostsDeletionPeriodTitle = "Okunan gönderileri şu süreden sonra sil",
    settingsPostsDeletionPeriodOneWeek = "1 hafta",
    settingsPostsDeletionPeriodOneMonth = "1 ay",
    settingsPostsDeletionPeriodThreeMonths = "3 ay",
    settingsPostsDeletionPeriodSixMonths = "6 ay",
    settingsPostsDeletionPeriodOneYear = "1 yıl",
    settingsShowReaderViewTitle = "Okuma görünümünü etkinleştir",
    settingsShowReaderViewSubtitle =
      "Makaleler basitleştirilmiş, okunması kolay bir görünümde gösterilecek",
    settingsThemeAuto = "Otomatik",
    settingsThemeLight = "Açık",
    settingsThemeDark = "Koyu",
    feeds = "Beslemeler",
    editFeeds = "Beslemeleri düzenle",
    comments = "Yorumlar",
    about = "Hakkında",
    aboutRoleDeveloper = "Geliştirici",
    aboutRoleDesigner = "Tasarımcı",
    aboutSocialTwitter = "Twitter",
    aboutSocialThreads = "Threads",
    aboutSocialGitHub = "GitHub",
    aboutSocialWebsite = "Web sitesi",
    feedsSearchHint = "Filtrele",
    allFeeds = "Beslemeler",
    pinnedFeeds = "Sabitlenmiş",
    markAllAsRead = "Tümünü okundu olarak işaretle",
    openWebsite = "Web sitesini aç",
    noNewPosts = "Yeni içerik yok",
    noNewPostsSubtitle = "Daha sonra kontrol edin veya şimdi yeni içerik için aşağı çekin",
    postsAll = "Tüm makaleler",
    postsUnread = "Okunmamış",
    postsToday = "Bugün",
    postsLast24Hours = "Son 24 saat",
    openSource = "Açık Kaynağı Destekle",
    openSourceDesc =
      "Twine bir açık kaynak projesidir ve ücretsiz olarak kullanılabilir. Bu projeyi nasıl destekleyeceğinizi öğrenmek için buraya tıklayın veya Twine'ın kaynak kodunu ya da diğer popüler projelerimi görüntüleyin.",
    markAsRead = "Okundu olarak işaretle",
    markAsUnRead = "Okunmadı olarak işaretle",
    removeFeed = "Beslemeyi kaldır",
    delete = "Sil",
    removeFeedDesc = { "\"${it}\" öğesini kaldırmak istiyor musunuz?" },
    alwaysFetchSourceArticle = "Okuma görünümünde her zaman tam makaleleri al",
    getFeedInfo = "Bilgi al",
    newTag = "Yeni etiket",
    tags = "Etiketler",
    addTagTitle = "Etiket ekle",
    tagNameHint = "Ad",
    tagSaveButton = "Kaydet",
    deleteTagTitle = "Etiketi sil?",
    deleteTagDesc =
      "Etiket silinecek ve tüm atanan beslemelerden kaldırılacak. Beslemeleriniz silinmeyecek.",
    feedOptionShare = "Paylaş",
    feedOptionWebsite = "Web sitesi",
    feedOptionRemove = "Kaldır",
    feedTitleHint = "Başlık",
    noUnreadPostsInFeed = "Okunmamış makale yok",
    numberOfUnreadPostsInFeed = { numberOfUnreadPosts ->
      when (numberOfUnreadPosts) {
        1L -> "$numberOfUnreadPosts okunmamış makale"
        else -> "$numberOfUnreadPosts okunmamış makale"
      }
    },
    feedsSortLatest = "Son eklenen",
    feedsSortOldest = "İlk eklenen",
    feedsSortAlphabetical = "A-Z",
    feedsBottomBarNewGroup = "Yeni grup",
    feedsBottomBarNewFeed = "Yeni besleme",
    actionPin = "Sabitle",
    actionUnpin = "Sabitlemeyi kaldır",
    actionDelete = "Sil",
    actionAddTo = "Ekle",
    actionMoveTo = "Taşı",
    actionUngroup = "Grubu çöz",
    createGroup = "Grup oluştur",
    createFeed = "Besleme ekle",
    groupNameHint = "Ad",
    feedGroupNoFeeds = "Besleme yok",
    feedGroupFeeds = { numberOfFeeds ->
      when (numberOfFeeds) {
        1 -> "$numberOfFeeds besleme"
        else -> "$numberOfFeeds besleme"
      }
    },
    actionGroupsTooltip = "Gruplar başka grupların içinde olamaz.",
    groupAddNew = "Yeni ekle",
    appBarAllFeeds = "Tüm beslemeler",
    edit = "Düzenle",
    buttonAddToGroup = "Gruba ekle...",
    removeSources = "Kaynakları sil",
    removeSourcesDesc = "Seçilen kaynakları silmek istiyor musunuz?",
    noPinnedSources = "Sabitlenmiş besleme/grup yok",
    databaseMaintainenceTitle = "Lütfen bekleyin...",
    databaseMaintainenceSubtitle = "Veritabanı bakımı yapılıyor, uygulamayı kapatmayın",
    cdLoadFullArticle = "Tam makaleyi yükle",
    enableAutoSyncTitle = "Otomatik senkronizasyonu etkinleştir",
    enableAutoSyncDesc = "Açıldığında, beslemeler arka planda güncellenecek",
    showFeedFavIconTitle = "Besleme favori simgesini göster",
    showFeedFavIconDesc =
      "Kapalı olduğunda, web sitesinin favori simgesi yerine besleme simgesi gösterilir",
    blockedWords = "Engellenen kelimeler",
    blockedWordsHint = "Bir kelime girin",
    blockedWordsDesc =
      "Gönderiler, metinlerine göre ana ekrandan gizlenebilir. Birçok gönderide görünen yaygın kelimelerden kaçınmanızı öneririz, çünkü bu, hiç gönderi gösterilmemesine veya uygulamanın performansını olumsuz etkilemesine neden olabilir. \n\nGizli gönderiler arama ve yer işaretlerinde hala görünecektir.",
    blockedWordsEmpty = "Henüz hiçbir kelimeyi engellemediniz",
    markArticleAsRead = "Makaleyi okundu olarak işaretle",
    markArticleAsReadOnOpen = "Açıldığında",
    markArticleAsReadOnScroll = "Kaydırıldığında"
  )
