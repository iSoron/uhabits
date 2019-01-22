import Foundation

@objc(CoreModule)
class CoreModule: RCTEventEmitter {
  
  @objc
  open override func supportedEvents() -> [String] {
    return ["onHabitList"]
  }
  
  @objc
  func requestHabitList() {
    DispatchQueue.main.async {
      let habits = AppDelegate.backend.getHabitList()
      let result = habits.map {
        ["key": String($0.key.intValue),
         "name": $0.value["name"],
         "color": $0.value["color"]]
      }
      self.sendEvent(withName: "onHabitList", body: result)
    }
  }
  
  @objc
  func createHabit(_ name: String) {
    DispatchQueue.main.async {
      AppDelegate.backend.createHabit(name: name)
    }
  }
  
  @objc
  func deleteHabit(_ id: Int32) {
    DispatchQueue.main.async {
      AppDelegate.backend.deleteHabit(id: id)
    }
  }
  
  @objc
  func updateHabit(_ id: Int32, _ name: String) {
    DispatchQueue.main.async {
      AppDelegate.backend.updateHabit(id: id, name: name)
    }
  }
  
  @objc
  override static func requiresMainQueueSetup() -> Bool {
    return true
  }
}
