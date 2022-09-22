package demo.feed.model

case class ScoreboardMessage(
    id: String,
    stage: Int,
    scores: Seq[Score],
    timer: Option[Timer] = None,
)
