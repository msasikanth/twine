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
        let appComponent = InjectAppComponent(
            driverFactory: DriverFactory()
        )
        
        let homeComponent = InjectHomeComponent(
            componentContext: DefaultComponentContext(lifecycle: rootHolder.lifecycle),
            appComponent: appComponent
        )

		WindowGroup {
            ContentView(homeComponent: homeComponent)
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
