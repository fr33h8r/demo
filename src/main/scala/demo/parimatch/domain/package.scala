package demo.parimatch

package object domain {
  trait MappingError {
    val message: String
  }

  case class ScoreboardMappingError(override val message: String)
      extends MappingError {}

  object Sport {
    val CounterStrike = "CounterStrike"
    val Dota2 = "Dota2"
    val LoL = "LoL"
    val Nba = "Nba"
    val Valorant = "Valorant"
    val TableTennis = "TableTennis"
    val Fifa = "Fifa"
  }

  object EsportScoreboardItem {
    val MapsPeriodScores: String = "1"
    val RoundsPeriodScores: String = "2"
  }

  object NbaScoreboardItem {
    val PointsPeriodScores: String = "1"
  }

  object TableTennisScoreboardItem {
    val SetsPeriodScores: String = "1"
    val PointsPeriodScores: String = "2"
  }

  object EsportStage {
    val NotStarted: Int = 15
    val Map1: Int = 1
    val Break1: Int = 2
    val Map2: Int = 3
    val Break2: Int = 4
    val Map3: Int = 5
    val Break3: Int = 6
    val Map4: Int = 7
    val Break4: Int = 8
    val Map5: Int = 9
    val Break5: Int = 10
    val Map6: Int = 11
    val Break6: Int = 12
    val Map7: Int = 13
    val Finished: Int = 14
    val Undefined: Int = 15
  }

  object TableTennisStage {
    val Start = 9
    val Set1 = 1
    val Set2 = 2
    val Set3 = 3
    val Set4 = 4
    val Set5 = 5
    val Set6 = 6
    val Set7 = 7
    val Finished = 8
  }

  object TableTennisPeriod {
    val Match = 1
    val Set1 = 2
    val Set2 = 3
    val Set3 = 4
    val Set4 = 5
    val Set5 = 6
    val Set6 = 7
    val Set7 = 8
  }

  object NbaStage {
    val Quarter1: Int = 1
    val Break1: Int = 2
    val Quarter2: Int = 3
    val Break2: Int = 4
    val Quarter3: Int = 5
    val Break3: Int = 6
    val Quarter4: Int = 7
    val Break4: Int = 8
    val Overtime1: Int = 9
    val Break5: Int = 10
    val Overtime2: Int = 11
    val Break6: Int = 12
    val Overtime3: Int = 13
    val Finish: Int = 14
    val Start: Int = 15
  }

  object NbaPeriod {
    val MainTime: Int = 1
    val Half1: Int = 2
    val Half2: Int = 3
    val Quarter1: Int = 4
    val Quarter2: Int = 5
    val Quarter3: Int = 6
    val Quarter4: Int = 7
    val FullTime: Int = 8
  }

  object EsportPeriod {
    val Match: Int = 1
    val Map1: Int = 2
    val Map2: Int = 3
    val Map3: Int = 4
    val Map4: Int = 5
    val Map5: Int = 6
    val Map6: Int = 7
    val Map7: Int = 8
  }

  object FifaScoreboardItem {
    val GoalPeriodScores: String = "1"
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

    val NOT_STARTED, PLAYING, PAUSED, CANCELLED, FINISHED = Value
  }

  object Team extends Enumeration {
    type HomeAway = Value
    val HOME, AWAY, UNSPECIFIED = Value
  }
}
