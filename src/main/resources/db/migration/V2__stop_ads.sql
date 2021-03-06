
create table stop_ads (
    stop_id  integer references stop(id),
    time     timestamp not null,
    platform platform not null,
    user_id  varchar not null,
    text     varchar not null
);
