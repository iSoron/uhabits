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
    var ring = ComponentView(frame: CGRect(), component: nil)
    var label = UILabel()
    var buttons: [ComponentView] = []
    
    override init(style: UITableViewCell.CellStyle, reuseIdentifier: String?) {
        super.init(style: .default, reuseIdentifier: reuseIdentifier)
    }
    required init?(coder aDecoder: NSCoder) {
        fatalError()
    }
    
    func update(habit: Habit, checkmarks: [Checkmark], score: Score, theme: Theme, nButtons: Int) {
        if buttons.count != nButtons {
            buttons.removeAll()
            for v in contentView.subviews { v.removeFromSuperview() }
            
            let size = CGFloat(theme.checkmarkButtonSize)
            let stack = UIStackView(frame: contentView.frame)
            stack.autoresizingMask = [.flexibleWidth, .flexibleHeight]
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
            
            for _ in 1...nButtons {
                let btn = ComponentView(frame: frame, component: nil)
                btn.backgroundColor = .white
                btn.widthAnchor.constraint(equalToConstant: size).isActive = true
                btn.heightAnchor.constraint(equalToConstant: size).isActive = true
                buttons.append(btn)
                stack.addArrangedSubview(btn)
            }
        }
        
        var color = theme.color(paletteIndex: habit.color.index)
        if habit.isArchived { color = theme.mediumContrastTextColor }
        label.text = habit.name
        label.textColor = color.uicolor
        ring.component = Ring(color: color,
                              percentage: score.value,
                              thickness: 2.5,
                              radius: 7,
                              theme: theme,
                              label: false)
        ring.setNeedsDisplay()
        
        for i in 0..<buttons.count {
            if habit.type == HabitType.numericalHabit {
                buttons[i].component = NumberButton(color: color,
                                                    value: Double(checkmarks[i].value) / 1000.0,
                                                    threshold: habit.target,
                                                    units: habit.unit,
                                                    theme: theme)
            } else {
                buttons[i].component = CheckmarkButton(value: checkmarks[i].value,
                                                       color: color,
                                                       theme: theme)
            }
            buttons[i].setNeedsDisplay()
        }
    }
}

class MainScreenController: UITableViewController, MainScreenDataSourceListener {
    
    var backend: Backend
    var dataSource: MainScreenDataSource
    var data: MainScreenDataSource.Data?
    var preferences: Preferences
    var theme: Theme
    var nButtons = 3
    var strings = Strings()
    
    required init?(coder aDecoder: NSCoder) {
        fatalError()
    }
    
    init(withBackend backend:Backend) {
        self.backend = backend
        self.strings = backend.strings
        self.dataSource = backend.mainScreenDataSource
        self.theme = backend.theme
        self.preferences = backend.preferences
        super.init(nibName: nil, bundle: nil)
        self.dataSource.observable.addListener(listener: self)
        self.dataSource.requestData()
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
        computeNumberOfButtons(Double(view.frame.width))
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
        let cell = tableView.dequeueReusableCell(withIdentifier: "cell", for: indexPath) as! MainScreenCell
        let habit = data!.habits[indexPath.row]
        cell.update(habit: habit,
                    checkmarks: data!.checkmarks[habit]!,
                    score: data!.scores[habit]!,
                    theme: theme,
                    nButtons: nButtons)
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
        return CGFloat(theme.checkmarkButtonSize) + 3
    }
    
    override func tableView(_ tableView: UITableView, didSelectRowAt indexPath: IndexPath) {
        let habit = data!.habits[indexPath.row]
        self.navigationController?.pushViewController(DetailScreenController(habit: habit, backend: backend), animated: true)
    }
    
    override func viewWillTransition(to size: CGSize, with coordinator: UIViewControllerTransitionCoordinator) {
        computeNumberOfButtons(Double(size.width))
        reload()
    }
    
    @objc func onCreateHabitClicked() {
        self.navigationController?.pushViewController(EditHabitController(), animated: true)
    }
    
    @objc func onMoreActionsClicked() {
        let alert = UIAlertController(title: nil, message: nil, preferredStyle: .actionSheet)

        if isThereAnyArchivedHabit() {
            if preferences.showArchived {
                alert.addAction(UIAlertAction(title: strings.hide_archived, style: .default) {
                    (action: UIAlertAction) -> Void in
                    self.preferences.showArchived = false
                    self.dataSource.requestData()
                })
            } else {
                alert.addAction(UIAlertAction(title: strings.show_archived, style: .default) {
                    (action: UIAlertAction) -> Void in
                    self.preferences.showArchived = true
                    self.dataSource.requestData()
                })
            }
        }

        if preferences.showCompleted {
            alert.addAction(UIAlertAction(title: strings.hide_completed, style: .default) {
                (action: UIAlertAction) -> Void in
                self.preferences.showCompleted = false
                self.dataSource.requestData()
            })
        } else {
            alert.addAction(UIAlertAction(title: strings.show_completed, style: .default) {
                (action: UIAlertAction) -> Void in
                self.preferences.showCompleted = true
                self.dataSource.requestData()
            })
        }

        if preferences.nightMode {
            alert.addAction(UIAlertAction(title: strings.day_mode, style: .default) {
                (action: UIAlertAction) -> Void in
                self.preferences.nightMode = false
            })
        } else {
            alert.addAction(UIAlertAction(title: strings.night_mode, style: .default) {
                (action: UIAlertAction) -> Void in
                self.preferences.nightMode = true
            })
        }

        alert.addAction(UIAlertAction(title: strings.help, style: .default) {
            (action: UIAlertAction) -> Void in
            if let link = URL(string: "http://loophabits.org/faq") {
                UIApplication.shared.open(link)
            }
        })
        alert.addAction(UIAlertAction(title: strings.about, style: .default) {
            (action: UIAlertAction) -> Void in
            self.navigationController?.pushViewController(AboutScreenController(), animated: true)
        })
        alert.addAction(UIAlertAction(title: strings.cancel, style: .cancel) {
            (action: UIAlertAction) -> Void in
            // Do nothing
        })
        present(alert, animated: true, completion: nil)
    }
    
    func onDataChanged(newData: MainScreenDataSource.Data) {
        data = newData
        reload()
    }
    
    func computeNumberOfButtons(_ width: Double) {
        nButtons = Int((width - 220) / theme.checkmarkButtonSize)
        nButtons = max(nButtons, 3)
        nButtons = min(nButtons, Int(dataSource.maxNumberOfButtons))
    }
    
    func reload() {
        let sections = NSIndexSet(indexesIn: NSMakeRange(0, self.tableView.numberOfSections))
        tableView.reloadSections(sections as IndexSet, with: .automatic)
    }
    
    func isThereAnyArchivedHabit() -> Bool {
        return data!.habits.filter({ $0.isArchived }).count > 0
    }
}
