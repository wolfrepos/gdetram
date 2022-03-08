package io.github.oybek.gdetram.service.impl

import cats.implicits.toFunctorOps
import cats.implicits.catsSyntaxFlatMapOps
import doobie.ConnectionIO
import doobie.implicits.toSqlInterpolator
import doobie.util.update.Update0
import io.github.oybek.gdetram.service.UserService

object UserServiceImpl extends UserService[ConnectionIO] {
  override def refreshUserInfo: ConnectionIO[Unit] =
    sql"update user_info set last_month_active_days = 0".update.run.void >>
      refreshUserInfoQ.run.void

  val refreshUserInfoQ: Update0 =
    sql"""with
            month_messages as
            (select platform, user_id, time from journal where time > now() - interval '30 days' ),
            active_days as
            (select count(distinct(date(time))), platform, user_id from month_messages group by (user_id, platform))
          update user_info
          set last_month_active_days = active_days.count
          from active_days
          where user_info.platform = active_days.platform and user_info.id::varchar = active_days.user_id""".update
}
