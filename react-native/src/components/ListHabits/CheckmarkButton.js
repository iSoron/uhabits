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

import PropTypes from 'prop-types';
import React from 'react';
import {
  StyleSheet,
  Text,
  View,
} from 'react-native';
import FontAwesome from '../../helpers/FontAwesome';
import { Colors } from '../../helpers/Colors';

const styles = StyleSheet.create({
  checkmarkBox: {
    width: 44,
    height: 44,
    justifyContent: 'center',
    alignItems: 'center',
  },
  checkmark: {
    fontFamily: 'FontAwesome',
    fontSize: 14,
  },
});

function randomInt(max) {
  return Math.floor(Math.random() * Math.floor(max));
}

export default function CheckmarkButton(props) {
  let text;
  const { color } = props;
  const value = Math.min(2, randomInt(5));

  if (value === 2) {
    text = (
      <Text style={[styles.checkmark, { color }]}>
        {FontAwesome.check}
      </Text>
    );
  } else if (value === 1) {
    text = (
      <Text style={[styles.checkmark, { color: Colors.unchecked }]}>
        {FontAwesome.check}
      </Text>
    );
  } else {
    text = (
      <Text style={[styles.checkmark, { color: Colors.unchecked }]}>
        {FontAwesome.times}
      </Text>
    );
  }
  return (
    <View style={styles.checkmarkBox}>{text}</View>
  );
}

CheckmarkButton.propTypes = {
  color: PropTypes.string.isRequired,
};
