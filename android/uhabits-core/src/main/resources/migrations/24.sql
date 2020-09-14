alter table Repetitions add column manualInput INTEGER not null default 0;

update repetitions set manualInput=1 where value>1 and (select type from habits where id=repetitions.habit)=0; -- yes/no habit
update repetitions set manualInput=1 where value>0 and (select type from habits where id=repetitions.habit)=1; -- numerical habit
