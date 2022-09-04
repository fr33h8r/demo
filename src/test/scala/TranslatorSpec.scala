import demo.feed.model.{ScoreboardMessage, TaxonomyMessage, Score => FeedScore}
import demo.parimatch.domain.EventStatuses.NOT_STARTED
import demo.parimatch.domain.{Score => PmScore}
import demo.parimatch.model.{Period, PeriodKey, Scoreboard}
import demo.parimatch.model.Scoreboard.{MessageProperties, Payload}
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
      TaxonomyMessage(sport = "CounterStrike", status = 1)
    )

    result.isRight shouldBe true
    result shouldBe Right(
      Scoreboard(
        Some(MessageProperties(2)),
        Some(
          Payload(
            eventStatus = NOT_STARTED,
            currentStage = 1,
            currentPeriod = Some(PeriodKey(1, None)),
            periods = List(
              Period(
                Some(PeriodKey(periodType = 1)),
                Map(2 -> PmScore("12", "10"))
              )
            )
          )
        )
      )
    )
  }
}
