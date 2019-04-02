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

class MainScreenCell : UITableViewCell {
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
        
        for _ in 1...3 {
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
    
    func update(name: String,
                color: Color,
                values: [KotlinInt]) {
        
        label.text = name
        label.textColor = color.uicolor
        
        ring.component = Ring(color: color,
                              percentage: Double.random(in: 0...1),
                              thickness: 2.5,
                              radius: 7,
                              theme: theme,
                              label: false)
        ring.setNeedsDisplay()
        
        for i in 0..<buttons.count {
            buttons[i].component = CheckmarkButton(value: Int32(truncating: values[i]),
                                                   color: color,
                                                   theme: theme)
            buttons[i].setNeedsDisplay()
        }
    }
}

class MainScreenController: UITableViewController, MainScreenDataSourceListener {
    
    var backend: Backend
    var dataSource: MainScreenDataSource
    var data: MainScreenDataSource.Data?
    var theme: Theme
    var nButtons = 3
    var strings: Strings
    
    required init?(coder aDecoder: NSCoder) {
        fatalError()
    }
    
    init(withBackend backend:Backend) {
        self.backend = backend
        self.strings = backend.strings
        self.dataSource = backend.mainScreenDataSource
        self.theme = backend.theme
        super.init(nibName: nil, bundle: nil)
        self.dataSource.observable.addListener(listener: self)
        self.dataSource.requestData()
    }
    
    func onDataChanged(newData: MainScreenDataSource.Data) {
        self.data = newData
    }

    override func viewDidLoad() {
        self.title = strings.main_activity_title
        
        self.navigationItem.rightBarButtonItems = [
            UIBarButtonItem(image: UIImage(named: "ic_more"),
                            style: .plain,
                            target: self,
                            action: #selector(self.onMoreActionsClicked)),
            UIBarButtonItem(barButtonSystemItem: .add,
                            target: self,
                            action: #selector(self.onCreateHabitClicked)),
        ]
        tableView.register(MainScreenCell.self, forCellReuseIdentifier: "cell")
        tableView.backgroundColor = theme.headerBackgroundColor.uicolor
    }
    
    override func viewDidAppear(_ animated: Bool) {
        self.navigationController?.navigationBar.barStyle = .default
        self.navigationController?.navigationBar.tintColor = theme.highContrastTextColor.uicolor
        self.navigationController?.navigationBar.barTintColor = .white
        self.navigationController?.navigationBar.titleTextAttributes = [NSAttributedString.Key.foregroundColor: UIColor.black]
    }
    
    override func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        return data?.habits.count ?? 0
    }
    
    override func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
        let row = indexPath.row
        let cell = tableView.dequeueReusableCell(withIdentifier: "cell", for: indexPath) as! MainScreenCell
        let habit = data!.habits[row]
        cell.update(name: habit.name,
                    color: theme.color(paletteIndex: habit.color.index),
                    values: data!.checkmarkValues[habit]!)
        return cell
    }
    
    override func tableView(_ tableView: UITableView, viewForHeaderInSection section: Int) -> UIView? {
        let component = HabitListHeader(today: LocalDate(year: 2019, month: 3, day: 24),
                                        nButtons: Int32(nButtons),
                                        theme: theme,
                                        fmt: IosLocalDateFormatter())
        return ComponentView(frame: CGRect(x: 0, y: 0, width: 100, height: CGFloat(theme.checkmarkButtonSize)),
                             component: component)
    }
    
    override func tableView(_ tableView: UITableView, heightForHeaderInSection section: Int) -> CGFloat {
        return CGFloat(theme.checkmarkButtonSize)
    }
    
    override func tableView(_ tableView: UITableView, heightForRowAt indexPath: IndexPath) -> CGFloat {
        return CGFloat(theme.checkmarkButtonSize) + 1
    }
    
    override func tableView(_ tableView: UITableView, didSelectRowAt indexPath: IndexPath) {
        let habit = data!.habits[indexPath.row]
        let color = theme.color(paletteIndex: habit.color.index)
        self.navigationController?.pushViewController(DetailScreenController(habit: habit, backend: backend),
                                                      animated: true)
    }
    
    @objc func onCreateHabitClicked() {
        self.navigationController?.pushViewController(EditHabitController(), animated: true)
    }
    
    @objc func onMoreActionsClicked() {
        let alert = UIAlertController(title: nil, message: nil, preferredStyle: .actionSheet)
        alert.addAction(UIAlertAction(title: strings.show_archived, style: .default) {
            (action: UIAlertAction) -> Void in
            // TODO
        })
        alert.addAction(UIAlertAction(title: strings.hide_completed, style: .default) {
            (action: UIAlertAction) -> Void in
            // TODO
        })
        alert.addAction(UIAlertAction(title: strings.night_mode, style: .default) {
            (action: UIAlertAction) -> Void in
            // TODO
        })
        alert.addAction(UIAlertAction(title: strings.help, style: .default) {
            (action: UIAlertAction) -> Void in
            // TODO
        })
        alert.addAction(UIAlertAction(title: strings.about, style: .default) {
            (action: UIAlertAction) -> Void in
            // TODO
        })
        alert.addAction(UIAlertAction(title: strings.cancel, style: .cancel) {
            (action: UIAlertAction) -> Void in
            // Do nothing
        })
        present(alert, animated: true, completion: nil)
    }
}
