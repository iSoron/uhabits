**Discussion Title**

Proposal: Progress View — Daily Aggregate Progress & Progress Widget

**Discussion Details**

Overview
- This proposal introduces a new "Progress" view that surfaces an overall, per-day progress percentage computed from all active (non-archived) tracked habits. The goal is to give users a single, clear indicator answering "Am I making progress?" across all habits.

Why the name "Progress"
- Clear forward movement: progress literally communicates improvement over time.
- Aligns with streak logic: progress is treated as an increase in aggregate score.
- Matches user mental model: users commonly ask "Am I making progress?" rather than "What's my average score?".
- Semantic fit: "progress days" are days where the aggregate score increased vs the previous day.

UX & behavior
- Daily aggregate: for each day we compute the average score across all active (non-archived) habits that are tracked that day.
- Display: the Progress view shows the daily percentage for the selected date range and allows drilling into the details view (reusing the existing yes/no charts used by habit detail pages).
- Progress widget: a small widget is added to the main habit listing. It shows (1) today's avg %; (2) change in % vs previous day; (3) progress/streak days count (TBD — initial implementation shows placeholders until counting logic is added).
- Streak rule: a day counts as a progress day when the daily aggregate increases (delta > 0). Consecutive progress days contribute to a progress streak.

Implementation notes (high level)
- Reuse the existing yes/no chart components from the habit details view for the Progress detail screen.
- Aggregation logic computes: today's avg = mean(score_i) for i in active tracked habits; previous day's avg computed similarly; delta = today - previous. All calculations exclude archived habits.
- Performance: compute aggregates off the main UI thread and cache per-day results to avoid UI jank for users with many habits.

Open questions for discussion
- Naming: confirm "Progress" is preferred vs alternatives ("Aggregate", "Overview", "Momentum").
- Placement: should the Progress widget be visible by default on the main listing, or an opt-in/optional widget?
- Streak semantics: should equal days (delta == 0) break streaks or be neutral? (current proposal: only positive delta counts as progress)
- Telemetry / analytics: do we want to record aggregate progress changes for A/B testing or analysis?

---

**Pull Request Description (Technical Draft)**

Summary
- Adds a new overall Progress view and a small Progress widget in the main habit listing. The Progress view aggregates per-habit scores for each day and presents a daily percentage representing the user's overall progress. The detail view reuses the existing yes/no type charts from habit details.

Motivation
- Users often want a single, simple metric that answers "Am I making progress?" across all habits. Individual habit details are useful, but an aggregate view makes trends and streaks easier to understand.

Key behavior
- Aggregate calculation: for each day, compute the average score across all non-archived tracked habits.
- Progress day: a day is considered a progress day if the aggregate percentage increased compared to the previous day (delta > 0).
- Widget: displays today's average %, the delta vs previous day (signed), and a counter for progress/streak days (initial implementation shows the first two values; progress/streak counter will be added in a follow-up if desired).

Files / modules touched (conceptual)
- UI: add `ProgressView` screen and `ProgressWidget` component integrated into the main habit-listing layout.
- Charts: reuse `YesNoChart` / `HabitDetailCharts` components for the Progress detail screen.
- Domain / data: add `ProgressAggregator` service that computes per-day aggregates and exposes an API for UI consumption. Move heavy computation off the UI thread and add simple in-memory or on-disk caching keyed by date range.
- Tests: add unit tests for `ProgressAggregator` covering edge cases (no habits, all archived, score ties, large habit sets) and a small UI test for widget display.

Implementation details
- Aggregation algorithm:
  - Fetch all active (non-archived) tracked habits for the target date range.
  - For each date, compute habit-level scores (existing score calculation logic) and take the arithmetic mean across habits with valid scores that day.
  - Persist/cache the computed daily aggregates to avoid recomputing for every UI render.
  - Delta calculation: `delta = todayAvg - previousDayAvg`.

- Threading / performance:
  - All aggregation runs on a background coroutine / worker thread and posts results to the UI via LiveData/Flow/State.
  - Cache eviction: simple TTL or LRU keyed by date range should suffice; we can optimize based on profiling.

Testing & QA
- Unit tests for `ProgressAggregator` covering:
  - Empty set of habits
  - All habits archived
  - Single habit
  - Multiple habits with different scoring
  - Consecutive days with increases, decreases, and ties
- Integration / UI tests:
  - Verify `ProgressWidget` shows today's average and correct delta vs yesterday.
  - Verify `ProgressView` charts render and match aggregator output.

How to test locally
1. Build and run the app on an emulator or device.
2. Create several habits (mix of yes/no and scored types), mark some archived to confirm exclusion.
3. Populate a few days of activity (via the UI or test helpers) and open the main listing — confirm the Progress widget displays today's avg and delta.
4. Open the Progress view and verify charts and numbers match the expected aggregates.

Migration / backwards compatibility
- No db schema changes expected. If any caching or persisted aggregates are added, they should be optional and regeneratable.

Risks & mitigations
- Performance with large habit sets: mitigate by moving calculations off the main thread and caching.
- Naming / UX confusion: PR includes a discussion so maintainers can decide naming and placement.

Notes / follow-ups
- Add progress/streak days counter (complete counting logic) and an optional toggle to include/exclude specific habits from the aggregate.
- Consider exposing an API for third-party widgets to read the daily aggregate.

Screenshots / design
- This draft does not include final mockups. Screenshots and designs will be provided in a follow-up commit if maintainers agree with the approach.

Request for maintainers
- Please review the naming and placement decisions. Feedback on whether progress days should be delta > 0 or delta >= 0 is especially welcome.
