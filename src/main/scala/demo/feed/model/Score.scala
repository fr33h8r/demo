package demo.feed.model

case class Score(
    interval: Int,
    result: Int,
    score: String,
    subInterval: Option[Int] = None
)
