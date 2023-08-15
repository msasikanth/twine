//
//  AppDelegate.swift
//  iosApp
//
//  Created by Sasikanth Miriyampalli on 22/05/23.
//  Copyright © 2023 orgName. All rights reserved.
//

import UIKit
import shared

class AppDelegate: NSObject, UIApplicationDelegate {
	let rootHolder: RootHolder = RootHolder()
    
    lazy var applicationComponent: InjectApplicationComponent = InjectApplicationComponent()
}
