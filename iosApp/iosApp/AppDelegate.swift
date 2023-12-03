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
import Sentry

class AppDelegate: NSObject, UIApplicationDelegate {
	let rootHolder: RootHolder = RootHolder()
    
    lazy var applicationComponent: InjectApplicationComponent = InjectApplicationComponent(
      uiViewControllerProvider: { UIApplication.topViewController()! }
    )
    
    func application(_ application: UIApplication, didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey : Any]? = nil) -> Bool {

        applicationComponent.initializers
            .compactMap { ($0 as! any Initializer) }
            .forEach { initializer in
                initializer.initialize()
            }
        
        BGTaskScheduler.shared.register(forTaskWithIdentifier: "dev.sasikanth.reader.feeds_refresh", using: nil) { (task) in
            self.refreshFeeds(task: task as! BGAppRefreshTask)
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
        // Schedule next clean up task 24 hours in future
        scheduleCleanUpPosts(earliest: Date(timeIntervalSinceNow: 60 * 60 * 24))
        
        Task(priority: .background) {
            do {
                let postsDeletionPeriod = try await applicationComponent.settingsRepository.postsDeletionPeriodImmediate()
                let before = postsDeletionPeriod.calculateInstantBeforePeriod()

                try await applicationComponent.rssRepository.deleteReadPosts(before: before)
                task.setTaskCompleted(success: true)
            } catch {
                let breadcrumb = Breadcrumb()
                breadcrumb.level = .info
                breadcrumb.category = "Background"
                
                let scope = Scope()
                scope.addBreadcrumb(breadcrumb)

                SentrySDK.capture(error: error, scope: scope)
                
                task.setTaskCompleted(success: false)
            }
        }
    }

    func scheduledRefreshFeeds() {
        let request = BGAppRefreshTaskRequest(identifier: "dev.sasikanth.reader.feeds_refresh")
        request.earliestBeginDate = Date(timeIntervalSinceNow: 60 * 60) // 1 hour
        
        do {
            try BGTaskScheduler.shared.submit(request)
        } catch {
            print("Could not schedule app refresh: \(error)")
        }
    }
    
    func refreshFeeds(task: BGAppRefreshTask) {
        scheduledRefreshFeeds()
        Task(priority: .background) {
            do {
                let hasLastUpdatedAtExpired = try await applicationComponent.lastUpdatedAt.hasExpired().boolValue
                if hasLastUpdatedAtExpired {
                    try await applicationComponent.rssRepository.updateFeeds()
                    try await applicationComponent.lastUpdatedAt.refresh()
                }
                
                task.setTaskCompleted(success: true)
            } catch {
                let breadcrumb = Breadcrumb()
                breadcrumb.level = .info
                breadcrumb.category = "Background"
                
                let scope = Scope()
                scope.addBreadcrumb(breadcrumb)

                SentrySDK.capture(error: error, scope: scope)
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
