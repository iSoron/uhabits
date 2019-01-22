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
  StyleSheet,
  View,
  ToolbarAndroid,
  StatusBar,
} from 'react-native';
import ListHabitScene from './src/components/ListHabits/index';
import { Colors } from './src/helpers/Colors';

const icAdd = require('./res/images/ic_add.png');
const icFilter = require('./res/images/ic_filter.png');

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: Colors.appBackground,
  },
  toolbar: {
    backgroundColor: Colors.toolbarBackground,
    height: 56,
    alignSelf: 'stretch',
    elevation: 8,
  },
});

function RootComponent() {
  return (
    <View style={styles.container}>
      <ToolbarAndroid
        title="Habits"
        style={styles.toolbar}
        actions={[
          { title: 'Add', icon: icAdd, show: 'always' },
          { title: 'Filter', icon: icFilter, show: 'always' },
          { title: 'Settings', show: 'never' },
        ]}
      />
      <StatusBar
        backgroundColor={Colors.statusBarBackground}
        barStyle={Colors.statusBarStyle}
      />
      <ListHabitScene />
    </View>
  );
}

AppRegistry.registerComponent('LoopHabitTracker', () => RootComponent);
