/*
 * Copyright (C) 2016-2019 Álinson Santos Xavier <isoron@gmail.com>
 *
 * This file is part of Loop Habit Tracker.
 *
 * Loop Habit Tracker is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version.
 *
 * Loop Habit Tracker is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
 * more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program. If not, see <http://www.gnu.org/licenses/>.
 */

import UIKit

@UIApplicationMain class AppDelegate: UIResponder, UIApplicationDelegate, BackendListener {
   
    var window: UIWindow?
    var nav: UINavigationController?
    let log = StandardLog()
    var backend: Backend?
    
    func application(_ application: UIApplication,
                     didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey: Any]?) -> Bool {
        
        backend = Backend(databaseName: "uhabits.db",
                          databaseOpener: IosDatabaseOpener(),
                          fileOpener: IosFileOpener(),
                          localeHelper: IosLocaleHelper(log: log),
                          log: log,
                          scope: UIDispatcher())
        
        backend?.observable.addListener(listener: self)
        backend?.doInit()
        
        window = UIWindow(frame: UIScreen.main.bounds)
        nav = UINavigationController()
        window?.backgroundColor = UIColor.white
        window?.rootViewController = nav
        window?.makeKeyAndVisible()
        
        return true
    }
    
    func onReady() {
        nav?.viewControllers = [MainScreenController(withBackend: backend!)]
    }
}
