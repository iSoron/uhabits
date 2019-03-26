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

import XCTest
@testable import uhabits

class IosFilesTest: XCTestCase {
  func testResourceFiles() {
    let fileOpener = IosFileOpener()
    let file = fileOpener.openResourceFile(filename: "migrations/010.sql")
    let lines = file.readLines()
    XCTAssertEqual(lines[0], "delete from Score")
  }
  
  func testUserFiles() throws {
    let fm = FileManager.default
    let root = try fm.url(for: .documentDirectory, in: .userDomainMask, appropriateFor: nil, create: false).path
    let path = "\(root)/test.txt"
    print(path)
    fm.createFile(atPath: path, contents: "Hello world\nThis is line 2".data(using: .utf8), attributes: nil)
    
    let fileOpener = IosFileOpener()
    let file = fileOpener.openUserFile(filename: "test.txt")
    XCTAssertTrue(file.exists())
    
    file.delete()
    XCTAssertFalse(file.exists())
  }
}
