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

extension LocalDate {
    var iosDate : Date {
        let calendar = Calendar(identifier: .gregorian)
        var dc = DateComponents()
        dc.year = Int(self.year)
        dc.month = Int(self.month)
        dc.day = Int(self.day)
        dc.hour = 13
        dc.minute = 0
        return calendar.date(from: dc)!
    }
}

extension Date {
    var localDate : LocalDate {
        let calendar = Calendar(identifier: .gregorian)
        return LocalDate(year: Int32(calendar.component(.year, from: self)),
                         month: Int32(calendar.component(.month, from: self)),
                         day: Int32(calendar.component(.day, from: self)))
    }
}

class IosLocalDateFormatter : NSObject, LocalDateFormatter {
    let fmt = DateFormatter()
    
    func shortMonthName(date: LocalDate) -> String {
        fmt.dateFormat = "MMM"
        return fmt.string(from: date.iosDate)
    }
    
    func shortWeekdayName(date: LocalDate) -> String {
        fmt.dateFormat = "EEE"
        return fmt.string(from: date.iosDate)
    }
}

class IosLocalDateCalculator : NSObject, LocalDateCalculator {
    func toTimestamp(date: LocalDate) -> Timestamp {
        return Timestamp(unixTimeInMillis: Int64(date.iosDate.timeIntervalSince1970 * 1000))
    }
    
    func fromTimestamp(timestamp: Timestamp) -> LocalDate {
        return Date.init(timeIntervalSince1970: Double(timestamp.unixTimeInMillis / 1000)).localDate
    }
    
    let calendar = Calendar(identifier: .gregorian)

    func dayOfWeek(date: LocalDate) -> DayOfWeek {
        let weekday = calendar.component(.weekday, from: date.iosDate)
        switch(weekday) {
            case 1: return DayOfWeek.sunday
            case 2: return DayOfWeek.monday
            case 3: return DayOfWeek.tuesday
            case 4: return DayOfWeek.wednesday
            case 5: return DayOfWeek.thursday
            case 6: return DayOfWeek.friday
            default: return DayOfWeek.saturday
        }
    }
    
    func plusDays(date: LocalDate, days: Int32) -> LocalDate {
        let d2 = date.iosDate.addingTimeInterval(24.0 * 60 * 60 * Double(days))
        return LocalDate(year: Int32(calendar.component(.year, from: d2)),
                         month: Int32(calendar.component(.month, from: d2)),
                         day: Int32(calendar.component(.day, from: d2)))
    }
}
