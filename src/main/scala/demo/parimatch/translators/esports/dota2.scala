package demo.parimatch.translators.esports

import demo.feed.model.ScoreboardMessage
import demo.parimatch.model.Scoreboard
import demo.parimatch.translators.{esports, toEventStatus, toPeriods}

object dota2 {

  def translate(
      scoreboard: ScoreboardMessage,
      taxonomyStatus: Int
  ): Option[Scoreboard] = {
    for {
      eventStatus <- toEventStatus(
        taxonomyStatus,
        scoreboard.stage,
        esports.toEventStatus
      )
      currentStage <- esports.toCurrentStage(scoreboard.stage)
      periods = toPeriods(
        esports.toPeriodKey,
        scoreboard.scores
      )
    } yield Scoreboard(
      eventStatus = eventStatus,
      currentStage = currentStage,
      periods = periods
    )
  }
}
