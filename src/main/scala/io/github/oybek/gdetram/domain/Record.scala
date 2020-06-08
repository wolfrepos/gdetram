package io.github.oybek.gdetram.domain

import java.sql.Timestamp

case class Record(stopId: Int,
                  time: Timestamp,
                  userId: String,
                  text: String,
                  platform: Platform)
