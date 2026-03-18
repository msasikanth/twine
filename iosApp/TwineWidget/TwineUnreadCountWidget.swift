//
//  TwineUnreadCountWidget.swift
//  iosApp
//
//  Created by Sasikanth Miriyampalli on 18/03/26.
//  Copyright © 2026 orgName. All rights reserved.
//

import SwiftUI
import WidgetKit
import shared

struct TwineUnreadCountWidgetEntryView : View {
    var entry: UnreadCountEntry

    var body: some View {
        VStack(alignment: .center) {
            Text(String(entry.count))
                .font(.system(size: 48, weight: .bold))
                .foregroundColor(.blue)
            
            Text("widget_unread_count_name")
                .font(.headline)
                .foregroundColor(.secondary)
        }.frame(maxWidth: .infinity, maxHeight: .infinity)
    }
}

struct TwineUnreadCountWidget: Widget {
    let kind: String = "TwineUnreadCountWidget"

    var body: some WidgetConfiguration {
        StaticConfiguration(kind: kind, provider: UnreadCountProvider()) { entry in
            if #available(iOS 17.0, *) {
                TwineUnreadCountWidgetEntryView(entry: entry)
                    .containerBackground(.fill.tertiary, for: .widget)
            } else {
                TwineUnreadCountWidgetEntryView(entry: entry)
                    .padding()
                    .background()
            }
        }
        .configurationDisplayName(String(localized: "widget_unread_count_name"))
        .supportedFamilies([.systemSmall])
    }
}

struct UnreadCountEntry: TimelineEntry {
    let date: Date
    let count: Int
}

struct UnreadCountProvider: TimelineProvider {

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
    
    func placeholder(in context: Context) -> UnreadCountEntry {
        UnreadCountEntry(date: Date(), count: 0)
    }

    func getSnapshot(in context: Context, completion: @escaping (UnreadCountEntry) -> ()) {
        Task {
            let currentDate = Date()
            let entry = await makeEntry(date: currentDate)
            
            if entry == nil {
                return
            }
            
            completion(entry!)
        }
    }

    func getTimeline(in context: Context, completion: @escaping (Timeline<UnreadCountEntry>) -> ()) {
        Task {
            let currentDate = Date()
            let entry = await makeEntry(date: currentDate)
            if entry == nil {
                return
            }
            
            let widgetUpdateDate = Calendar.current.date(byAdding: .hour, value: 1, to: currentDate)

            let timeline = Timeline(entries: [entry!], policy: .after(widgetUpdateDate!))
            completion(timeline)
        
        }
    }
    
    private func makeEntry(date: Date) async -> UnreadCountEntry? {
        do {
            let repository = component.widgetDataRepository
            let unreadPostsCount = try await repository.unreadPostsCountBlocking()
            
            let currentDate = Date()
            return UnreadCountEntry(date: currentDate, count: Int(unreadPostsCount))
        } catch {
            print("Failed to create entry: \(error)")
            return nil
        }
    }
}
