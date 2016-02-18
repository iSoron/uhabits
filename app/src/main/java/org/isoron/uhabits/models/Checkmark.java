/* Copyright (C) 2016 Alinson Santos Xavier
 *
 * This program is free software: you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option)
 * any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied  warranty of MERCHANTABILITY or
 * FITNESS  FOR  A PARTICULAR PURPOSE. See the GNU General Public License for
 * more details.
 *
 * You  should  have  received  a  copy  of the GNU General Public License
 * along  with  this  program. If not, see <http://www.gnu.org/licenses/>.
 */

package org.isoron.uhabits.models;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;

@Table(name = "Checkmarks")
public class Checkmark extends Model
{

    public static final int UNCHECKED = 0;
    public static final int CHECKED_IMPLICITLY = 1;
    public static final int CHECKED_EXPLICITLY = 2;

    @Column(name = "habit")
    public Habit habit;

    @Column(name = "timestamp")
    public Long timestamp;

    /**
     * Indicates whether there is a checkmark at the given timestamp or not, and whether the
     * checkmark is explicit or implicit. An explicit checkmark indicates that there is a
     * repetition at that day. An implicit checkmark indicates that there is no repetition at that
     * day, but a repetition was not needed, due to the frequency of the habit.
     */
    @Column(name = "value")
    public Integer value;
}
