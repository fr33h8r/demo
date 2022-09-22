package demo.parimatch

package object domain {

  object EsportStage {
    val NotStarted: Int = 15
    val Map1: Int = 1
    val Break1: Int = 2
    val Map2: Int = 3
    val Break2: Int = 4
    val Map3: Int = 5
    val Finished: Int = 14
  }

  object EsportPeriod {
    val Match: Int = 1
    val Map1: Int = 2
    val Map2: Int = 3
    val Map3: Int = 4
  }

  object FifaStage {
    val Half1: Int = 1
    val Break1: Int = 2
    val Half2: Int = 3
    val Finish: Int = 9
    val Start: Int = 10
  }

  object FifaPeriod {
    val MainTime: Int = 1
    val Half1: Int = 2
    val Half2: Int = 3
    val FullTime: Int = 4
  }

  case class SportClockSetting(
      autoClock: Boolean,
      frequency: Int
  )

  case class Score(home: String, away: String)

  object EventStatuses extends Enumeration {
    type EventStatus = Value

    val PLAYING, PAUSED, FINISHED = Value
  }
}
