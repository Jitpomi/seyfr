//
//  AppDelegate.swift
//  Seyfr
//
//  Created by Samson Ssali on 5/5/26.
//

import Foundation
import UIKit

class AppDelegate: NSObject, UIApplicationDelegate {
    func application(
        _ application: UIApplication,
        didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey: Any]? = nil
    ) -> Bool {
        UIView.appearance().tintColor = .label
        return true
    }
}

