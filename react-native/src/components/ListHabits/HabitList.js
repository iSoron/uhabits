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
  FlatList,
  StyleSheet,
  Text,
  View,
} from 'react-native';
import { Colors } from '../../helpers/Colors';
import { Emitter, Backend } from '../../helpers/Backend';
import Ring from '../common/Ring';
import CheckmarkButton from './CheckmarkButton';

const styles = StyleSheet.create({
  item: {
    backgroundColor: Colors.itemBackground,
    padding: 1,
    marginTop: 0,
    marginBottom: 1,
    marginLeft: 0,
    marginRight: 0,
    elevation: 0,
    flexDirection: 'row',
    alignItems: 'stretch',
  },
  ringContainer: {
    width: 35,
    height: 45,
    justifyContent: 'center',
    alignItems: 'center',
  },
  labelContainer: {
    width: 44,
    flex: 1,
    justifyContent: 'center',
  },
});

export default class HabitList extends React.Component {
  constructor(props) {
    super(props);
    this.state = { habits: [] };
  }

  componentDidMount() {
    Emitter.addListener('onHabitList', (e) => {
      this.setState({ habits: e });
    });
    Backend.requestHabitList();
  }

  render() {
    const { habits } = this.state;
    return (
      <FlatList
        style={styles.container}
        data={habits}
        renderItem={({ item }) => (
          <View style={styles.item}>
            <View style={styles.ringContainer}>
              <Ring
                color={Colors[item.color]}
                size={14}
                strokeWidth={20}
                percentage={Math.random()}
              />
            </View>
            <View style={styles.labelContainer}>
              <Text
                numberOfLines={2}
                style={{
                  fontSize: 14,
                  color: Colors[item.color],
                }}
              >
                {item.name}
              </Text>
            </View>
            <CheckmarkButton color={Colors[item.color]} />
            <CheckmarkButton color={Colors[item.color]} />
            <CheckmarkButton color={Colors[item.color]} />
            <CheckmarkButton color={Colors[item.color]} />
          </View>
        )}
      />
    );
  }
}
