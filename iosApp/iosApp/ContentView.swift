import UIKit
import SwiftUI
import shared

struct ComposeView: UIViewControllerRepresentable {
    
    let homeViewControllerComponent: InjectHomeViewControllerComponent
    let backDispatcher: BackDispatcher
	
    init(homeViewControllerComponent: InjectHomeViewControllerComponent, backDispatcher: BackDispatcher) {
        self.homeViewControllerComponent = homeViewControllerComponent
        self.backDispatcher = backDispatcher
	}
	
	func makeUIViewController(context: Context) -> UIViewController {
        return homeViewControllerComponent.homeViewControllerFactory(backDispatcher)
	}

    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {
        
    }
}

struct ContentView: View {
	
    let homeViewControllerComponent: InjectHomeViewControllerComponent
    let backDispatcher: BackDispatcher
	
    init(homeViewControllerComponent: InjectHomeViewControllerComponent, backDispatcher: BackDispatcher) {
        self.homeViewControllerComponent = homeViewControllerComponent
        self.backDispatcher = backDispatcher
	}

	var body: some View {
        ComposeView(homeViewControllerComponent: homeViewControllerComponent, backDispatcher: backDispatcher)
			.ignoresSafeArea(.keyboard) // Compose has own keyboard handler
			.edgesIgnoringSafeArea(.all)
	}
}
