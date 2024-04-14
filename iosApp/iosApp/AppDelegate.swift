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

class AppDelegate: NSObject, UIApplicationDelegate {
	let rootHolder: RootHolder = RootHolder()
    
    lazy var applicationComponent: InjectApplicationComponent = InjectApplicationComponent(
      uiViewControllerProvider: { UIApplication.topViewController()! }
    )
    
    func application(_ application: UIApplication, didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey : Any]? = nil) -> Bool {
        #if !DEBUG
        Bugsnag.start()
        let config = BugsnagConfiguration.loadConfig()
        BugsnagConfigKt.startBugsnag(config: config)
        #endif

        applicationComponent.initializers
            .compactMap { ($0 as! any Initializer) }
            .forEach { initializer in
                initializer.initialize()
            }
        
        BGTaskScheduler.shared.register(forTaskWithIdentifier: "dev.sasikanth.reader.feeds_refresh", using: nil) { (task) in
            self.refreshFeeds(task: task as! BGProcessingTask)
        }
        
        BGTaskScheduler.shared.register(forTaskWithIdentifier: "dev.sasikanth.reader.posts_cleanup", using: nil) { (task) in
            self.cleanUpPosts(task: task as! BGProcessingTask)
        }
        
        return true
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
                    try await applicationComponent.rssRepository.updateFeedsLastCleanUpAt(feedIds: feedsDeletedFrom)
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
                let hasLastUpdatedAtExpired = try await applicationComponent.lastUpdatedAt.hasExpired().boolValue
                if hasLastUpdatedAtExpired {
                    try await applicationComponent.rssRepository.updateFeeds()
                    try await applicationComponent.lastUpdatedAt.refresh()
                }
                
                task.setTaskCompleted(success: true)
            } catch {
                Bugsnag.notifyError(error)
                task.setTaskCompleted(success: false)
            }
        }
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
