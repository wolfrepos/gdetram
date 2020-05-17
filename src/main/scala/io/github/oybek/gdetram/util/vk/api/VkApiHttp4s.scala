package io.github.oybek.gdetram.util.vk.api

import cats.MonadError
import cats.effect.{ConcurrentEffect, ContextShift, Sync}
import cats.implicits._
import io.circe.generic.auto._
import org.http4s._
import org.http4s.circe._
import org.http4s.client.Client
import org.http4s.dsl.io._

import scala.concurrent.ExecutionContext

class VkApiHttp4s[F[_]: ConcurrentEffect: ContextShift](client: Client[F])(
  implicit F: MonadError[F, Throwable],
  ec: ExecutionContext
) extends VkApi[F] {

  private lazy val baseUrl = "https://api.vk.com"
  private lazy val methodUrl = baseUrl + "/method"

  override def getLongPollServer(
    getLongPollServerReq: GetLongPollServerReq
  ): F[GetLongPollServerRes] = {
    for {
      uri <- F.fromEither[Uri](
        Uri.fromString(
          s"$methodUrl/groups.getLongPollServer?${getLongPollServerReq.toRequestStr}"
        )
      )
      req = Request[F]().withMethod(GET).withUri(uri)
      res <- client.expect(req)(jsonOf[F, GetLongPollServerRes])
    } yield res
  }

  def poll(pollReq: PollReq): F[PollRes] = {
    for {
      uri <- F.fromEither[Uri](Uri.fromString(s"${pollReq.toRequestStr}"))
      req = Request[F]().withMethod(GET).withUri(uri)
      res <- client.expect(req)(jsonOf[F, PollRes])
    } yield res
  }

  override def sendMessage(sendMessageReq: SendMessageReq): F[String] = {
    for {
      uri <- F.fromEither[Uri](Uri.fromString(s"$methodUrl/messages.send"))
      entity = sendMessageReq.toMultipart[F]
      req = Request[F]()
        .withMethod(POST)
        .withUri(uri)
        .withEntity(entity)
        .withHeaders(entity.headers)
      res <- client.expect[String](req)
      _ <- Sync[F].delay { println(s"sendMessage: $res") }
    } yield res
  }

  override def wallComment(wallCommentReq: WallCommentReq): F[WallCommentRes] =
    for {
      uri <- F.fromEither[Uri](
        Uri.fromString(
          s"$methodUrl/wall.createComment?${wallCommentReq.toRequestStr}"
        )
      )
      req = Request[F]()
        .withMethod(POST)
        .withUri(uri)
      res <- client.expect(req)(jsonOf[F, WallCommentRes])
    } yield res

  override def wallGet(wallGetReq: WallGetReq): F[WallGetRes] =
    for {
      uri <- F.fromEither[Uri](
        Uri.fromString(s"$methodUrl/wall.get?${wallGetReq.toRequestStr}")
      )
      req = Request[F]()
        .withMethod(POST)
        .withUri(uri)
      res <- client.expect(req)(jsonOf[F, WallGetRes])
    } yield res
}
