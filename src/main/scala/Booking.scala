import upickle.default._

case class Booking (bookingId: String = "booking1", items: Set[Item] = Set.empty) derives ReadWriter