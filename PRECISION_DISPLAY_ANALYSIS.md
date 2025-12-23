# Precision Display Analysis for Historical Days

## Current State

### What Shows Precision (5 decimal places):
1. **ProgressStatsCardView**: Shows yesterday and today with format `%.5f%%`
2. **Progress Summary Widget**: Shows today's score and change with 5 decimal places using `DecimalFormat("0.00000")`

### What Doesn't Show Precision:
1. **ScoreChart** (Line chart): Y-axis labels show integers only (`%d%%`), no hover tooltips
2. **BarChart** (Vertical bars): No value labels on bars, only visual height
3. **HistoryChart** (Calendar heatmap): Uses color gradients, no numeric tooltips

## User Request

> "on this daily progress the precision for historic days is not visible anywhere, currently it shown as rounded to xx.x format in default bar charts, with vertical bars; no other chart shows this info, so it would be great to have it shown somewhere, so in any historical day, could see how close/away were there"

## Analysis

### Problem
Users cannot see exact percentage values (with high precision) for historical days. They can only see visual representations (bar heights, calendar colors) which don't communicate exact values like 78.12345% vs 78.67890%.

### Why This Matters
- Small daily changes (0.1-1%) are significant for tracking gradual improvement
- Visual representations can't distinguish between similar values (78.1% vs 78.2%)
- Users want to understand exact progress on specific historical dates

## Proposed Solutions

### Option 1: Add Tooltips to Charts (Recommended)
**Approach**: When user taps/long-presses a date on any chart, show a tooltip with precise value

**Implementation**:
- Add `OnClickListener` to ScoreChart, BarChart datapoints
- Display AlertDialog or Snackbar with: `Date: Dec 18, 2025\nProgress: 78.12345%`
- Already works for HistoryChart (opens edit dialog), extend to show read-only value

**Pros**:
- Minimal UI changes
- Works across all chart types
- On-demand (doesn't clutter UI)

**Cons**:
- Requires user interaction
- Not immediately visible

**Files to Modify**:
```
uhabits-android/src/main/java/org/isoron/uhabits/activities/common/views/ScoreChart.kt
  - Add OnTouchListener/GestureDetector
  - Detect tap on data point
  - Show tooltip with precise score

uhabits-android/src/main/java/org/isoron/uhabits/activities/habits/progress/views/ProgressBarCardView.kt
  - Similar touch handling for bar chart
  - Show tooltip on bar tap

uhabits-android/src/main/res/layout/tooltip_precision.xml (new)
  - Simple layout for precision tooltip
```

### Option 2: Add Value Labels on Bars
**Approach**: Display exact percentage on top of each bar in BarChart

**Implementation**:
- Modify BarChart.kt to draw text labels above bars
- Use DecimalFormat("0.00") for space efficiency (2 decimals on chart, full precision on tap)

**Pros**:
- Immediately visible
- No interaction needed

**Cons**:
- Can clutter chart with many datapoints
- Limited space for full 5 decimal precision
- May overlap for closely spaced bars

### Option 3: Horizontal Bar Chart View (User Suggestion)
**Approach**: Create alternative visualization with horizontal bars showing precise labels

**Implementation**:
- New `ProgressPrecisionCardView` with horizontal bar layout
- Each row: `Date | ████████████░░░░ | 78.12345%`
- Scrollable list for historical data

**Pros**:
- Dedicated precision view
- Clear association of date → value
- No overlap issues

**Cons**:
- Requires new card/view
- Takes more screen space
- Duplicate information (already in other charts)

### Option 4: Relative Y-Axis Scaling (User Suggestion)
**Approach**: Instead of fixed 0-100% scale, use dynamic min-max from data

**Implementation**:
- Calculate yMin = min(scores) - padding, yMax = max(scores) + padding
- Scale chart to this range
- Y-axis labels show actual range (e.g., 75% to 82%)

**Pros**:
- Better visualization of small changes
- Makes small differences more visible

**Cons**:
- Can be misleading (makes small changes look dramatic)
- User suggested this might require chart re-rendering with different direction
- ScoreChart already has `setYAxisBounds()` method - partially implemented

## Recommended Implementation Plan

### Phase 1: Quick Win - Tooltip on Tap (Easiest)
1. Add onTouchListener to ScoreChart
2. Detect which data point was tapped
3. Show simple AlertDialog with:
   ```
   December 18, 2025
   Progress: 78.12345%
   vs Previous Day: +2.45678%
   ```
4. Extend to BarChart and HistoryChart

**Estimated Effort**: 2-4 hours
**User Impact**: High (solves core problem)

### Phase 2: Enhanced Visualization - Relative Y-Axis (Medium)
1. Use existing `setYAxisBounds()` in ScoreChart
2. Add toggle in settings: "Use relative Y-axis for progress charts"
3. Calculate dynamic bounds: 
   - Min = floor(minScore * 10) / 10 (e.g., 78.3% → 78%)
   - Max = ceil(maxScore * 10) / 10 (e.g., 82.7% → 83%)
4. Update Y-axis labels to show actual range

**Estimated Effort**: 3-6 hours
**User Impact**: Medium (better visualization, but tooltip alone may suffice)

### Phase 3: Optional - Dedicated Precision View
1. Create new "Detailed History" card
2. Show last 30 days in scrollable list with exact values
3. Add to Progress activity below other cards

**Estimated Effort**: 4-8 hours
**User Impact**: Low-Medium (nice-to-have, not essential)

## Technical Notes

### Existing Capabilities
- ScoreChart already has `yAxisMin` and `yAxisMax` fields
- ProgressPresenter already commented on dynamic Y-axis: 
  ```kotlin
  @Deprecated("ScoreChart now handles Y-axis scaling")
  private fun normalizeScoresForChart(scores: List<Score>): List<Score>
  ```
- HistoryChart already has `OnDateClickedListener` interface

### Implementation Files
```
Core Changes:
- ScoreChart.kt: Add tap detection & tooltip display
- ProgressBarCardView.kt: Add tap handling
- ProgressHistoryCardView.kt: Extend existing OnDateClickedListener

Supporting:
- strings.xml: Add tooltip format strings
- (Optional) New layout for fancy tooltip

Settings (Phase 2):
- preferences.xml: Add "relativeYAxis" toggle
- Preferences.kt: Add getter/setter
- ProgressPresenter.kt: Apply dynamic bounds when enabled
```

## Conclusion

**Recommended approach**: Implement **Phase 1 (Tooltips)** first as it directly addresses the user's need with minimal complexity. If users want better visualization of small changes, add **Phase 2 (Relative Y-Axis)** as an optional setting.

The horizontal bar chart (Option 3) is overkill for this problem - tooltips provide the same information with less UI complexity.
