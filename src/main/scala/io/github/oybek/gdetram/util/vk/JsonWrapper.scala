package io.github.oybek.gdetram.util.vk

import io.circe.{Json, JsonObject}

object Util {
  implicit class JsonWrapper(val json: Json) {
    def removeNulls(): Json = {
      val result = json match {
        case j1 if j1.isArray =>
          j1.mapArray(_.map(_.removeNulls()))

        case j2 if j2.isObject =>
          j2.mapObject(_.removeNulls())

        case v => v
      }
      result
    }
  }

  implicit class JsonObjectWrapper(val json: JsonObject) {
    def removeNulls(): JsonObject = {
      json
        .mapValues {
          case v if v.isObject => v.removeNulls()
          case v if v.isArray  => v.mapArray(_.map(_.removeNulls()))
          case other           => other
        }
        .filter(!_._2.isNull)
    }
  }
}
