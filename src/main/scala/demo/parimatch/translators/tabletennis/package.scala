package demo.parimatch.translators

import demo.feed.model.{
  Score,
  ScoreboardMessage,
  Interval,
  Stage => FeedStage
}
import demo.parimatch.domain.EventStatuses.EventStatus
import demo.parimatch.domain.Team.HomeAway
import demo.parimatch.domain.{
  EventStatuses,
  MappingError,
  ScoreboardMappingError,
  TableTennisStage,
  Team,
  TableTennisPeriod => PmPeriod,
  TableTennisScoreboardItem => PmScoreboardItem
}
import demo.parimatch.model.{PeriodKey, Scoreboard}
import demo.parimatch.translators

package object tabletennis {

  object FeedScoreboardItem {
    val SetsPeriodScores: Int = 6
    val PointsPeriodScores: Int = 5
  }

  object ServerItem {
    val Home: String = "1"
    val Away: String = "2"
    val Unspecified: String = "0"
  }

  def translate(
      scoreboard: ScoreboardMessage,
      taxonomyStatus: Int
  ): Either[MappingError, Scoreboard.Payload] = {
    for {
      eventStatus <- translators.toEventStatus(
        taxonomyStatus,
        scoreboard.stage,
        toEventStatus
      )
      currentStage <- toCurrentStage(scoreboard.stage)
      periods <- toPeriods(
        filterPeriods(scoreboard.scores),
        convertScoreboardItem,
        toPeriodKey
      )
      server <- toServer(scoreboard.server)
    } yield Scoreboard.Payload(
      eventStatus = eventStatus,
      currentStage = currentStage,
      periods = periods,
      server = Some(server)
    )
  }

  def toEventStatus(stage: Int): Either[MappingError, EventStatus] =
    stage match {
      case FeedStage.NotStarted | FeedStage.Undefined =>
        Right(EventStatuses.NOT_STARTED)
      case FeedStage.Set1 | FeedStage.Set2 | FeedStage.Set3 |
          FeedStage.Set4 | FeedStage.Set5 =>
        Right(EventStatuses.PLAYING)
      case FeedStage.Finished => Right(EventStatuses.FINISHED)
      case s =>
        Left(ScoreboardMappingError(s"Error converting event status $s"))
    }

  // we do not need total points for match interval
  private def filterPeriods(scores: Seq[Score]): Seq[Score] =
    scores.filterNot(score =>
      score.result == FeedScoreboardItem.PointsPeriodScores && score.interval == Interval.Match
    )

  def toCurrentStage(stage: Int): Either[MappingError, Int] =
    stage match {
      case FeedStage.NotStarted | FeedStage.Undefined =>
        Right(TableTennisStage.Start)
      case FeedStage.Set1     => Right(TableTennisStage.Set1)
      case FeedStage.Set2     => Right(TableTennisStage.Set2)
      case FeedStage.Set3     => Right(TableTennisStage.Set3)
      case FeedStage.Set4     => Right(TableTennisStage.Set4)
      case FeedStage.Set5     => Right(TableTennisStage.Set5)
      case FeedStage.Finished => Right(TableTennisStage.Finished)
      case s =>
        Left(ScoreboardMappingError(s"Error converting current stage $s"))
    }

  def toPeriod(interval: Int): Either[MappingError, Int] =
    interval match {
      case Interval.Match => Right(PmPeriod.Match)
      case Interval.Set1  => Right(PmPeriod.Set1)
      case Interval.Set2  => Right(PmPeriod.Set2)
      case Interval.Set3  => Right(PmPeriod.Set3)
      case Interval.Set4  => Right(PmPeriod.Set4)
      case Interval.Set5  => Right(PmPeriod.Set5)
      case s                 => Left(ScoreboardMappingError(s"Error converting period $s"))
    }

  def toServer(server: Option[String]): Either[MappingError, HomeAway] =
    server match {
      case Some(ServerItem.Home)        => Right(Team.HOME)
      case Some(ServerItem.Away)        => Right(Team.AWAY)
      case Some(ServerItem.Unspecified) => Right(Team.UNSPECIFIED)
      case s                            => Left(ScoreboardMappingError(s"Error converting server $s"))
    }

  private def convertScoreboardItem(feedScoreboard: Int) = {
    feedScoreboard match {
      case FeedScoreboardItem.SetsPeriodScores =>
        Some(PmScoreboardItem.SetsPeriodScores)
      case FeedScoreboardItem.PointsPeriodScores =>
        Some(PmScoreboardItem.PointsPeriodScores)
      case _ => None
    }
  }

  private def toPeriodKey(score: Score): Either[MappingError, PeriodKey] =
    toPeriod(score.interval)
      .map(periodType => PeriodKey(periodType, score.subInterval))
}
