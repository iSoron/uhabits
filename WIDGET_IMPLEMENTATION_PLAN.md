# Live Progress Widget Implementation Plan

## Overview

You mentioned: "introducing this overview/progress screen's overview section only in least small space consuming way"

This document outlines how to add a compact live progress indicator to the main habits list.

## Requirements (From Your Request)

1. ✅ **Compact**: Minimal space consumption
2. ✅ **Live Updates**: Show real-time progress when habits are checked
3. ✅ **Optional**: "by default it should not be shown, if user needs they can enable it"
4. ✅ **Settings Toggle**: User can enable/disable

## Design Proposal

### Visual Layout

```
┌─────────────────────────────────────────────────────────┐
│  Loop Habit Tracker                            [menu]   │
├─────────────────────────────────────────────────────────┤
│  📊 Today's Progress: 78% (+3%)          [Hide] [Details]│  ← New widget
├─────────────────────────────────────────────────────────┤
│  ◯ Morning Meditation                    ✓ ✓ ✓ ✓ ✓ ✓ ✓ │
│  ◯ Exercise                               ✓ ✓ ✓ ✓ ✗ ✓ ✓ │
│  ◯ Read                                   ✓ ✓ ✓ ✗ ✓ ✓ ✓ │
└─────────────────────────────────────────────────────────┘
```

**Dimensions:**
- Height: ~40dp (single line)
- Background: Subtle card with rounded corners
- Margins: 8dp on all sides
- Text: Small size, secondary color

### Information Displayed

**Primary Text:** "Today's Progress: 78%"
- Current aggregate score for today
- Updates live when habits are checked/unchecked

**Secondary Text:** "(+3%)"
- Change from yesterday
- Color-coded: Green (+), Red (-), Gray (0)

**Actions:**
- **[Hide]** button: Dismisses widget, updates preference
- **[Details]** button: Opens full Progress screen

## Implementation Guide

### 1. Create Widget View

**File:** `uhabits-android/src/main/java/org/isoron/uhabits/activities/habits/list/views/ProgressSummaryWidget.kt`

```kotlin
package org.isoron.uhabits.activities.habits.list.views

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.cardview.widget.CardView
import org.isoron.uhabits.databinding.ProgressSummaryWidgetBinding

class ProgressSummaryWidget(
    context: Context,
    attrs: AttributeSet? = null
) : CardView(context, attrs) {
    
    private val binding = ProgressSummaryWidgetBinding.inflate(
        LayoutInflater.from(context), this, true
    )
    
    fun setProgress(todayScore: Double, yesterdayScore: Double) {
        val change = todayScore - yesterdayScore
        
        binding.progressText.text = context.getString(
            R.string.progress_summary,
            (todayScore * 100).toInt()
        )
        
        binding.changeText.text = when {
            change > 0 -> "+${(change * 100).toInt()}%"
            change < 0 -> "${(change * 100).toInt()}%"
            else -> "±0%"
        }
        
        binding.changeText.setTextColor(context.getColor(when {
            change > 0 -> R.color.green
            change < 0 -> R.color.red
            else -> R.color.grey
        }))
    }
    
    fun setOnHideClickListener(listener: () -> Unit) {
        binding.hideButton.setOnClickListener { listener() }
    }
    
    fun setOnDetailsClickListener(listener: () -> Unit) {
        binding.detailsButton.setOnClickListener { listener() }
    }
}
```

### 2. Create Widget Layout

**File:** `uhabits-android/src/main/res/layout/progress_summary_widget.xml`

```xml
<?xml version="1.0" encoding="utf-8"?>
<merge xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:layout_width="match_parent"
    android:layout_height="wrap_content">

    <androidx.cardview.widget.CardView
        android:layout_width="match_parent"
        android:layout_height="40dp"
        android:layout_margin="8dp"
        app:cardCornerRadius="8dp"
        app:cardElevation="2dp">

        <LinearLayout
            android:layout_width="match_parent"
            android:layout_height="match_parent"
            android:orientation="horizontal"
            android:gravity="center_vertical"
            android:paddingStart="12dp"
            android:paddingEnd="12dp">

            <TextView
                android:id="@+id/progressText"
                android:layout_width="0dp"
                android:layout_height="wrap_content"
                android:layout_weight="1"
                android:text="Today's Progress: 0%"
                android:textSize="14sp"
                android:textStyle="bold" />

            <TextView
                android:id="@+id/changeText"
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:layout_marginStart="8dp"
                android:text="±0%"
                android:textSize="12sp" />

            <Button
                android:id="@+id/hideButton"
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:layout_marginStart="8dp"
                android:text="Hide"
                android:textSize="12sp"
                style="@style/Widget.AppCompat.Button.Borderless.Colored" />

            <Button
                android:id="@+id/detailsButton"
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:layout_marginStart="4dp"
                android:text="Details"
                android:textSize="12sp"
                style="@style/Widget.AppCompat.Button.Borderless.Colored" />

        </LinearLayout>

    </androidx.cardview.widget.CardView>

</merge>
```

### 3. Update Preferences

**File:** `uhabits-core/src/jvmMain/java/org/isoron/uhabits/core/preferences/Preferences.kt`

Add property:

```kotlin
var showProgressWidget: Boolean
    get() = storage.getBoolean("pref_show_progress_widget", false)
    set(value) {
        storage.putBoolean("pref_show_progress_widget", value)
    }
```

### 4. Add to Settings

**File:** `uhabits-android/src/main/res/xml/preferences.xml`

Add preference:

```xml
<SwitchPreferenceCompat
    android:key="pref_show_progress_widget"
    android:title="Show Progress Widget"
    android:summary="Display live progress indicator on main screen"
    android:defaultValue="false" />
```

### 5. Integrate into List View

**File:** `uhabits-android/src/main/java/org/isoron/uhabits/activities/habits/list/views/ListHabitsRootView.kt`

```kotlin
class ListHabitsRootView(context: Context) : FrameLayout(context) {
    
    private val progressWidget = ProgressSummaryWidget(context).apply {
        visibility = if (preferences.showProgressWidget) VISIBLE else GONE
    }
    
    init {
        // Add widget after toolbar, before habit list
        addView(progressWidget, LayoutParams(
            LayoutParams.MATCH_PARENT,
            LayoutParams.WRAP_CONTENT
        ).apply {
            topMargin = toolbarHeight
        })
        
        // Setup listeners
        progressWidget.setOnHideClickListener {
            preferences.showProgressWidget = false
            progressWidget.visibility = GONE
        }
        
        progressWidget.setOnDetailsClickListener {
            // Open ProgressActivity
            controller.onViewProgress()
        }
    }
    
    fun updateProgress() {
        if (!preferences.showProgressWidget) return
        
        val calculator = AggregateScoreCalculator()
        val today = DateUtils.getToday()
        val yesterday = today.minus(1)
        
        val activeHabits = habitList.getFiltered(
            HabitMatcher(isArchivedAllowed = false)
        )
        
        val todayScore = calculator.computeAggregateScores(
            activeHabits, today, today
        ).firstOrNull()?.value ?: 0.0
        
        val yesterdayScore = calculator.computeAggregateScores(
            activeHabits, yesterday, yesterday
        ).firstOrNull()?.value ?: 0.0
        
        progressWidget.setProgress(todayScore, yesterdayScore)
    }
}
```

### 6. Update on Habit Changes

**File:** `uhabits-android/src/main/java/org/isoron/uhabits/activities/habits/list/ListHabitsScreen.kt`

Add observer:

```kotlin
override fun onResume() {
    super.onResume()
    
    // Listen for habit check events
    habitList.observable.addListener(object : ModelObservable.Listener {
        override fun onModelChange() {
            rootView.updateProgress()
        }
    })
    
    // Initial update
    rootView.updateProgress()
}
```

## String Resources

**File:** `uhabits-android/src/main/res/values/strings.xml`

Add:

```xml
<string name="progress_summary">Today\'s Progress: %d%%</string>
<string name="show_progress_widget">Show Progress Widget</string>
<string name="show_progress_widget_summary">Display live progress indicator on main screen</string>
```

## Testing Checklist

- [ ] Widget hidden by default (fresh install)
- [ ] Widget shows when preference enabled
- [ ] Progress updates when habit checked
- [ ] Progress updates when habit unchecked
- [ ] Change % color coded correctly (green/red/gray)
- [ ] Hide button works (updates preference)
- [ ] Details button opens Progress screen
- [ ] Widget respects theme colors
- [ ] Performance acceptable (no lag on updates)
- [ ] Works with 0 habits (shows 0%)
- [ ] Works with 1 habit
- [ ] Works with many habits (50+)

## Performance Considerations

### Optimization 1: Debounce Updates
```kotlin
private val updateHandler = Handler(Looper.getMainLooper())
private var updateRunnable: Runnable? = null

fun updateProgress() {
    // Cancel pending update
    updateRunnable?.let { updateHandler.removeCallbacks(it) }
    
    // Schedule new update with 200ms delay
    updateRunnable = Runnable {
        doUpdateProgress()
    }
    updateHandler.postDelayed(updateRunnable!!, 200)
}
```

### Optimization 2: Cache Results
```kotlin
private var cachedTodayScore: Double? = null
private var lastUpdateDate: Timestamp? = null

fun updateProgress() {
    val today = DateUtils.getToday()
    
    // Use cache if same day
    if (lastUpdateDate == today && cachedTodayScore != null) {
        progressWidget.setProgress(cachedTodayScore!!, yesterdayScore)
        return
    }
    
    // Recompute
    // ... calculate scores ...
    cachedTodayScore = todayScore
    lastUpdateDate = today
}
```

## Estimated Implementation Time

- **Widget View & Layout**: 1 hour
- **Preference Integration**: 30 minutes
- **List View Integration**: 1 hour
- **Live Update Logic**: 1 hour
- **Testing & Refinement**: 1-2 hours
- **Documentation**: 30 minutes

**Total: 5-6 hours**

## Alternative: Even More Minimal

If you want even less space, consider a single-line status bar style:

```
┌─────────────────────────────────────────────────────────┐
│  Loop Habit Tracker                            [menu]   │
├─────────────────────────────────────────────────────────┤
│  📊 78% (+3%)                                     [×]    │  ← Ultra-compact
├─────────────────────────────────────────────────────────┤
│  ◯ Morning Meditation                    ✓ ✓ ✓ ✓ ✓ ✓ ✓ │
```

Height: ~24dp (even smaller)
Tap to open full Progress screen
Long-press to hide

## Next Steps

1. Finish current PR (rename only)
2. After PR merged, implement widget in separate PR
3. Test with beta users
4. Gather feedback on placement and size
5. Iterate based on user preferences

## Questions to Consider

1. **Placement**: Before habits list or after toolbar?
2. **Update frequency**: Every habit check or debounced?
3. **Animation**: Smooth number transitions or instant?
4. **Haptic feedback**: Vibrate on progress milestone?
5. **Notifications**: Optional notification for daily progress?

---

**Note**: This is future work, NOT part of the current rename PR. Keep the current PR focused on the rename only.
