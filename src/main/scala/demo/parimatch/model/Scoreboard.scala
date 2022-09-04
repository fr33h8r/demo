package demo.parimatch.model

import demo.parimatch.domain.EventStatuses.EventStatus
import demo.parimatch.domain.Team.HomeAway
import demo.parimatch.model.Scoreboard.Payload.Clock
import demo.parimatch.model.Scoreboard.{MessageProperties, Payload}

case class Scoreboard(
    properties: Option[MessageProperties] = None,
    payload: Option[Payload] = None
)
object Scoreboard {
  case class MessageProperties(sport: Int)
  case class Payload(
      eventStatus: EventStatus,
      currentStage: Int,
      currentPeriod: Option[PeriodKey] = None,
      periods: Seq[Period],
      clock: Option[Clock] = None,
      server: Option[HomeAway] = None
  )
  object Payload {
    case class Clock(
        stopped: Boolean,
        matchTime: Option[Double],
        accelerationFactor: Option[Int] = None,
        autoClock: Option[Boolean] = None,
        frequency: Option[Int] = None
    )
  }
}
