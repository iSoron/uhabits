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
  StyleSheet,
  TextInput,
  View,
  Text,
  ScrollView,
  TouchableOpacity,
  TouchableHighlight,
} from 'react-native';
import FontAwesome from '../../helpers/FontAwesome';
import { Colors } from '../../helpers/Colors';
import ColorCircle from '../common/ColorCircle';

const styles = StyleSheet.create({
  container: {
    backgroundColor: Colors.appBackground,
    flex: 1,
  },
  item: {
    fontSize: 17,
    paddingTop: 15,
    paddingBottom: 15,
    paddingRight: 15,
    paddingLeft: 15,
    flex: 1,
    flexDirection: 'row',
    justifyContent: 'center',
    alignItems: 'center',
    backgroundColor: '#fff',
  },
  label: {
    fontSize: 17,
    flex: 1,
  },
  value: {
    fontSize: 17,
  },
  multiline: {
  },
  middle: {
    borderBottomColor: Colors.headerBorderColor,
    borderBottomWidth: StyleSheet.hairlineWidth,
  },
  section: {
    backgroundColor: Colors.appBackground,
    marginTop: 30,
    borderTopColor: Colors.headerBorderColor,
    borderTopWidth: StyleSheet.hairlineWidth,
    borderBottomColor: Colors.headerBorderColor,
    borderBottomWidth: StyleSheet.hairlineWidth,
  },
  icon: {
    fontFamily: 'FontAwesome',
    color: Colors.unchecked,
    marginLeft: 10,
    fontSize: 12,
    paddingTop: 2,
  },
  text: {
    borderWidth: 1,
    padding: 25,
    backgroundColor: '#fff',
  },
});

export default class EditHabitsScene extends React.Component {
  render() {
    return (
      <ScrollView style={styles.container}>
        <View style={styles.section}>
          <TextInput
            autoFocus
            style={[styles.item, styles.middle, { color: Colors[1] }]}
            placeholder="Name"
          />
          <TextInput
            style={[styles.item]}
            placeholder="Question (e.g. Did you exercise today?)"
            multiline
          />
        </View>
        <View style={styles.section}>
          <TouchableHighlight onPress={() => {}}>
            <View style={[styles.item, styles.middle]}>
              <Text style={styles.label}>Color</Text>
              <ColorCircle size={20} color={Colors[1]} />
              <Text style={styles.icon}>{FontAwesome.chevronRight}</Text>
            </View>
          </TouchableHighlight>
          <TouchableHighlight onPress={() => {}}>
            <View style={[styles.item, styles.middle]}>
              <Text style={styles.label}>Repeat</Text>
              <Text style={styles.value}>Every Day</Text>
              <Text style={styles.icon}>{FontAwesome.chevronRight}</Text>
            </View>
          </TouchableHighlight>
          <TouchableHighlight onPress={() => {}}>
            <View style={[styles.item, styles.middle]}>
              <Text style={styles.label}>Reminder</Text>
              <Text style={styles.value}>12:30</Text>
              <Text style={styles.icon}>{FontAwesome.chevronRight}</Text>
            </View>
          </TouchableHighlight>
        </View>
      </ScrollView>
    );
  }
}
