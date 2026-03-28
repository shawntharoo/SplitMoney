//
//  ApplicationStartup.swift
//  iosApp
//
//  Created by Sandy Adikaram on 22/12/2024.
//  Copyright © 2024 orgName. All rights reserved.
//
import shared
import UIKit


extension UIResponder {
    func registerApplicationStartup() {
        KoinRuntimeContainer.companion.registerSwiftNativeServices(
            defaultGen: DefaultKt.defaultModule,
            navigationService: {_ in NavigationSwiftService()}
        )
    }
}

