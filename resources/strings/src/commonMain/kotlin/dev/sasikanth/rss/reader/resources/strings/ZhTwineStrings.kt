package dev.sasikanth.rss.reader.resources.strings

import cafe.adriel.lyricist.LyricistStrings

@LyricistStrings(languageTag = Locales.ZH, default = false)
val ZhTwineStrings =
  TwineStrings(
    appName = "Twine",
    postSourceUnknown = "未知",
    buttonAll = "全部",
    buttonAddFeed = "添加订阅",
    buttonGoBack = "返回",
    buttonCancel = "取消",
    buttonAdd = "添加",
    buttonChange = "完成",
    buttonConfirm = "确认",
    feedEntryLinkHint = "订阅链接",
    feedEntryTitleHint = "标题（可选）",
    share = "分享",
    scrollToTop = "返回顶部",
    noFeeds = "暂无订阅！",
    swipeUpGetStarted = "向上滑动以开始使用",
    feedNameHint = "订阅名称",
    editFeedName = "编辑",
    errorUnsupportedFeed = "链接不包含任何 RSS/Atom 订阅源。",
    errorMalformedXml = "提供的链接不包含有效的 RSS/Atom 订阅源",
    errorRequestTimeout = "请求超时，请检查您的网络连接并稍后重试",
    errorFeedNotFound = { "($it):在提供的链接中未找到任何内容。" },
    errorServer = { "($it): 服务器错误。请稍后再试或联系网站管理员。" },
    errorTooManyRedirects = "提供的URL包含过多重定向。请使用其他URL。",
    errorUnAuthorized = { "($it): 您没有权限访问链接上的内容。" },
    errorUnknownHttpStatus = { "加载内容失败，HTTP 状态码： ($it)" },
    postsSearchHint = "搜索文章",
    searchSortNewest = "最新",
    searchSortNewestFirst = "最新优先",
    searchSortOldest = "最早",
    searchSortOldestFirst = "最早优先",
    searchResultsCount = { count ->
      when (count) {
        1 -> "$count 个结果"
        else -> "$count 个结果"
      }
    },
    bookmark = "收藏",
    bookmarks = "收藏夹",
    bookmarksPlaceholder = "收藏的文章将显示在这里 🔖",
    settings = "设置",
    moreMenuOptions = "更多选项",
    settingsHeaderBehaviour = "行为",
    settingsHeaderFeedback = "反馈与错误报告",
    settingsHeaderOpml = "OPML",
    settingsHeaderTheme = "Theme",
    settingsBrowserTypeTitle = "使用内置浏览器",
    settingsBrowserTypeSubtitle = "如果禁用，链接将在默认浏览器中打开。",
    settingsEnableBlurTitle = "在主页启用模糊效果",
    settingsEnableBlurSubtitle = "如果禁用，将显示渐变而非模糊的图像。可以提高性能。",
    settingsShowUnreadCountTitle = "显示未读文章的数量",
    settingsShowUnreadCountSubtitle = "在订阅选择上方显示一个气泡",
    settingsReportIssue = "报告问题",
    settingsVersion = { versionName, versionCode -> "$versionName ($versionCode)" },
    settingsAboutTitle = "关于 Twine",
    settingsAboutSubtitle = "了解开发人员",
    settingsOpmlImport = "导入",
    settingsOpmlExport = "导出",
    settingsOpmlImporting = { progress -> "正在导入... $progress%" },
    settingsOpmlExporting = { progress -> "正在导出... $progress%" },
    settingsOpmlCancel = "取消",
    settingsPostsDeletionPeriodTitle = "删除已读文章",
    settingsPostsDeletionPeriodOneWeek = "一周",
    settingsPostsDeletionPeriodOneMonth = "一个月",
    settingsPostsDeletionPeriodThreeMonths = "三个月",
    settingsPostsDeletionPeriodSixMonths = "六个月",
    settingsPostsDeletionPeriodOneYear = "一年",
    settingsShowReaderViewTitle = "启用阅读视图",
    settingsShowReaderViewSubtitle = "文章将以简化、易读的视图显示",
    settingsThemeAuto = "Auto",
    settingsThemeLight = "Light",
    settingsThemeDark = "Dark",
    feeds = "订阅",
    editFeeds = "编辑订阅",
    comments = "评论",
    about = "关于",
    aboutRoleDeveloper = "开发者",
    aboutRoleDesigner = "设计师",
    aboutSocialTwitter = "Twitter",
    aboutSocialThreads = "Threads",
    aboutSocialGitHub = "GitHub",
    aboutSocialWebsite = "网站",
    feedsSearchHint = "筛选",
    allFeeds = "所有订阅",
    pinnedFeeds = "已置顶的订阅",
    openWebsite = "打开网站",
    markAllAsRead = "全部标记为已读",
    noNewPosts = "暂无新内容",
    noNewPostsSubtitle = "请稍后检查，或下拉以检查是否有新的内容。",
    postsAll = "所有文章",
    postsUnread = "未读",
    postsToday = "今天",
    postsLast24Hours = "最近24小时",
    openSource = "支持开源",
    openSourceDesc = "Twine 是一个开源项目，可免费使用。单击此处了解更多有关如何支持此项目的信息，查看 Twine 或我的其他一些热门项目的源代码。",
    markAsRead = "标记为已读",
    markAsUnRead = "标记为未读",
    removeFeed = "移除订阅",
    delete = "删除",
    removeFeedDesc = { "您确定要移除 \"${it}\"?" },
    alwaysFetchSourceArticle = "始终在阅读视图中获取完整文章",
    getFeedInfo = "获取信息",
    newTag = "新标签",
    tags = "标签",
    addTagTitle = "添加标签",
    tagNameHint = "名称",
    tagSaveButton = "保存",
    deleteTagTitle = "删除标签？",
    deleteTagDesc = "标签将被删除并从所有已关联的订阅中移除。您的订阅不会被删除。",
    feedOptionShare = "分享",
    feedOptionWebsite = "网站",
    feedOptionRemove = "移除",
    feedTitleHint = "标题",
    noUnreadPostsInFeed = "暂无未读文章",
    numberOfUnreadPostsInFeed = { numberOfUnreadPosts ->
      when (numberOfUnreadPosts) {
        1L -> "$numberOfUnreadPosts 篇未读文章"
        else -> "$numberOfUnreadPosts 篇未读文章"
      }
    },
    feedsSortLatest = "最新添加",
    feedsSortOldest = "最早添加",
    feedsSortAlphabetical = "A-Z",
    feedsBottomBarNewGroup = "新建分组",
    feedsBottomBarNewFeed = "新建订阅",
    actionPin = "置顶",
    actionUnpin = "取消置顶",
    actionDelete = "删除",
    actionAddTo = "添加至",
    actionMoveTo = "移动至",
    actionUngroup = "取消分组",
    createGroup = "创建分组",
    createFeed = "添加订阅",
    groupNameHint = "名称",
    feedGroupNoFeeds = "没有订阅",
    feedGroupFeeds = { numberOfFeeds ->
      when (numberOfFeeds) {
        1 -> "$numberOfFeeds 个订阅"
        else -> "$numberOfFeeds 个订阅"
      }
    },
    actionGroupsTooltip = "分组不能包含在其他分组中。",
    groupAddNew = "新增分组",
    appBarAllFeeds = "所有订阅",
    edit = "编辑",
    buttonAddToGroup = "添加至分组...",
    removeSources = "删除来源",
    removeSourcesDesc = "您确定要删除所选的来源吗？",
    noPinnedSources = "没有置顶的订阅/分组",
    databaseMaintainenceTitle = "请稍候...",
    databaseMaintainenceSubtitle = "正在进行数据库维护，请勿关闭应用",
    cdLoadFullArticle = "Load full article",
    enableAutoSyncTitle = "Enable auto sync",
    enableAutoSyncDesc = "When turned-on, feeds will be updated in the background",
  )
