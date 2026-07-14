import UIKit
import WidgetKit
import SwiftUI
import shared

struct ComposeView: UIViewControllerRepresentable {
    
    let homeViewControllerComponent: InjectHomeViewControllerComponent
	
    init(homeViewControllerComponent: InjectHomeViewControllerComponent) {
        self.homeViewControllerComponent = homeViewControllerComponent
	}
	
	func makeUIViewController(context: Context) -> UIViewController {
        return homeViewControllerComponent.homeViewController.viewController()
	}

    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {
        
    }
}

struct ContentView: View {
	
    let homeViewControllerComponent: InjectHomeViewControllerComponent
    
    init(homeViewControllerComponent: InjectHomeViewControllerComponent) {
        self.homeViewControllerComponent = homeViewControllerComponent
	}

	var body: some View {
        ComposeView(homeViewControllerComponent: homeViewControllerComponent)
			.ignoresSafeArea(.keyboard) // Compose has own keyboard handler
			.edgesIgnoringSafeArea(.all)
            .onOpenURL { url in
                ExternalUriHandler.shared.onNewUri(uri: url.absoluteString)
            }
            // Note: iOS provides no signal for "app opened from the system Now Playing
            // controls", so we intentionally don't synthesize the currently-playing
            // deep link here. Doing it on scenePhase changes navigated to the reader on
            // *every* foreground (recents, app icon) while audio was playing.
            .onReceive(NotificationCenter.default.publisher(for: UIScene.didEnterBackgroundNotification)) { output in
                // Handled by RssRepository
            }
	}
}
