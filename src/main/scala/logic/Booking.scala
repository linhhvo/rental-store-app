package logic

import com.aventrix.jnanoid.jnanoid.NanoIdUtils.*
import logic.enums.SavedItemStatus.*
import upickle.default.*

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import scala.collection.mutable.Buffer

@upickle.implicits.serializeDefaults( true )
case class Booking( items:            Buffer[SavedItem] = Buffer.empty,
                    bookingStartTime: LocalDateTime,
                    bookingEndTime:   LocalDateTime,
                    totalCost:        Double,
                    renter:           String,
                    bookingId:        String = randomNanoId()
) derives ReadWriter:

   /** Get item details for items in this booking */
   def getItemDetails: Buffer[Item] = this.items.flatMap( _.getDetailsFromItemList() )

   /** Get item details for all items that are being rented out and have not been returned */
   def outstandingItems: Buffer[Item] =
      this.items
         .filter( i => i.savedItemStatus == Booked )
         .flatMap( _.getDetailsFromItemList() )

   /** Return selected items in this booking */
   def returnItems( targetItemNames: Buffer[String] ) =
      this.items
         .filter( si =>
            si.getDetailsFromItemList() match
               case Some( value ) => targetItemNames.contains( value.name )
               case None          => false
         )
         .foreach( _.returnFromBooking() )
end Booking

object Booking:
   implicit val dateTimeReadWrite: ReadWriter[LocalDateTime] =
      readwriter[String].bimap[LocalDateTime](
          dateTime => dateTime.format( DateTimeFormatter.ofPattern( "d-MM-yyyy HH:mm" ) ),
          str => LocalDateTime.parse( str, DateTimeFormatter.ofPattern( "d-MM-yyyy HH:mm" ) )
      )