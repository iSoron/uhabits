create index idx_score_habit_timestamp on Score(habit, timestamp);
create index idx_checkmark_habit_timestamp on Checkmarks(habit, timestamp);
create index idx_repetitions_habit_timestamp on Repetitions(habit, timestamp);
create index idx_streak_habit_end on Streak(habit, end);