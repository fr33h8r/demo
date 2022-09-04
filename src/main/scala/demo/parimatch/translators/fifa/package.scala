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
  FifaStage,
  MappingError,
  ScoreboardMappingError,
  SportClockSetting,
  FifaPeriod => PmPeriod,
  FifaScoreboardItem => PmScoreboardItem
}
import demo.parimatch.model.Scoreboard.Payload.Clock
import demo.parimatch.model.{Period, PeriodKey, Scoreboard}
import demo.parimatch.translators
import demo.parimatch.translators.fifa.{ScoreboardItem => FeedScoreboardItem}

package object fifa {
  val clockSettings: SportClockSetting = sportClockSettings("Fifa")

  def toEventStatus(stage: Int): Either[MappingError, EventStatus] =
    stage match {
      case FeedStage.NotStarted | FeedStage.Undefined =>
        Right(EventStatuses.NOT_STARTED)
      case FeedStage.Finished => Right(EventStatuses.FINISHED)
      case FeedStage.Time1 | FeedStage.Time2 =>
        Right(EventStatuses.PLAYING)
      case FeedStage.Break1 =>
        Right(EventStatuses.PLAYING)
      case s =>
        Left(ScoreboardMappingError(s"Error converting event status $s"))
    }

  def toCurrentStage(stage: Int): Either[MappingError, Int] =
    stage match {
      case FeedStage.NotStarted | FeedStage.Undefined =>
        Right(FifaStage.Start)
      case FeedStage.Time1    => Right(FifaStage.Half1)
      case FeedStage.Break1   => Right(FifaStage.Break1)
      case FeedStage.Time2    => Right(FifaStage.Half2)
      case FeedStage.Finished => Right(FifaStage.Finish)
      case s =>
        Left(ScoreboardMappingError(s"Error converting current stage $s"))
    }

  def toPeriodType(interval: Int): Either[MappingError, Int] =
    interval match {
      case Interval.Match => Right(PmPeriod.MainTime)
      case Interval.Time1 => Right(PmPeriod.Half1)
      case Interval.Time2 => Right(PmPeriod.Half2)
      case s              => Left(ScoreboardMappingError(s"Error converting period $s"))
    }

  def toClock(timer: Option[Timer]): Option[Clock] = timer.map(t =>
    Clock(
      stopped = !t.enabled,
      matchTime = Some(t.timerValue / 1000),
      accelerationFactor = Some(t.accelerationFactor),
      autoClock = Some(clockSettings.autoClock),
      frequency = Some(clockSettings.frequency)
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
      periods = periods ++ getCopiedPeriod(
        periods,
        PmPeriod.MainTime,
        PmPeriod.FullTime
      ),
      clock = toClock(scoreboard.timer)
    )
  }

  private def convertScoreboardItem(feedScoreboard: Int): Option[String] = {
    feedScoreboard match {
      case FeedScoreboardItem.GoalsPeriodScores =>
        Some(PmScoreboardItem.GoalPeriodScores)
      case _ => None
    }
  }

  private def getCopiedPeriod(
      periods: Seq[Period],
      sourcePeriodType: Int,
      destinationPeriodType: Int
  ): Option[Period] =
    periods.iterator.collectFirst {
      case p if p.getKey.periodType == sourcePeriodType =>
        p.withKey(p.getKey.withPeriodType(destinationPeriodType))
    }

  private def toPeriodKey(score: Score): Either[MappingError, PeriodKey] =
    toPeriodType(score.interval)
      .map(periodType => PeriodKey(periodType, score.subInterval))
}
