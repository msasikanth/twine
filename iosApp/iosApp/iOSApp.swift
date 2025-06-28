import SwiftUI
import shared

@main
struct iOSApp: App {
	
	@UIApplicationDelegateAdaptor(AppDelegate.self)
	var appDelegate: AppDelegate

	var body: some Scene {
        let homeViewControllerComponent = InjectHomeViewControllerComponent(
            applicationComponent: appDelegate.applicationComponent
        )

		WindowGroup {
            ContentView(homeViewControllerComponent: homeViewControllerComponent)
		}
	}
}
