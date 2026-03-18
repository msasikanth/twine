//
//  TwineStatsWidget.swift
//  iosApp
//
//  Created by Sasikanth Miriyampalli on 18/03/26.
//  Copyright © 2026 orgName. All rights reserved.
//

import SwiftUI
import WidgetKit
import shared

struct TwineStatsWidgetEntryView : View {
    var entry: StatsEntry

    var body: some View {
        HStack(alignment: .center) {
            VStack(alignment: .center) {
                Text(String(entry.totalReadCount))
                    .font(.system(size: 32, weight: .bold))
                
                Text("widget_stats_total_read")
                    .font(.caption)
                    .foregroundColor(.secondary)
                    .multilineTextAlignment(.center)
            }.frame(maxWidth: .infinity)
            
            Divider()
            
            VStack(alignment: .center) {
                Text(String(entry.dailyAverage))
                    .font(.system(size: 32, weight: .bold))
                
                Text("widget_stats_daily_average")
                    .font(.caption)
                    .foregroundColor(.secondary)
                    .multilineTextAlignment(.center)
            }.frame(maxWidth: .infinity)
        }.frame(maxWidth: .infinity, maxHeight: .infinity)
    }
}

struct TwineStatsWidget: Widget {
    let kind: String = "TwineStatsWidget"

    var body: some WidgetConfiguration {
        StaticConfiguration(kind: kind, provider: StatsProvider()) { entry in
            if #available(iOS 17.0, *) {
                TwineStatsWidgetEntryView(entry: entry)
                    .containerBackground(.fill.tertiary, for: .widget)
            } else {
                TwineStatsWidgetEntryView(entry: entry)
                    .padding()
                    .background()
            }
        }
        .configurationDisplayName(String(localized: "widget_stats_name"))
        .supportedFamilies([.systemSmall, .systemMedium])
    }
}

struct StatsEntry: TimelineEntry {
    let date: Date
    let totalReadCount: Int
    let dailyAverage: Int
}

struct StatsProvider: TimelineProvider {

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
    
    func placeholder(in context: Context) -> StatsEntry {
        StatsEntry(date: Date(), totalReadCount: 0, dailyAverage: 0)
    }

    func getSnapshot(in context: Context, completion: @escaping (StatsEntry) -> ()) {
        Task {
            let currentDate = Date()
            let entry = await makeEntry(date: currentDate)
            
            if entry == nil {
                return
            }
            
            completion(entry!)
        }
    }

    func getTimeline(in context: Context, completion: @escaping (Timeline<StatsEntry>) -> ()) {
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
    
    private func makeEntry(date: Date) async -> StatsEntry? {
        do {
            let repository = component.widgetDataRepository
            let statistics = try await repository.statsBlocking()
            
            let currentDate = Date()
            return StatsEntry(date: currentDate, totalReadCount: Int(truncating: statistics.totalReadCount), dailyAverage: Int(truncating: statistics.dailyAverage))
        } catch {
            print("Failed to create entry: \(error)")
            return nil
        }
    }
}
