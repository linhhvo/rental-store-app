package logic

import com.aventrix.jnanoid.jnanoid.NanoIdUtils.*
import logic.RentalStore.{bookings, overlapBookingList}
import logic.enums.SavedItemStatus.*
import logic.util.ItemDoesNotExist
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

   /** get item details for items in this booking */
   def getItemDetails: Buffer[Item] = this.items.flatMap( _.getDetailsFromItemList() )

   /** get items that are being rented out */
   def outstandingItems: Buffer[Item] =
      this.items
         .filter( i => i.savedItemStatus == Booked )
         .flatMap( _.getDetailsFromItemList() )

   /** return items in this booking */
   def returnItems( targetItemIds: Buffer[String] ) =
      this.items
         .filter( si =>
            si.getDetailsFromItemList() match
               case Some( value ) => targetItemIds.contains( value.name )
               case None          => false
         )
         .foreach( i =>
            i.savedItemStatus = Returned
            i.getDetailsFromItemList() match
               case Some( item ) => item.free( i.rentQuantity )
               case None => throw ItemDoesNotExist( "This item doesn't exist or is not active" )
         )
end Booking

object Booking:
   implicit val dateTimeReadWrite: ReadWriter[LocalDateTime] =
      readwriter[String].bimap[LocalDateTime](
          dateTime => dateTime.format( DateTimeFormatter.ofPattern( "d-MM-yyyy HH:mm" ) ),
          str => LocalDateTime.parse( str, DateTimeFormatter.ofPattern( "d-MM-yyyy HH:mm" ) )
      )

   def areDateRangeOverlapped( start1: LocalDateTime,
                               end1:   LocalDateTime,
                               start2: LocalDateTime,
                               end2:   LocalDateTime
   ): Boolean =
      start1.isBefore( end2 ) && end1.isAfter( start2 )

