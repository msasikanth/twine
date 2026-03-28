//
//  TwineUnreadLargeWidget.swift
//  iosApp
//
//  Created by Sasikanth Miriyampalli on 26/03/26.
//  Copyright © 2026 orgName. All rights reserved.
//

import AppIntents
import SwiftUI
import WidgetKit
import shared

struct TwineUnreadLargeWidgetEntryView: View {
    var entry: UnreadLargePostsEntry

    var body: some View {
        ZStack {
            if entry.isSubscribed {
                if entry.posts.isEmpty {
                    noPosts
                } else {
                    unreadPostsList
                }
            } else {
                twinePremium
            }
        }
        .frame(maxWidth: .infinity, maxHeight: .infinity)
    }

    var unreadPostsList: some View {
        VStack(spacing: 4) {
            // Header
            HStack(alignment: .center) {
                Text("widget_latest")
                    .font(.system(size: 16, weight: .medium))

                Spacer()

                HStack {
                    Button(intent: LargeRefreshIntent()) {
                        headerIcon(name: "arrow.triangle.2.circlepath")
                    }
                    .buttonStyle(.plain)
                    .frame(width: 32, height: 32)

                    Link(destination: URL(string: "twine://bookmarks")!) {
                        headerIcon(name: "bookmark")
                    }
                    .frame(width: 32, height: 32)
                }.padding(.vertical, 8)
            }
            .padding(.horizontal, 8)

            // Posts List
            VStack(alignment: .leading, spacing: 0) {
                ForEach(Array(entry.posts.enumerated()), id: \.offset) { index, post in
                    Link(destination: createDeepLink(postIndex: index, postId: post.id)) {
                        postItem(post: post)
                    }
                }
            }
            .padding(.bottom, 8)
        }
    }

    func postItem(post: UIWidgetPost) -> some View {
        HStack(alignment: .center, spacing: 12) {
            VStack(alignment: .leading, spacing: 8) {
                Text(post.title ?? String(localized: "unread_widget_no_title"))
                    .font(.system(size: 12, weight: .medium))
                    .lineLimit(2)
                    .frame(height: 32, alignment: .topLeading)
                    .frame(maxWidth: .infinity, alignment: .leading)

                footer(post: post)
            }
            .frame(maxWidth: .infinity)

            if let postImage = post.postImage {
                Image(uiImage: postImage)
                    .resizable()
                    .scaledToFill()
                    .frame(width: 56, height: 56)
                    .clipShape(RoundedRectangle(cornerRadius: 14))
            }
        }
        .padding(.horizontal, 12)
        .padding(.vertical, 8)
    }

    func footer(post: UIWidgetPost) -> some View {
        HStack(spacing: 4) {
            if let uiIcon = post.feedIcon {
                Image(uiImage: uiIcon)
                    .resizable()
                    .frame(width: 12, height: 12)
                    .cornerRadius(2)
            } else {
                Image(systemName: "newspaper")
                    .resizable()
                    .frame(width: 12, height: 12)
            }

            Text(post.feedName ?? "")
                .font(.system(size: 10, weight: .medium))
                .lineLimit(1)

            let relativeTime = RelativeTimeFormatter.format(post.postedOn)
            if !relativeTime.isEmpty {
                Text(" \u{2022} \(relativeTime)")
                    .font(.system(size: 10, weight: .regular))
                    .foregroundColor(.secondary)
                    .lineLimit(1)
            }

            if post.readingTimeEstimate > 0 {
                Text(" \u{2022} \(post.readingTimeEstimate)m read")
                    .font(.system(size: 10, weight: .regular))
                    .foregroundColor(.secondary)
                    .lineLimit(1)
            }
        }
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

    func headerIcon(name: String) -> some View {
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

struct TwineUnreadLargeWidget: Widget {
    let kind: String = "TwineUnreadLargeWidget"

    var body: some WidgetConfiguration {
        StaticConfiguration(kind: kind, provider: UnreadLargeProvider()) {
            entry in
            TwineUnreadLargeWidgetEntryView(entry: entry)
                .containerBackground(.background, for: .widget)
                .padding(4)
        }
        .contentMarginsDisabled()
        .configurationDisplayName(
            String(localized: "widget_unread_recent_large_name")
        )
        .description(String(localized: "widget_unread_recent_large_desc"))
        .supportedFamilies([.systemLarge])
    }
}

struct UnreadLargePostsEntry: TimelineEntry {
    let date: Date
    let posts: [UIWidgetPost]
    let isSubscribed: Bool
}

struct UnreadLargeProvider: TimelineProvider {
    private let postImageMaxPixelSize = 200
    private let feedIconMaxPixelSize = 24
    private let numberOfPosts = 4

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

    func placeholder(in context: Context) -> UnreadLargePostsEntry {
        UnreadLargePostsEntry(
            date: Date(),
            posts: [],
            isSubscribed: true
        )
    }

    func getSnapshot(
        in context: Context,
        completion: @escaping (UnreadLargePostsEntry) -> Void
    ) {
        Task {
            let currentDate = Date()
            let entry = await makeEntry(date: currentDate)
            completion(entry ?? placeholder(in: context))
        }
    }

    func getTimeline(
        in context: Context,
        completion: @escaping (Timeline<UnreadLargePostsEntry>) -> Void
    ) {
        Task {
            let currentDate = Date()

            do {
                let repository = component.widgetDataRepository
                let unreadPosts = try await repository.unreadPostsBlocking(
                    numberOfPosts: Int32(numberOfPosts)
                )
                let isSubscribed =
                    try await component.billingHandler.customerResult()
                        is SubscriptionResultSubscribed

                if unreadPosts.isEmpty {
                    let entry = UnreadLargePostsEntry(
                        date: currentDate,
                        posts: [],
                        isSubscribed: isSubscribed
                    )
                    completion(Timeline(entries: [entry], policy: .atEnd))
                    return
                }

                let uiPosts = await fetchUIWidgetPosts(from: unreadPosts)
                let entry = UnreadLargePostsEntry(
                    date: currentDate,
                    posts: uiPosts,
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

    private func makeEntry(date: Date) async -> UnreadLargePostsEntry? {
        do {
            let repository = component.widgetDataRepository
            let unreadPosts = try await repository.unreadPostsBlocking(
                numberOfPosts: Int32(numberOfPosts)
            )
            let isSubscribed =
                try await component.billingHandler.customerResult()
                    is SubscriptionResultSubscribed

            let uiPosts = await fetchUIWidgetPosts(from: unreadPosts)

            return UnreadLargePostsEntry(
                date: date,
                posts: uiPosts,
                isSubscribed: isSubscribed
            )
        } catch {
            return nil
        }
    }
}

@available(iOS 17.0, *)
struct LargeRefreshIntent: AppIntent {
    static var title: LocalizedStringResource = "Refresh"

    func perform() async throws -> some IntentResult {
        let component = InjectApplicationComponent(uiViewControllerProvider: {
            UIViewController()
        })

        component.initializers
            .compactMap { ($0 as? any Initializer) }
            .forEach { initializer in
                initializer.initialize()
            }

        try await component.syncCoordinator.pull()
        WidgetCenter.shared.reloadTimelines(ofKind: "TwineUnreadLargeWidget")
        return .result()
    }
}
