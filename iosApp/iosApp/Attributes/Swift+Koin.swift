//
//  Swift+Koin.swift
//  iosApp
//
//  Created by Sandy Adikaram on 2/3/2025.
//  Copyright © 2025 orgName. All rights reserved.
//

import shared
import Foundation

class SwiftKClass<T>: NSObject, KotlinKClass {
    func isInstance(value: Any?) -> Bool {
        value is T
    }
    var qualifiedName: String? { String(reflecting: T.self) }
    var simpleName: String? { String(describing: T.self) }
}

extension Koin_coreScope {
    func get<T>() -> T {
        get(clazz: KClass(for: T.self), qualifier: nil, parameters: nil) as! T
    }
}

func KClass<T>(for type: T.Type) -> KotlinKClass {
    SwiftType(type: type, swiftClazz: SwiftKClass<T>()).getClazz()
}

func inject<T>(qualifier: Koin_coreQualifier? = nil, parameters: (() -> Koin_coreParametersHolder)? = nil) -> T {
    KoinGetKt.koinGet(clazz: KClass(for: T.self), qualifier: qualifier, parameters: parameters) as! T
}
