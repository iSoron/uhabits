create table SharedIds (
    name text primary key,
    next_id integer not null
);

insert into SharedIds (name, next_id) values ('habitandgroup', (select coalesce(max(id),0) from Habits) + 1 );

alter table Habits rename to HabitsOld;

create table Habits (
    id integer primary key,
    archived integer,
    color integer,
    description text,
    freq_den integer,
    freq_num integer,
    highlight integer,
    name text,
    position integer,
    reminder_hour integer,
    reminder_min integer,
    reminder_days integer not null default 127,
    type integer not null default 0,
    target_type integer not null default 0,
    target_value real not null default 0,
    unit text not null default "",
    question text,
    uuid text
);

insert into Habits (id, archived, color, description, freq_den, freq_num, highlight, name, position, reminder_min, reminder_days, type, target_type, target_value, unit, question, uuid)
select id, archived, color, description, freq_den, freq_num, highlight, name, position, reminder_min, reminder_days, type, target_type, target_value, unit, question, uuid
from HabitsOld;

drop table HabitsOld;

create table HabitGroups (
    id integer primary key,
    archived integer,
    color integer,
    description text not null default "",
    highlight integer,
    name text,
    position integer,
    reminder_days integer not null default 127,
    reminder_hour integer,
    reminder_min integer,
    question text not null default "",
    uuid text
);

alter table Habits add column group_uuid text references habitgroups(uuid);
alter table Habits add column group_id integer references habitgroups(id);