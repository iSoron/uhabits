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

class IosResourceFile : NSObject, ResourceFile {
    
    var path: String
    var fileManager = FileManager.default
    
    init(forPath path: String) {
        self.path = path
    }
    
    func readLines() -> [String] {
        do {
            let contents = try String(contentsOfFile: self.path, encoding: .utf8)
            return contents.components(separatedBy: CharacterSet.newlines)
        } catch {
            return ["ERROR"]
        }
    }
}

class IosUserFile : NSObject, UserFile {
    
    var path: String
    
    init(forPath path: String) {
        self.path = path
    }
    
    func delete() {
        do {
            try FileManager.default.removeItem(atPath: path)
        } catch {
            
        }
    }
    
    func exists() -> Bool {
        return FileManager.default.fileExists(atPath: path)
    }
}

class IosFileOpener : NSObject, FileOpener {
    func openResourceFile(filename: String) -> ResourceFile {
        let path = "\(Bundle.main.resourcePath!)/\(filename)"
        return IosResourceFile(forPath: path)
    }
    
    func openUserFile(filename: String) -> UserFile {
        do {
            let root = try FileManager.default.url(for: .documentDirectory,
                                                   in: .userDomainMask,
                                                   appropriateFor: nil,
                                                   create: false).path
            return IosUserFile(forPath: "\(root)/\(filename)")
        } catch {
            return IosUserFile(forPath: "invalid")
        }
    }
}
