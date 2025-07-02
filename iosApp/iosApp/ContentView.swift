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
            .onReceive(NotificationCenter.default.publisher(for: UIScene.didEnterBackgroundNotification)) { output in
                WidgetCenter.shared.reloadTimelines(ofKind: AppDelegate.unreadWidgetKind)
            }
	}
}
