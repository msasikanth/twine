//
//  AppDelegate.swift
//  iosApp
//
//  Created by Sasikanth Miriyampalli on 22/05/23.
//  Copyright © 2023 orgName. All rights reserved.
//

import UIKit
import shared
import BackgroundTasks
import Bugsnag
import RevenueCat
import WidgetKit
import UserNotifications

class AppDelegate: NSObject, UIApplicationDelegate, UNUserNotificationCenterDelegate {
    
    static let unreadWidgetKind = "TwineUnreadWidget"

    lazy var applicationComponent: InjectApplicationComponent = InjectApplicationComponent(
      uiViewControllerProvider: { UIApplication.topViewController()! }
    )
    
    private let feedsRefreshTaskIdentifier = "dev.sasikanth.reader.feeds_refresh"
    private let postsCleanupTaskIdentifier = "dev.sasikanth.reader.posts_cleanup"
    private let cloudSyncTaskIdentifier = "dev.sasikanth.reader.cloud_sync"
    
    func application(_ application: UIApplication, didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey : Any]? = nil) -> Bool {
        BGTaskScheduler.shared.register(forTaskWithIdentifier: feedsRefreshTaskIdentifier, using: nil) { (task) in
            self.handleRefreshFeeds(task: task as! BGProcessingTask)
        }
        
        BGTaskScheduler.shared.register(forTaskWithIdentifier: postsCleanupTaskIdentifier, using: nil) { (task) in
            self.handlePostsCleanup(task: task as! BGProcessingTask)
        }
        
        BGTaskScheduler.shared.register(forTaskWithIdentifier: cloudSyncTaskIdentifier, using: nil) { (task) in
            self.handleCloudSync(task: task as! BGProcessingTask)
        }

        #if !DEBUG
        Bugsnag.start()
        let config = BugsnagConfiguration.loadConfig()
        BugsnagConfigKt.startBugsnag(config: config)
        #endif

        UNUserNotificationCenter.current().delegate = self

        applicationComponent.initializers
            .compactMap { ($0 as! any Initializer) }
            .forEach { initializer in
                initializer.initialize()
            }

        return true
    }

    func scheduledRefreshFeeds() {
        let request = BGProcessingTaskRequest(identifier: feedsRefreshTaskIdentifier)
        request.earliestBeginDate = Date(timeIntervalSinceNow: 60 * 60)
        request.requiresNetworkConnectivity = true
        
        do {
            try BGTaskScheduler.shared.submit(request)
        } catch {
            print("Could not schedule app refresh: \(error)")
        }
    }
    
    func scheduleCleanUpPosts() {
        let request = BGProcessingTaskRequest(identifier: postsCleanupTaskIdentifier)
        request.earliestBeginDate = Date(timeIntervalSinceNow: 60 * 60 * 24)
        
        do {
            try BGTaskScheduler.shared.submit(request)
        } catch {
            print("Could not schedule posts cleanup \(error)")
        }
    }
    
    func scheduleCloudSync() {
        let request = BGProcessingTaskRequest(identifier: cloudSyncTaskIdentifier)
        request.earliestBeginDate = Date(timeIntervalSinceNow: 15 * 60)
        request.requiresNetworkConnectivity = true
        
        do {
            try BGTaskScheduler.shared.submit(request)
        } catch {
            print("Could not schedule dropbox sync \(error)")
        }
    }

    func handleRefreshFeeds(task: BGProcessingTask) {
        Bugsnag.leaveBreadcrumb(withMessage: "Background Processing")

        scheduledRefreshFeeds()
        Task(priority: .background) {
            do {
                let isAutoSyncEnabled = try await applicationComponent.settingsRepository.enableAutoSyncImmediate().boolValue
                
                if !isAutoSyncEnabled {
                    task.setTaskCompleted(success: false)
                    return
                }
                
                let hasLastUpdatedAtExpired = try await applicationComponent.lastRefreshedAt.hasExpired().boolValue
                if hasLastUpdatedAtExpired {
                    try await applicationComponent.syncCoordinator.pull()

                    try await applicationComponent.newArticleNotifier.notifyIfNewArticles(
                        title: { count in
                            return String.localizedStringWithFormat(NSLocalizedString("notification_new_articles_title", comment: ""), count)
                        },
                        content: {
                            return NSLocalizedString("notification_new_articles_content", comment: "")
                        }
                    )
                }
                
                WidgetCenter.shared.reloadTimelines(ofKind: AppDelegate.unreadWidgetKind)
                task.setTaskCompleted(success: true)
            } catch {
                Bugsnag.notifyError(error)
                task.setTaskCompleted(success: false)
            }
        }
    }

    func handlePostsCleanup(task: BGProcessingTask) {
        Bugsnag.leaveBreadcrumb(withMessage: "Background Processing")

        scheduleCleanUpPosts()
        
        Task(priority: .background) {
            do {
                let postsDeletionPeriod = try await applicationComponent.settingsRepository.postsDeletionPeriodImmediate()
                let before = postsDeletionPeriod.calculateInstantBeforePeriod()

                let feedsDeletedFrom = try await applicationComponent.rssRepository.deleteReadPosts(before: before)
                if !feedsDeletedFrom.isEmpty {
                    try await applicationComponent.rssRepository.updateFeedsLastCleanUpAt(feedIds: feedsDeletedFrom, lastCleanUpAt: KotlinInstant.companion.currentMoment())
                }
                task.setTaskCompleted(success: true)
            } catch {
                Bugsnag.notifyError(error)
                task.setTaskCompleted(success: false)
            }
        }
    }
    
    func handleCloudSync(task: BGProcessingTask) {
        Bugsnag.leaveBreadcrumb(withMessage: "Background Processing")

        scheduleDropboxSync()
        
        Task(priority: .background) {
            do {
                try await applicationComponent.syncCoordinator.push()
                task.setTaskCompleted(success: true)
            } catch {
                Bugsnag.notifyError(error)
                task.setTaskCompleted(success: false)
            }
        }
    }

    func userNotificationCenter(_ center: UNUserNotificationCenter, didReceive response: UNNotificationResponse, withCompletionHandler completionHandler: @escaping () -> Void) {
        completionHandler()
    }
}

extension UIApplication {

    private class func keyWindowCompat() -> UIWindow? {
         return UIApplication
             .shared
             .connectedScenes
             .flatMap { ($0 as? UIWindowScene)?.windows ?? [] }
             .last { $0.isKeyWindow }
     }

    class func topViewController(
        base: UIViewController? = UIApplication.keyWindowCompat()?.rootViewController
    ) -> UIViewController? {
        if let nav = base as? UINavigationController {
            return topViewController(base: nav.visibleViewController)
        }

        if let tab = base as? UITabBarController {
            let moreNavigationController = tab.moreNavigationController

            if let top = moreNavigationController.topViewController, top.view.window != nil {
                return topViewController(base: top)
            } else if let selected = tab.selectedViewController {
                return topViewController(base: selected)
            }
        }

        if let presented = base?.presentedViewController {
            return topViewController(base: presented)
        }

        return base
    }
}
