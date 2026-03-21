//
//  TwineUnreadSmallWidget.swift
//  iosApp
//
//  Created by Sasikanth Miriyampalli on 19/03/26.
//  Copyright © 2026 orgName. All rights reserved.
//

import AppIntents
import SwiftUI
import WidgetKit
import shared

let numberOfUnreadPostsInWidget = 5

struct TwineUnreadSmallWidgetEntryView: View {
    var entry: UnreadSmallPostsEntry

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
        ZStack(alignment: .top) {
            VStack(alignment: .leading, spacing: 0) {
                // No image content
                Spacer()

                VStack(alignment: .leading, spacing: 8) {
                    Text(
                        post.title
                            ?? String(localized: "unread_widget_no_title")
                    )
                    .font(.system(size: 16, weight: .medium))
                    .lineLimit(2...3)

                    footer(post: post, index: index)
                }
                .padding(.horizontal, 8)
                .padding(.top, 8)
                .padding(.bottom, 12)
            }
            .frame(maxHeight: .infinity)
            .widgetURL(createDeepLink(postIndex: index, postId: post.id))

            // Navigation Buttons
            HStack {
                if index > 0 {
                    Button(intent: PreviousPostIntent()) {
                        navigationIcon(name: "chevron.left")
                    }
                    .buttonStyle(.plain)
                } else {
                    Spacer()
                        .frame(width: 48, height: 48)
                }

                Spacer()

                if index < entry.posts.count - 1 {
                    Button(intent: NextPostIntent()) {
                        navigationIcon(name: "chevron.right")
                    }
                    .buttonStyle(.plain)
                } else {
                    Spacer()
                        .frame(width: 48, height: 48)
                }
            }
        }
    }

    func footer(post: UIWidgetPost, index: Int) -> some View {
        HStack(alignment: .center) {
            // Publisher Info
            HStack(spacing: 4) {
                ZStack(alignment: .bottomTrailing) {
                    if let uiIcon = post.feedIcon {
                        Image(uiImage: uiIcon)
                            .resizable()
                            .frame(width: 12, height: 12)
                            .cornerRadius(2)
                    } else {
                        Image(systemName: "rss")
                            .resizable()
                            .frame(width: 12, height: 12)
                    }
                }

                Text(post.feedName ?? "")
                    .font(.system(size: 9, weight: .medium))
                    .lineLimit(1)
            }

            Spacer()

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
                        .frame(width: 5, height: 5)
                }
            }
        }.padding(.horizontal, 4)
    }

    func navigationIcon(name: String) -> some View {
        ZStack {
            Circle()
                .fill(Color(red: 240 / 255, green: 240 / 255, blue: 240 / 255))
                .frame(width: 24, height: 24)

            Image(systemName: name)
                .font(.system(size: 12, weight: .bold))
                .foregroundColor(.black)
        }
        .frame(width: 48, height: 48)
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

struct TwineUnreadSmallWidget: Widget {
    let kind: String = "TwineUnreadSmallWidget"

    var body: some WidgetConfiguration {
        StaticConfiguration(kind: kind, provider: UnreadSmallProvider()) {
            entry in
            TwineUnreadSmallWidgetEntryView(entry: entry)
                .containerBackground(for: .widget) {
                    Color("WidgetBackground")
                }
                .padding(4)
        }
        .contentMarginsDisabled()
        .configurationDisplayName(
            String(localized: "widget_unread_recent_name")
        )
        .supportedFamilies([.systemSmall])
    }
}

@available(iOS 17.0, *)
struct NextPostIntent: AppIntent {
    static var title: LocalizedStringResource = "Next Post"

    func perform() async throws -> some IntentResult {
        let defaults = UserDefaults(suiteName: "group.dev.sasikanth.rss.reader")
        let index = defaults?.integer(forKey: "unread_widget_index") ?? 0
        defaults?.set(
            (index + 1) % numberOfUnreadPostsInWidget,
            forKey: "unread_widget_index"
        )
        WidgetCenter.shared.reloadTimelines(ofKind: "TwineUnreadSmallWidget")
        return .result()
    }
}

@available(iOS 17.0, *)
struct PreviousPostIntent: AppIntent {
    static var title: LocalizedStringResource = "Previous Post"

    func perform() async throws -> some IntentResult {
        let defaults = UserDefaults(suiteName: "group.dev.sasikanth.rss.reader")
        let index = defaults?.integer(forKey: "unread_widget_index") ?? 0
        if index > 0 {
            defaults?.set(index - 1, forKey: "unread_widget_index")
        }
        WidgetCenter.shared.reloadTimelines(ofKind: "TwineUnreadSmallWidget")
        return .result()
    }
}

struct UIWidgetPost {
    let id: String
    let title: String?
    let postImage: UIImage?
    let feedName: String?
    let feedIcon: UIImage?
}

struct UnreadSmallPostsEntry: TimelineEntry {
    let date: Date
    let count: Int
    let posts: [UIWidgetPost]
    let currentIndex: Int
    let isSubscribed: Bool
}

struct UnreadSmallProvider: TimelineProvider {

    private let rotationInterval: TimeInterval = 5

    let component = InjectApplicationComponent(uiViewControllerProvider: {
        UIViewController()
    })

    init() {
        component.initializers
            .compactMap { ($0 as! any Initializer) }
            .forEach { initializer in
                initializer.initialize()
            }
    }

    private func fetchImage(from urlString: String?) async -> UIImage? {
        guard let urlString = urlString, let url = URL(string: urlString) else {
            return nil
        }
        do {
            let (data, _) = try await URLSession.shared.data(from: url)
            return UIImage(data: data)
        } catch {
            return nil
        }
    }

    private func fetchUIWidgetPosts(from posts: [ModelWidgetPost]) async
        -> [UIWidgetPost]
    {
        await withTaskGroup(of: UIWidgetPost.self) { group in
            for post in posts {
                group.addTask {
                    let feedIcon = await self.fetchImage(from: post.feedIcon)
                    let postImage = await self.fetchImage(from: post.image)

                    return UIWidgetPost(
                        id: post.id,
                        title: post.title,
                        postImage: postImage,
                        feedName: post.feedName,
                        feedIcon: feedIcon
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

    func placeholder(in context: Context) -> UnreadSmallPostsEntry {
        UnreadSmallPostsEntry(
            date: Date(),
            count: 0,
            posts: [],
            currentIndex: 0,
            isSubscribed: true
        )
    }

    func getSnapshot(
        in context: Context,
        completion: @escaping (UnreadSmallPostsEntry) -> Void
    ) {
        Task {
            let currentDate = Date()
            let entry = await makeEntry(date: currentDate)
            completion(entry ?? placeholder(in: context))
        }
    }

    func getTimeline(
        in context: Context,
        completion: @escaping (Timeline<UnreadSmallPostsEntry>) -> Void
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
                    defaults?.integer(forKey: "unread_widget_index") ?? 0

                if unreadPosts.isEmpty {
                    let entry = UnreadSmallPostsEntry(
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

                var entries: [UnreadSmallPostsEntry] = []
                let postCount = uiPosts.count

                // Generate 50 entries (250 seconds of rotation) to cycle through posts
                // Modulo by postCount ensures it circles back to the first post
                for i in 0..<50 {
                    let entryDate = currentDate.addingTimeInterval(
                        Double(i) * rotationInterval
                    )
                    let currentIndex = (startIndex + i) % postCount

                    let entry = UnreadSmallPostsEntry(
                        date: entryDate,
                        count: Int(truncating: unreadPostsCount),
                        posts: uiPosts,
                        currentIndex: currentIndex,
                        isSubscribed: isSubscribed
                    )
                    entries.append(entry)
                }

                // Persist the next starting index for the next timeline refresh
                let nextStartIndex = (startIndex + 50) % postCount
                defaults?.set(nextStartIndex, forKey: "unread_widget_index")

                let timeline = Timeline(entries: entries, policy: .atEnd)
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

    private func makeEntry(date: Date) async -> UnreadSmallPostsEntry? {
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
            let index = defaults?.integer(forKey: "unread_widget_index") ?? 0

            let uiPosts = await fetchUIWidgetPosts(from: unreadPosts)

            return UnreadSmallPostsEntry(
                date: date,
                count: Int(truncating: unreadPostsCount),
                posts: uiPosts,
                currentIndex: index,
                isSubscribed: isSubscribed
            )
        } catch {
            return nil
        }
    }
}
