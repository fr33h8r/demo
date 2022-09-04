package demo.feed.model

case class ScoreboardMessage(
    id: String,
    traceId: Option[String] = None,
    stage: Int,
    scores: Seq[Score],
    timer: Option[Timer] = None,
    server: Option[String] = None,
    timestamp: Option[Long] = None
)
