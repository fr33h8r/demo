import demo.feed.model.{ScoreboardMessage, TaxonomyMessage, Score => FeedScore}
import demo.parimatch.domain.EventStatuses.PLAYING
import demo.parimatch.domain.{Score => PmScore}
import demo.parimatch.model.{Period, PeriodKey, Scoreboard}
import demo.parimatch.translators
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers.convertToAnyShouldWrapper

class TranslatorSpec extends AnyFlatSpec {
  it should "translate feed scoreboard" in {
    val result = translators.toScoreboard(
      ScoreboardMessage(
        id = "some-id",
        stage = 8,
        scores = Seq(FeedScore(interval = 1, result = 8, score = "12-10"))
      ),
      TaxonomyMessage(sport = "CounterStrike", status = 2)
    )

    result.nonEmpty shouldBe true
    result shouldBe Some(
      Scoreboard(
        eventStatus = PLAYING,
        currentStage = 1,
        periods = List(
          Period(
            Some(PeriodKey(periodType = 1)),
            PmScore("12", "10")
          )
        )
      )
    )
  }
}
