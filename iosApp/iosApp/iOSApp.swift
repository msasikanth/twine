import SwiftUI
import shared

@main
struct iOSApp: App {
	
	init() {
		LoggingKt.initialiseLogging()
	}
	
	var body: some Scene {
		WindowGroup {
			ContentView()
		}
	}
}
