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
import PropTypes from 'prop-types';
import Svg, { Circle } from 'react-native-svg';
import { Colors } from '../../helpers/Colors';

export default function ColorCircle(props) {
  const { size, color } = props;
  return (
    <Svg height={size} width={size} viewBox="0 0 100 100">
      <Circle cx={50} cy={50} r={50} fill={color} />
      <Circle cx={50} cy={50} r={30} fill={Colors.itemBackground} />
    </Svg>
  );
}

ColorCircle.propTypes = {
  size: PropTypes.number.isRequired,
  color: PropTypes.string.isRequired,
};
