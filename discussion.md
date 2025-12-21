# Proposal: Progress View — Daily Aggregate Progress & Progress Widget

Personally, this feature adds value for me and I want to understand whether other users feel the same.

A little about me: I'm a novice in FOSS and eager to learn and start contributing. As an SDET by profession, I’ve been happily using uHabits for a while. The idea of tracking daily progress has been on my mind for a long time, and I’ve explored it in various small projects using Python and web technologies. Recently, I decided to try implementing a proof-of-concept directly in uHabits to see how effective it could be. With no prior Android app development experience, I’ve been working on this for the past couple of days, using AI assistance to refine the implementation. The screenshots below are from this experiment.

This proposal introduces a new "Progress" view that surfaces an overall, per-day progress percentage computed from all active (non-archived) tracked habits. The goal is to give users a single, clear indicator that answers "Am I making progress?" across all habits — a quick way to tell whether today's changes represent real forward movement.

### Why the name "Progress"
- Clear forward movement: "progress" directly communicates improvement over time.
- Aligns with streak logic: progress is treated as an increase in the aggregate score.
- Matches user mental model: users commonly ask "Am I making progress?" rather than "What's my average score?".
- Semantic fit: "progress days" are days when the aggregate score increased compared to the previous day.

Why I'm asking the community
- Personal need: from a user's point of view, I wanted a single metric to answer whether I was making meaningful overall progress on any given day. Individual habit detail pages are useful, but they don't answer the aggregate question quickly.
- Validation: I built a small POC and used it locally for several days; the results felt useful to me, and I'm sharing screenshots and this proposal to see whether this is valuable to others and worth polishing into a PR.
- Collaboration: I'm seeking feedback on naming, placement, streak semantics , and whether the widget should be shown by default or optional. [ currently it's opt in via a toggle in settign screen ]

### Implementation notes (high level)
- Reuse the existing yes/no chart components from the habit details view for the Progress detail screen.
- Aggregation logic computes: today's avg = mean(score_i) for i in active tracked habits; previous day's avg is computed similarly; delta = today - previous. All calculations exclude archived habits.

## The images below show the UI from my local POC:

1. A gist in the main / all-habits listing screen (thinking of adding the progress days / streak count to the widget)

![Progress gist via widget in main screen (cropped)](https://github.com/user-attachments/assets/d9041c24-f5ef-49b9-b1dd-28ac07229efd)

2. The detailed view

![Positive progress detailed view](https://github.com/user-attachments/assets/21de9d1a-26b5-46d6-ade1-cc3a87a6ef55)
