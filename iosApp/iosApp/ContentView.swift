import UIKit
import SwiftUI
import shared

struct ComposeView: UIViewControllerRepresentable {
	let lifecyle: LifecycleRegistry = LifecycleRegistryKt.LifecycleRegistry()
	
	init() {
		LifecycleRegistryExtKt.create(lifecyle)
	}
	
	func makeUIViewController(context: Context) -> UIViewController {
		let dispatchersProvider = DefaultDispatchersProvider()
		let database = DatabaseKt.createDatabase(driverFactory: DriverFactory())
		let component = HomeComponent(
			componentContext: DefaultComponentContext(lifecycle: lifecyle),
			rssRepository: RssRepository(
				feedQueries: database.feedQueries,
				postQueries: database.postQueries,
				ioDispatcher: dispatchersProvider.io
			),
			dispatchersProvider: dispatchersProvider
		)
		
		let controller = Main_iosKt.MainViewController(component: component)
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



