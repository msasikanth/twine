import SwiftUI
import shared

@main
struct iOSApp: App {

    @UIApplicationDelegateAdaptor(AppDelegate.self)
    private var appDelegate: AppDelegate

    var body: some Scene {
        WindowGroup {
            ContentView(homeViewControllerComponent: appDelegate.homeViewControllerComponent)
        }
    }
}
