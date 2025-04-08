package logic

import com.aventrix.jnanoid.jnanoid.NanoIdUtils.*
import logic.Booking.areDateRangeOverlapped
import logic.RentalStore.bookings
import logic.SavedItemStatus.{Booked, Returned}
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

   def isItemAvailable( targetItem: SavedItem, bookingList: Buffer[Booking] = bookings ): Boolean =
      // get the item instance of the target item
      val itemDetails = targetItem.getDetailsFromItemList()

      // get a list of existing booking that contains the target itemem
      val relevantBookings: Buffer[Booking] =
         bookingList.filter( b => b.outstandingItems.map( _.itemId ).contains( targetItem.itemId ) )

      // get a list of booking that contains the target item and overlap with the current booking
      val overlapBookings: Buffer[Booking] = relevantBookings.filter( b =>
         areDateRangeOverlapped( b.bookingStartTime,
                                 b.bookingEndTime,
                                 this.bookingStartTime,
                                 this.bookingEndTime
         )
      )

      println( "overlap bookings: " + overlapBookings.mkString )

      /* if there is no overlap booking, check if the item has enough quantity based on current
       * available quantity */
      if overlapBookings.isEmpty then
         itemDetails.exists( _.hasEnoughQuantityNow( targetItem.rentQuantity ) )
      else
         /* if there are overlap bookings, get the total rented quantity of the target item from
          * these bookings */
         val reservedQuantity = overlapBookings
            .flatMap( b => b.items.filter( si => si.itemId == targetItem.itemId ) )
            .map( _.rentQuantity )
            .sum

         println( "rented quantity during the requested period " + reservedQuantity )

         itemDetails.exists( _.hasEnoughQuantityThen( reservedQuantity, targetItem.rentQuantity ) )

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