alter table Habits add column question text;

update Habits set question = description;

update Habits set description = "";