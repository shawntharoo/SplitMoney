//
//  BaseViewController.swift
//  iosApp
//
//  Created by Sandy Adikaram on 2/3/2025.
//  Copyright © 2025 orgName. All rights reserved.
//

import shared
import UIKit

class BaseViewController<TViewModel: BaseViewModel> : UIViewController, ViewControllerBinding {
    typealias Service = TViewModel
    @LazyLoading<TViewModel> var viewModel
    
    func setupInterfaceBinding(viewModel: TViewModel) {}
    
    override func viewDidLoad() {
        super.viewDidLoad()
        if let viewModel {
            viewModel.onInitialize()
            setupInterfaceBinding(viewModel: viewModel)
        }
    }
    
}

