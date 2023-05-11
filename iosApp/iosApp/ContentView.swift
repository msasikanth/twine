import UIKit
import SwiftUI
import shared

struct ComposeView: UIViewControllerRepresentable {
	let lifecyle: LifecycleRegistry = LifecycleRegistryKt.LifecycleRegistry()
	
	init() {
		LifecycleRegistryExtKt.create(lifecyle)
	}
	
	func makeUIViewController(context: Context) -> UIViewController {
		let appComponent = InjectDIAppComponent(componentContext: DefaultComponentContext(lifecycle: lifecyle), driverFactory: DriverFactory())
		let controller = Main_iosKt.MainViewController(component: appComponent.homeComponent)
		LifecycleRegistryExtKt.resume(lifecyle)
		return controller
	}
	
	func updateUIViewController(_ uiViewController: UIViewController, context: Context) {}
}

struct ContentView: View {
	var body: some View {
		ComposeView()
			.ignoresSafeArea(.keyboard) // Compose has own keyboard handler
			.edgesIgnoringSafeArea(.all)
	}
}



