//
//  TwineBookmarkWidget.swift
//  iosApp
//
//  Created by Sasikanth Miriyampalli on 18/03/26.
//  Copyright © 2026 orgName. All rights reserved.
//

import SwiftUI
import WidgetKit
import shared

struct TwineBookmarkWidgetEntryView : View {
    var entry: BookmarkPostsEntry
    
    private let formatter: RelativeDateTimeFormatter = {
        let formatter = RelativeDateTimeFormatter()
        formatter.unitsStyle = .short
        return formatter
    }()

    var body: some View {
        if entry.isSubscribed {
            if entry.posts.isEmpty {
                noPosts
            } else {
                bookmarkPostsView
            }
        } else {
            twinePremium
        }
    }
    
    var bookmarkPostsView: some View {
        VStack(alignment: .leading) {
            ForEach(entry.posts.indices, id: \.self) { index in
                let post = entry.posts[index]
                let seconds = post.postedOn.epochSeconds
                let date = Date(timeIntervalSince1970: TimeInterval(seconds))

                
                Link(destination: createDeepLink(postIndex: index, postId: post.id)) {
                    HStack(alignment: .center, spacing: 4) {
                        if let feedIconString = post.feedIcon,
                           let feedIconURL = URL(string: feedIconString) {
                            if let feedIconData = try? Data(contentsOf: feedIconURL),
                               let uiImage = UIImage(data: feedIconData) {
                                Image(uiImage: uiImage)
                                    .resizable()
                                    .aspectRatio(contentMode: .fill)
                                    .frame(width: 24, height: 24)
                                    .cornerRadius(4)
                                    .clipped()
                            } else {
                                Spacer()
                                    .frame(width: 24, height: 24)
                                    .background(Color.gray.opacity(0.2))
                                    .cornerRadius(4)
                            }
                        } else {
                            Spacer()
                                .frame(width: 24, height: 24)
                                .background(Color.gray.opacity(0.2))
                                .cornerRadius(4)
                        }
                        
                        VStack(
                            alignment: .leading,
                            spacing: 8
                        ) {
                            Text(post.title ?? String(localized: "unread_widget_no_title"))
                                .lineLimit(2)
                                .font(.headline)

                            HStack(alignment: .center) {
                                Text(post.feedName ?? String(localized: "unread_widget_no_feed_name"))
                                    .lineLimit(1)
                                    .font(.caption2)
                                
                                Text("bullet_separator")
                                    .font(.caption2)

                                Text(formatter.localizedString(for: date, relativeTo: Date()))
                                    .lineLimit(1)
                                    .font(.caption2)
                            }

                        }.padding(.horizontal, 16)
                    }
                }
                
                if (index != entry.posts.count - 1) {
                    Divider()
                        .padding(.horizontal, 16)
                }
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
        VStack {
            Text("bookmarks_no_posts")
                .font(.body)
                .multilineTextAlignment(.center)
        }.frame(maxHeight: .infinity, alignment: .center)
    }
    
    private func createDeepLink(postIndex: Int, postId: String) -> URL {
        let fromScreenType = "dev.sasikanth.rss.reader.reader.ReaderScreenArgs.FromScreen.Bookmarks"
        let json = "{\"postIndex\":\(postIndex),\"postId\":\"\(postId)\",\"fromScreen\":{\"type\":\"\(fromScreenType)\"}}"
        let encodedJson = json.addingPercentEncoding(withAllowedCharacters: .urlPathAllowed) ?? ""
        let urlString = "twine://reader/\(encodedJson)"
        return URL(string: urlString) ?? URL(string: "twine://")!
    }
}

struct TwineBookmarkWidget: Widget {
    let kind: String = "TwineBookmarkWidget"

    var body: some WidgetConfiguration {
        StaticConfiguration(kind: kind, provider: BookmarkProvider()) { entry in
            if #available(iOS 17.0, *) {
                TwineBookmarkWidgetEntryView(entry: entry)
                    .containerBackground(.fill.tertiary, for: .widget)
            } else {
                TwineBookmarkWidgetEntryView(entry: entry)
                    .padding()
                    .background()
            }
        }
        .configurationDisplayName(String(localized: "widget_bookmarks_name"))
        .supportedFamilies([.systemMedium, .systemLarge])
    }
}

struct BookmarkPostsEntry: TimelineEntry {
    let date: Date
    let count: Int
    let posts: [ModelWidgetPost]
    let isSubscribed: Bool
}

struct BookmarkProvider: TimelineProvider {

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
    
    func placeholder(in context: Context) -> BookmarkPostsEntry {
        BookmarkPostsEntry(date: Date(), count: 0, posts: [], isSubscribed: true)
    }

    func getSnapshot(in context: Context, completion: @escaping (BookmarkPostsEntry) -> ()) {
        Task {
            let currentDate = Date()
            let entry = await makeEntry(widgetFamily: context.family, date: currentDate)
            
            if entry == nil {
                return
            }
            
            completion(entry!)
        }
    }

    func getTimeline(in context: Context, completion: @escaping (Timeline<BookmarkPostsEntry>) -> ()) {
        Task {
            let currentDate = Date()
            let entry = await makeEntry(widgetFamily: context.family, date: currentDate)
            if entry == nil {
                return
            }
            
            let widgetUpdateDate = Calendar.current.date(byAdding: .hour, value: 1, to: currentDate)

            let timeline = Timeline(entries: [entry!], policy: .after(widgetUpdateDate!))
            completion(timeline)
        
        }
    }
    
    private func makeEntry(widgetFamily: WidgetFamily, date: Date) async -> BookmarkPostsEntry? {
        do {
            let numberOfPosts: Int
            switch widgetFamily {
            case .systemMedium:
                numberOfPosts = 2
            default:
                numberOfPosts = 4
            }
            let repository = component.widgetDataRepository
            let bookmarkPostsCount = try await repository.bookmarkPostsCountBlocking()
            let bookmarkPosts = try await repository.bookmarkPostsBlocking(numberOfPosts: Int32(numberOfPosts))
            let isSubscribed = try await component.billingHandler.customerResult() is SubscriptionResultSubscribed
            
            let currentDate = Date()
            return BookmarkPostsEntry(date: currentDate, count: Int(bookmarkPostsCount), posts: bookmarkPosts, isSubscribed: isSubscribed)
        } catch {
            print("Failed to create entry: \(error)")
            return nil
        }
    }
}
