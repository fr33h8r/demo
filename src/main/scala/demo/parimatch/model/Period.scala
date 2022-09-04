package demo.parimatch.model

import demo.parimatch.domain.Score

case class Period(key: Option[PeriodKey], scores: Map[String, Score]) {
  def getKey: PeriodKey = key.getOrElse(PeriodKey.defaultInstance)
  def withKey(key: PeriodKey): Period = copy(Option(key))
}
