package demo.parimatch.translators

import demo.feed.model.ScoreboardMessage
import demo.parimatch.domain.{
  EsportScoreboardItem => PmScoreboardItem,
  MappingError
}
import demo.parimatch.model.Scoreboard
import demo.parimatch.translators.counterstrike.{
  ScoreboardItem => FeedScoreboardItem
}

package object counterstrike {

  val unsupportedSubIntervals = Seq(101, 102)

  def translate(
      scoreboard: ScoreboardMessage,
      taxonomyStatus: Int
  ): Either[MappingError, Scoreboard.Payload] = {
    val scores = scoreboard.scores.filterNot(i =>
      unsupportedSubIntervals.contains(i.subInterval.getOrElse(0))
    )

    for {
      eventStatus <- toEventStatus(
        taxonomyStatus,
        scoreboard.stage,
        esport.toEventStatus
      )
      currentStage <- esport.toCurrentStage(scoreboard.stage)
      currentPeriod <- esport.toCurrentPeriod(
        scores,
        FeedScoreboardItem.RoundsPeriodScores
      )
      periods <- toPeriods(
        scores,
        convertScoreboardItem,
        esport.toPeriodKey
      )
    } yield Scoreboard.Payload(
      eventStatus = eventStatus,
      currentStage = currentStage,
      currentPeriod = Some(currentPeriod),
      periods = periods
    )
  }

  private def convertScoreboardItem(feedScoreboard: Int): Option[String] = {
    feedScoreboard match {
      case FeedScoreboardItem.MapsPeriodScores =>
        Some(PmScoreboardItem.MapsPeriodScores)
      case FeedScoreboardItem.RoundsPeriodScores =>
        Some(PmScoreboardItem.RoundsPeriodScores)
      case _ => None
    }
  }
}
