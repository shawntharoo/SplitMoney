//
//  ViewControllerBinding.swift
//  iosApp
//
//  Created by Sandy Adikaram on 2/3/2025.
//  Copyright © 2025 orgName. All rights reserved.
//
import Foundation

protocol ViewControllerBinding {
    associatedtype Service
    func setupInterfaceBinding(viewModel: Service)
}
