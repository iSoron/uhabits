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

import React from 'react';
import {
  AppRegistry,
  NavigatorIOS,
} from 'react-native';
import ListHabitsScene from './src/components/ListHabits/index';
import EditHabitScene from './src/components/EditHabit/index';

let navigator;

const routes = {
  index: {
    component: ListHabitsScene,
    title: 'Habits',
    rightButtonSystemIcon: 'add',
    onRightButtonPress: () => navigator.push(routes.newHabit),
    passProps: {
      onClickHabit: () => navigator.push(routes.newHabit),
      onClickCheckmark: () => {},
    },
  },
  newHabit: {
    component: EditHabitScene,
    title: 'New Habit',
    leftButtonTitle: 'Cancel',
    rightButtonTitle: 'Save',
    onLeftButtonPress: () => navigator.pop(),
    onRightButtonPress: () => navigator.pop(),
  },
};


class RootComponent extends React.Component {
  render() {
    return (
      <NavigatorIOS
        ref={(c) => { navigator = c; }}
        translucent={false}
        initialRoute={routes.index}
        style={{ flex: 1 }}
      />
    );
  }
}

AppRegistry.registerComponent('LoopHabitTracker', () => RootComponent);
