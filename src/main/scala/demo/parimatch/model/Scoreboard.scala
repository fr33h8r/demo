package demo.parimatch.model

import demo.parimatch.domain.EventStatuses.EventStatus
import demo.parimatch.model.Scoreboard.Clock

case class Scoreboard(
    eventStatus: EventStatus,
    currentStage: Int,
    currentPeriod: Option[PeriodKey] = None,
    periods: Seq[Period],
    clock: Option[Clock] = None
)
object Scoreboard {
  case class Clock(
      stopped: Boolean,
      matchTime: Option[Double],
      accelerationFactor: Option[Int] = None,
      autoClock: Option[Boolean] = None,
      frequency: Option[Int] = None
  )
}
