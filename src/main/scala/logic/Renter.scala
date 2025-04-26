package logic

import logic.BookingList.bookings
import logic.util.IOHandler.getRenterData
import upickle.default.*

@upickle.implicits.serializeDefaults( true )
case class Renter( name: String, email: String ) derives ReadWriter:

   def getRentedItems =
      bookings.filter( _.renter == this.name ).flatMap( _.getItemDetails ).toSet

object Renter:
   val renters = getRenterData()