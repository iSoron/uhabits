create index idx_score_habit_timestamp on score(habit, timestamp);
create index idx_checkmark_habit_timestamp on checkmarks(habit, timestamp);
create index idx_repetitions_habit_timestamp on repetitions(habit, timestamp);
create index idx_streak_habit_end on streak(habit, end);