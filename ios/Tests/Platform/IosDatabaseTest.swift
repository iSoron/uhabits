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

class IosDatabaseTest: XCTestCase {
    func testUsage() {
        let databaseOpener = IosDatabaseOpener(withLog: StandardLog())
        let fileOpener = IosFileOpener()

        let dbFile = fileOpener.openUserFile(path: "test.sqlite3")
        if dbFile.exists() {
            dbFile.delete()
        }
        let db = databaseOpener.open(file: dbFile)

        var stmt = db.prepareStatement(sql: "drop table if exists demo")
        stmt.step()
        stmt.finalize()

        stmt = db.prepareStatement(sql: "begin")
        stmt.step()
        stmt.finalize()

        stmt = db.prepareStatement(sql: "create table if not exists demo(key int, value text)")
        stmt.step()
        stmt.finalize()

        stmt = db.prepareStatement(sql: "insert into demo(key, value) values (?, ?)")
        stmt.bindInt(index: 0, value: 42)
        stmt.bindText(index: 1, value: "Hello World")
        stmt.step()
        stmt.finalize()

        stmt = db.prepareStatement(sql: "select * from demo where key > ?")
        stmt.bindInt(index: 0, value: 10)
        var result = stmt.step()
        XCTAssertEqual(result, StepResult.row)
        XCTAssertEqual(stmt.getInt(index: 0), 42)
        XCTAssertEqual(stmt.getText(index: 1), "Hello World")
        result = stmt.step()
        XCTAssertEqual(result, StepResult.done)
        stmt.finalize()

        stmt = db.prepareStatement(sql: "drop table demo")
        stmt.step()
        stmt.finalize()

        stmt = db.prepareStatement(sql: "commit")
        stmt.step()
        stmt.finalize()

        db.close()
        dbFile.delete()
    }
}
