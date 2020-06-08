package io.github.oybek.gdetram.domain

import cats.data.NonEmptyList

case class Stop(id: Int,
                name: String,
                latitude: Float,
                longitude: Float,
                url: String,
                city: City)
