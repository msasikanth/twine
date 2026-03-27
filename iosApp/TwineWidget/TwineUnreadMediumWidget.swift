//
//  TwineUnreadMediumWidget.swift
//  iosApp
//
//  Created by Sasikanth Miriyampalli on 26/03/26.
//  Copyright © 2026 orgName. All rights reserved.
//

import AppIntents
import ImageIO
import SwiftUI
import WidgetKit
import shared

struct TwineUnreadMediumWidgetEntryView: View {
    var entry: UnreadMediumPostsEntry

    var body: some View {
        ZStack {
            if entry.isSubscribed {
                if entry.posts.isEmpty {
                    noPosts
                } else {
                    let safeIndex =
                        entry.currentIndex < entry.posts.count
                        ? entry.currentIndex : 0
                    let post = entry.posts[safeIndex]
                    unreadPostView(post: post, index: safeIndex)
                }
            } else {
                twinePremium
            }
        }
        .frame(maxWidth: .infinity, maxHeight: .infinity)
    }

    func unreadPostView(post: UIWidgetPost, index: Int) -> some View {
        VStack(alignment: .leading, spacing: 0) {
            // Header
            HStack(alignment: .center) {
                Text("widget_latest")
                    .font(.system(size: 16, weight: .medium))
                
                Spacer()
                    .frame(width: 16)
                
                // Pagination Dots
                HStack(spacing: 4) {
                    ForEach(
                        0..<min(entry.posts.count, numberOfUnreadPostsInWidget),
                        id: \.self
                    ) { i in
                        Circle()
                            .fill(
                                i == (index % numberOfUnreadPostsInWidget)
                                    ? Color.primary : Color.primary.opacity(0.4)
                            )
                            .frame(width: 6, height: 6)
                    }
                }
                
                Spacer()
                
                // Navigation Buttons
                HStack(spacing: 8) {
                    if index > 0 {
                        Button(intent: MediumPreviousPostIntent()) {
                            navigationIcon(name: "chevron.left")
                        }
                        .buttonStyle(.plain)
                    } else {
                        Spacer()
                            .frame(width: 32, height: 32)
                    }

                    if index < entry.posts.count - 1 {
                        Button(intent: MediumNextPostIntent()) {
                            navigationIcon(name: "chevron.right")
                        }
                        .buttonStyle(.plain)
                    } else {
                        Spacer()
                            .frame(width: 32, height: 32)
                    }
                }
            }
            .padding(.horizontal, 16)
            .padding(.top, 8)

            Spacer()

            // Card Content
            HStack(alignment: .center, spacing: 16) {
                VStack(alignment: .leading, spacing: 8) {
                    Text(
                        post.title
                            ?? String(localized: "unread_widget_no_title")
                    )
                    .font(.system(size: 16, weight: .medium))
                    .lineLimit(3)
                    .frame(height: 64, alignment: .topLeading)
                    .frame(maxWidth: .infinity, alignment: .leading)

                    footer(post: post)
                }
                .frame(maxWidth: .infinity)

                Box(cornerRadius: 24) {
                    if let postImage = post.postImage {
                        Image(uiImage: postImage)
                            .resizable()
                            .scaledToFill()
                            .frame(width: 110, height: 110)
                    } else {
                        Color.gray.opacity(0.1)
                            .frame(width: 110, height: 110)
                    }
                }
            }
            .padding(.horizontal, 16)
            .padding(.bottom, 8)
            .widgetURL(createDeepLink(postIndex: index, postId: post.id))
            
            Spacer()
        }
    }

    func footer(post: UIWidgetPost) -> some View {
        HStack(spacing: 4) {
            if let uiIcon = post.feedIcon {
                Image(uiImage: uiIcon)
                    .resizable()
                    .frame(width: 16, height: 16)
                    .cornerRadius(2)
            } else {
                Image(systemName: "newspaper")
                    .resizable()
                    .frame(width: 16, height: 16)
            }

            Text(post.feedName ?? "")
                .font(.system(size: 12, weight: .medium))
                .lineLimit(1)
            
            let relativeTime = RelativeTimeFormatter.format(post.postedOn)
            if !relativeTime.isEmpty {
                Text(" \u{2022} \(relativeTime)")
                    .font(.system(size: 12, weight: .regular))
                    .foregroundColor(.secondary)
                    .lineLimit(1)
            }
            
            if post.readingTimeEstimate > 0 {
                Text(" \u{2022} \(post.readingTimeEstimate)m read")
                    .font(.system(size: 12, weight: .regular))
                    .foregroundColor(.secondary)
                    .lineLimit(1)
            }
        }
    }

    func navigationIcon(name: String) -> some View {
        ZStack {
            Circle()
                .fill(Color(red: 240 / 255, green: 240 / 255, blue: 240 / 255))
                .frame(width: 32, height: 32)

            Image(systemName: name)
                .font(.system(size: 12, weight: .bold))
                .foregroundColor(.black)
        }
        .frame(width: 32, height: 32)
    }

    var twinePremium: some View {
        VStack {
            Text("widget_premium")
                .font(.body)
                .multilineTextAlignment(.center)
        }.frame(maxHeight: .infinity, alignment: .center)
    }

    var noPosts: some View {
        VStack(spacing: 8) {
            Image(systemName: "newspaper")
                .font(.system(size: 32))
            Text("unread_no_posts")
                .font(.system(size: 14, weight: .medium))
                .multilineTextAlignment(.center)
        }.frame(maxHeight: .infinity, alignment: .center)
    }

    private func createDeepLink(postIndex: Int, postId: String) -> URL {
        let fromScreenType =
            "dev.sasikanth.rss.reader.reader.ReaderScreenArgs.FromScreen.UnreadWidget"
        let json =
            "{\"postIndex\":\(postIndex),\"postId\":\"\(postId)\",\"fromScreen\":{\"type\":\"\(fromScreenType)\"}}"
        let encodedJson =
            json.addingPercentEncoding(withAllowedCharacters: .urlPathAllowed)
            ?? ""
        let urlString = "twine://reader/\(encodedJson)"
        return URL(string: urlString) ?? URL(string: "twine://")!
    }
}

struct RelativeTimeFormatter {
    static func format(_ instant: Kotlinx_datetimeInstant) -> String {
        let now = Date()
        let date = Date(timeIntervalSince1970: Double(instant.epochSeconds))
        let diff = now.timeIntervalSince(date)
        
        let minutes = Int(diff / 60)
        let hours = Int(diff / 3600)
        let days = Int(diff / 86400)
        
        if minutes < 1 {
            return "<1m"
        } else if minutes < 60 {
            return "\(minutes)m"
        } else if hours < 24 {
            return "\(hours)h"
        } else if days < 7 {
            return "\(days)d"
        } else {
            return ""
        }
    }
}

struct TwineUnreadMediumWidget: Widget {
    let kind: String = "TwineUnreadMediumWidget"

    var body: some WidgetConfiguration {
        StaticConfiguration(kind: kind, provider: UnreadMediumProvider()) {
            entry in
            TwineUnreadMediumWidgetEntryView(entry: entry)
                .containerBackground(.background, for: .widget)
        }
        .contentMarginsDisabled()
        .configurationDisplayName(
            String(localized: "widget_unread_recent_medium_name")
        )
        .description(String(localized: "widget_unread_recent_medium_desc"))
        .supportedFamilies([.systemMedium])
    }
}

@available(iOS 17.0, *)
struct MediumNextPostIntent: AppIntent {
    static var title: LocalizedStringResource = "Next Post"

    func perform() async throws -> some IntentResult {
        let defaults = UserDefaults(suiteName: "group.dev.sasikanth.rss.reader")
        let index = defaults?.integer(forKey: "unread_medium_widget_index") ?? 0
        defaults?.set(
            (index + 1) % numberOfUnreadPostsInWidget,
            forKey: "unread_medium_widget_index"
        )
        WidgetCenter.shared.reloadTimelines(ofKind: "TwineUnreadMediumWidget")
        return .result()
    }
}

@available(iOS 17.0, *)
struct MediumPreviousPostIntent: AppIntent {
    static var title: LocalizedStringResource = "Previous Post"

    func perform() async throws -> some IntentResult {
        let defaults = UserDefaults(suiteName: "group.dev.sasikanth.rss.reader")
        let index = defaults?.integer(forKey: "unread_medium_widget_index") ?? 0
        if index > 0 {
            defaults?.set(index - 1, forKey: "unread_medium_widget_index")
        }
        WidgetCenter.shared.reloadTimelines(ofKind: "TwineUnreadMediumWidget")
        return .result()
    }
}

struct UnreadMediumPostsEntry: TimelineEntry {
    let date: Date
    let count: Int
    let posts: [UIWidgetPost]
    let currentIndex: Int
    let isSubscribed: Bool
}

struct UnreadMediumProvider: TimelineProvider {
    private let postImageMaxPixelSize = 300
    private let feedIconMaxPixelSize = 24

    let component = InjectApplicationComponent(uiViewControllerProvider: {
        UIViewController()
    })

    init() {
        component.initializers
            .compactMap { ($0 as? any Initializer) }
            .forEach { initializer in
                initializer.initialize()
            }
    }

    private func fetchImage(from urlString: String?, maxPixelSize: Int) async
        -> UIImage?
    {
        guard let urlString = urlString, let url = URL(string: urlString) else {
            return nil
        }
        do {
            let (data, _) = try await URLSession.shared.data(from: url)
            return downsampleImage(data: data, maxPixelSize: maxPixelSize)
        } catch {
            return nil
        }
    }

    private func downsampleImage(data: Data, maxPixelSize: Int) -> UIImage? {
        let options = [kCGImageSourceShouldCache: false] as CFDictionary
        guard let imageSource = CGImageSourceCreateWithData(
            data as CFData,
            options
        ) else {
            return nil
        }

        let downsampleOptions =
            [
                kCGImageSourceCreateThumbnailFromImageAlways: true,
                kCGImageSourceShouldCacheImmediately: true,
                kCGImageSourceCreateThumbnailWithTransform: true,
                kCGImageSourceThumbnailMaxPixelSize: maxPixelSize,
            ] as CFDictionary

        guard let cgImage = CGImageSourceCreateThumbnailAtIndex(
            imageSource,
            0,
            downsampleOptions
        ) else {
            return nil
        }

        return UIImage(cgImage: cgImage)
    }

    private func fetchUIWidgetPosts(from posts: [ModelWidgetPost]) async
        -> [UIWidgetPost]
    {
        await withTaskGroup(of: UIWidgetPost.self) { group in
            for post in posts {
                group.addTask {
                    let feedIcon = await self.fetchImage(
                        from: post.feedIcon,
                        maxPixelSize: self.feedIconMaxPixelSize
                    )
                    let postImage = await self.fetchImage(
                        from: post.image,
                        maxPixelSize: self.postImageMaxPixelSize
                    )

                    return UIWidgetPost(
                        id: post.id,
                        title: post.title,
                        postImage: postImage,
                        feedName: post.feedName,
                        feedIcon: feedIcon,
                        postedOn: post.postedOn,
                        readingTimeEstimate: post.readingTimeEstimate
                    )
                }
            }

            var results: [UIWidgetPost] = []
            for await result in group {
                results.append(result)
            }

            // Re-sort to maintain original order
            return posts.compactMap { post in
                results.first(where: { $0.id == post.id })
            }
        }
    }

    func placeholder(in context: Context) -> UnreadMediumPostsEntry {
        UnreadMediumPostsEntry(
            date: Date(),
            count: 0,
            posts: [],
            currentIndex: 0,
            isSubscribed: true
        )
    }

    func getSnapshot(
        in context: Context,
        completion: @escaping (UnreadMediumPostsEntry) -> Void
    ) {
        Task {
            let currentDate = Date()
            let entry = await makeEntry(date: currentDate)
            completion(entry ?? placeholder(in: context))
        }
    }

    func getTimeline(
        in context: Context,
        completion: @escaping (Timeline<UnreadMediumPostsEntry>) -> Void
    ) {
        Task {
            let currentDate = Date()

            do {
                let repository = component.widgetDataRepository
                let unreadPostsCount =
                    try await repository.unreadPostsCountBlocking()
                let unreadPosts = try await repository.unreadPostsBlocking(
                    numberOfPosts: Int32(numberOfUnreadPostsInWidget)
                )
                let isSubscribed =
                    try await component.billingHandler.customerResult()
                    is SubscriptionResultSubscribed

                let defaults = UserDefaults(
                    suiteName: "group.dev.sasikanth.rss.reader"
                )
                let startIndex =
                    defaults?.integer(forKey: "unread_medium_widget_index") ?? 0

                if unreadPosts.isEmpty {
                    let entry = UnreadMediumPostsEntry(
                        date: currentDate,
                        count: 0,
                        posts: [],
                        currentIndex: 0,
                        isSubscribed: isSubscribed
                    )
                    completion(Timeline(entries: [entry], policy: .atEnd))
                    return
                }

                let uiPosts = await fetchUIWidgetPosts(from: unreadPosts)
                let safeIndex = min(startIndex, max(uiPosts.count - 1, 0))
                let entry = UnreadMediumPostsEntry(
                    date: currentDate,
                    count: Int(truncating: unreadPostsCount),
                    posts: uiPosts,
                    currentIndex: safeIndex,
                    isSubscribed: isSubscribed
                )
                let timeline = Timeline(entries: [entry], policy: .atEnd)
                completion(timeline)
            } catch {
                let entry = await makeEntry(date: currentDate)
                let timeline = Timeline(
                    entries: [entry ?? placeholder(in: context)],
                    policy: .atEnd
                )
                completion(timeline)
            }
        }
    }

    private func makeEntry(date: Date) async -> UnreadMediumPostsEntry? {
        do {
            let repository = component.widgetDataRepository
            let unreadPostsCount =
                try await repository.unreadPostsCountBlocking()
            let unreadPosts = try await repository.unreadPostsBlocking(
                numberOfPosts: Int32(numberOfUnreadPostsInWidget)
            )
            let isSubscribed =
                try await component.billingHandler.customerResult()
                is SubscriptionResultSubscribed
            let defaults = UserDefaults(
                suiteName: "group.dev.sasikanth.rss.reader"
            )
            let index = defaults?.integer(forKey: "unread_medium_widget_index") ?? 0

            let uiPosts = await fetchUIWidgetPosts(from: unreadPosts)

            return UnreadMediumPostsEntry(
                date: date,
                count: Int(truncating: unreadPostsCount),
                posts: uiPosts,
                currentIndex: uiPosts.isEmpty ? 0 : min(index, uiPosts.count - 1),
                isSubscribed: isSubscribed
            )
        } catch {
            return nil
        }
    }
}
