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

import UIKit

class ComponentView : UIView {
    var component: Component?
    
    init(frame: CGRect, component: Component?) {
        self.component = component
        super.init(frame: frame)
    }
    
    required init?(coder aDecoder: NSCoder) {
        fatalError()
    }
    
    override func draw(_ rect: CGRect) {
        let canvas = IosCanvas(withBounds: bounds)
        component?.draw(canvas: canvas)
    }
}

class IosCanvas : NSObject, Canvas {
    func fillArc(centerX: Double, centerY: Double, radius: Double, startAngle: Double, swipeAngle: Double) {
        let center = CGPoint(x: CGFloat(centerX), y: CGFloat(centerY))
        let a1 = startAngle / 180 * .pi * (-1)
        let a2 = a1 - swipeAngle / 180 * .pi
        self.ctx.beginPath()
        self.ctx.move(to: center)
        self.ctx.addArc(center: center,
                        radius: CGFloat(radius),
                        startAngle: CGFloat(a1),
                        endAngle: CGFloat(a2),
                        clockwise: swipeAngle >= 0)
        self.ctx.closePath()
        self.ctx.fillPath()
    }
    
    func fillCircle(centerX: Double, centerY: Double, radius: Double) {
        self.ctx.fillEllipse(in: CGRect(x: CGFloat(centerX - radius),
                                        y: CGFloat(centerY - radius),
                                        width: CGFloat(radius * 2),
                                        height: CGFloat(radius * 2)))
    }
    
    var bounds: CGRect
    var ctx: CGContext
    
    var font = Font.regular
    var textSize = CGFloat(12)
    var textColor = UIColor.black
    
    init(withBounds bounds: CGRect) {
        self.bounds = bounds
        self.ctx = UIGraphicsGetCurrentContext()!
    }
    
    func setColor(color: Color) {
        self.ctx.setStrokeColor(color.cgcolor)
        self.ctx.setFillColor(color.cgcolor)
        textColor = color.uicolor
    }
    
    func drawLine(x1: Double, y1: Double, x2: Double, y2: Double) {
        self.ctx.move(to: CGPoint(x: CGFloat(x1), y: CGFloat(y1)))
        self.ctx.addLine(to: CGPoint(x: CGFloat(x2), y: CGFloat(y2)))
        self.ctx.strokePath()
        
    }
    
    func drawText(text: String, x: Double, y: Double) {
        let nsText = text as NSString
        
        var uifont = UIFont.systemFont(ofSize: textSize)
        if font == Font.bold {
            uifont = UIFont.boldSystemFont(ofSize: textSize)
        }
        if font == Font.fontAwesome {
            uifont = UIFont(name: "FontAwesome", size: textSize)!
        }
        
        let attrs = [NSAttributedString.Key.font: uifont,
                     NSAttributedString.Key.foregroundColor: textColor]

        let size = nsText.size(withAttributes: attrs)
        nsText.draw(at: CGPoint(x: CGFloat(x) - size.width / 2,
                                y : CGFloat(y) - size.height / 2),
                    withAttributes: attrs)
    }
    
    func drawRect(x: Double, y: Double, width: Double, height: Double) {
        self.ctx.stroke(CGRect(x: CGFloat(x),
                               y: CGFloat(y),
                               width: CGFloat(width),
                               height: CGFloat(height)))
    }
    
    func fillRect(x: Double, y: Double, width: Double, height: Double) {
        self.ctx.fill(CGRect(x: CGFloat(x),
                             y: CGFloat(y),
                             width: CGFloat(width),
                             height: CGFloat(height)))
    }
    
    func getHeight() -> Double {
        return Double(bounds.height)
    }
    
    func getWidth() -> Double {
        return Double(bounds.width)
    }
    
    func setTextSize(size: Double) {
        self.textSize = CGFloat(size)
    }
    
    func setFont(font: Font) {
        self.font = font
    }
    
    func setStrokeWidth(size: Double) {
        self.ctx.setLineWidth(CGFloat(size))
    }
}
