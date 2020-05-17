package io.github.oybek.gdetram.util.vk.api

import io.circe.Decoder
import io.circe.generic.extras.Configuration
import io.github.oybek.gdetram.util.vk.WallPostNew
import io.circe.generic.extras.semiauto._
import io.circe.generic.extras.auto._

case class WallGetReq(ownerId: Long,
                      offset: Long,
                      count: Long,
                      version: String,
                      accessToken: String)
    extends Req {
  def toRequestStr: String =
    Seq(
      "owner_id" -> ownerId,
      "offset" -> offset,
      "count" -> count,
      "v" -> version,
      "access_token" -> accessToken
    ).map {
        case (k, v) => k + "=" + v
      }
      .mkString("&")
}

case class WallGetRes(response: Response)
case class Response(count: Long, items: List[WallPostNew])

object WallGetRes {
  import io.github.oybek.gdetram.util.vk.Event.decodeGeo

  implicit val customConfig: Configuration =
    Configuration.default.withSnakeCaseMemberNames

  implicit val decodeWallGet: Decoder[WallGetRes] =
    deriveConfiguredDecoder[WallGetRes]
}
