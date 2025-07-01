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

    var body: some View {
        VStack {
            ForEach(entry.posts, id: \.id) { post in
                Text(post.title ?? "No title")
                    .lineLimit(2)
                    .padding(.all, 16)
                
                Divider()
                    .padding(.horizontal, 16)
            }
        }
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
        .configurationDisplayName("Unread posts")
        .supportedFamilies([.systemMedium, .systemLarge])
    }
}

struct UnreadPostsEntry: TimelineEntry {
    let date: Date
    let count: Int
    let posts: [ModelWidgetPost]
}

#Preview(as: .systemMedium) {
    TwineUnreadWidget()
} timeline: {
    UnreadPostsEntry(date: .now, count: 0, posts: [])
}
