package io.github.oybek.gdetram.domain

import doobie.util.meta.Meta
import doobie.postgres.implicits._

sealed trait Platform

object Platform {

  case object Vk extends Platform
  case object Tg extends Platform

  def toEnum(e: Platform): String =
    e match {
      case Vk => "vk"
      case Tg => "tg"
    }

  def fromEnum(s: String): Option[Platform] =
    Option(s) collect {
      case "vk" => Vk
      case "tg" => Tg
    }

  implicit val platformMeta: Meta[Platform] =
    pgEnumStringOpt("PLATFORM", Platform.fromEnum, Platform.toEnum)
}
