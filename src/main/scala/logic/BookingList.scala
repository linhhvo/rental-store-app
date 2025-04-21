package logic

import logic.util.IOHandler.{getDataFromFile, prodDataFilePath, writeBookings}
import logic.util.ItemNotAvailable
import upickle.default.read

import java.time.{LocalDate, LocalDateTime}
import scala.collection.mutable.Buffer

object BookingList:

   // get booking list from external file
   val bookings =
      getDataFromFile( "bookings" ).map( item => read[Booking]( item ) )

   /** Add a new rental reservation */
   def addBooking( newBooking:   Booking,
                   bookingList:  Buffer[Booking] = bookings,
                   dataFilePath: String = prodDataFilePath
   ) =
      // check if saved items are available during requested booking period
      val isAvailable: Boolean =
         newBooking.items.forall( si =>
            si.getDetailsFromItemList().exists( _.isItemAvailable( newBooking, si.rentQuantity ) )
         )

      if isAvailable then
         bookingList += newBooking
         newBooking.items.foreach( bi => bi.confirmBooking() )
         writeBookings( bookingList, dataFilePath)
      else
         throw ItemNotAvailable(
             s"Some item in this bundle is not available for rent"
         )

   /** Get a list of bookings that have the end date falls within a time interval */
   def bookingListByDates( start:       LocalDate,
                           end:         LocalDate,
                           bookingList: Buffer[Booking] = bookings
   ) =
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
                           bookingList:  Buffer[Booking] = bookings
   ): Buffer[Booking] =
      bookingList
         .filter( _.outstandingItems.map( _.itemId ).contains( targetItemId ) )
         .filter( b => b.bookingStartTime.isBefore( end ) && b.bookingEndTime.isAfter( start ) )