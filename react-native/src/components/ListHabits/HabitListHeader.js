/*
 * Copyright (C) 2016-2019 Álinson Santos Xavier <isoron@gmail.com>
 * Copyright (C) 2019 Javier Artiles
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
import { View, StyleSheet, Text } from 'react-native';
import { Colors } from '../../helpers/Colors';

const styles = StyleSheet.create({
  container: {
    height: 50,
    paddingRight: 1,
    backgroundColor: Colors.headerBackground,
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'flex-end',
    elevation: 4,
    borderBottomColor: Colors.headerBorderColor,
    borderBottomWidth: StyleSheet.hairlineWidth,
  },
  column: {
    width: 44,
    alignItems: 'center',
  },
  text: {
    color: Colors.headerTextColor,
    fontWeight: 'bold',
  },
  dayName: {
    fontSize: 10,
  },
  dayNumber: {
    fontSize: 12,
  },
});

export default class HabitListHeader extends React.Component {
  static renderColumn(dayName, dayNumber) {
    return (
      <View
        key={dayNumber}
        style={styles.column}
      >
        <Text style={[styles.text, styles.dayName]}>
          {dayName.toUpperCase()}
        </Text>
        <Text style={[styles.text, styles.dayNumber]}>
          {dayNumber}
        </Text>
      </View>
    );
  }

  static renderColumns() {
    return [
      {
        dayName: 'Sat',
        dayNumber: '5',
      },
      {
        dayName: 'Fri',
        dayNumber: '4',
      },
      {
        dayName: 'Thu',
        dayNumber: '3',
      },
      {
        dayName: 'Wed',
        dayNumber: '2',
      },

    ].map((day) => {
      const { dayName, dayNumber } = day;
      return HabitListHeader.renderColumn(dayName, dayNumber);
    });
  }

  render() {
    return (
      <View style={styles.container}>
        {HabitListHeader.renderColumns()}
      </View>
    );
  }
}
