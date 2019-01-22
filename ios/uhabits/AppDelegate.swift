import Foundation

@UIApplicationMain

class AppDelegate: UIResponder, UIApplicationDelegate {
  var window: UIWindow?
  var bridge: RCTBridge!
  static var backend = Backend()
  
  func application(_ application: UIApplication, didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey: Any]?) -> Bool {
    AppDelegate.backend.createHabit(name: "Wake up early")
    AppDelegate.backend.createHabit(name: "Wash clothes")
    AppDelegate.backend.createHabit(name: "Exercise")
    AppDelegate.backend.createHabit(name: "Meditate")
    AppDelegate.backend.createHabit(name: "Take vitamins")
    AppDelegate.backend.createHabit(name: "Write 'the quick brown fox jumps over the lazy dog' daily")
    AppDelegate.backend.createHabit(name: "Write journal")
    AppDelegate.backend.createHabit(name: "Study French")
    
    let jsCodeLocation = RCTBundleURLProvider.sharedSettings().jsBundleURL(forBundleRoot: "index.ios", fallbackResource: nil)
    let rootView = RCTRootView(bundleURL: jsCodeLocation, moduleName: "LoopHabitTracker", initialProperties: nil, launchOptions: launchOptions)
    rootView?.backgroundColor = UIColor(red: 1.0, green: 1.0, blue: 1.0, alpha: 1.0)
    self.window = UIWindow(frame: UIScreen.main.bounds)
    let rootViewController = UIViewController()
    rootViewController.view = rootView
    self.window?.rootViewController = rootViewController
    self.window?.makeKeyAndVisible()
    return true
  }
}
