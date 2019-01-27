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

import Foundation

extension String: Error {}

@objc(CoreModule)
class CoreModule: RCTEventEmitter {
  
  func convert(_ obj: Any?) -> Any? {
    if obj is KotlinInt {
      return (obj as! KotlinInt).intValue
    }
    if obj is NSString {
      return obj
    }
    if obj is Dictionary<String, Any> {
      return (obj as! Dictionary<String, Any>).mapValues{ convert($0) }
    }
    if obj is Array<Any> {
      return (obj as! Array<Any>).map { convert($0) }
    }
    return nil
  }
  
  @objc
  open override func supportedEvents() -> [String] {
    return ["onHabitList"]
  }
  
  @objc
  func requestHabitList() {
    DispatchQueue.main.async {
      let result = AppDelegate.backend.getHabitList()
      self.sendEvent(withName: "onHabitList", body: self.convert(result))
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
