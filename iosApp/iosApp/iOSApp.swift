import SwiftUI
import shared
import BackgroundTasks

@main
struct iOSApp: App {
	
	@UIApplicationDelegateAdaptor(AppDelegate.self)
	private var appDelegate: AppDelegate
    
    @Environment(\.scenePhase)
    private var scenePhase

	var body: some Scene {
        let homeViewControllerComponent = InjectHomeViewControllerComponent(
            applicationComponent: appDelegate.applicationComponent
        )

		WindowGroup {
            ContentView(homeViewControllerComponent: homeViewControllerComponent)
        }
        .onChange(of: scenePhase) { newValue in
            if newValue == .background {
                Task.detached(priority: .background) {
                    print("Twine: App entered background")
                    
                    let pendingTasks = await BGTaskScheduler.shared.pendingTaskRequests()
                    guard pendingTasks.isEmpty else { return }
                    
                    await appDelegate.scheduleCleanUpPosts()
                    await appDelegate.scheduledRefreshFeeds()
                    await appDelegate.scheduleDropboxSync()
                    
                }
            }
        }
	}
}
