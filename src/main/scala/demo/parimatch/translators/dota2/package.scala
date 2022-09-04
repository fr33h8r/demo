package demo.parimatch.translators

import demo.feed.model.ScoreboardMessage
import demo.parimatch.domain.{
  MappingError,
  EsportScoreboardItem => PmScoreboardItem
}
import demo.parimatch.model.Scoreboard
import demo.parimatch.translators.dota2.{ScoreboardItem => FeedScoreboardItem}

package object dota2 {

  def translate(
      scoreboard: ScoreboardMessage,
      taxonomyStatus: Int
  ): Either[MappingError, Scoreboard.Payload] = {
    for {
      eventStatus <- toEventStatus(
        taxonomyStatus,
        scoreboard.stage,
        esport.toEventStatus
      )
      currentStage <- esport.toCurrentStage(scoreboard.stage)
      currentPeriod <- esport.toCurrentPeriod(
        scoreboard.scores,
        FeedScoreboardItem.RoundsPeriodScores
      )
      periods <- toPeriods(
        scoreboard.scores,
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
