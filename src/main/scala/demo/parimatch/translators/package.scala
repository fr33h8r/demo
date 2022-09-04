package demo.parimatch

import demo.feed.model.{
  ScoreboardMessage,
  TaxonomyMessage,
  Score => FeedScore,
  Status => FeedStatus
}
import demo.parimatch.domain.EventStatuses.EventStatus
import demo.parimatch.domain.Sport._
import demo.parimatch.domain.{
  EventStatuses,
  MappingError,
  Score,
  ScoreboardMappingError,
  SportClockSetting
}
import demo.parimatch.model.Scoreboard.{MessageProperties, Payload}
import demo.parimatch.model.{Period, PeriodKey, Scoreboard}

package object translators {
  val supportedSports: Map[String, Int] = Map(
    "CounterStrike" -> 2,
    "Dota2" -> 2,
    "LoL" -> 2,
    "Valorant" -> 2,
    "Nba" -> 3,
    "Fifa" -> 1,
    "TableTennis" -> 6
  )

  val sportClockSettings: Map[String, SportClockSetting] = Map(
    "Fifa" -> SportClockSetting(
      autoClock = true,
      frequency = 60
    )
  )

  def toSport(sportName: String): Either[MappingError, Int] =
    supportedSports
      .get(sportName)
      .toRight(ScoreboardMappingError(s"No mapping for sport $sportName"))

  def translate(
      taxonomy: TaxonomyMessage,
      scoreboard: ScoreboardMessage
  ): Either[MappingError, Payload] =
    taxonomy.sport match {
      case CounterStrike => counterstrike.translate(scoreboard, taxonomy.status)
      case Dota2         => dota2.translate(scoreboard, taxonomy.status)
      case LoL           => lol.translate(scoreboard, taxonomy.status)
      case Nba           => nba.translate(scoreboard, taxonomy.status)
      case Valorant      => valorant.translate(scoreboard, taxonomy.status)
      case Fifa          => fifa.translate(scoreboard, taxonomy.status)
      case TableTennis   => tabletennis.translate(scoreboard, taxonomy.status)
    }

  def toScoreboard(
      scoreboard: ScoreboardMessage,
      taxonomy: TaxonomyMessage
  ): Either[MappingError, Scoreboard] =
    for {
      sport <- toSport(taxonomy.sport)
      payload <- translate(taxonomy, scoreboard)
    } yield Scoreboard(
      properties = Some(MessageProperties(sport)),
      payload = Some(payload)
    )

  def toEventStatus(
      taxonomyStage: Int,
      scoreboardStage: Int,
      scoreboardStatus: Int => Either[MappingError, EventStatus]
  ): Either[MappingError, EventStatus] =
    taxonomyStage match {
      case FeedStatus.Undefined | FeedStatus.NotStarted =>
        Right(EventStatuses.NOT_STARTED)
      case FeedStatus.Started =>
        Right(EventStatuses.PLAYING)
      case FeedStatus.Suspended => Right(EventStatuses.PAUSED)
      case FeedStatus.Finished  => scoreboardStatus(scoreboardStage)
      case FeedStatus.Canceled  => Right(EventStatuses.CANCELLED)
      case s =>
        Left(ScoreboardMappingError(s"Error converting event status $s"))
    }

  def toPeriods(
      scores: Seq[FeedScore],
      convertScoreboardItem: Int => Option[String],
      toPeriodKey: FeedScore => Either[MappingError, PeriodKey]
  ): Either[MappingError, Seq[Period]] =
    for (
      periods <- scores
        .flatMap(score =>
          convertScoreboardItem(score.result)
            .map(scoreboardItem => toPeriod(score, scoreboardItem, toPeriodKey))
        )
        .foldRight(Right(Nil): Either[MappingError, List[Period]]) { (e, acc) =>
          for (xs <- acc; x <- e) yield x :: xs
        }
    ) yield periods.sortWith(_.getKey.periodType < _.getKey.periodType)

  private def toPeriod(
      score: FeedScore,
      scoreboardItem: String,
      toPeriodKey: FeedScore => Either[MappingError, PeriodKey]
  ): Either[MappingError, Period] =
    for {
      periodKey <- toPeriodKey(score)
      score <- toScore(score)
      scores = Map(scoreboardItem -> score)
    } yield Period(Some(periodKey), scores)

  private def toScore(
      score: FeedScore
  ): Either[MappingError, Score] = {
    val scores =
      score.score.split("-").zipWithIndex.map { case (v, i) => i -> v }.toMap
    for {
      home <- scores
        .get(0)
        .toRight(ScoreboardMappingError("Missing home score"))
      away <- scores
        .get(1)
        .toRight(ScoreboardMappingError("Missing away score"))
    } yield Score(home, away)
  }
}
