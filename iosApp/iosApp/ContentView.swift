import UIKit
import SwiftUI
import shared

struct ComposeView: UIViewControllerRepresentable {
    let homeComponent: InjectHomeComponent
	
    init(homeComponent: InjectHomeComponent) {
        self.homeComponent = homeComponent
	}
	
	func makeUIViewController(context: Context) -> UIViewController {
        let controller = Main_iosKt.MainViewController(homeViewModelFactory: homeComponent.homeViewModelFactory, openLink: { url in
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
	
    let homeComponent: InjectHomeComponent
	
    init(homeComponent: InjectHomeComponent) {
        self.homeComponent = homeComponent
	}

	var body: some View {
        ComposeView(homeComponent: homeComponent)
			.ignoresSafeArea(.keyboard) // Compose has own keyboard handler
			.edgesIgnoringSafeArea(.all)
	}
}



