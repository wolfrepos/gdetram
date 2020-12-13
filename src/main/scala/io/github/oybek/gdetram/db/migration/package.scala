package io.github.oybek.gdetram.db

import doobie.implicits._
import io.github.oybek.dbrush.model.{Migration => M}

package object migration {

  // only appends to list
  val migrations =
    List(
      M("Initialize", createTables, createMsgTables),
      M("Drop flyway_schema_history", dropFlyway),
    )

  private lazy val dropFlyway = sql"drop table if exists flyway_schema_history"
  private lazy val createTables =
    sql"""
         |CREATE EXTENSION fuzzystrmatch;
         |CREATE TYPE PLATFORM AS ENUM('vk', 'tg');
         |
         |CREATE TABLE city (
         |    id          SERIAL PRIMARY KEY,
         |    name        VARCHAR NOT NULL,
         |    latitude    REAL NOT NULL,
         |    longitude   REAL NOT NULL
         |);
         |
         |INSERT INTO city(id, name, latitude, longitude) values
         |    (1, 'Екатеринбург', 56.833332, 60.583332),
         |    (2, 'Казань', 55.7887383, 49.122139),
         |    (3, 'Пермь', 58.1130346, 56.2789048),
         |    (4, 'Хельсинки', 60.1695, 24.9354);
         |
         |CREATE TABLE stop (
         |    id        SERIAL PRIMARY KEY,
         |    name      VARCHAR NOT NULL,
         |    latitude  REAL NOT NULL,
         |    longitude REAL NOT NULL,
         |    url       VARCHAR NOT NULL,
         |    city_id INTEGER REFERENCES city(id) NOT NULL
         |);
         |
         |CREATE TABLE journal (
         |    stop_id INTEGER REFERENCES stop(id),
         |    time TIMESTAMP NOT NULL,
         |    user_id VARCHAR NOT NULL,
         |    text VARCHAR NOT NULL,
         |    platform PLATFORM NOT NULL
         |);
         |
         |CREATE TABLE message(
         |    id SERIAL PRIMARY KEY,
         |    text VARCHAR NOT NULL
         |);
         |
         |CREATE TABLE delivered(
         |    message_id INTEGER REFERENCES message(id),
         |    user_id VARCHAR NOT NULL,
         |    platform PLATFORM NOT NULL
         |);
         |
         |CREATE TABLE usr (
         |    platform    PLATFORM NOT NULL,
         |    id          INTEGER NOT NULL,
         |    city_id     INTEGER NOT NULL REFERENCES city(id),
         |    PRIMARY KEY (platform, id)
         |);
         |
         |create table daily_metrics(
         |    date_when timestamp not null,
         |    city_id integer not null references city(id),
         |    active integer not null,
         |    passive integer not null
         |);
         |""".stripMargin

  private lazy val createMsgTables =
    sql"""
         |create table sync_message (
         |    platform Platform not null,
         |    id       bigint not null,
         |    text     varchar not null
         |);
         |
         |create table async_message (
         |    platform Platform not null,
         |    id       bigint not null,
         |    text     varchar not null
         |);
         |
         |drop table delivered;
         |drop table message;
         |""".stripMargin
}
