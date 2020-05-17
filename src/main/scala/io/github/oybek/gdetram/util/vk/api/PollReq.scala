package io.github.oybek.gdetram.util.vk.api

import cats.syntax.functor._
import io.circe.Decoder
import io.circe.generic.auto._
import io.github.oybek.gdetram.util.vk.Event

case class PollReq(server: String,
                   act: String = "a_check",
                   key: String,
                   ts: Long,
                   waitt: Long) {
  def toRequestStr: String = {
    server + "?" +
      Seq("act" -> act, "key" -> key, "ts" -> ts, "wait" -> waitt)
        .map {
          case (k, v) => k + "=" + v
        }
        .mkString("&")
  }
}

sealed trait PollRes
case class PollWithUpdates(ts: Long, updates: List[Event]) extends PollRes
case class PollFailed(ts: Option[Long], failed: Long) extends PollRes

object PollRes {
  implicit val PollResDecoder: Decoder[PollRes] =
    List[Decoder[PollRes]](
      Decoder[PollWithUpdates].widen,
      Decoder[PollFailed].widen
    ).reduce(_ or _)
}
