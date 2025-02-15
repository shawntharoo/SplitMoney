//
//  SceneDelegate.swift
//  iosApp
//
//  Created by Sandy Adikaram on 15/2/2025.
//  Copyright © 2025 orgName. All rights reserved.
//

import UIKit

class SceneDelegate: UIResponder, UIWindowSceneDelegate {

    var window: UIWindow?

    func scene(
        _ scene: UIScene,
        willConnectTo session: UISceneSession,
        options connectionOptions: UIScene.ConnectionOptions
    ) {
        guard let windowScene = (scene as? UIWindowScene) else { return }
        
        window = UIWindow(windowScene: windowScene)
        window?.rootViewController = ViewController() // Set your main screen
        window?.makeKeyAndVisible()
        
        print("Scene Connected")
    }

    func sceneDidDisconnect(_ scene: UIScene) {
        print("Scene Disconnected")
    }

    func sceneDidBecomeActive(_ scene: UIScene) {
        print("Scene Became Active")
    }

    func sceneWillResignActive(_ scene: UIScene) {
        print("Scene Will Resign Active")
    }

    func sceneWillEnterForeground(_ scene: UIScene) {
        print("Scene Will Enter Foreground")
    }

    func sceneDidEnterBackground(_ scene: UIScene) {
        print("Scene Entered Background")
    }
}
