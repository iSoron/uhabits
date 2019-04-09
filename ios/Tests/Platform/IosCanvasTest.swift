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

class IosCanvasTest : XCTestCase {
    func testDraw() {
        UIGraphicsBeginImageContext(CGSize(width: 500, height: 400))
        let canvas = IosCanvas(withBounds: CGRect(x: 0, y: 0, width: 500, height: 400))
        
        canvas.setColor(color: Color(rgb: 0x303030))
        canvas.fillRect(x: 0.0, y: 0.0, width: 500.0, height: 400.0)
        
        canvas.setColor(color: Color(rgb: 0x606060))
        canvas.setStrokeWidth(size: 25.0)
        canvas.drawRect(x: 100.0, y: 100.0, width: 300.0, height: 200.0)
        
        canvas.setColor(color: Color(rgb: 0xFFFF00))
        canvas.setStrokeWidth(size: 1.0)
        canvas.fillCircle(centerX: 50.0, centerY: 50.0, radius: 30.0)
        canvas.fillArc(centerX: 50.0, centerY: 150.0, radius: 30.0, startAngle: 90.0, swipeAngle: 135.0)
        canvas.fillArc(centerX: 50.0, centerY: 250.0, radius: 30.0, startAngle: 90.0, swipeAngle: -135.0)
        canvas.fillArc(centerX: 50.0, centerY: 350.0, radius: 30.0, startAngle: 45.0, swipeAngle: 90.0)
        canvas.drawRect(x: 0.0, y: 0.0, width: 100.0, height: 100.0)
        canvas.drawRect(x: 0.0, y: 100.0, width: 100.0, height: 100.0)
        canvas.drawRect(x: 0.0, y: 200.0, width: 100.0, height: 100.0)
        canvas.drawRect(x: 0.0, y: 300.0, width: 100.0, height: 100.0)
        
        canvas.setColor(color: Color(rgb: 0xFF0000))
        canvas.setStrokeWidth(size: 2.0)
        canvas.drawLine(x1: 0.0, y1: 0.0, x2: 500.0, y2: 400.0)
        canvas.drawLine(x1: 500.0, y1: 0.0, x2: 0.0, y2: 400.0)
        
        canvas.setFontSize(size: 50.0)
        canvas.setColor(color: Color(rgb: 0x00FF00))
        canvas.drawText(text: "Test", x: 250.0, y: 200.0)
        
        canvas.setFont(font: Font.bold)
        canvas.drawText(text: "Test", x: 250.0, y: 100.0)
        
        canvas.setFont(font: Font.fontAwesome)
        canvas.drawText(text: FontAwesome.Companion().check, x: 250.0, y: 300.0)
        
        let image = UIGraphicsGetImageFromCurrentImageContext()!
        let paths = FileManager.default.urls(for: .documentDirectory, in: .userDomainMask)
        let filePath = paths.first?.appendingPathComponent("IosCanvasTest.png")
        try! image.pngData()!.write(to: filePath!, options: .atomic)
        UIGraphicsEndImageContext()
    }
}
