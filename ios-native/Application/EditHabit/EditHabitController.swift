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

class EditHabitTableViewController: NSObject, UITableViewDataSource, UITableViewDelegate {
    func disclosure(title: String, subtitle: String) -> UITableViewCell {
        let cell = UITableViewCell(style: .value1, reuseIdentifier: nil)
        cell.textLabel?.text = title
        cell.detailTextLabel?.text = subtitle
        cell.accessoryType = .disclosureIndicator
        return cell
    }

    func input(title: String) -> UITableViewCell {
        let cell = UITableViewCell()
        let field = UITextField(frame: cell.bounds.insetBy(dx: 20, dy: 0))
        field.placeholder = title
        field.autoresizingMask = [UIView.AutoresizingMask.flexibleWidth,
                                  UIView.AutoresizingMask.flexibleHeight]
        cell.contentView.addSubview(field)
        return cell
    }

    var primary = [UITableViewCell]()
    var secondary = [UITableViewCell]()
    var parentController: EditHabitController

    init(withParentController parentController: EditHabitController) {
        self.parentController = parentController
        super.init()
        primary.append(input(title: "Name"))
        primary.append(input(title: "Question (e.g. Did you wake up early today?)"))
        secondary.append(disclosure(title: "Color", subtitle: "Blue"))
        secondary.append(disclosure(title: "Repeat", subtitle: "Daily"))
        secondary.append(disclosure(title: "Reminder", subtitle: "Disabled"))
    }

    func numberOfSections(in tableView: UITableView) -> Int {
        return 2
    }

    func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        return section == 0 ? primary.count : secondary.count
    }

    func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
        return indexPath.section == 0 ? primary[indexPath.item] : secondary[indexPath.item]
    }

//    func tableView(_ tableView: UITableView, didSelectRowAt indexPath: IndexPath) {
//        let alert = UIAlertController(title: "Hello", message: "You selected something", preferredStyle: .alert)
//        parentController.present(alert, animated: true)
//    }
}

class EditHabitController: UIViewController {
    override func viewWillAppear(_ animated: Bool) {
        super.viewWillAppear(animated)
        let bounds = UIScreen.main.bounds
        let tableController = EditHabitTableViewController(withParentController: self)
        let table = UITableView(frame: bounds, style: .grouped)
        table.dataSource = tableController
        table.delegate = tableController
        self.view = table
    }

    override func viewDidLoad() {
        self.title = "Edit Habit"
    }
}
