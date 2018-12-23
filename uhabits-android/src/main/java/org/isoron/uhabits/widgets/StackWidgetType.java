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
    STREAKS(4);

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
        }
        return 0;
    }

}
