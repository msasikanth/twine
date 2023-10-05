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

class AppDelegate: NSObject, UIApplicationDelegate {
	let rootHolder: RootHolder = RootHolder()
    
    lazy var applicationComponent: InjectApplicationComponent = InjectApplicationComponent()
    
    func application(_ application: UIApplication, didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey : Any]? = nil) -> Bool {

        applicationComponent.initializers
            .compactMap { $0 as! any Initializer }
            .forEach { initializer in
                initializer.initialize()
            }
        
        BGTaskScheduler.shared.register(forTaskWithIdentifier: "dev.sasikanth.reader.feeds_refresh", using: DispatchQueue.main) { (task) in
            self.refreshFeeds(task: task as! BGAppRefreshTask)
        }
        
        return true
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
        applicationComponent.rssRepository.updateFeeds { error in
            if error != nil {
                self.applicationComponent.lastUpdatedAt.refresh { error in
                    // no-op
                }
            }
            task.setTaskCompleted(success: error == nil)
        }
    }
}
