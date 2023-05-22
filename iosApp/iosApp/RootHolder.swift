//
//  RootHolder.swift
//  iosApp
//
//  Created by Sasikanth Miriyampalli on 22/05/23.
//  Copyright © 2023 orgName. All rights reserved.
//

import Foundation
import shared

class RootHolder : ObservableObject {
	let lifecycle: LifecycleRegistry
	
	init() {
		lifecycle = LifecycleRegistryKt.LifecycleRegistry()
		LifecycleRegistryExtKt.create(lifecycle)
	}
	
	deinit {
		// Destroy the root component before it is deallocated
		LifecycleRegistryExtKt.destroy(lifecycle)
	}
}
