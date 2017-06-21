delete from repetitions where habit not in (select id from habits);
delete from repetitions where timestamp is null;
delete from repetitions where habit is null;
delete from repetitions where rowid not in (
    select min(rowid) from repetitions group by habit, timestamp
);

begin transaction;

    alter table Repetitions rename to RepetitionsBak;
    create table Repetitions (
        id integer primary key autoincrement,
        habit integer not null references habits(id),
        timestamp integer not null,
        value integer not null);
    drop index idx_repetitions_habit_timestamp;
    create unique index idx_repetitions_habit_timestamp on Repetitions(
        habit, timestamp);
    insert into Repetitions select * from RepetitionsBak;
    drop table RepetitionsBak;

commit;

pragma foreign_keys=ON;