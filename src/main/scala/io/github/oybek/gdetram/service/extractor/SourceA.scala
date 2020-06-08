package io.github.oybek.gdetram.service.extractor

import cats.effect.{Clock, Sync}
import cats.syntax.all._
import io.github.oybek.gdetram.domain.Stop
import io.github.oybek.plato.model.Arrival
import io.github.oybek.plato.parser.ParserA

class SourceA[F[_]: Sync: Clock](
  implicit documentFetcher: DocumentFetcherAlg[F]
) extends ExtractorAlg[F] {

  override def extractInfo(stop: Stop): F[List[(String, List[Arrival])]] =
    documentFetcher.fetchCached(stop.url).map { doc =>
      doc.map(ParserA.parse).getOrElse(List())
    }
}
