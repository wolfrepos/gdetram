--
create table sync_message (
    platform Platform not null,
    id       bigint not null,
    text     varchar not null
);

create table async_message (
    platform Platform not null,
    id       bigint not null,
    text     varchar not null
);

drop table delivered;
drop table message;
