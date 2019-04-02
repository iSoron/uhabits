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
import SQLite3

internal let SQLITE_STATIC = unsafeBitCast(0, to: sqlite3_destructor_type.self)
internal let SQLITE_TRANSIENT = unsafeBitCast(-1, to: sqlite3_destructor_type.self)

class IosPreparedStatement : NSObject, PreparedStatement {

    
  var db: OpaquePointer
  var statement: OpaquePointer
  
  init(withStatement statement: OpaquePointer, withDb db: OpaquePointer) {
    self.statement = statement
    self.db = db
  }
  
  func step() -> StepResult {
    let result = sqlite3_step(statement)
    if result == SQLITE_ROW {
      return StepResult.row
    } else if result == SQLITE_DONE {
      return StepResult.done
    } else {
      let errMsg = String(cString: sqlite3_errmsg(db)!)
      fatalError("Database error: \(errMsg) (\(result))")
    }
  }
  
  func getInt(index: Int32) -> Int32 {
    return sqlite3_column_int(statement, index)
  }
    
  func getLong(index: Int32) -> Int64 {
    return sqlite3_column_int64(statement, index)
  }
  
  func getText(index: Int32) -> String {
    return String(cString: sqlite3_column_text(statement, index))
  }
  
  func getReal(index: Int32) -> Double {
    return sqlite3_column_double(statement, index)
  }
  
  func bindInt(index: Int32, value: Int32) {
    sqlite3_bind_int(statement, index + 1, value)
  }
  
  func bindText(index: Int32, value: String) {
    sqlite3_bind_text(statement, index + 1, value, -1, SQLITE_TRANSIENT)
  }
  
  func bindReal(index: Int32, value: Double) {
    sqlite3_bind_double(statement, index + 1, value)
  }
  
  func reset() {
    sqlite3_reset(statement)
  }
  
  override func finalize() {
    sqlite3_finalize(statement)
  }
    
  func bindLong(index: Int32, value: Int64) {
    sqlite3_bind_int64(statement, index + 1, value)
  }
}

class IosDatabase : NSObject, Database {
  var db: OpaquePointer
  var log: Log
  
  init(withDb db: OpaquePointer, withLog log: Log) {
    self.db = db
    self.log = log
  }
  
  func prepareStatement(sql: String) -> PreparedStatement {
    if sql.isEmpty {
      fatalError("Provided SQL query is empty")
    }
    var statement : OpaquePointer?
    let result = sqlite3_prepare_v2(db, sql, -1, &statement, nil)
    if result == SQLITE_OK {
      return IosPreparedStatement(withStatement: statement!, withDb: db)
    } else {
      let errMsg = String(cString: sqlite3_errmsg(db)!)
      fatalError("Database error: \(errMsg)")
    }
  }
  
  func close() {
    sqlite3_close(db)
  }
}

class IosDatabaseOpener : NSObject, DatabaseOpener {
  
  var log: Log
  
  init(withLog log: Log) {
    self.log = log
  }
  
  func open(file: UserFile) -> Database {
    let dbPath = (file as! IosUserFile).path
    
    let version = String(cString: sqlite3_libversion())
    log.info(tag: "IosDatabaseOpener", msg: "SQLite \(version)")
    log.info(tag: "IosDatabaseOpener", msg: "Opening database: \(dbPath)")
    var db: OpaquePointer?
    let result = sqlite3_open(dbPath, &db)
    if result == SQLITE_OK {
      return IosDatabase(withDb: db!, withLog: log)
    } else {
      fatalError("Error opening database (code \(result))")
    }
  }
}
