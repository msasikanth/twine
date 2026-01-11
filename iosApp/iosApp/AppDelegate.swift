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
    
    func application(_ application: UIApplication, didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey : Any]? = nil) -> Bool {
        BGTaskScheduler.shared.register(forTaskWithIdentifier: "dev.sasikanth.reader.feeds_refresh", using: nil) { (task) in
            self.refreshFeeds(task: task as! BGProcessingTask)
        }
        
        BGTaskScheduler.shared.register(forTaskWithIdentifier: "dev.sasikanth.reader.posts_cleanup", using: nil) { (task) in
            self.cleanUpPosts(task: task as! BGProcessingTask)
        }
        
        BGTaskScheduler.shared.register(forTaskWithIdentifier: "dev.sasikanth.reader.dropbox_sync", using: nil) { (task) in
            self.syncDropbox(task: task as! BGProcessingTask)
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

    func scheduleDropboxSync(earliest: Date) {
        let request = BGProcessingTaskRequest(identifier: "dev.sasikanth.reader.dropbox_sync")
        request.earliestBeginDate = earliest
        request.requiresNetworkConnectivity = true
        
        do {
            try BGTaskScheduler.shared.submit(request)
        } catch {
            print("Could not schedule dropbox sync \(error)")
        }
    }
    
    func syncDropbox(task: BGProcessingTask) {
        Bugsnag.leaveBreadcrumb(withMessage: "Background Processing")

        // Schedule next sync task 15 minutes in future
        scheduleDropboxSync(earliest: Date(timeIntervalSinceNow: 15 * 60))
        
        Task(priority: .background) {
            do {
                let isDropboxSignedIn = try await applicationComponent.dropboxSyncProvider.isSignedInImmediate().boolValue
                if isDropboxSignedIn {
                    try await applicationComponent.cloudSyncService.sync(provider: applicationComponent.dropboxSyncProvider)
                }
                task.setTaskCompleted(success: true)
            } catch {
                Bugsnag.notifyError(error)
                task.setTaskCompleted(success: false)
            }
        }
    }
    
    func scheduleCleanUpPosts(earliest: Date) {
        let request = BGProcessingTaskRequest(identifier: "dev.sasikanth.reader.posts_cleanup")
        request.earliestBeginDate = earliest
        
        do {
            try BGTaskScheduler.shared.submit(request)
        } catch {
            print("Could not schedule posts cleanup \(error)")
        }
    }
    
    func cleanUpPosts(task: BGProcessingTask) {
        Bugsnag.leaveBreadcrumb(withMessage: "Background Processing")

        // Schedule next clean up task 24 hours in future
        scheduleCleanUpPosts(earliest: Date(timeIntervalSinceNow: 60 * 60 * 24))
        
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

    func scheduledRefreshFeeds(earliest: Date) {
        let request = BGProcessingTaskRequest(identifier: "dev.sasikanth.reader.feeds_refresh")
        request.earliestBeginDate = earliest
        request.requiresNetworkConnectivity = true
        
        do {
            try BGTaskScheduler.shared.submit(request)
        } catch {
            print("Could not schedule app refresh: \(error)")
        }
    }
    
    func refreshFeeds(task: BGProcessingTask) {
        Bugsnag.leaveBreadcrumb(withMessage: "Background Processing")

        scheduledRefreshFeeds(earliest: Date(timeIntervalSinceNow: 60 * 60)) // 1 hour
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
                            return String.localizedStringWithFormat(NSLocalizedString("notification_new_articles_title \(count)", comment: ""), count)
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
