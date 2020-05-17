package io.github.oybek.gdetram.service.extractor

import org.jsoup.nodes.Document

trait DocumentFetcherAlg[F[_]] {
  def fetch(url: String): F[Option[Document]]
}
