//
//  NavigationSwiftService.swift
//  iosApp
//
//  Created by Sandy Adikaram on 16/2/2025.
//  Copyright © 2025 orgName. All rights reserved.
//
import shared
import XCoordinator

class NavigationSwiftService: NavigationCoreServices {
    static let router = AppCoordinator()
    static let mainAppRouter = router.strongRouter
    func navigateToViewModel(destination: RoutingNames, parameters: Any?) {
        switch(destination) {
        default :
            print("Not Implemented")
            self.trigger(.Home)
        }
    }
    
    private func trigger(_ route: AppRoutes, completion: PresentationHandler? = nil) {
        DispatchQueue.main.async {
            Self.mainAppRouter.trigger(route, completion: completion)
        }
    }
}
