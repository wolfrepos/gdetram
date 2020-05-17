package io.github.oybek.gdetram.service

import io.github.oybek.gdetram.model.Platform

trait SpamServiceAlg[F[_]] {
  // creates row in 'message' table
  def createMessage(text: String): F[Int]

  // gives text field of first message not in delivered
  def getNotDeliveredMessageFor(user: (Platform, Long)): F[Option[String]]
}
