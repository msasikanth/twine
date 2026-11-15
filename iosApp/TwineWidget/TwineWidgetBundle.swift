//
//  TwineWidgetBundle.swift
//  TwineWidget
//
//  Created by Sasikanth Miriyampalli on 30/06/25.
//  Copyright © 2025 orgName. All rights reserved.
//

import UIKit
import WidgetKit
import SwiftUI
import shared

enum SharedWidgetComponent {
    static let instance: InjectApplicationComponent = {
        let component = InjectApplicationComponent(uiViewControllerProvider: {
            UIViewController()
        })
        component.initializers
            .compactMap { $0 as? any Initializer }
            .filter { !($0 is PostSyncProcessorInitializer) }
            .forEach { initializer in
                initializer.initialize()
            }
        return component
    }()
}

@main
struct TwineWidgetBundle: WidgetBundle {
    var body: some Widget {
        TwineUnreadSmallWidget()
        TwineUnreadMediumWidget()
        TwineUnreadLargeWidget()
    }
}
