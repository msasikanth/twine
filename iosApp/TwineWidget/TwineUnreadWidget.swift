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

struct TwineUnreadWidgetEntryViewModel : View {
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
            
            let morePostsCount = (entry.count - entry.posts.count).description
            Text("widget_unread_remaining \(morePostsCount)")
                .foregroundColor(.blue)
                .font(.footnote)
                .padding(.top, 8)
                .padding(.horizontal, 16)
                .frame(maxWidth: .infinity, alignment: .trailing)
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
        let urlString = "twine://reader/{\"postIndex\":\(postIndex),\"postId\":\"\(postId)\",\"fromScreen\":{\"type\":\"\(fromScreenType)\"}}"
        return URL(string: urlString) ?? URL(string: "twine://")!
    }
}

struct TwineUnreadWidget: Widget {
    let kind: String = "TwineUnreadWidget"

    var body: some WidgetConfiguration {
        StaticConfiguration(kind: kind, provider: Provider()) { entry in
            if #available(iOS 17.0, *) {
                TwineUnreadWidgetEntryViewModel(entry: entry)
                    .containerBackground(.fill.tertiary, for: .widget)
            } else {
                TwineUnreadWidgetEntryViewModel(entry: entry)
                    .padding()
                    .background()
            }
        }
        .configurationDisplayName(String(localized: "widget_name"))
        .supportedFamilies([.systemMedium, .systemLarge])
    }
}

struct UnreadPostsEntry: TimelineEntry {
    let date: Date
    let count: Int
    let posts: [ModelWidgetPost]
    let isSubscribed: Bool
}

#Preview(as: .systemMedium) {
    TwineUnreadWidget()
} timeline: {
    UnreadPostsEntry(date: .now, count: 0, posts: [], isSubscribed: true)
}
