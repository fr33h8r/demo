package demo.parimatch

import demo.feed.model.{
  ScoreboardMessage,
  TaxonomyMessage,
  Score => FeedScore,
  Status => FeedStatus
}
import demo.parimatch.domain.EventStatuses.EventStatus
import demo.parimatch.domain._
import demo.parimatch.model.{Period, PeriodKey, Scoreboard}

package object translators {
  val sportClockSettings: Map[String, SportClockSetting] = Map(
    "Fifa" -> SportClockSetting(
      autoClock = true,
      frequency = 60
    )
  )

  def translate(
      taxonomy: TaxonomyMessage,
      scoreboard: ScoreboardMessage
  ): Option[Scoreboard] =
    taxonomy.sport match {
      case "CounterStrike" =>
        esports.counterstrike.translate(scoreboard, taxonomy.status)
      case "Dota2" => esports.dota2.translate(scoreboard, taxonomy.status)
      case "Fifa"  => fifa.translate(scoreboard, taxonomy.status)
    }

  def toScoreboard(
      scoreboard: ScoreboardMessage,
      taxonomy: TaxonomyMessage
  ): Option[Scoreboard] = translate(taxonomy, scoreboard)

  def toEventStatus(
      taxonomyStatus: Int,
      scoreboardStage: Int,
      scoreboardStatus: Int => Option[EventStatus]
  ): Option[EventStatus] =
    taxonomyStatus match {
      case FeedStatus.Started   => Some(EventStatuses.PLAYING)
      case FeedStatus.Suspended => Some(EventStatuses.PAUSED)
      case FeedStatus.Finished  => scoreboardStatus(scoreboardStage)
      case _                    => None
    }

  def toPeriods(
      toPeriodKey: FeedScore => Option[PeriodKey],
      scores: Seq[FeedScore]
  ): Seq[Period] =
    scores
      .flatMap(score => toPeriod(toPeriodKey, score))
      .sortWith(_.getKey.periodType < _.getKey.periodType)

  private def toPeriod(
      toPeriodKey: FeedScore => Option[PeriodKey],
      feedScore: FeedScore
  ): Option[Period] =
    for {
      periodKey <- toPeriodKey(feedScore)
      score <- toScore(feedScore)
    } yield Period(Some(periodKey), score)

  private def toScore(
      score: FeedScore
  ): Option[Score] = {
    val scores =
      score.score.split("-").zipWithIndex.map { case (v, i) => i -> v }.toMap
    for {
      home <- scores.get(0)
      away <- scores.get(1)
    } yield Score(home, away)
  }
}
