//
//  TwineWidget.swift
//  TwineWidget
//
//  Created by Sasikanth Miriyampalli on 30/06/25.
//  Copyright © 2025 orgName. All rights reserved.
//
import SwiftUI
import WidgetKit
import shared

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
            return UnreadPostsEntry(date: currentDate, count: unreadPostsCount.intValue, posts: unreadPosts, isSubscribed: isSubscribed)
        } catch {
            print("Failed to create entry: \(error)")
            return nil
        }
    }
}
