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
import WidgetKit
import UserNotifications

class AppDelegate: NSObject, UIApplicationDelegate, UNUserNotificationCenterDelegate {

    static let unreadWidgetKind = "TwineUnreadWidget"

    lazy var applicationComponent: InjectApplicationComponent = InjectApplicationComponent(
        uiViewControllerProvider: { UIApplication.topViewController() ?? UIViewController() }
    )

    lazy var homeViewControllerComponent: InjectHomeViewControllerComponent = InjectHomeViewControllerComponent(
        applicationComponent: applicationComponent
    )

    private static let backgroundTaskUnwindTimeoutSeconds: TimeInterval = 2.0

    private let feedsRefreshTaskIdentifier = "dev.sasikanth.reader.feeds_refresh"
    private let postsCleanupTaskIdentifier = "dev.sasikanth.reader.posts_cleanup"
    private let cloudSyncTaskIdentifier = "dev.sasikanth.reader.cloud_sync"
    private let backgroundTaskQueue = DispatchQueue(
        label: "dev.sasikanth.reader.background_tasks",
        qos: .utility,
        attributes: .concurrent
    )
    private var backgroundObserver: NSObjectProtocol?

    func application(_ application: UIApplication, didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey : Any]? = nil) -> Bool {
        BGTaskScheduler.shared.register(forTaskWithIdentifier: feedsRefreshTaskIdentifier, using: backgroundTaskQueue) { (task) in
            self.handleRefreshFeeds(task: task as! BGProcessingTask)
        }

        BGTaskScheduler.shared.register(forTaskWithIdentifier: postsCleanupTaskIdentifier, using: backgroundTaskQueue) { (task) in
            self.handlePostsCleanup(task: task as! BGProcessingTask)
        }

        BGTaskScheduler.shared.register(forTaskWithIdentifier: cloudSyncTaskIdentifier, using: backgroundTaskQueue) { (task) in
            self.handleCloudSync(task: task as! BGProcessingTask)
        }

        backgroundObserver = NotificationCenter.default.addObserver(
            forName: UIApplication.didEnterBackgroundNotification,
            object: nil,
            queue: nil
        ) { [weak self] _ in
            guard let self else { return }
            Task.detached(priority: .background) {
                let pendingTasks = await BGTaskScheduler.shared.pendingTaskRequests()
                guard pendingTasks.isEmpty else { return }

                await self.scheduleCleanUpPosts()
                await self.scheduledRefreshFeeds()
                await self.scheduleCloudSync()
            }
        }

        #if !DEBUG
        Bugsnag.start()
        let config = BugsnagConfiguration.loadConfig()
        BugsnagConfigKt.startBugsnag(config: config)
        #endif

        UNUserNotificationCenter.current().delegate = self

        IosWidgetUpdateBridge.shared.register {
            WidgetCenter.shared.reloadAllTimelines()
        }

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

        runBackgroundProcessingTask(task) { [applicationComponent] in
            let isAutoSyncEnabled = try await applicationComponent.settingsRepository.enableAutoSyncImmediate().boolValue
            if !isAutoSyncEnabled {
                return false
            }

            try Task.checkCancellation()
            let hasLastUpdatedAtExpired = try await applicationComponent.refreshPolicy.hasExpired().boolValue
            if hasLastUpdatedAtExpired {
                let lastRefreshedAt = try await applicationComponent.refreshPolicy.fetchLastRefreshedAt()
                try await applicationComponent.syncCoordinator.pull()
                try? await applicationComponent.processSyncedPosts()
                WidgetCenter.shared.reloadAllTimelines()
                try Task.checkCancellation()

                try await applicationComponent.newArticleNotifier.notifyIfNewArticles(
                    lastRefreshedAt: lastRefreshedAt,
                    title: { count in
                        return String.localizedStringWithFormat(NSLocalizedString("notification_new_articles_title", comment: ""), Int(truncating: count))
                    },
                    content: {
                        return NSLocalizedString("notification_new_articles_content", comment: "")
                    },
                    perFeedTitle: { feedName, count in
                        return String.localizedStringWithFormat(NSLocalizedString("notification_new_articles_per_feed_title", comment: ""), feedName, Int(truncating: count))
                    }
                )
            }

            return true
        }
    }

    func handlePostsCleanup(task: BGProcessingTask) {
        Bugsnag.leaveBreadcrumb(withMessage: "Background Processing")
        scheduleCleanUpPosts()

        runBackgroundProcessingTask(task) { [applicationComponent] in
            let postsDeletionPeriod = try await applicationComponent.settingsRepository.postsDeletionPeriodImmediate()
            let before = postsDeletionPeriod.calculateInstantBeforePeriod()

            try Task.checkCancellation()
            let feedsDeletedFrom = try await applicationComponent.rssRepository.deleteReadPosts(before: before)
            if !feedsDeletedFrom.isEmpty {
                try Task.checkCancellation()
                try await applicationComponent.rssRepository.updateFeedsLastCleanUpAt(
                    feedIds: feedsDeletedFrom,
                    lastCleanUpAt: KotlinInstant.companion.currentMoment()
                )
                WidgetCenter.shared.reloadAllTimelines()
            }
            return true
        }
    }

    func handleCloudSync(task: BGProcessingTask) {
        Bugsnag.leaveBreadcrumb(withMessage: "Background Processing")
        scheduleCloudSync()

        runBackgroundProcessingTask(task) { [applicationComponent] in
            try await applicationComponent.syncCoordinator.push()
            return true
        }
    }

    private func runBackgroundProcessingTask(
        _ task: BGProcessingTask,
        operation: @escaping () async throws -> Bool
    ) {
        let lock = NSLock()
        var isCompleted = false
        let workFinishedSemaphore = DispatchSemaphore(value: 0)
        let completeOnce: (Bool) -> Void = { success in
            lock.lock()
            defer { lock.unlock() }
            guard !isCompleted else { return }
            isCompleted = true
            task.setTaskCompleted(success: success)
        }

        let workTask = Task(priority: .background) {
            defer { workFinishedSemaphore.signal() }
            do {
                let success = try await operation()
                completeOnce(success)
            } catch is CancellationError {
                completeOnce(false)
            } catch let error as NSError where error.domain == NSCocoaErrorDomain && error.code == NSUserCancelledError {
                completeOnce(false)
            } catch {
                if !Task.isCancelled {
                    Bugsnag.notifyError(error)
                }
                completeOnce(false)
            }
        }

        task.expirationHandler = { [backgroundTaskQueue] in
            workTask.cancel()
            let deadline = DispatchTime.now() + Self.backgroundTaskUnwindTimeoutSeconds
            if Thread.isMainThread {
                backgroundTaskQueue.async {
                    _ = workFinishedSemaphore.wait(timeout: deadline)
                    completeOnce(false)
                }
            } else {
                _ = workFinishedSemaphore.wait(timeout: deadline)
                completeOnce(false)
            }
        }
    }

    func userNotificationCenter(_ center: UNUserNotificationCenter, willPresent notification: UNNotification, withCompletionHandler completionHandler: @escaping (UNNotificationPresentationOptions) -> Void) {
        completionHandler([.banner, .list, .sound])
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
