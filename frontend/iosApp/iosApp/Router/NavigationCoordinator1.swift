//
//  NavigationCoordinator.swift
//  iosApp
//
//  Created by Sandy Adikaram on 16/2/2025.
//  Copyright © 2025 orgName. All rights reserved.
//

import UIKit

class NavigationCoordinator1 {
    var navigationController: UINavigationController!
    
    init() {
        let rootViewController = LaunchViewController()
        self.navigationController = UINavigationController(rootViewController: rootViewController)
    }
    
    func start() -> UIViewController {
        return navigationController
    }
    
    func navigateToHome() {
        let homeVC = HomeViewController()
        navigationController.pushViewController(homeVC, animated: true)
    }
    
}
