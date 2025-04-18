//
//  AppCoordinator.swift
//  iosApp
//
//  Created by Sandy Adikaram on 16/2/2025.
//  Copyright © 2025 orgName. All rights reserved.
//

import UIKit
import XCoordinator
import Foundation

enum AppRoutes : Route {
    case Launch
    case Home
}

class AppCoordinator: NavigationCoordinator<AppRoutes> {
    init() {
        super.init(initialRoute: .Launch)
    }
    
    override func prepareTransition(for route: AppRoutes) -> NavigationTransition {
        switch(route) {
        case .Launch:
            return .push(LaunchViewController())
        case .Home:
            return .push(HomeViewController())
        }
    }
}
