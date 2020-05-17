package io.github.oybek.gdetram.model

import cats.data.NonEmptyList

case class Stop(id: Int,
                name: String,
                latitude: Float,
                longitude: Float,
                url: String,
                city: City)
