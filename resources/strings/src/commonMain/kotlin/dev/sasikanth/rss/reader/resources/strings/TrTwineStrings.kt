package dev.sasikanth.rss.reader.resources.strings

import cafe.adriel.lyricist.LyricistStrings

@LyricistStrings(languageTag = Locales.TR, default = false)
val TrTwineStrings =
  TwineStrings(
    appName = "Twine",
    postSourceUnknown = "Bilinmiyor",
    buttonAll = "TÜMÜ",
    buttonAddFeed = "İçerik ekle",
    buttonGoBack = "Geri dön",
    buttonCancel = "İptal",
    buttonAdd = "Ekle",
    buttonChange = "Bitti",
    buttonConfirm = "Onayla",
    feedEntryLinkHint = "Yayın bağlantısı",
    feedEntryTitleHint = "Başlık (İsteğe Bağlı)",
    share = "Paylaş",
    scrollToTop = "Yukarı kaydır",
    noFeeds = "İçerik yok!",
    swipeUpGetStarted = "Başlamak için yukarı kaydırın",
    feedNameHint = "Yayın adı",
    editFeedName = "Düzenle",
    errorUnsupportedFeed = "Bağlantı herhangi bir RSS/Atom yayını içermiyor.",
    errorMalformedXml = "Bağlantı geçerli RSS/Atom yayını içermiyor",
    errorRequestTimeout = "Zaman aşımı, ağ bağlantınızı kontrol edin ve daha sonra tekrar deneyin",
    errorFeedNotFound = { "($it): Verilen bağlantıda yayın bulunamadı." },
    errorServer = {
      "($it): Sunucu hatası. Lütfen daha sonra tekrar deneyin veya web sitesi yöneticisiyle iletişime geçin."
    },
    errorTooManyRedirects =
      "Verilen URL'de çok fazla yönlendirme var. Lütfen farklı bir URL kullanın.",
    errorUnAuthorized = { "($it): Bu bağlantıdaki içeriğe erişim yetkiniz bulunmamaktadır." },
    errorUnknownHttpStatus = { "HTTP koduna sahip yayın yüklenemedi: ($it)" },
    postsSearchHint = "Gönderileri ara",
    searchSortNewest = "En Yeni",
    searchSortNewestFirst = "Önce en yeni",
    searchSortOldest = "En eski",
    searchSortOldestFirst = "Önce en eski",
    searchResultsCount = { count ->
      when (count) {
        1 -> "$count sonuç"
        else -> "$count sonuçlar"
      }
    },
    bookmark = "Yer İşareti",
    bookmarks = "Yer İşaretleri",
    bookmarksPlaceholder = "Yer imlerine eklenen gönderiler burada görünecektir 🔖",
    settings = "Ayarlar",
    moreMenuOptions = "Daha fazla menü seçeneği",
    settingsHeaderBehaviour = "Davranış",
    settingsHeaderFeedback = "Geri bildirim & Hata raporları",
    settingsHeaderOpml = "OPML",
    settingsBrowserTypeTitle = "Uygulama içi tarayıcıyı kullan",
    settingsBrowserTypeSubtitle =
      "Kapatıldığında, bağlantılar varsayılan tarayıcınızda açılacaktır.",
    settingsEnableBlurTitle = "Ana sayfada bulanıklaştırmayı etkinleştir",
    settingsEnableBlurSubtitle =
      "Devre dışı bırakıldığında, bulanık görüntüler yerine gradyanları görüntüler. Performansı artırabilir.",
    settingsShowUnreadCountTitle = "Okunmamış gönderi sayısını göster",
    settingsShowUnreadCountSubtitle = "İçerik seçimlerinin üzerinde bir baloncuk görüntüler.",
    settingsReportIssue = "Sorun bildir",
    settingsVersion = { versionName, versionCode -> "$versionName ($versionCode)" },
    settingsAboutTitle = "Twine hakkında",
    settingsAboutSubtitle = "Yazarları tanıyın",
    settingsOpmlImport = "İçe aktar",
    settingsOpmlExport = "Dışa aktar",
    settingsOpmlImporting = { progress -> "İçe aktarılıyor.. $progress%" },
    settingsOpmlExporting = { progress -> "Dışa aktarılıyor.. $progress%" },
    settingsOpmlCancel = "İptal",
    settingsPostsDeletionPeriodTitle = "Okunmuş gönderileri sil",
    settingsPostsDeletionPeriodOneWeek = "1 hafta",
    settingsPostsDeletionPeriodOneMonth = "1 ay",
    settingsPostsDeletionPeriodThreeMonths = "3 ay",
    settingsPostsDeletionPeriodSixMonths = "6 ay",
    settingsPostsDeletionPeriodOneYear = "1 yıl",
    settingsShowReaderViewTitle = "Okuyucu görünümünü etkinleştir",
    settingsShowReaderViewSubtitle =
      "Gönderiler basitleştirilmiş, okunması kolay bir görünümde gösterilecek.",
    feeds = "İçerikler",
    editFeeds = "İçerikleri düzenle",
    comments = "Yorumlar",
    about = "Hakkında",
    aboutRoleDeveloper = "Geliştirici",
    aboutRoleDesigner = "Tasarımcı",
    aboutSocialTwitter = "Twitter",
    aboutSocialThreads = "Threads",
    aboutSocialGitHub = "GitHub",
    aboutSocialWebsite = "Web sitesi",
    feedsSearchHint = "Filtre",
    allFeeds = "Yayınlar",
    pinnedFeeds = "Sabitlenmiş",
    openWebsite = "Web sitesini aç",
    markAllAsRead = "Tümünü okundu olarak işaretle",
    noNewPosts = "Yeni gönderi yok",
    noNewPostsSubtitle =
      "Daha sonra tekrar kontrol edin veya yeni içeriği şimdi kontrol etmek için aşağı çekin",
    postsAll = "Tüm gönderiler",
    postsUnread = "Okunmamış",
    postsToday = "Bugün",
    postsLast24Hours = "Son 24 saat",
    openSource = "Açık kaynağı destekle",
    openSourceDesc =
      "Twine açık kaynaklı bir projedir ve ücretsiz olarak kullanılabilir. Bu projeyi nasıl destekleyeceğiniz hakkında daha fazla bilgi edinmek için buraya tıklayın veya Twine'ın veya diğer popüler projelerimden bazılarının kaynak kodunu görüntüleyin.",
    markAsRead = "Okundu olarak işaretle",
    markAsUnRead = "Okunmamış olarak işaretle",
    removeFeed = "İçeriği kaldır",
    delete = "Sil",
    removeFeedDesc = { "\"${it}\"'i kaldırmak istiyor musunuz?" },
    alwaysFetchSourceArticle = "Okuyucu görünümünde her zaman tam gönderileri getir",
    getFeedInfo = "Bilgi al",
    newTag = "Yeni etiket",
    tags = "Etiketler",
    addTagTitle = "Etiket ekle",
    tagNameHint = "Ad",
    tagSaveButton = "Kaydet",
    deleteTagTitle = "Etiket silinsin mi?",
    deleteTagDesc =
      "Etiket silinecek ve atanan tüm yayınlarınızdan kaldırılacaktır. İçerikleriniz silinmeyecek",
    feedOptionShare = "Paylaş",
    feedOptionWebsite = "Web sitesi",
    feedOptionRemove = "Kaldır",
    feedTitleHint = "Başlık",
    noUnreadPostsInFeed = "Okunmamış gönderi yok",
    numberOfUnreadPostsInFeed = { numberOfUnreadPosts ->
      when (numberOfUnreadPosts) {
        1L -> "$numberOfUnreadPosts okunmamış gönderi"
        else -> "$numberOfUnreadPosts okunmamış gönderiler"
      }
    },
    feedsSortLatest = "En son eklenen",
    feedsSortOldest = "İlk eklenen",
    feedsSortAlphabetical = "A-Z",
    feedsBottomBarNewGroup = "Yeni Grup",
    feedsBottomBarNewFeed = "Yeni Yayın",
    actionPin = "Sabitle",
    actionUnpin = "Sabitlemeyi Kaldır",
    actionAddTo = "Şuraya ekle",
    actionMoveTo = "Şuraya taşı",
    actionDelete = "Sil",
    actionUngroup = "Gruplandırılmamış",
    createGroup = "Grup oluştur",
    createFeed = "İçerik ekle",
    groupNameHint = "İsim",
    feedGroupNoFeeds = "Yayın yok",
    feedGroupFeeds = { numberOfFeeds ->
      when (numberOfFeeds) {
        1 -> "$numberOfFeeds yayın"
        else -> "$numberOfFeeds yayınlar"
      }
    },
    actionGroupsTooltip = "Gruplar başka grupların içinde olamaz.",
    groupAddNew = "Yeni ekle",
    appBarAllFeeds = "Tüm yayınlar",
    edit = "Düzenle",
    buttonAddToGroup = "Gruba ekle...",
    removeSources = "Kaynakları sil",
    removeSourcesDesc = "Seçili kaynakları silmek istiyor musunuz?",
    noPinnedSources = "Sabitlenmiş yayın/grup yok",
    databaseMaintainenceTitle = "Lütfen bekleyin...",
    databaseMaintainenceSubtitle = "Veritabanı bakımı gerçekleştiriliyor, uygulamayı kapatmayın",
  )
