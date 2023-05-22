import UIKit
import SwiftUI
import shared

struct ComposeView: UIViewControllerRepresentable {
	let lifecyle: Lifecycle
	
	init(lifecyle: Lifecycle) {
		self.lifecyle = lifecyle
	}
	
	func makeUIViewController(context: Context) -> UIViewController {
		let appComponent = InjectAppComponent(componentContext: DefaultComponentContext(lifecycle: lifecyle), driverFactory: DriverFactory())
		let controller = Main_iosKt.MainViewController(homeViewModelFactory: appComponent.homeViewModelFactory)
		return controller
	}
	
	func updateUIViewController(_ uiViewController: UIViewController, context: Context) {}
}

struct ContentView: View {
	
	let lifecycle: Lifecycle
	
	init(lifecycle: Lifecycle) {
		self.lifecycle = lifecycle
	}
	
	var body: some View {
		ComposeView(lifecyle: lifecycle)
			.ignoresSafeArea(.keyboard) // Compose has own keyboard handler
			.edgesIgnoringSafeArea(.all)
	}
}



