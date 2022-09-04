package demo.parimatch.translators

import demo.feed.model.{
  Score,
  ScoreboardMessage,
  Timer,
  Interval,
  Stage => FeedStage
}
import demo.parimatch.domain.EventStatuses.EventStatus
import demo.parimatch.domain.{
  EventStatuses,
  MappingError,
  NbaStage,
  ScoreboardMappingError,
  NbaPeriod => PmPeriod,
  NbaScoreboardItem => PmScoreboardItem
}
import demo.parimatch.model.{PeriodKey, Scoreboard}
import demo.parimatch.model.Scoreboard.Payload.Clock
import demo.parimatch.translators
import demo.parimatch.translators.nba.{ScoreboardItem => FeedScoreboardItem}

package object nba {

  def toEventStatus(
      scoreboardStage: Int
  ): Either[MappingError, EventStatus] =
    scoreboardStage match {
      case FeedStage.NotStarted | FeedStage.Undefined =>
        Right(EventStatuses.NOT_STARTED)
      case FeedStage.Finished => Right(EventStatuses.FINISHED)
      case FeedStage.Quarter1 | FeedStage.Quarter2 | FeedStage.Quarter3 |
           FeedStage.Quarter4 =>
        Right(EventStatuses.PLAYING)
      case FeedStage.Break1 | FeedStage.Break2 | FeedStage.Break3 =>
        Right(EventStatuses.PLAYING)
      case FeedStage.Overtime1 | FeedStage.Overtime2 | FeedStage.Overtime3 =>
        Right(EventStatuses.PLAYING)
      case FeedStage.BreakBeforeOvertime | FeedStage.OvertimeBreak1 |
          FeedStage.OvertimeBreak2 =>
        Right(EventStatuses.PLAYING)
      case s =>
        Left(ScoreboardMappingError(s"Error converting event status $s"))
    }

  def toCurrentStage(stage: Int): Either[MappingError, Int] =
    stage match {
      case FeedStage.NotStarted | FeedStage.Undefined =>
        Right(NbaStage.Start)
      case FeedStage.Quarter1            => Right(NbaStage.Quarter1)
      case FeedStage.Break1              => Right(NbaStage.Break1)
      case FeedStage.Quarter2            => Right(NbaStage.Quarter2)
      case FeedStage.Break2              => Right(NbaStage.Break2)
      case FeedStage.Quarter3            => Right(NbaStage.Quarter3)
      case FeedStage.Break3              => Right(NbaStage.Break3)
      case FeedStage.Quarter4            => Right(NbaStage.Quarter4)
      case FeedStage.BreakBeforeOvertime => Right(NbaStage.Break4)
      case FeedStage.Overtime1           => Right(NbaStage.Overtime1)
      case FeedStage.OvertimeBreak1      => Right(NbaStage.Break5)
      case FeedStage.Overtime2           => Right(NbaStage.Overtime2)
      case FeedStage.OvertimeBreak2      => Right(NbaStage.Break6)
      case FeedStage.Overtime3           => Right(NbaStage.Overtime3)
      case FeedStage.Finished            => Right(NbaStage.Finish)
      case s =>
        Left(ScoreboardMappingError(s"Error converting current stage $s"))
    }

  def toPeriodType(interval: Int): Either[MappingError, Int] =
    interval match {
      case Interval.Match    => Right(PmPeriod.FullTime)
      case Interval.MainTime => Right(PmPeriod.MainTime)
      case Interval.Quarter1 => Right(PmPeriod.Quarter1)
      case Interval.Quarter2 => Right(PmPeriod.Quarter2)
      case Interval.Quarter3 => Right(PmPeriod.Quarter3)
      case Interval.Quarter4 => Right(PmPeriod.Quarter4)
      case Interval.Half1    => Right(PmPeriod.Half1)
      case Interval.Half2    => Right(PmPeriod.Half2)
      case s                    => Left(ScoreboardMappingError(s"Error converting period $s"))
    }

  def toClock(timer: Option[Timer]): Option[Clock] = timer.map(t =>
    Clock(
      stopped = !t.enabled,
      matchTime = Some(t.timerValue / 1000)
    )
  )

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
        scoreboard.scores,
        convertScoreboardItem,
        toPeriodKey
      )
    } yield Scoreboard.Payload(
      eventStatus = eventStatus,
      currentStage = currentStage,
      currentPeriod = Some(PeriodKey(periodType = -1)),
      periods = periods,
      clock = toClock(scoreboard.timer)
    )
  }

  private def convertScoreboardItem(feedScoreboard: Int): Option[String] = {
    feedScoreboard match {
      case FeedScoreboardItem.PointsPeriodScores =>
        Some(PmScoreboardItem.PointsPeriodScores)
      case _ => None
    }
  }

  private def toPeriodKey(score: Score): Either[MappingError, PeriodKey] =
    toPeriodType(score.interval)
      .map(periodType => PeriodKey(periodType, score.subInterval))
}
