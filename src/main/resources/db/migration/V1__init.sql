CREATE EXTENSION fuzzystrmatch;
CREATE TYPE PLATFORM AS ENUM('vk', 'tg');

--
CREATE TABLE city (
    id          SERIAL PRIMARY KEY,
    name        VARCHAR NOT NULL,
    latitude    REAL NOT NULL,
    longitude   REAL NOT NULL
);

INSERT INTO city(id, name, latitude, longitude) values
    (1, 'Екатеринбург', 56.833332, 60.583332),
    (2, 'Казань', 55.7887383, 49.122139),
    (3, 'Пермь', 58.1130346, 56.2789048);

--
CREATE TABLE stop (
    id        SERIAL PRIMARY KEY,
    name      VARCHAR NOT NULL,
    latitude  REAL NOT NULL,
    longitude REAL NOT NULL,
    url       VARCHAR NOT NULL,
    city_id INTEGER REFERENCES city(id) NOT NULL
);

--
CREATE TABLE journal (
    stop_id INTEGER REFERENCES stop(id),
    time TIMESTAMP NOT NULL,
    user_id VARCHAR NOT NULL,
    text VARCHAR NOT NULL,
    platform PLATFORM NOT NULL
);

--
CREATE TABLE message(
    id SERIAL PRIMARY KEY,
    text VARCHAR NOT NULL
);

CREATE TABLE delivered(
    message_id INTEGER REFERENCES message(id),
    user_id VARCHAR NOT NULL,
    platform PLATFORM NOT NULL
);

CREATE TABLE usr (
    platform    PLATFORM NOT NULL,
    id          INTEGER NOT NULL,
    city_id     INTEGER NOT NULL REFERENCES city(id),
    PRIMARY KEY (platform, id)
);

create table daily_metrics(
    date_when timestamp not null,
    city_id integer not null references city(id),
    active integer not null,
    passive integer not null
);
