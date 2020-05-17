package io.github.oybek.gdetram.util.vk.api

case class GetLongPollServerReq(groupId: String,
                                accessToken: String,
                                version: String) {
  def toRequestStr: String = {
    Seq("group_id" -> groupId, "access_token" -> accessToken, "v" -> version)
      .map {
        case (k, v) => k + "=" + v
      }
      .mkString("&")
  }
}

case class GetLongPollServerRes(response: LongPollServer)
case class LongPollServer(key: String, server: String, ts: Long)
