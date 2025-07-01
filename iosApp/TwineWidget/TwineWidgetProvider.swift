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
import KMPNativeCoroutinesAsync
import KMPNativeCoroutinesCombine

struct Provider: TimelineProvider {

    let component = InjectApplicationComponent {
        UIViewController()
    }
    
    func placeholder(in context: Context) -> UnreadPostsEntry {
        UnreadPostsEntry(date: Date(), count: 0, posts: [])
    }

    func getSnapshot(in context: Context, completion: @escaping (UnreadPostsEntry) -> ()) {
        Task {
            do {
                let repository = component.widgetDataRepository
                let unreadPostsCount = try await repository.unreadPostsCountBlocking()
                let unreadPosts = try await repository.unreadPostsBlocking(numberOfPosts: 15)
                let entry = UnreadPostsEntry(date: Date(), count: unreadPostsCount.intValue, posts: unreadPosts)

                completion(entry)
            } catch {
                print("Error fetching data: \(error)")
            }
        }
    }

    func getTimeline(in context: Context, completion: @escaping (Timeline<UnreadPostsEntry>) -> ()) {
        Task {
            do {
                let repository = component.widgetDataRepository
                let unreadPostsCount = try await repository.unreadPostsCountBlocking()
                let unreadPosts = try await repository.unreadPostsBlocking(numberOfPosts: 15)
                
                let currentDate = Date()
                let entry = UnreadPostsEntry(date: currentDate, count: unreadPostsCount.intValue, posts: unreadPosts)
                let widgetUpdateDate = Calendar.current.date(byAdding: .hour, value: 1, to: currentDate)

                let timeline = Timeline(entries: [entry], policy: .after(widgetUpdateDate!))
                completion(timeline)
            
            } catch {
                print("Error fetching data: \(error)")
            }
        }
    }
}
