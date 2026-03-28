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
    private var localViewmodel: LaunchScreenViewModel?
    private var hasNavigated = false

    override func setupInterfaceBinding(viewModel: LaunchScreenViewModel) {
        self.localViewmodel = viewModel
        view.backgroundColor = .white
        setupSplashUI()
    }

    override func viewDidAppear(_ animated: Bool) {
        super.viewDidAppear(animated)
        autoNavigateToHome()
    }

    private func setupSplashUI() {
        let stack = UIStackView()
        stack.axis = .vertical
        stack.alignment = .center
        stack.spacing = 12
        stack.translatesAutoresizingMaskIntoConstraints = false

        let iconView = UIImageView(image: UIImage(systemName: "dollarsign.circle.fill"))
        iconView.tintColor = .systemGreen
        iconView.contentMode = .scaleAspectFit
        iconView.translatesAutoresizingMaskIntoConstraints = false
        iconView.widthAnchor.constraint(equalToConstant: 82).isActive = true
        iconView.heightAnchor.constraint(equalToConstant: 82).isActive = true

        let titleLabel = UILabel()
        titleLabel.text = "SplitMoney"
        titleLabel.font = .boldSystemFont(ofSize: 32)
        titleLabel.textColor = .label
        titleLabel.textAlignment = .center

        stack.addArrangedSubview(iconView)
        stack.addArrangedSubview(titleLabel)
        view.addSubview(stack)

        NSLayoutConstraint.activate([
            stack.centerXAnchor.constraint(equalTo: view.centerXAnchor),
            stack.centerYAnchor.constraint(equalTo: view.centerYAnchor)
        ])
    }

    private func autoNavigateToHome() {
        if hasNavigated { return }
        hasNavigated = true

        DispatchQueue.main.asyncAfter(deadline: .now() + 1.2) { [weak self] in
            guard let self = self, let viewModel = self.localViewmodel else { return }
            viewModel.navigateToNextScreen()
        }
    }
}
