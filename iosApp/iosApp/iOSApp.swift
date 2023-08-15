import SwiftUI
import shared

@main
struct iOSApp: App {
	
	@UIApplicationDelegateAdaptor(AppDelegate.self)
	var appDelegate: AppDelegate
	
	@Environment(\.scenePhase)
	var scenePhase: ScenePhase
	
	var rootHolder: RootHolder { appDelegate.rootHolder }
	
	init() {
		LoggingKt.initialiseLogging()
	}
	
	var body: some Scene {
        let applicationComponent = InjectApplicationComponent()
        
        let homeViewControllerComponent = InjectHomeViewControllerComponent(
            componentContext: DefaultComponentContext(lifecycle: rootHolder.lifecycle),
            applicationComponent: applicationComponent
        )

		WindowGroup {
            ContentView(homeViewControllerComponent: homeViewControllerComponent)
				.onChange(of: scenePhase) { newPhase in
					switch newPhase {
						case .background: LifecycleRegistryExtKt.stop(rootHolder.lifecycle)
						case .inactive: LifecycleRegistryExtKt.pause(rootHolder.lifecycle)
						case .active: LifecycleRegistryExtKt.resume(rootHolder.lifecycle)
						@unknown default: break
					}
				}
		}
	}
}
