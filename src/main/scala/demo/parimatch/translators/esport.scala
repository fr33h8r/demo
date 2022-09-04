package demo.parimatch.translators

import demo.feed.model.{Interval, Score => FeedScore, Stage => FeedStage}
import demo.parimatch.domain.EventStatuses.EventStatus
import demo.parimatch.domain.{EsportPeriod, EventStatuses, MappingError, ScoreboardMappingError, EsportStage => PmStage}
import demo.parimatch.model.PeriodKey

import scala.util.chaining.scalaUtilChainingOps

object esport {

  def toCurrentStage(stage: Int): Either[MappingError, Int] =
    stage match {
      case FeedStage.Undefined  => Right(PmStage.Undefined)
      case FeedStage.NotStarted => Right(PmStage.NotStarted)
      case FeedStage.Finished   => Right(PmStage.Finished)
      case FeedStage.Map1       => Right(PmStage.Map1)
      case FeedStage.Break1     => Right(PmStage.Break1)
      case FeedStage.Map2       => Right(PmStage.Map2)
      case FeedStage.Break2     => Right(PmStage.Break2)
      case FeedStage.Map3       => Right(PmStage.Map3)
      case FeedStage.Break3     => Right(PmStage.Break3)
      case FeedStage.Map4       => Right(PmStage.Map4)
      case FeedStage.Break4     => Right(PmStage.Break4)
      case FeedStage.Map5       => Right(PmStage.Map5)
      case FeedStage.Break5     => Right(PmStage.Break5)
      case FeedStage.Map6       => Right(PmStage.Map6)
      case FeedStage.Break6     => Right(PmStage.Break6)
      case FeedStage.Map7       => Right(PmStage.Map7)
      case s =>
        Left(ScoreboardMappingError(s"Error converting current stage $s"))
    }

  def toEventStatus(stage: Int): Either[MappingError, EventStatus] =
    stage match {
      case FeedStage.Undefined | FeedStage.NotStarted =>
        Right(EventStatuses.NOT_STARTED)
      case FeedStage.Map1 | FeedStage.Break1 | FeedStage.Map2 |
           FeedStage.Break2 | FeedStage.Map3 | FeedStage.Break3 |
           FeedStage.Map4 | FeedStage.Break4 | FeedStage.Map5 |
           FeedStage.Break5 | FeedStage.Map6 | FeedStage.Break6 |
           FeedStage.Map7 =>
        Right(EventStatuses.PLAYING)
      case FeedStage.Finished => Right(EventStatuses.FINISHED)
      case s =>
        Left(ScoreboardMappingError(s"Error converting event status $s"))
    }

  def toCurrentPeriod(
      scores: Seq[FeedScore],
      roundsPeriodScores: Int
  ): Either[MappingError, PeriodKey] = {
    scores
      .filter(s => s.result == roundsPeriodScores)
      .maxByOption(_.interval)
      .map(p => p.interval.pipe(toPeriodType).map(p => PeriodKey(p)))
      .getOrElse(
        Left(ScoreboardMappingError(s"Error converting current period."))
      )
  }

  def toPeriodType(interval: Int): Either[MappingError, Int] =
    interval match {
      case Interval.Match => Right(EsportPeriod.Match)
      case Interval.Map1  => Right(EsportPeriod.Map1)
      case Interval.Map2  => Right(EsportPeriod.Map2)
      case Interval.Map3  => Right(EsportPeriod.Map3)
      case Interval.Map4  => Right(EsportPeriod.Map4)
      case Interval.Map5  => Right(EsportPeriod.Map5)
      case Interval.Map6  => Right(EsportPeriod.Map6)
      case Interval.Map7  => Right(EsportPeriod.Map7)
      case s                 => Left(ScoreboardMappingError(s"Error converting period $s"))
    }

  def toPeriodKey(score: FeedScore): Either[MappingError, PeriodKey] =
    toPeriodType(score.interval)
      .map(periodType => PeriodKey(periodType, score.subInterval))
}
