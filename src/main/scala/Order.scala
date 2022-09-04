package pm.sport.unifiedfeed

case class RequisitionRequest()
case class Invoice()
sealed trait PaymentMethod
object PaymentMethods {
  case object Cash extends PaymentMethod
  case object CreditCard extends PaymentMethod
}

case class Order(
    id: String,
    paymentMethod: PaymentMethod,
    requisitionRequest: Option[RequisitionRequest],
    invoice: Option[Invoice],
    paymentId: Option[String],
    fulfilled: Boolean,
    processedTimestamp: Option[Long]
)
