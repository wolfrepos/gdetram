package io.github.oybek.gdetram.service.extractor

import io.github.oybek.gdetram.model.Stop
import io.github.oybek.plato.model.Arrival

trait ExtractorAlg[F[_]] {
  def extractInfo(stop: Stop): F[List[(String, List[Arrival])]]
}
