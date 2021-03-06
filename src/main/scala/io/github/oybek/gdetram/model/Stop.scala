package io.github.oybek.gdetram.domain.model

case class Stop(id: Int,
                name: String,
                latitude: Float,
                longitude: Float,
                url: String,
                city: City)
