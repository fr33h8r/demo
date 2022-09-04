package demo.parimatch.model

case class PeriodKey(periodType: Int, subperiodNumber: Option[Int] = None) {
  def withPeriodType(periodType: Int): PeriodKey = copy(periodType)
}
object PeriodKey {
  val defaultInstance: PeriodKey = PeriodKey(0)
}
