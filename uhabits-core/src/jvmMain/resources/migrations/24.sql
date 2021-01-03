alter table habits add column uuid text;
update habits set uuid = lower(hex(randomblob(16) || id));