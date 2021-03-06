package io.github.oybek.gdetram.domain.chain.model

sealed trait Input
case class Text(text: String) extends Input
case class Geo(latitude: Float, longitude: Float) extends Input
