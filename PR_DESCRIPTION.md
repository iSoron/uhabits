# PR: Rename "Overview" to "Progress" for Aggregate Tracking Feature

## Summary

This PR renames the aggregate habit tracking feature from "Overview" to "Progress" for better semantic accuracy. The feature displays cross-habit progress metrics, and "Progress" better represents the concept of improvement over time (where progress days = today's score > yesterday's score).

## Changes Made

### 1. Package & File Renames (Git History Preserved)

**Directory Structure:**
- `overview/` → `progress/` (git mv)

**Core Files:**
- `OverviewActivity.kt` → `ProgressActivity.kt`
- `OverviewPresenter.kt` → `ProgressPresenter.kt`
- `OverviewState.kt` → `ProgressState.kt`
- `AggregateScoreCalculator.kt` - package updated to `.progress`

**View Files (9 files):**
- `OverviewStatsCardView.kt` → `ProgressStatsCardView.kt`
- `OverviewScoreCardView.kt` → `ProgressScoreCardView.kt`
- `OverviewBarCardView.kt` → `ProgressBarCardView.kt`
- `OverviewHistoryCardView.kt` → `ProgressHistoryCardView.kt`
- `OverviewFrequencyCardView.kt` → `ProgressFrequencyCardView.kt`
- `OverviewBarCardState.kt` → `ProgressBarCardState.kt`
- `OverviewHistoryCardState.kt` → `ProgressHistoryCardState.kt`
- `OverviewFrequencyCardState.kt` → `ProgressFrequencyCardState.kt`
- `BestStreakCardView.kt` - package updated

**Layout Files (6 files):**
- `activity_overview.xml` → `activity_progress.xml`
- `overview_stats_card.xml` → `progress_stats_card.xml`
- `overview_score_card.xml` → `progress_score_card.xml`
- `overview_bar_card.xml` → `progress_bar_card.xml`
- `overview_history_card.xml` → `progress_history_card.xml`
- `overview_frequency_card.xml` → `progress_frequency_card.xml`

**Test Files:**
- `AggregateScoreCalculatorTest.kt` - package updated to `.progress`

### 2. Code Updates

**Package Declarations:**
- Updated all Kotlin files to use `org.isoron.uhabits.activities.habits.progress` package

**Imports:**
- Updated all import statements referencing the old package
- Updated references to renamed classes (ProgressActivity, ProgressState, etc.)

**Navigation & Integration:**
- `ListHabitsMenuBehavior.kt`: `onViewOverview()` → `onViewProgress()`, `showOverviewScreen()` → `showProgressScreen()`
- `ListHabitsScreen.kt`: Intent now launches `ProgressActivity`
- `ListHabitsMenu.kt`: Menu handler updated to `R.id.actionProgress`
- `list_habits.xml`: Menu item ID and title updated

**XML Layouts:**
- Updated all `<view class="...">` references to new package path
- Updated string resource references: `@string/overview_*` → `@string/progress_*`

**AndroidManifest.xml:**
```xml
<activity
    android:name=".activities.habits.progress.ProgressActivity"
    android:label="@string/progress">
```

### 3. Preferences & Data Migration

**Backward-Compatible Migration:**
```kotlin
var progressScoreSpinnerPosition: Int
    get() {
        val newValue = storage.getInt("pref_progress_score_spinner", -1)
        if (newValue < 0) {
            // Fallback to old key for migration
            val oldValue = storage.getInt("pref_overview_score_spinner", 1)
            return min(4, max(0, oldValue))
        }
        return min(4, max(0, newValue))
    }
    set(position) { 
        storage.putInt("pref_progress_score_spinner", position) 
    }
```

**Storage Keys:**
- `pref_overview_score_spinner` → `pref_progress_score_spinner`
- `pref_overview_bar_spinner` → `pref_progress_bar_spinner`
- Old keys automatically migrated on first access

### 4. String Resources

**Main English strings (strings.xml):**
```xml
<string name="overview">Overview</string>        <!-- Kept for individual habit detail card -->
<string name="progress">Progress</string>         <!-- New aggregate screen title -->
<string name="progress_empty_message">Start adding habits to see the overall progress trend</string>
<string name="progress_history">Calendar</string>
<string name="progress_frequency">Frequency</string>
```

**Note on Translations:**
- ~30 translation files still reference "Overview" strings
- These will continue to work (no build errors)
- Future PR can add proper "Progress" translations for each language
- Left as-is to keep this PR focused on English rename

### 5. Documentation

**New File: `FEATURE_PROGRESS.md`**
- Comprehensive feature documentation
- Explains motivation ("Progress" vs "Overview" semantics)
- Architecture diagrams and design decisions
- Testing strategy (13 unit tests)
- Usage examples
- Future enhancement ideas
- Migration guide for contributors

## Testing

### Build Verification
```bash
.\gradlew clean assembleDebug --no-daemon
# Result: BUILD SUCCESSFUL in 1m 8s
```

### Test Suite
```bash
.\gradlew test --no-daemon
# Result: BUILD SUCCESSFUL in 1m 22s
# All 13 AggregateScoreCalculator tests passing
```

### Manual Testing Checklist
- [x] App builds without errors
- [x] All tests pass
- [x] Menu item shows "Progress"
- [x] Progress screen opens correctly
- [x] All cards display properly
- [x] Time range spinners work
- [x] Empty state displays when no habits
- [x] Preference migration works (tested with old pref keys)

## Breaking Changes

**None.** This is purely a rename with backward compatibility:
- ✅ Existing user preferences automatically migrated
- ✅ No database schema changes
- ✅ No API changes
- ✅ Individual habit "Overview" card unchanged
- ✅ All functionality preserved

## Semantic Rationale

### Why "Progress" is More Accurate

**Old Naming:** "Overview"
- Implies a summary or general view
- Doesn't communicate the improvement-tracking aspect
- Could be confused with a dashboard or summary screen

**New Naming:** "Progress"
- Clearly indicates forward movement
- Aligns with streak logic (progress = score improvement)
- Matches user mental model ("Am I making progress?")
- Semantic fit: Progress days = days where aggregate score increased

**Progress Detection Logic:**
```kotlin
// A "progress day" occurs when today > yesterday
val isProgressDay = aggregateScoreToday > aggregateScoreYesterday

// Streaks measure consecutive progress days
val streak = consecutiveProgressDays
```

This naming better represents what the feature actually measures: **improvement over time**.

## Files Changed Summary

- **Kotlin files**: 20 (renamed + content updated)
- **XML layouts**: 6 (renamed + view references updated)
- **Test files**: 1 (package updated)
- **XML resources**: 1 (strings.xml)
- **Configuration files**: 2 (AndroidManifest.xml, Preferences.kt)
- **Navigation files**: 3 (Menu, Screen, Behavior)
- **Documentation**: 1 new (FEATURE_PROGRESS.md)

**Total**: ~34 files modified

## Screenshots

[User's existing screenshots from implementation - not attached to PR description]

The feature UI remains unchanged - only naming updated.

## Review Checklist

- [x] Code builds successfully
- [x] All tests pass
- [x] No breaking changes
- [x] Backward compatibility ensured
- [x] Git history preserved (used git mv)
- [x] Documentation created
- [x] Semantic naming justified
- [x] Translation files noted (future work)

## Related Issues

This addresses semantic accuracy following the initial 3-day implementation of the aggregate habit tracking feature. The feature was functional with "Overview" naming, but "Progress" better communicates its purpose.

## Future Work

1. Add "Progress" translations for ~30 language files
2. Consider adding live progress widget to main habits list (optional, disabled by default)
3. Explore trend analysis features (moving averages, predictions)

---

**Note for Maintainers**: This rename was done after the feature reached a stable state to ensure semantic accuracy before wider use. All git history preserved via `git mv` commands.
