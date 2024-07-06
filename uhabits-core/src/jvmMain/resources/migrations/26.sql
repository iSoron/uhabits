create table HabitGroups (
    id integer primary key autoincrement,
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

alter table Habits add column parent_uuid text references habitgroups(uuid);
alter table Habits add column parent_id integer references habitgroups(id);