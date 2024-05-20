create table Habits (
    id integer primary key autoincrement,
    archived integer,
    color integer,
    description text,
    freq_den integer,
    freq_num integer,
    highlight integer,
    name text,
    position integer,
    reminder_hour integer,
    reminder_min integer
);

create table Checkmarks (
    id integer primary key autoincrement,
    habit integer references habits(id),
    timestamp integer,
    value integer
);

create table Repetitions (
    id integer primary key autoincrement,
    habit integer references habits(id),
    timestamp integer
);

create table Streak (
    id integer primary key autoincrement,
    end integer,
    habit integer references habits(id),
    length integer,
    start integer
);

create table Score (
    id integer primary key autoincrement,
    habit integer references habits(id),
    score integer,
    timestamp integer
);
