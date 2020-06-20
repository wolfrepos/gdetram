package io.github.oybek.gdetram.util.vk.api

import java.net.URLEncoder

import io.circe.{Encoder, Json, Printer}
import io.circe.generic.extras.Configuration
import io.circe.generic.extras.semiauto._
import io.circe.syntax._
import io.github.oybek.gdetram.util.vk.Util._
import org.http4s.multipart.Multipart
import org.http4s.multipart.Part
import cats.effect.Sync

case class SendMessageReq(peerId: Long,
                          message: String,
                          version: String,
                          randomId: Long,
                          accessToken: String,
                          attachment: Option[String] = None,
                          keyboard: Option[Keyboard] = None)
    extends Req {
  def toRequestStr: String = ""

  def toMultipart[F[_]: Sync]: Multipart[F] =
    Multipart[F](
      Vector(
        "peer_id" -> Some(peerId.toString),
        "access_token" -> Some(accessToken),
        "v" -> Some(version),
        "random_id" -> Some(randomId.toString),
        "message" -> Some(message),
        "attachment" -> attachment,
        "keyboard" -> keyboard.map(x =>
          Printer.noSpaces
            .copy(dropNullValues = true)
            .print(x.asJson)
        )
      ).collect { case (k, Some(v)) => k -> v }
        .map {
          case (k, v) => Part.formData[F](k, v)
        }
    )
}

case class Keyboard(oneTime: Option[Boolean] = None, inline: Boolean, buttons: List[List[Button]])
case class Button(action: Action, color: Option[String] = None)
case class Action(`type`: String,
                  link: Option[String] = None,
                  label: Option[String] = None,
                  payload: Option[String] = None,
                  hash: Option[String] = None)

object Keyboard {
  implicit val customConfig: Configuration =
    Configuration.default.withSnakeCaseMemberNames

  implicit val encodeKeyboard: Encoder[Keyboard] = deriveConfiguredEncoder
  implicit val encodeButton: Encoder[Button] = deriveConfiguredEncoder
  implicit val encodeAction: Encoder[Action] = deriveConfiguredEncoder
}

case class SendMessageRes(response: Option[Long])
