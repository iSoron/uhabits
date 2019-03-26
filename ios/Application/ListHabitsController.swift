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

class ListHabitsCell : UITableViewCell {
    var ring: ComponentView
    var label = UILabel()
    var buttons: [ComponentView] = []
    var theme = LightTheme()
    
    override init(style: UITableViewCell.CellStyle, reuseIdentifier: String?) {
        ring = ComponentView(frame: CGRect(), component: nil)
        
        super.init(style: .default, reuseIdentifier: reuseIdentifier)
        let size = CGFloat(theme.checkmarkButtonSize)
        
        let stack = UIStackView(frame: contentView.frame)
        stack.autoresizingMask = [.flexibleWidth, .flexibleHeight]
        stack.backgroundColor = .red
        stack.axis = .horizontal
        stack.distribution = .fill
        stack.alignment = .center
        contentView.addSubview(stack)

        ring.backgroundColor = .white
        ring.widthAnchor.constraint(equalToConstant: size * 0.75).isActive = true
        ring.heightAnchor.constraint(equalToConstant: size).isActive = true
        stack.addArrangedSubview(ring)
        
        label.backgroundColor = .white
        label.heightAnchor.constraint(equalToConstant: size).isActive = true
        stack.addArrangedSubview(label)
        
        for _ in 1...4 {
            let btn = ComponentView(frame: frame, component: nil)
            btn.backgroundColor = .white
            btn.widthAnchor.constraint(equalToConstant: size).isActive = true
            btn.heightAnchor.constraint(equalToConstant: size).isActive = true
            buttons.append(btn)
            stack.addArrangedSubview(btn)
        }
    }
    required init?(coder aDecoder: NSCoder) {
        fatalError()
    }
    
    func setColor(_ color: Color) {
        label.textColor = color.uicolor
        ring.component = Ring(color: color,
                              percentage: Double.random(in: 0...1),
                              thickness: 2.5,
                              radius: 7,
                              theme: theme,
                              label: false)
        ring.setNeedsDisplay()
        let isNumerical = Int.random(in: 1...4) == 1
        for btn in buttons {
            if isNumerical {
                btn.component = NumberButton(color: color,
                                             value: Double.random(in: 0...5000),
                                             threshold: 2000,
                                             units: "steps",
                                             theme: theme)
            } else {
                btn.component = CheckmarkButton(value: Int32.random(in: 0...2),
                                                color: color,
                                                theme: theme)
            }
            btn.setNeedsDisplay()
        }
    }
}

class ListHabitsController: UITableViewController {
    var backend: Backend
    var habits: [[String: Any]]
    var theme: Theme
    
    required init?(coder aDecoder: NSCoder) {
        fatalError()
    }
    
    init(withBackend backend:Backend) {
        self.backend = backend
        self.habits = backend.getHabitList()
        self.theme = backend.theme
        super.init(nibName: nil, bundle: nil)
    }

    override func viewDidLoad() {
        self.title = "Habits"
        self.navigationItem.rightBarButtonItem = UIBarButtonItem(barButtonSystemItem: .add,
                                                                 target: self,
                                                                 action: #selector(self.onCreateHabitClicked))
        tableView.register(ListHabitsCell.self, forCellReuseIdentifier: "cell")
        tableView.backgroundColor = theme.headerBackgroundColor.uicolor
    }

    @objc func onCreateHabitClicked() {
        self.navigationController?.pushViewController(EditHabitController(), animated: true)
    }
    
    override func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        return habits.count
    }
    
    override func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
        let row = indexPath.row
        let cell = tableView.dequeueReusableCell(withIdentifier: "cell", for: indexPath) as! ListHabitsCell
        let color = theme.color(paletteIndex: habits[row]["color"] as! Int32)
        cell.label.text = habits[row]["name"] as? String
        cell.setColor(color)
        return cell
    }
    
    override func tableView(_ tableView: UITableView, viewForHeaderInSection section: Int) -> UIView? {
        let component = HabitListHeader(today: LocalDate(year: 2019, month: 3, day: 24),
                                        nButtons: 4,
                                        theme: theme,
                                        fmt: IosLocalDateFormatter(),
                                        calc: IosLocalDateCalculator())
        return ComponentView(frame: CGRect(x: 0, y: 0, width: 100, height: CGFloat(theme.checkmarkButtonSize)),
                             component: component)
    }
    
    override func tableView(_ tableView: UITableView, heightForHeaderInSection section: Int) -> CGFloat {
        return CGFloat(theme.checkmarkButtonSize)
    }
    
    override func tableView(_ tableView: UITableView, heightForRowAt indexPath: IndexPath) -> CGFloat {
        return CGFloat(theme.checkmarkButtonSize) + 1
    }
    
}
