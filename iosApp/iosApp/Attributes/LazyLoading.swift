//
//  LazyLoading.swift
//  iosApp
//
//  Created by Sandy Adikaram on 2/3/2025.
//  Copyright © 2025 orgName. All rights reserved.
//
import shared

@propertyWrapper
struct LazyLoading<TService> {
    private var dependency : TService?
    
    var wrappedValue: TService? {
        mutating get {
            if dependency == nil {
                let cacheDependency :  TService = inject()
                dependency = cacheDependency
                return cacheDependency
            }
            return dependency
        }
    }
}
