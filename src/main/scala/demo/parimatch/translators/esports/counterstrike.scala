package demo.parimatch.translators.esports

import demo.feed.model.ScoreboardMessage
import demo.parimatch.model.Scoreboard
import demo.parimatch.translators.{esports, toEventStatus, toPeriods}

object counterstrike {

  val unsupportedSubIntervals = Seq(101, 102)

  def translate(
      scoreboard: ScoreboardMessage,
      taxonomyStatus: Int
  ): Option[Scoreboard] = {
    val scores = scoreboard.scores.filterNot(i =>
      unsupportedSubIntervals.contains(i.subInterval.getOrElse(0))
    )

    for {
      eventStatus <- toEventStatus(
        taxonomyStatus,
        scoreboard.stage,
        esports.toEventStatus
      )
      currentStage <- esports.toCurrentStage(scoreboard.stage)
      periods = toPeriods(
        esports.toPeriodKey,
        scores
      )
    } yield Scoreboard(
      eventStatus = eventStatus,
      currentStage = currentStage,
      periods = periods
    )
  }
}
