alter table Habits add column target_type integer not null default 0;
alter table Habits add column target_value real not null default 0;
alter table Habits add column unit text not null default "";