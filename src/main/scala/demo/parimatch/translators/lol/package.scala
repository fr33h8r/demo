package demo.parimatch.translators

import demo.feed.model.ScoreboardMessage
import demo.parimatch.domain.{
  MappingError,
  EsportScoreboardItem => PmScoreboardItem
}
import demo.parimatch.model.Scoreboard
import demo.parimatch.translators.lol.{ScoreboardItem => FeedScoreboardItem}

package object lol {

  def translate(
      scoreboard: ScoreboardMessage,
      eventStatus: Int
  ): Either[MappingError, Scoreboard.Payload] = {
    for {
      eventStatus <- toEventStatus(
        eventStatus,
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

  private def convertScoreboardItem(feedScoreboard: Int) = {
    feedScoreboard match {
      case FeedScoreboardItem.MapsPeriodScores =>
        Some(PmScoreboardItem.MapsPeriodScores)
      case FeedScoreboardItem.RoundsPeriodScores =>
        Some(PmScoreboardItem.RoundsPeriodScores)
      case _ => None
    }
  }
}
