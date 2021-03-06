package io.github.oybek.gdetram.domain.model

import java.sql.Timestamp

case class UserInfo(user: User, lastWriteTime: Timestamp)
