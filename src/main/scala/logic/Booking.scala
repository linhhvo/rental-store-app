package logic

import upickle.default.*

case class Booking( bookingId: String = "booking1", items: Set[Item] = Set.empty )
    derives ReadWriter
