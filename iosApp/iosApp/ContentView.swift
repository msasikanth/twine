import UIKit
import SwiftUI
import shared

struct ComposeView: UIViewControllerRepresentable {
    let homeViewControllerComponent: InjectHomeViewControllerComponent
	
    init(homeViewControllerComponent: InjectHomeViewControllerComponent) {
        self.homeViewControllerComponent = homeViewControllerComponent
	}
	
	func makeUIViewController(context: Context) -> UIViewController {
        let controller = Main_iosKt.MainViewController(
            homeViewModelFactory: homeViewControllerComponent.homeViewModelFactory,
            imageLoader: homeViewControllerComponent.applicationComponent.imageLoader,
            openLink: { url in
                openLink(url: url)
            })
		return controller
	}
	
	func updateUIViewController(_ uiViewController: UIViewController, context: Context) {}
    
    private func openLink(url: String) {
        UIApplication.shared.open(URL(string: url)!, options: [:], completionHandler: nil)
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
	}
}



