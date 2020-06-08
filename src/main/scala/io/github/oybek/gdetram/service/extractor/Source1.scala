package io.github.oybek.gdetram.service.extractor

import cats.effect.{Clock, Sync}
import cats.syntax.all._
import io.github.oybek.gdetram.model.Stop
import io.github.oybek.plato.model.Arrival
import io.github.oybek.plato.parser.Parser001

class Source1[F[_]: Sync: Clock](
  implicit documentFetcher: DocumentFetcherAlg[F]
) extends ExtractorAlg[F] {

  override def extractInfo(stop: Stop): F[List[(String, List[Arrival])]] =
    documentFetcher.fetchCached(stop.url).map { doc =>
      doc.map(Parser001.parse).getOrElse(List())
    }
}
