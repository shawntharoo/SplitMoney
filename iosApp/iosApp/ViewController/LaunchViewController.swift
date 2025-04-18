//
//  LaunchViewController.swift
//  iosApp
//
//  Created by Sandy Adikaram on 15/2/2025.
//  Copyright © 2025 orgName. All rights reserved.
//

import UIKit
import shared

class LaunchViewController: BaseViewController<LaunchScreenViewModel> {
    var coordinator: NavigationCoordinator1?
    var localViewmodel: LaunchScreenViewModel?
    override func setupInterfaceBinding(viewModel : LaunchScreenViewModel) {
        print("Hello, iOS!")
        self.localViewmodel = viewModel
        view.backgroundColor = .white
        
        let label = UILabel()
        label.text = "Hello, UIKit!"
        label.textAlignment = .center
        label.font = UIFont.systemFont(ofSize: 24)
        label.frame = view.bounds
        
        let button = UIButton(type: .system)
        button.setTitle("Go to Home", for: .normal)
        button.addTarget(self, action: #selector(navigateToDetail), for: .touchUpInside)
        button.frame = CGRect(x: 100, y: 200, width: 200, height: 50)
        
        view.addSubview(button)
        view.addSubview(label)
    }
    
    @objc func navigateToDetail() {
        print("DEBUG: coordinator ->", self.coordinator ?? "❌ nil")
        //self.coordinator?.navigateToHome()
        guard let viewModel = localViewmodel else { return }
        viewModel.navigateToNextScreen()
    }

}
