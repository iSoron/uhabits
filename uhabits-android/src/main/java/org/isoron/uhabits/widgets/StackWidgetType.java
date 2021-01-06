/*
 * Copyright (C) 2016-2021 Álinson Santos Xavier <git@axavier.org>
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

package org.isoron.uhabits.widgets;

import org.isoron.uhabits.R;

/**
 * Created by victoryu on 11/3/17.
 */

public enum StackWidgetType {

    CHECKMARK(0),
    FREQUENCY(1),
    SCORE(2), // habit strength widget
    HISTORY(3),
    STREAKS(4),
    TARGET(5);

    private int value;
    StackWidgetType(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static StackWidgetType getWidgetTypeFromValue(int value) {
        if (CHECKMARK.getValue() == value) {
            return CHECKMARK;
        } else if (FREQUENCY.getValue() == value) {
            return FREQUENCY;
        } else if (SCORE.getValue() == value) {
            return SCORE;
        } else if (HISTORY.getValue() == value) {
            return HISTORY;
        } else if (STREAKS.getValue() == value) {
            return STREAKS;
        } else if (TARGET.getValue() == value) {
            return TARGET;
        }
        return null;
    }

    public static int getStackWidgetLayoutId(StackWidgetType type) {
        switch (type) {
            case CHECKMARK:
                return R.layout.checkmark_stackview_widget;
            case FREQUENCY:
                return R.layout.frequency_stackview_widget;
            case SCORE:
                return R.layout.score_stackview_widget;
            case HISTORY:
                return R.layout.history_stackview_widget;
            case STREAKS:
                return R.layout.streak_stackview_widget;
            case TARGET:
                return R.layout.target_stackview_widget;
        }
        return 0;
    }

    public static int getStackWidgetAdapterViewId(StackWidgetType type) {
        switch (type) {
            case CHECKMARK:
                return R.id.checkmarkStackWidgetView;
            case FREQUENCY:
                return R.id.frequencyStackWidgetView;
            case SCORE:
                return R.id.scoreStackWidgetView;
            case HISTORY:
                return R.id.historyStackWidgetView;
            case STREAKS:
                return R.id.streakStackWidgetView;
            case TARGET:
                return R.id.targetStackWidgetView;
        }
        return 0;
    }

    public static int getStackWidgetEmptyViewId(StackWidgetType type) {
        switch (type) {
            case CHECKMARK:
                return R.id.checkmarkStackWidgetEmptyView;
            case FREQUENCY:
                return R.id.frequencyStackWidgetEmptyView;
            case SCORE:
                return R.id.scoreStackWidgetEmptyView;
            case HISTORY:
                return R.id.historyStackWidgetEmptyView;
            case STREAKS:
                return R.id.streakStackWidgetEmptyView;
            case TARGET:
                return R.id.targetStackWidgetEmptyView;
        }
        return 0;
    }

}
