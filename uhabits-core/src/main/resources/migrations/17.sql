drop table Score;
create table Score (
    id integer primary key autoincrement,
    habit integer references habits(id),
    score real,
    timestamp integer);

create index idx_score_habit_timestamp on Score(habit, timestamp);

delete from streak;
delete from checkmarks;