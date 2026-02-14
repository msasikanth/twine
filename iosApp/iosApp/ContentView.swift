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
    
    @Environment(\.scenePhase)
    private var scenePhase
    
    @State private var wasInBackground = false
	
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
            .onChange(of: scenePhase) { oldPhase, newPhase in
                if newPhase == .background {
                    wasInBackground = true
                }

                if newPhase == .active && wasInBackground {
                    ExternalUriHandler.shared.onNewUri(uri: "twine://reader/currently-playing")
                    wasInBackground = false
                }
            }
            .onReceive(NotificationCenter.default.publisher(for: UIScene.didEnterBackgroundNotification)) { output in
                WidgetCenter.shared.reloadTimelines(ofKind: AppDelegate.unreadWidgetKind)
            }
	}
}
