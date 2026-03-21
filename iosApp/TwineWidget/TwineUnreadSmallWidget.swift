//
//  TwineUnreadSmallWidget.swift
//  iosApp
//
//  Created by Sasikanth Miriyampalli on 19/03/26.
//  Copyright © 2026 orgName. All rights reserved.
//

import SwiftUI
import WidgetKit
import shared

struct TwineUnreadSmallWidgetEntryView : View {
    var entry: UnreadSmallPostsEntry
    
    var body: some View {
        if entry.posts.isEmpty {
            noPosts
        } else {
            let safeIndex = entry.currentIndex < entry.posts.count ? entry.currentIndex : 0
            let post = entry.posts[safeIndex]
            unreadPostView(post: post, index: safeIndex)
        }
    }
    
    func unreadPostView(post: ModelWidgetPost, index: Int) -> some View {
        VStack(alignment: .leading, spacing: 0) {
            ZStack(alignment: .topLeading) {
                if let imageString = post.image,
                   let imageURL = URL(string: imageString) {
                    if let imageData = try? Data(contentsOf: imageURL),
                       let uiImage = UIImage(data: imageData) {
                        Image(uiImage: uiImage)
                            .resizable()
                            .aspectRatio(contentMode: .fill)
                            .frame(maxWidth: .infinity)
                            .frame(height: 100)
                            .clipShape(RoundedRectangle(cornerRadius: 12))
                    } else {
                        Color.gray.opacity(0.1)
                            .frame(height: 100)
                            .clipShape(RoundedRectangle(cornerRadius: 12))
                    }
                } else {
                    Spacer()
                        .frame(height: 60)
                }
                
                HStack {
                    if index > 0 {
                        Image(systemName: "chevron.left")
                            .font(.system(size: 10, weight: .bold))
                            .padding(6)
                            .background(Color.white.opacity(0.8))
                            .clipShape(Circle())
                            .padding(8)
                    } else {
                        Spacer()
                            .frame(width: 34)
                    }
                    
                    Spacer()
                    
                    if index < entry.posts.count - 1 {
                        Image(systemName: "chevron.right")
                            .font(.system(size: 10, weight: .bold))
                            .padding(6)
                            .background(Color.white.opacity(0.8))
                            .clipShape(Circle())
                            .padding(8)
                    } else {
                        Spacer()
                            .frame(width: 34)
                    }
                }
            }
            
            if post.image != nil {
                Spacer(minLength: 4)
            } else {
                Spacer(minLength: 0)
            }
            
            Text(post.title ?? post.description_ ?? String(localized: "unread_widget_no_title"))
                .font(.system(size: post.image != nil ? 10 : 14, weight: .medium))
                .lineLimit(post.image != nil ? 2 : 4)
                .padding(.horizontal, 8)
            
            Spacer(minLength: 0)
            
            HStack {
                // Publisher Info
                HStack(spacing: 4) {
                    ZStack(alignment: .bottomTrailing) {
                        if let iconString = post.feedIcon,
                           let iconURL = URL(string: iconString),
                           let iconData = try? Data(contentsOf: iconURL),
                           let uiIcon = UIImage(data: iconData) {
                            Image(uiImage: uiIcon)
                                .resizable()
                                .frame(width: 10, height: 12)
                                .cornerRadius(2)
                        } else {
                            Image(systemName: "rss")
                                .resizable()
                                .frame(width: 10, height: 10)
                        }
                        
                        Circle()
                            .fill(Color.blue)
                            .frame(width: 4, height: 4)
                            .offset(x: 1, y: 1)
                    }
                    
                    Text(post.feedName ?? "")
                        .font(.system(size: 8, weight: .medium))
                        .lineLimit(1)
                }
                
                Spacer()
                
                // Pagination Dots
                HStack(spacing: 4) {
                    ForEach(0..<min(entry.posts.count, 5), id: \.self) { i in
                        Circle()
                            .fill(i == (index % 5) ? Color.black : Color.black.opacity(0.4))
                            .frame(width: 4, height: 4)
                    }
                }
            }
            .padding(.horizontal, 8)
            .padding(.bottom, 10)
        }
        .widgetURL(createDeepLink(postIndex: index, postId: post.id))
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
        let fromScreenType = "dev.sasikanth.rss.reader.reader.ReaderScreenArgs.FromScreen.UnreadWidget"
        let json = "{\"postIndex\":\(postIndex),\"postId\":\"\(postId)\",\"fromScreen\":{\"type\":\"\(fromScreenType)\"}}"
        let encodedJson = json.addingPercentEncoding(withAllowedCharacters: .urlPathAllowed) ?? ""
        let urlString = "twine://reader/\(encodedJson)"
        return URL(string: urlString) ?? URL(string: "twine://")!
    }
}

struct TwineUnreadSmallWidget: Widget {
    let kind: String = "TwineUnreadSmallWidget"

    var body: some WidgetConfiguration {
        StaticConfiguration(kind: kind, provider: UnreadSmallProvider()) { entry in
            if #available(iOS 17.0, *) {
                TwineUnreadSmallWidgetEntryView(entry: entry)
                    .containerBackground(.fill.tertiary, for: .widget)
            } else {
                TwineUnreadSmallWidgetEntryView(entry: entry)
                    .padding(4)
                    .background()
            }
        }
        .configurationDisplayName(String(localized: "widget_unread_recent_name"))
        .supportedFamilies([.systemSmall])
    }
}

struct UnreadSmallPostsEntry: TimelineEntry {
    let date: Date
    let count: Int
    let posts: [ModelWidgetPost]
    let currentIndex: Int
    let isSubscribed: Bool
}

struct UnreadSmallProvider: TimelineProvider {

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
    
    func placeholder(in context: Context) -> UnreadSmallPostsEntry {
        UnreadSmallPostsEntry(date: Date(), count: 0, posts: [], currentIndex: 0, isSubscribed: true)
    }

    func getSnapshot(in context: Context, completion: @escaping (UnreadSmallPostsEntry) -> ()) {
        Task {
            let currentDate = Date()
            let entry = await makeEntry(date: currentDate, index: 0)
            
            if entry == nil {
                return
            }
            
            completion(entry!)
        }
    }

    func getTimeline(in context: Context, completion: @escaping (Timeline<UnreadSmallPostsEntry>) -> ()) {
        Task {
            let currentDate = Date()
            var entries: [UnreadSmallPostsEntry] = []
            
            // Generate entries to cycle through 10 posts, 10 minutes apart.
            for i in 0..<10 {
                if let entry = await makeEntry(date: Calendar.current.date(byAdding: .minute, value: i * 10, to: currentDate)!, index: i) {
                    entries.append(entry)
                }
            }
            
            if entries.isEmpty {
                return
            }
            
            let timeline = Timeline(entries: entries, policy: .atEnd)
            completion(timeline)
        }
    }
    
    private func makeEntry(date: Date, index: Int) async -> UnreadSmallPostsEntry? {
        do {
            let repository = component.widgetDataRepository
            let unreadPostsCount = try await repository.unreadPostsCountBlocking()
            let unreadPosts = try await repository.unreadPostsBlocking(numberOfPosts: 10)
            let isSubscribed = try await component.billingHandler.customerResult() is SubscriptionResultSubscribed

            return UnreadSmallPostsEntry(date: date, count: Int(unreadPostsCount), posts: unreadPosts, currentIndex: index, isSubscribed: isSubscribed)
        } catch {
            print("Failed to create entry: \(error)")
            return nil
        }
    }
}
