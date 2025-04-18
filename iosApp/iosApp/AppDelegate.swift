//
//  AppDelegate.swift
//  iosApp
//
//  Created by Sandy Adikaram on 15/2/2025.
//  Copyright © 2025 orgName. All rights reserved.
//

import UIKit

@main
class AppDelegate: UIResponder, UIApplicationDelegate {

    func application(
        _ application: UIApplication,
        didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey: Any]?
    ) -> Bool {
        print("App Launched")
        registerApplicationStartup()
        return true
    }

    // Required when using SceneDelegate
    func application(_ application: UIApplication, configurationForConnecting connectingSceneSession: UISceneSession, options: UIScene.ConnectionOptions) -> UISceneConfiguration {
        print("✅ AppDelegate: Creating Scene Configuration")
        return UISceneConfiguration(name: "Default Configuration", sessionRole: connectingSceneSession.role)
    }

    func application(_ application: UIApplication, didDiscardSceneSessions sceneSessions: Set<UISceneSession>) {
        print("Scene Discarded")
    }
}
