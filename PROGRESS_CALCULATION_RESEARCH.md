# Progress Calculation Research Document

## Overview

This document explains how uHabits calculates scores for individual habits and how the Progress view aggregates them.

## Individual Habit Score Calculation

### Core Formula

Found in [Score.kt](c:\Users\w11-d\src\gh\uhabits\uhabits-core\src\jvmMain\java\org\isoron\uhabits\core\models\Score.kt#L29-L48):

```kotlin
fun compute(frequency: Double, previousScore: Double, checkmarkValue: Double): Double {
    val multiplier = 0.5.pow(sqrt(frequency) / 13.0)
    var score = previousScore * multiplier
    score += checkmarkValue * (1 - multiplier)
    return score
}
```

**Explanation:**
- `frequency` = repetitions / interval (e.g., 3 times per 8 days = 0.375)
- `multiplier` = decay factor based on frequency
  - Daily habits (freq=1.0): multiplier ≈ 0.948 (scores decay ~5.2% per day if not done)
  - Weekly habits (freq=0.14): multiplier ≈ 0.974 (slower decay)
- `checkmarkValue` = 0 (not done), 1 (done), or partial value for numerical habits
- New score = (old score × decay) + (today's value × growth factor)

**Key Insights:**
- Scores build up gradually and decay gradually
- More frequent habits decay faster (need more consistency)
- Achieving 99% score requires sustained performance:
  - Daily habits: ~3 months of consistent checks
  - Weekly habits: ~9 months
  - Monthly habits: ~18 months

### Boolean (Yes/No) Habits

**Checkmark Values:**
- `Entry.YES_MANUAL` = 1 → checkmarkValue = 1.0
- `Entry.NO` = 0 → checkmarkValue = 0.0
- `Entry.SKIP` → day is ignored (not counted)
- `Entry.UNKNOWN` → treated as missed day

**Example (Daily Habit):**
```
Day 1: Check → Score = 0.0 * 0.948 + 1.0 * 0.052 = 0.052
Day 2: Check → Score = 0.052 * 0.948 + 1.0 * 0.052 = 0.101
Day 3: Miss → Score = 0.101 * 0.948 + 0.0 * 0.052 = 0.096
...
Day 90: (Consistent checks) → Score ≈ 0.99+
```

### Numerical Habits

**Two Target Types:**

1. **AT_LEAST** (e.g., "Drink 8 glasses of water")
   - Target: 8 glasses (stored as 8000 in DB, divided by 1000)
   - Entry value: Actual amount achieved
   - `checkmarkValue = min(1.0, actualValue / targetValue)`
   - Examples:
     - Drink 8 glasses → 8000/8000 = 1.0 (full credit)
     - Drink 4 glasses → 4000/8000 = 0.5 (half credit)
     - Drink 10 glasses → 10000/8000 = 1.25 → capped at 1.0 (no extra credit)

2. **AT_MOST** (e.g., "Smoke less than 5 cigarettes")
   - Target: 5 cigarettes
   - Entry value: Actual amount
   - `checkmarkValue = max(0.0, 1.0 - actualValue / targetValue)`
   - Examples:
     - Smoke 0 → 1.0 - 0/5 = 1.0 (full credit)
     - Smoke 2 → 1.0 - 2/5 = 0.6 (partial credit)
     - Smoke 5+ → 1.0 - 5/5 = 0.0 (no credit)
     - Scores start at 1.0 (perfect) and decay when targets are exceeded

**Important Notes:**
- Over-achieving doesn't give extra credit (capped at 1.0)
- Under-achieving on AT_MOST doesn't penalize beyond 0.0
- Numerical habits use same exponential smoothing formula as boolean habits

### Frequency Handling

**Daily (Frequency = 1.0):**
- Expected every day
- Fast score growth and decay
- Multiplier ≈ 0.948

**Weekly (Frequency = 1/7 ≈ 0.143):**
- Expected once per 7 days
- Slower score changes
- More forgiving of missed days

**Custom (e.g., 3 times per 8 days = 0.375):**
- System automatically adjusts decay rate
- For non-daily boolean habits, numerator and denominator are doubled to smooth irregular schedules

**Examples from Tests:**
```kotlin
// Perfect weekly habit (3x per 7 days)
habit.frequency = Frequency(3, 7)
// Missing 1 rep per week → Score converges to ~66%

// Monthly habit (1x per 30 days)
habit.frequency = Frequency(1, 30)
// Takes 18 months to reach 99% score
```

### Skip Days

**Behavior:**
- `Entry.SKIP` days are completely ignored in calculations
- It's as if that day never existed
- Score remains unchanged from previous day
- Useful for vacations, illness, etc.

**Example:**
```
Day 1: Score = 0.5, Check → Score = 0.526
Day 2: SKIP → Score stays 0.526 (no change)
Day 3: Check → Score calculated from 0.526 (Day 1's score)
```

**Implementation:** [ScoreListTest.kt](c:\Users\w11-d\src\gh\uhabits\uhabits-core\src\jvmTest\java\org\isoron\uhabits\core\models\ScoreListTest.kt#L423)
```kotlin
@Test
fun skipsShouldNotAffectScore() {
    addEntries(0, 500, 1000)
    val initialScore = habit.scores[today].value

    addEntries(500, 1000, SKIP)
    assertThat(habit.scores[today].value, closeTo(initialScore, E))
    // Score unchanged after 500 skip days!
}
```

### Rolling Window Calculation

From [ScoreList.kt](c:\Users\w11-d\src\gh\uhabits\uhabits-core\src\jvmMain\java\org\isoron\uhabits\core\models\ScoreList.kt#L82-L107):

**For Numerical Habits:**
- Uses rolling sum over denominator days
- `checkmarkValue = rollingSum / (numerator * targetValue)`
- Smooths out irregular completion patterns

**For Boolean Habits (non-daily):**
- Doubles numerator and denominator for smoothing
- Prevents score spikes from irregular day-of-week patterns
- Example: Weekly habit done Monday one week, Thursday next week

## Aggregate Score Calculation (Progress View)

### Algorithm

From [AggregateScoreCalculator.kt](c:\Users\w11-d\src\gh\uhabits\uhabits-android\src\main\java\org\isoron\uhabits\activities\habits\progress\AggregateScoreCalculator.kt):

```kotlin
fun computeAggregateScores(habits: List<Habit>, fromDate: Timestamp, toDate: Timestamp): List<Score>
```

**Steps for each day:**
1. For each active (non-archived) habit:
   - Get habit's score for that day
2. Sum all scores
3. Divide by number of habits
4. Result = aggregate score (0.0 to 1.0, or 0% to 100%)

**Formula:**
```
Aggregate Score = (Σ habit_scores) / total_habits

Where:
- habit_scores = individual habit scores (0.0 to 1.0)
- total_habits = count of non-archived habits
```

### Edge Cases

#### Empty Habit List
- Returns empty score list
- Widget shows 0.00000%

#### Single Habit
- Aggregate = that habit's score
- Aggregate is just tracking individual habit

#### Mixed Habit Types
- Boolean and numerical habits weighted equally
- Example:
  ```
  Habit 1 (Boolean): Score = 0.80 (80%)
  Habit 2 (Numerical AT_LEAST): Score = 0.60 (60%)
  Habit 3 (Numerical AT_MOST): Score = 0.90 (90%)
  
  Aggregate = (0.80 + 0.60 + 0.90) / 3 = 0.767 (76.7%)
  ```

#### Archived Habits
- Completely excluded from calculation
- Archiving a high-performing habit lowers aggregate
- Archiving a low-performing habit raises aggregate

#### New Habits
- Start with score = 0.0
- Pull aggregate down initially
- Gradually rise as user completes them

## Real-World Scenarios

### Scenario 1: User with 5 Daily Boolean Habits, All Completed Daily

**Setup:**
- 5 habits, all frequency = daily
- All checked every day for 90 days

**Result:**
- Each habit score → 0.99+
- Aggregate = (0.99 + 0.99 + 0.99 + 0.99 + 0.99) / 5 ≈ 0.99 (99%)

**Analysis:**
- Progress widget shows consistent 99%
- Streak counter increments daily (score increases daily)
- Very stable, predictable progress

### Scenario 2: User with 10 Habits (Mixed), Some Completed Daily

**Setup:**
- 5 boolean habits (daily)
- 5 numerical habits (daily, AT_LEAST type)
- User checks 3 boolean habits daily (60% completion)
- User partially completes numerical habits (50% average)

**Day 30 Example:**
```
Boolean habits:
  H1: Checked → Score ≈ 0.99
  H2: Checked → Score ≈ 0.99
  H3: Checked → Score ≈ 0.99
  H4: Not checked → Score ≈ 0.01
  H5: Not checked → Score ≈ 0.01

Numerical habits (all AT_LEAST, target=10):
  H6: Value=5 → checkmark=0.5 → Score ≈ 0.50
  H7: Value=5 → checkmark=0.5 → Score ≈ 0.50
  H8: Value=5 → checkmark=0.5 → Score ≈ 0.50
  H9: Value=5 → checkmark=0.5 → Score ≈ 0.50
  H10: Value=5 → checkmark=0.5 → Score ≈ 0.50

Aggregate = (0.99*3 + 0.01*2 + 0.50*5) / 10
         = (2.97 + 0.02 + 2.50) / 10
         = 5.49 / 10
         = 0.549 (54.9%)
```

**Analysis:**
- Mixed performance visible in aggregate
- Numerical habits at 50% credit (half target)
- Overall progress = 54.9%

### Scenario 3: User with Many Habits, None Completed

**Setup:**
- 20 habits (any mix)
- User stops tracking after initial setup

**Result:**
- All habits decay toward 0.0
- Daily habits decay fastest (~5% per day)
- After 30 days: most scores near 0.0
- Aggregate ≈ 0.01 (1%)

**Analysis:**
- Progress widget shows very low percentage
- No streaks (no improvement day-to-day)
- Clear signal to user: not making progress

### Scenario 4: User with Weekly Habits

**Setup:**
- 3 habits, all frequency = weekly (1x per 7 days)
- User completes each once per week (irregular days)

**Result:**
- After 9 months: each habit score ≈ 0.99
- Aggregate ≈ 0.99 (99%)

**Analysis:**
- Takes longer than daily habits to build score
- More forgiving of missed occasional days
- Good for users who don't want daily pressure

### Scenario 5: User Adds New Habit Mid-Journey

**Setup:**
- 5 existing habits with scores around 0.90 (90%)
- User adds 1 new habit (score = 0.0)

**Day 1 After Adding:**
```
Aggregate = (0.90*5 + 0.0*1) / 6 = 4.5 / 6 = 0.75 (75%)
```

**Day 30 (New habit completed daily):**
```
Aggregate = (0.90*5 + 0.99*1) / 6 = 5.49 / 6 = 0.915 (91.5%)
```

**Analysis:**
- Aggregate drops when new habit added
- Gradually recovers as new habit builds score
- User sees temporary dip in progress widget

### Scenario 6: Frequency Complexity - Every 6 Days

**Setup:**
- Habit frequency = 1 per 6 days (0.167 frequency)
- Includes overdues and auto-skip days

**How It Works:**
- System expects 1 completion per 6-day window
- If user completes on day 1, then day 7: on schedule
- If user misses day 6 window: score decays, but slowly
- Auto-skip days (from frequency settings): treated as SKIP
- Manual skip: user explicitly marks day as skipped

**Example Timeline:**
```
Day 1: Check → Score = 0.052
Day 2-6: No entry → Score slowly decays (multiplier ≈ 0.974 per day)
Day 7: Check → Score jumps back up
Day 13: Miss → Score decays more
Day 19: Check → Score recovers
```

**Overdue Calculation:**
- If interval passes without completion, entry becomes "overdue"
- Doesn't affect score calculation (score just decays normally)
- Visual indicator only (red highlight in UI)

## Why Progress Uses Score (Not Count)

**From todo.md user question:**
> "why progress by score? why not by count of done tasks?"

**Answer:**

### Complexity of "Done"

1. **Boolean Habits:**
   - "Done" is clear: YES or NO
   - Simple count would work

2. **Numerical Habits:**
   - What counts as "done"?
   - 50% of target? 100%? 80%?
   - Score handles partial completion automatically

3. **Mixed Habit Types:**
   - Can't compare "walked 5 miles" vs "meditated: yes"
   - Score normalizes both to 0.0-1.0 scale

### Frequency Problem

**Different Frequencies:**
```
Habit A: Daily (expected 30x per month)
Habit B: Weekly (expected 4x per month)
Habit C: Every 6 days (expected 5x per month)
```

**Count-Based Issues:**
```
Month 1:
  A: 25/30 done = 83% completion rate
  B: 3/4 done = 75% completion rate
  C: 4/5 done = 80% completion rate

How to aggregate these counts fairly?
- Average count: (25+3+4)/3 = 10.67 (meaningless number)
- Average percentage: (83+75+80)/3 = 79.3% (reasonable but reinvents score system)
```

**Score-Based Solution:**
- Each habit has score reflecting its performance vs its own frequency
- Already accounts for frequency in calculation
- Aggregate is simple mean of normalized scores

### Score Benefits

1. **Already Calculated:** Every habit already has a score (used for streaks, charts)
2. **Normalized:** All scores are 0.0-1.0, easy to average
3. **Momentum:** Scores show trend (building up / decaying), not just current state
4. **Frequency-Aware:** Automatically weighs habits by their expected frequency
5. **Partial Credit:** Handles numerical habits gracefully

### When Count Would Fail

**Example:**
```
User has:
- 10 daily habits
- 1 monthly habit

On any given day:
- Daily habits: 7/10 completed = 7 "done"
- Monthly habit: 0/1 completed = 0 "done"

Count = 7/11 = 63.6%

But monthly habit SHOULDN'T be expected today!
Score handles this: monthly habit score decays very slowly, doesn't drag down aggregate significantly on non-completion days.
```

## Technical Implementation Notes

### Score Storage
- Scores are lazy-computed and cached in `ScoreList`
- Recomputed when entries change via `habit.recompute()`
- Stored in memory, not persisted to database (computed from entries)

### Aggregate Calculation Performance
- For each day: iterates all active habits and sums scores
- With 100 habits and 365 days: 36,500 score lookups
- Scores are cached, so lookups are O(1) HashMap access
- Total complexity: O(days × habits)

### Streak Calculation
- Aggregate streak = consecutive days where aggregate score increased
- Uses same exponential smoothing as habit streaks
- Equal scores don't count as progress (strict improvement only)

## Conclusion

**Aggregate Progress by Score is Optimal Because:**
1. Handles boolean + numerical habits uniformly
2. Accounts for different frequencies automatically
3. Shows momentum/trend, not just snapshot
4. Reuses existing, well-tested score calculation
5. Simple to explain: "average of all your habit scores"

**Alternative (Count) Would Require:**
1. Define "completion" thresholds for numerical habits
2. Weight by frequency (reinventing score system)
3. Separate logic from existing score/streak systems
4. More complex edge cases

The score-based approach is simpler, more accurate, and leverages existing infrastructure.
