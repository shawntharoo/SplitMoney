//
//  HomeViewController.swift
//  iosApp
//
//  Created by Sandy Adikaram on 7/12/2024.
//  Copyright © 2024 orgName. All rights reserved.
//

import shared
import UIKit

class HomeViewController: UIViewController {
    
    override func viewDidLoad() {
        super.viewDidLoad()
        
        view.backgroundColor = .lightGray
        
        let label = UILabel()
        label.text = "Home Screen"
        label.textAlignment = .center
        label.font = UIFont.systemFont(ofSize: 24)
        label.frame = view.bounds
        
        view.addSubview(label)
    }
    
    
}

