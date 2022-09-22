package demo.parimatch.translators

import demo.feed.model.{Interval, Score => FeedScore, Stage => FeedStage}
import demo.parimatch.domain.EventStatuses.EventStatus
import demo.parimatch.domain.{
  EsportPeriod,
  EventStatuses,
  EsportStage => PmStage
}
import demo.parimatch.model.PeriodKey

package object esports {
  def toCurrentStage(stage: Int): Option[Int] =
    stage match {
      case FeedStage.Finished => Some(PmStage.Finished)
      case FeedStage.Map1     => Some(PmStage.Map1)
      case FeedStage.Break1   => Some(PmStage.Break1)
      case FeedStage.Map2     => Some(PmStage.Map2)
      case FeedStage.Break2   => Some(PmStage.Break2)
      case FeedStage.Map3     => Some(PmStage.Map3)
      case _                  => None
    }

  def toEventStatus(stage: Int): Option[EventStatus] =
    stage match {
      case FeedStage.Map1 | FeedStage.Break1 | FeedStage.Map2 |
          FeedStage.Break2 | FeedStage.Map3 =>
        Some(EventStatuses.PLAYING)
      case FeedStage.Finished => Some(EventStatuses.FINISHED)
      case _                  => None
    }

  def toPeriodType(interval: Int): Option[Int] =
    interval match {
      case Interval.Match => Some(EsportPeriod.Match)
      case Interval.Map1  => Some(EsportPeriod.Map1)
      case Interval.Map2  => Some(EsportPeriod.Map2)
      case Interval.Map3  => Some(EsportPeriod.Map3)
      case _              => None
    }

  def toPeriodKey(score: FeedScore): Option[PeriodKey] =
    toPeriodType(score.interval)
      .map(periodType => PeriodKey(periodType, score.subInterval))
}
