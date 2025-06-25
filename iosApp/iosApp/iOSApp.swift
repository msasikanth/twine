import SwiftUI
import shared

@main
struct iOSApp: App {
	
	@UIApplicationDelegateAdaptor(AppDelegate.self)
	var appDelegate: AppDelegate
	
	@Environment(\.scenePhase)
	var scenePhase: ScenePhase
	
	var rootHolder: RootHolder { appDelegate.rootHolder }

	var body: some Scene {
        let backDispatcher = BackDispatcherKt.BackDispatcher()
        let homeViewControllerComponent = InjectHomeViewControllerComponent(
            componentContext: DefaultComponentContext(lifecycle: rootHolder.lifecycle, stateKeeper: nil, instanceKeeper: nil, backHandler: backDispatcher),
            applicationComponent: appDelegate.applicationComponent,
            backDispatcher: backDispatcher
        )

		WindowGroup {
            ContentView(homeViewControllerComponent: homeViewControllerComponent, backDispatcher: backDispatcher)
				.onChange(of: scenePhase) { newPhase in
					switch newPhase {
                    case .background:
                        LifecycleRegistryExtKt.stop(rootHolder.lifecycle)
						// Run 5 minutes after the app goes into the background
                        let timeInterval = Date(timeIntervalSinceNow: 5.0 * 60)
                        appDelegate.scheduledRefreshFeeds(earliest: timeInterval)
                        appDelegate.scheduleCleanUpPosts(earliest: timeInterval)
                
                    case .inactive: LifecycleRegistryExtKt.pause(rootHolder.lifecycle)
                    case .active: LifecycleRegistryExtKt.resume(rootHolder.lifecycle)
                    @unknown default: break
					}
				}
		}
	}
}
