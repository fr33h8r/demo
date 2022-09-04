package demo.feed.model

case class Timer(
    timeStamp: Long,
    timerValue: Int,
    accelerationFactor: Int,
    enabled: Boolean
)