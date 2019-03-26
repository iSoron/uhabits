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

class IosLocalDateFormatter : NSObject, LocalDateFormatter {
    func shortWeekdayName(date: LocalDate) -> String {
        let calendar = Calendar(identifier: .gregorian)
        var dc = DateComponents()
        dc.year = Int(date.year)
        dc.month = Int(date.month)
        dc.day = Int(date.day)
        dc.hour = 13
        dc.minute = 0
        let d = calendar.date(from: dc)!
        let fmt = DateFormatter()
        fmt.dateFormat = "EEE"
        return fmt.string(from: d)
    }
}

class IosLocalDateCalculator : NSObject, LocalDateCalculator {
    func plusDays(date: LocalDate, days: Int32) -> LocalDate {
        let calendar = Calendar(identifier: .gregorian)
        var dc = DateComponents()
        dc.year = Int(date.year)
        dc.month = Int(date.month)
        dc.day = Int(date.day)
        dc.hour = 13
        dc.minute = 0
        let d1 = calendar.date(from: dc)!
        let d2 = d1.addingTimeInterval(24.0 * 60 * 60 * Double(days))
        return LocalDate(year: Int32(calendar.component(.year, from: d2)),
                         month: Int32(calendar.component(.month, from: d2)),
                         day: Int32(calendar.component(.day, from: d2)))
    }
    
    func minusDays(date: LocalDate, days: Int32) -> LocalDate {
        return plusDays(date: date, days: -days)
    }
}
