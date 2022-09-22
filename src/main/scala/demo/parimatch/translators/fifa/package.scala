package demo.parimatch.translators

import demo.feed.model.{
  Interval,
  Score,
  ScoreboardMessage,
  Timer,
  Stage => FeedStage
}
import demo.parimatch.domain.EventStatuses.EventStatus
import demo.parimatch.domain.{
  EventStatuses,
  FifaStage,
  SportClockSetting,
  FifaPeriod => PmPeriod
}
import demo.parimatch.model.Scoreboard.Clock
import demo.parimatch.model.{Period, PeriodKey, Scoreboard}
import demo.parimatch.translators

package object fifa {

  val clockSettings: SportClockSetting = sportClockSettings("Fifa")

  def toEventStatus(stage: Int): Option[EventStatus] =
    stage match {
      case FeedStage.Finished => Some(EventStatuses.FINISHED)
      case FeedStage.Time1 | FeedStage.Time2 =>
        Some(EventStatuses.PLAYING)
      case FeedStage.Break1 =>
        Some(EventStatuses.PLAYING)
      case _ => None
    }

  def toCurrentStage(stage: Int): Option[Int] =
    stage match {
      case FeedStage.Time1    => Some(FifaStage.Half1)
      case FeedStage.Break1   => Some(FifaStage.Break1)
      case FeedStage.Time2    => Some(FifaStage.Half2)
      case FeedStage.Finished => Some(FifaStage.Finish)
      case _                  => None
    }

  def toPeriodType(interval: Int): Option[Int] =
    interval match {
      case Interval.Match => Some(PmPeriod.MainTime)
      case Interval.Time1 => Some(PmPeriod.Half1)
      case Interval.Time2 => Some(PmPeriod.Half2)
      case _              => None
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
  ): Option[Scoreboard] = {
    for {
      eventStatus <- translators.toEventStatus(
        taxonomyStatus,
        scoreboard.stage,
        toEventStatus
      )
      currentStage <- toCurrentStage(scoreboard.stage)
      periods = toPeriods(
        toPeriodKey,
        scoreboard.scores
      )
    } yield Scoreboard(
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

  private def getCopiedPeriod(
      periods: Seq[Period],
      sourcePeriodType: Int,
      destinationPeriodType: Int
  ): Option[Period] =
    periods.iterator.collectFirst {
      case p if p.getKey.periodType == sourcePeriodType =>
        p.withKey(p.getKey.withPeriodType(destinationPeriodType))
    }

  private def toPeriodKey(score: Score): Option[PeriodKey] =
    toPeriodType(score.interval)
      .map(periodType => PeriodKey(periodType, score.subInterval))
}
