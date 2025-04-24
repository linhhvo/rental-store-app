package logic

import logic.ItemList.inventory
import logic.util.IOHandler.{getBookingData, prodDataFilePath, writeBookingData}
import logic.util.ItemNotAvailable

import java.time.{LocalDate, LocalDateTime}
import scala.collection.mutable.Buffer

object BookingList:
   // get booking list from external file
   val bookings = getBookingData()

   /** Add a new rental reservation */
   def addBooking( newBooking:   Booking,
                   bookingList:  Buffer[Booking] = bookings,
                   allItemsList: Buffer[Item] = inventory,
                   dataFilePath: String = prodDataFilePath
   ) =
      // check if saved items are available during requested booking period
      val isAvailable: Boolean =
         newBooking.items.forall( si =>
            si.getDetailsFromItemList( allItemsList )
               .exists(
                   _.isItemAvailable( newBooking, si.rentQuantity, bookingList, allItemsList )
               )
         )

      if isAvailable then
         bookingList += newBooking
         newBooking.items.foreach( bi => bi.confirmBooking() )
         writeBookingData( bookingList, dataFilePath )
      else
         throw ItemNotAvailable(
             s"Some item in this bundle is not available for rent"
         )

   /** Get a list of bookings that have the end date falls within a time interval */
   def bookingListByDates( start: LocalDate, end: LocalDate, bookingList: Buffer[Booking] ) =
      if start == null && end == null then bookingList
      else if start != null && end == null then
         bookingList.filter( b =>
            val bEnd = b.bookingEndTime.toLocalDate
            bEnd.isAfter( start ) || bEnd.isEqual( start )
         )
      else if start == null && end != null then
         bookingList.filter( b =>
            val bEnd = b.bookingEndTime.toLocalDate
            bEnd.isBefore( end ) || bEnd.isEqual( end )
         )
      else
         bookingList.filter( b =>
            val bEnd = b.bookingEndTime.toLocalDate
            ( bEnd.isAfter( start )
               || bEnd.isEqual( start ) )
            && ( bEnd.isBefore( end )
               || bEnd.isEqual( end ) )
         )

   /** Get a list of bookings that contain the target item as an outstanding item (haven't been returned)
     * and overlap with the given date range (the bookings might or might not have ended within this date range)
     */
   def overlapBookingList( targetItemId: String,
                           start:        LocalDateTime,
                           end:          LocalDateTime,
                           bookingList:  Buffer[Booking],
                           allItemsList: Buffer[Item]
   ): Buffer[Booking] =
      bookingList
         .filter( _.outstandingItems( allItemsList ).map( _.itemId ).contains( targetItemId ) )
         .filter( b => b.bookingStartTime.isBefore( end ) && b.bookingEndTime.isAfter( start ) )
