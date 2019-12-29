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

class DetailScreenController : UITableViewController {
    
    let theme: Theme
    let habit: Habit
    let color: Color
    var cells = [UITableViewCell]()
    
    required init?(coder aDecoder: NSCoder) {
        fatalError()
    }
    
    init(habit: Habit, backend: Backend) {
        self.theme = backend.theme
        self.habit = habit
        self.color = theme.color(paletteIndex: self.habit.color.index)
        super.init(style: .grouped)
    }
    
    override func viewDidLoad() {
        self.title = habit.name
        self.navigationItem.rightBarButtonItem = UIBarButtonItem(barButtonSystemItem: .edit,
                                                                 target: self,
                                                                 action: #selector(self.onEditHabitClicked))
        cells.append(buildBarChartCell())
        cells.append(buildHistoryChartCell())
    }
    
    func buildBarChartCell() -> UITableViewCell {
        let today = LocalDate(year: 2019, month: 3, day: 15)
        let axis = (0...365).map { today.minus(days: $0) }
        let component = BarChart(theme: theme,
                                 dateFormatter: IosLocalDateFormatter())
        component.axis = axis
        let cell = UITableViewCell()
        let view = ComponentView(frame: cell.frame, component: component)
        for k in 0...0 {
            var series = [KotlinDouble]()
            for _ in 1...365 {
                series.append(KotlinDouble(value: Double.random(in: 0...5000)))
            }
            component.series.add(series)
            let color = (self.habit.color.index + Int32(k * 3)) % 16
            component.colors.add(theme.color(paletteIndex: color))
        }
        view.autoresizingMask = [.flexibleWidth, .flexibleHeight]
        cell.contentView.addSubview(view)
        return cell
    }
    
    func buildHistoryChartCell() -> UITableViewCell {
        let component = CalendarChart(today: LocalDate(year: 2019, month: 3, day: 15),
                                      color: color,
                                      theme: theme,
                                      dateFormatter: IosLocalDateFormatter())
        let cell = UITableViewCell()
        let view = ComponentView(frame: cell.frame, component: component)
        var series = [KotlinDouble]()
        for _ in 1...365 {
            series.append(KotlinDouble(value: Double.random(in: 0...1)))
        }
        component.series = series
        view.autoresizingMask = [.flexibleWidth, .flexibleHeight]
        cell.contentView.addSubview(view)
        return cell
    }
    
    override func viewWillAppear(_ animated: Bool) {
        super.viewWillAppear(animated)
        self.navigationController?.navigationBar.barStyle = .blackOpaque
        self.navigationController?.navigationBar.barTintColor = color.uicolor
        self.navigationController?.navigationBar.tintColor = .white
        self.navigationController?.navigationBar.titleTextAttributes = [NSAttributedString.Key.foregroundColor: UIColor.white]
    }
    
    override func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        return 1
    }
    
    override func numberOfSections(in tableView: UITableView) -> Int {
        return cells.count
    }
    
    override func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
        return cells[indexPath.section]
    }
    
    @objc func onEditHabitClicked() {
        self.navigationController?.pushViewController(EditHabitController(), animated: true)
    }
    
    override func tableView(_ tableView: UITableView, heightForRowAt indexPath: IndexPath) -> CGFloat {
        return 200
    }
}
