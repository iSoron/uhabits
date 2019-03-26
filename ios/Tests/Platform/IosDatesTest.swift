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
import UIKit

@testable import uhabits

class IosDateTimeTest : XCTestCase {
    func testPlusMinusDay() {
        let today = LocalDate(year: 2019, month: 3, day: 25)
        let calc = IosLocalDateCalculator()
        let d1 = calc.minusDays(date: today, days: 28)
        let d2 = calc.plusDays(date: today, days: 7)
        let d3 = calc.plusDays(date: today, days: 42)
        XCTAssert(d1.isEqual(LocalDate(year: 2019, month: 2, day: 25)))
        XCTAssert(d2.isEqual(LocalDate(year: 2019, month: 4, day: 1)))
        XCTAssert(d3.isEqual(LocalDate(year: 2019, month: 5, day: 6)))
    }
}

class IosDateFormatterTest : XCTestCase {
    func testShortMonthName() {
        let fmt = IosLocalDateFormatter()
        let d1 = LocalDate(year: 2019, month: 3, day: 25)
        let d2 = LocalDate(year: 2019, month: 4, day: 4)
        let d3 = LocalDate(year: 2019, month: 5, day: 12)
        XCTAssertEqual(fmt.shortWeekdayName(date: d1), "Mon")
        XCTAssertEqual(fmt.shortWeekdayName(date: d2), "Thu")
        XCTAssertEqual(fmt.shortWeekdayName(date: d3), "Sun")
    }
}
