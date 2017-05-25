DROP TABLE Score;
CREATE TABLE Score (Id INTEGER PRIMARY KEY AUTOINCREMENT, habit INTEGER REFERENCES Habits(Id), score REAL, timestamp INTEGER);
CREATE INDEX idx_score_habit_timestamp on score(habit, timestamp);
delete from Streak;
delete from Checkmarks;