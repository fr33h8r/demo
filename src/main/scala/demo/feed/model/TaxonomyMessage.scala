package demo.feed.model

case class TaxonomyMessage(
    sport: String,
    status: Int,
    traceId: Option[String] = None,
    timestamp: Option[Long] = None
)
