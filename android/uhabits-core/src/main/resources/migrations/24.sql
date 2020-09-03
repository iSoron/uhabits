alter table Habits add column active_days integer not null default 127;

update Habits set active_days = reminder_days
