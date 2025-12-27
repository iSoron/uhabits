# Progress Tracking Feature

## Overview

The Progress feature provides aggregate tracking of habit completion across all active habits, giving users a holistic view of their overall progress trend rather than individual habit statistics.

## Motivation

### Why This Feature?

Users often want to understand their overall habit-tracking performance without diving into individual habits. Questions like:
- "Am I generally improving in building habits?"
- "What's my overall completion rate today vs yesterday?"
- "When do I perform best during the week?"
- "What are my longest improvement streaks?"

The Progress screen answers these questions by computing aggregate metrics across all active (non-archived) habits.

### Semantic Naming: "Progress" vs "Overview"

This feature was initially developed as "Overview" but was renamed to "Progress" for semantic accuracy:
- **Progress** = forward movement, improvement over time
- The feature tracks **progress days** where today's aggregate score > yesterday's score
- **Streaks** measure consecutive days of progress/improvement
- This aligns better with the app's philosophy of measuring improvement rather than just displaying data

## What It Does

### Core Functionality

1. **Aggregate Score Calculation**
   - Computes daily average completion percentage across all active habits
   - Formula: `Sum(habit scores) / Number of active habits`
   - Range: 0-100% representing overall completion rate

2. **Progress Detection**
   - A "progress day" occurs when today's score > yesterday's score
   - Example: Yesterday 65%, Today 78% = Progress ✓
   - Example: Yesterday 78%, Today 78% = No progress (equal)

3. **Improvement Streaks**
   - Tracks consecutive progress days
   - Shows best streaks (longest periods of consistent improvement)
   - Encourages sustained habit-building momentum

4. **Multiple Visualization Modes**
   - **Stats Card**: Yesterday/Today scores with % change
   - **Bar Chart**: Period aggregation (daily/weekly/monthly) with time range selector
   - **Calendar**: Heat map showing aggregate scores
   - **Frequency**: Weekday patterns (when you perform best)
   - **Best Streaks**: List of longest improvement periods

### User Experience

- Accessible via menu item "Progress" in main habits list
- Empty state message when no habits exist
- Time range selectors on bar chart (remembers last selection)
- Backward-compatible preference migration from "Overview" naming

## How It Works

### Architecture

The feature follows the MVP (Model-View-Presenter) architecture pattern used throughout uHabits:

```
┌─────────────────────────────────────────────────────────────┐
│                     ProgressActivity                         │
│  (UI Controller - handles user interactions)                │
└─────────────────────────────────────────────────────────────┘
                           │
                           ▼
┌─────────────────────────────────────────────────────────────┐
│                     ProgressPresenter                        │
│  (Business Logic Orchestrator)                              │
│  - Retrieves active habits                                   │
│  - Calls AggregateScoreCalculator                           │
│  - Builds ProgressState for UI                              │
└─────────────────────────────────────────────────────────────┘
                           │
                           ▼
┌─────────────────────────────────────────────────────────────┐
│                 AggregateScoreCalculator                     │
│  (Pure Business Logic - no Android dependencies)            │
│  - computeAggregateScores()                                 │
│  - calculateAggregateStreaks()                              │
│  - computeAggregateWeekdayFrequency()                       │
│  - computeAggregateEntriesByPeriod()                        │
└─────────────────────────────────────────────────────────────┘
```

### Key Components

#### 1. AggregateScoreCalculator
**Location**: `uhabits-core/src/jvmMain/.../progress/AggregateScoreCalculator.kt`

Pure Kotlin logic with no Android dependencies, making it highly testable:

```kotlin
class AggregateScoreCalculator {
    fun computeAggregateScores(
        habits: List<Habit>,
        fromDate: Timestamp,
        toDate: Timestamp
    ): List<Score>
    
    fun calculateAggregateStreaks(scores: List<Score>): List<Streak>
    
    fun computeAggregateWeekdayFrequency(...): HashMap<Timestamp, Array<Int>>
    
    fun computeAggregateEntriesByPeriod(...): List<Entry>
}
```

**Algorithm for Score Calculation:**
1. For each day in the date range:
   - Get score for each active habit
   - Sum all scores
   - Divide by number of habits
   - Result is aggregate score (0-100%)

**Algorithm for Streak Calculation:**
1. Compare consecutive day scores
2. If score(day N) > score(day N-1), mark as progress
3. Count consecutive progress days
4. Track all streaks, return longest ones

#### 2. ProgressPresenter
**Location**: `uhabits-android/.../progress/ProgressPresenter.kt`

Orchestrates business logic and prepares data for UI:
- Filters archived habits (only active habits included)
- Determines date ranges and periods based on time range selections
- Calls calculator methods
- Builds immutable state objects for each card

#### 3. ProgressActivity
**Location**: `uhabits-android/.../progress/ProgressActivity.kt`

Android UI controller:
- Inflates layout with ViewBinding
- Handles time range selector changes
- Updates UI when state changes
- Manages empty state display

#### 4. View Components
**Location**: `uhabits-android/.../progress/views/`

Custom views for each card:
- `ProgressStatsCardView` - Yesterday/Today comparison
- `ProgressBarCardView` - Bar chart with period aggregation and time selector
- `ProgressHistoryCardView` - Calendar heat map
- `ProgressFrequencyCardView` - Weekday frequency matrix
- `BestStreakCardView` - Streak list display

Each view has a corresponding `State` data class for immutability.

### Testing

**Test Coverage**: 13 unit tests for `AggregateScoreCalculator`

**Test Location**: `uhabits-core/.../progress/AggregateScoreCalculatorTest.kt`

**Key Test Cases:**
- Empty habit list handling
- Single habit scenarios
- Multiple habits with varying scores
- Date range boundaries
- Streak calculation with consecutive improvements
- Streak calculation with non-consecutive improvements
- Weekday frequency aggregation
- Period-based entry aggregation (daily, weekly, monthly)
- Edge cases (all zeros, all perfect scores, missing data)

### Data Storage

**Preferences Migration:**
```kotlin
// Old keys (from "Overview" naming)
pref_overview_bar_spinner

// New keys (Progress naming)
pref_progress_bar_spinner
```

**Backward Compatibility:**
- Reads new key first
- If not found, falls back to old key
- Migrates transparently on first access
- User settings preserved across rename

## File Structure

```
uhabits-android/src/main/java/.../habits/progress/
├── ProgressActivity.kt              # UI controller
├── ProgressPresenter.kt             # Business logic orchestrator
├── ProgressState.kt                 # Immutable state container
└── views/
    ├── ProgressStatsCardView.kt     # Stats card UI
    ├── ProgressBarCardView.kt       # Bar chart UI
    ├── ProgressBarCardState.kt      # Bar chart state
    ├── ProgressHistoryCardView.kt   # Calendar UI
    ├── ProgressHistoryCardState.kt  # Calendar state
    ├── ProgressFrequencyCardView.kt # Frequency UI
    ├── ProgressFrequencyCardState.kt # Frequency state
    └── BestStreakCardView.kt        # Streaks UI

uhabits-android/src/main/res/layout/
├── activity_progress.xml            # Main layout
├── progress_stats_card.xml          # Stats card layout
├── progress_bar_card.xml            # Bar chart layout
├── progress_history_card.xml        # Calendar layout
└── progress_frequency_card.xml      # Frequency layout

uhabits-core/src/jvmMain/.../progress/
├── AggregateScoreCalculator.kt      # Core business logic
└── AggregateScoreCalculatorTest.kt  # Unit tests
```

## Breaking Changes

**None.** This is a new feature with no breaking changes:
- All existing functionality preserved
- Preference migration ensures user settings carry forward
- Individual habit "Overview" card remains unchanged
- No database schema changes

## Future Enhancements

Potential improvements for future iterations:

1. **Live Progress Widget**
   - Small widget in main habits list showing today's progress
   - Optional (disabled by default)
   - Real-time updates when habits are checked
   - Settings toggle for visibility

2. **Goal Setting**
   - Set target aggregate score (e.g., "Achieve 80% daily")
   - Progress notifications when reaching milestones
   - Celebrate improvement streaks

3. **Trend Analysis**
   - Moving averages (7-day, 30-day)
   - Trend indicators (improving, stable, declining)
   - Predictive insights based on historical patterns

4. **Habit Contribution**
   - Show which habits contribute most/least to aggregate score
   - Identify habits needing attention
   - Balance recommendations

5. **Export/Share**
   - Export aggregate statistics to CSV
   - Share progress charts as images
   - Integration with fitness/productivity apps

## Design Philosophy Alignment

This feature aligns with uHabits' core principles:

1. **Simplicity**: Clean, focused UI without clutter
2. **Privacy**: All data local, no cloud sync required
3. **Transparency**: Open-source, auditable code
4. **Flexibility**: Multiple visualization modes for different preferences
5. **No Gamification**: Focus on genuine progress, not artificial rewards
6. **Free & Open**: FOSS, no paywalls or premium features

## Technical Decisions

### Why MVP Architecture?
- Consistent with existing codebase patterns
- Clear separation of concerns
- Highly testable (business logic independent of Android)
- Easy to maintain and extend

### Why Pure Kotlin for Calculator?
- Platform-independent (can be used in multiplatform projects)
- Unit testable without Android instrumentation
- Fast test execution
- No mocking required for tests

### Why Immutable State Objects?
- Predictable UI updates
- Thread-safe
- Easier debugging (state snapshots)
- Prevents accidental mutations

### Why Custom Views?
- Reusability across different screens
- Encapsulation of view-specific logic
- Consistent with existing chart components
- Easy to test in isolation

## Migration Guide (for Contributors)

If you're working on code that references the old "Overview" naming:

1. **Package imports**: Use `org.isoron.uhabits.activities.habits.progress.*`
2. **String resources**: Use `@string/progress` for aggregate screen, `@string/overview` for individual habit card
3. **Preferences**: Use `progressBarSpinnerPosition` for time range selection
4. **Activity intent**: Launch `ProgressActivity`
5. **Menu references**: Use `R.id.actionProgress`

## Known Issues & Future Work

### Before Public Release

#### 1. Day Extension Setting Integration
**Issue**: The app has a setting "Extend day for a few hours past midnight" that needs to be considered for progress calculation.

**Current Behavior**: Progress change shows today vs yesterday without considering the day extension setting.

**Required Fix**: 
- When day extension is active (e.g., extend until 3 AM), the "today" calculation should respect device time
- If current time is before 3 AM device time, show previous day's change only
- After 3 AM, show actual new day progress
- Example: If it's 2:30 AM, still show yesterday's stats as "today" until 3 AM threshold

**Impact**: Without this fix, users with day extension enabled will see inconsistent progress metrics during extended hours.

#### 2. Back Navigation Issues
**Known Issues**:
- Back navigation broken in Settings page
- Back navigation broken in About page

**Status**: Requires investigation and fix before public release.

**Priority**: High - affects core navigation UX

### Translation Updates

The ~30 translation files still use "Overview" strings. These should be updated to "Progress" with proper translations for each language:
- `<string name="progress">Progress</string>` (translated)
- `<string name="progress_empty_message">...</string>` (translated)
- `<string name="progress_history">Calendar</string>` (translated)
- `<string name="progress_frequency">Frequency</string>` (translated)

**Status**: Low priority - English strings work, no build errors. Can be done in separate PR.

## Contributors

This feature represents ~3 days of development work, including:
- Core calculator implementation (8 hours)
- UI/UX implementation (6 hours)
- Testing and bug fixes (4 hours)
- Refactoring and renaming (6 hours)
- Documentation (2 hours)

**Total effort**: ~26 hours

## License

This feature follows the project's GPLv3+ license. All code is free and open-source.

---

**Questions or Issues?**
- Check the test cases in `AggregateScoreCalculatorTest.kt` for usage examples
- Review existing habit score calculation in `Habit.kt` for consistency
- Follow the MVP pattern established in other activities
- Maintain separation between pure logic (core) and Android UI (android module)
