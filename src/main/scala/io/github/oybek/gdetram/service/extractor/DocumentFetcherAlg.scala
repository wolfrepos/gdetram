package io.github.oybek.gdetram.service.extractor

import org.jsoup.nodes.Document

trait DocumentFetcherAlg[F[_]] {
  def fetchCached(url: String): F[Option[Document]]
}
