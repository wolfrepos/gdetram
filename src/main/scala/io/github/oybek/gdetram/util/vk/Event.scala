package io.github.oybek.gdetram.util.vk

import io.circe.{Decoder, DecodingFailure, HCursor}
import io.circe.generic.extras.Configuration
import io.circe.generic.extras.semiauto._
import io.circe.generic.extras.auto._
import org.http4s.DecodeFailure

sealed trait Event

// --
case class MessageNew(id: Long,
                      date: Long,
                      peerId: Long,
                      fromId: Long,
                      text: String,
                      geo: Option[Geo])
    extends Event

case class Geo(coordinates: Coord, place: Option[Place])
case class Place(country: String, city: String, title: String)
case class Coord(latitude: Float, longitude: Float) {
  def sq(x: Float) = x * x
  def distSq(c: Coord): Float =
    sq(c.latitude - latitude) + sq(c.longitude - longitude)
}

// --
case class WallPostNew(id: Long,
                       fromId: Long,
                       ownerId: Long,
                       date: Long,
                       markedAsAds: Option[Long],
                       text: String,
                       signerId: Option[Long],
                       postType: Option[String],
                       geo: Option[Geo])
    extends Event

case class WallReplyNew(id: Long,
                        fromId: Long,
                        text: String,
                        postId: Long,
                        date: Long)
    extends Event

object Event {
  implicit val customConfig: Configuration =
    Configuration.default.withSnakeCaseMemberNames

  implicit val decodeMessageNew: Decoder[MessageNew] =
    deriveConfiguredDecoder[MessageNew]

  implicit val decodeGeo: Decoder[Geo] =
    (c: HCursor) =>
      for {
        coordEither <- c.downField("coordinates").as[Either[String, Coord]]
        coord = coordEither match {
          case Left(s) =>
            val coords = s.split(' ')
            Coord(coords(0).toFloat, coords(1).toFloat)
          case Right(coord) => coord
        }
        place <- c.downField("place").as[Option[Place]]
        res = Geo(coord, place)
      } yield res

  implicit val decodeEvent: Decoder[Event] =
    (c: HCursor) =>
      for {
        typee <- c.downField("type").as[String]
        res <- typee match {
          case "message_new" =>
            c.downField("object").downField("message").as[MessageNew]
          case "wall_post_new"  => c.downField("object").as[WallPostNew]
          case "wall_reply_new" => c.downField("object").as[WallReplyNew]
          case eventType =>
            Left(DecodingFailure(s"Unknown event type: $eventType", List()))
        }
      } yield res

  implicit def decodeEither[A, B](implicit a: Decoder[A],
                                  b: Decoder[B]): Decoder[Either[A, B]] = {
    val l: Decoder[Either[A, B]] = a.map(Left.apply)
    val r: Decoder[Either[A, B]] = b.map(Right.apply)
    l or r
  }
}
