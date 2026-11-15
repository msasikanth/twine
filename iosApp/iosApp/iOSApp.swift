import SwiftUI
import shared
import BackgroundTasks
import WidgetKit

@main
struct iOSApp: App {

	@UIApplicationDelegateAdaptor(AppDelegate.self)
	private var appDelegate: AppDelegate

    @Environment(\.scenePhase)
    private var scenePhase

	var body: some Scene {
		WindowGroup {
            ContentView(homeViewControllerComponent: appDelegate.homeViewControllerComponent)
        }
        .onChange(of: scenePhase) {
            if scenePhase == .background {
                Task.detached(priority: .background) {
                    let pendingTasks = await BGTaskScheduler.shared.pendingTaskRequests()
                    guard pendingTasks.isEmpty else { return }

                    await appDelegate.scheduleCleanUpPosts()
                    await appDelegate.scheduledRefreshFeeds()
                    await appDelegate.scheduleCloudSync()
                }
            }
        }
	}
}
