//
//  TwineWidgetEntryView.swift
//  iosApp
//
//  Created by Sasikanth Miriyampalli on 30/06/25.
//  Copyright © 2025 orgName. All rights reserved.
//
import SwiftUI
import WidgetKit
import shared

struct TwineUnreadWidgetEntryView : View {
    var entry: Provider.Entry
    
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
                unreadPostsView
            }
        } else {
            twinePremium
        }
    }
    
    var unreadPostsView: some View {
        VStack(alignment: .leading) {
            ForEach(entry.posts.indices, id: \.self) { index in
                let post = entry.posts[index]
                let seconds = post.postedOn.epochSeconds
                let date = Date(timeIntervalSince1970: TimeInterval(seconds))

                
                Link(destination: createDeepLink(postIndex: index, postId: post.id)) {
                    HStack(alignment: .center, spacing: 4) {
                        VStack(
                            alignment: .leading,
                            spacing: 4
                        ) {
                            Text(post.title ?? post.description_ ?? String(localized: "unread_widget_no_title"))
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
                                
                                if (post.readingTimeEstimate > 0) {
                                    Text("bullet_separator")
                                        .font(.caption2)
                                    
                                    Text("reading_time_estimate \(Int(post.readingTimeEstimate))")
                                        .lineLimit(1)
                                        .font(.caption2)
                                }
                            }

                        }.padding(.trailing, 16)
                        
                        Spacer()
                        
                        if let imageString = post.image,
                           let imageURL = URL(string: imageString) {
                            if let imageData = try? Data(contentsOf: imageURL),
                               let uiImage = UIImage(data: imageData) {
                                Image(uiImage: uiImage)
                                    .resizable()
                                    .aspectRatio(contentMode: .fill)
                                    .frame(width: 48, height: 48)
                                    .cornerRadius(12)
                                    .clipped()
                            }
                        }
                    }
                    .padding(.vertical, 8)
                    .padding(.horizontal, 16)
                }
                
                if (index != entry.posts.count - 1) {
                    Divider()
                        .padding(.horizontal, 16)
                }
            }
            
            let morePostsCount = Int(entry.count) - entry.posts.count
            if morePostsCount > 0 {
                Text("widget_unread_remaining \(morePostsCount)")
                    .foregroundColor(.blue)
                    .font(.footnote)
                    .padding(.top, 8)
                    .padding(.horizontal, 16)
                    .frame(maxWidth: .infinity, alignment: .trailing)
            }
            
            Spacer(minLength: 0)
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
            Text("unread_no_posts")
                .font(.body)
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

struct TwineUnreadWidget: Widget {
    let kind: String = "TwineUnreadWidget"

    var body: some WidgetConfiguration {
        StaticConfiguration(kind: kind, provider: Provider()) { entry in
            if #available(iOS 17.0, *) {
                TwineUnreadWidgetEntryView(entry: entry)
                    .containerBackground(.fill.tertiary, for: .widget)
            } else {
                TwineUnreadWidgetEntryView(entry: entry)
                    .padding()
                    .background()
            }
        }
        .configurationDisplayName(String(localized: "widget_name"))
        .supportedFamilies([.systemSmall, .systemMedium, .systemLarge])
    }
}

struct UnreadPostsEntry: TimelineEntry {
    let date: Date
    let count: Int
    let posts: [ModelWidgetPost]
    let isSubscribed: Bool
}

struct Provider: TimelineProvider {

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
    
    func placeholder(in context: Context) -> UnreadPostsEntry {
        UnreadPostsEntry(date: Date(), count: 0, posts: [], isSubscribed: true)
    }

    func getSnapshot(in context: Context, completion: @escaping (UnreadPostsEntry) -> ()) {
        Task {
            let currentDate = Date()
            let entry = await makeEntry(widgetFamily: context.family, date: currentDate)
            
            if entry == nil {
                return
            }
            
            completion(entry!)
        }
    }

    func getTimeline(in context: Context, completion: @escaping (Timeline<UnreadPostsEntry>) -> ()) {
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
    
    private func makeEntry(widgetFamily: WidgetFamily, date: Date) async -> UnreadPostsEntry? {
        do {
            let numberOfPosts: Int
            switch widgetFamily {
            case .systemSmall:
                numberOfPosts = 1
            case .systemMedium:
                numberOfPosts = 2
            default:
                numberOfPosts = 4
            }
            let repository = component.widgetDataRepository
            let unreadPostsCount = try await repository.unreadPostsCountBlocking()
            let unreadPosts = try await repository.unreadPostsBlocking(numberOfPosts: Int32(numberOfPosts))
            let isSubscribed = try await component.billingHandler.customerResult() is SubscriptionResultSubscribed
            
            let currentDate = Date()
            return UnreadPostsEntry(date: currentDate, count: Int(unreadPostsCount), posts: unreadPosts, isSubscribed: isSubscribed)
        } catch {
            print("Failed to create entry: \(error)")
            return nil
        }
    }
}

#Preview(as: .systemMedium) {
    TwineUnreadWidget()
} timeline: {
    UnreadPostsEntry(date: .now, count: 0, posts: [], isSubscribed: true)
}
