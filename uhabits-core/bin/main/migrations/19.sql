create table Events (
    id integer primary key autoincrement,
    timestamp integer,
    message text,
    server_id integer
);