package logic

import com.aventrix.jnanoid.jnanoid.NanoIdUtils.*
import logic.BookingList.{bookingListByDates, bookings, overlapBookingList}
import logic.ItemList.inventory
import logic.SavedList.calculateRentalCost
import logic.enums.{IncidentType, ItemStatus}
import logic.util.IOHandler.{prodDataFilePath, writeItemData}
import logic.util.{BookingDuration, MissingData}
import upickle.default.*

import java.time.{LocalDate, LocalDateTime}
import scala.collection.mutable
import scala.collection.mutable.{Buffer, SortedSet}

@upickle.implicits.serializeDefaults( true )
case class Item( name:             String,
                 desc:             String,
                 ownedQuantity:    Int,
                 category:         String,
                 ratePerHour:      Double,
                 ratePerDay:       Double,
                 ratePerWeek:      Double,
                 itemId:           String = randomNanoId(),
                 var incidents:    Buffer[Incident] = Buffer(),
                 var children:     Set[String] = Set.empty,
                 var image:        String = "",
                 comments:         SortedSet[String] = SortedSet.empty,
                 var lostQuantity: Int = 0,
                 var status:       ItemStatus = ItemStatus.Active
) derives ReadWriter:

   def addChildren( childItemId: String ) =
      children += childItemId

   def deleteChildren( childItemId: String ) =
      children -= childItemId

   /** Retrieve children item instances */
   def getChildrenDetails( allItemsList: Buffer[Item] = inventory ): Set[Item] =
      this.children.flatMap( c => allItemsList.filter( _.itemId == c ) )

   def availableQuantity: Int =
      ownedQuantity - lostQuantity

   /** Get item quantity rented out in a given time interval */
   def rentedQntByDateRange( start:        LocalDateTime,
                             end:          LocalDateTime,
                             bookingList:  Buffer[Booking] = bookings,
                             allItemsList: Buffer[Item] = inventory
   ): Int =
      overlapBookingList( this.itemId, start, end, bookingList, allItemsList )
         .flatMap( _.items.filter( _.itemId == this.itemId ) )
         .map( _.rentQuantity )
         .sum

   /** Check if item has enough quantity to be rented out in a specific booking */
   def isItemAvailable( currentBooking:  Booking,
                        requestQuantity: Int,
                        bookingList:     Buffer[Booking],
                        allItemsList:    Buffer[Item]
   ): Boolean =
      val reservedQuantity =
         this.rentedQntByDateRange( currentBooking.bookingStartTime,
                                    currentBooking.bookingEndTime,
                                    bookingList,
                                    allItemsList
         )

      this.ownedQuantity - this.lostQuantity - reservedQuantity - requestQuantity >= 0

   /** Add a new comment */
   def addComment( newCmt:       String,
                   allItemsList: Buffer[Item] = inventory,
                   dataFilePath: String = prodDataFilePath
   ) =
      this.comments += newCmt
      // update an item list in an external file
      writeItemData( allItemsList, dataFilePath )

   /** Add an incident */
   def reportIncident( newIncident:  Incident,
                       allItemsList: Buffer[Item] = inventory,
                       dataFilePath: String = prodDataFilePath
   ) =
      if newIncident.affectedQuantity <= 0 then throw MissingData( "Item quantity cannot be zero" )
      else
         this.incidents += newIncident
         if newIncident.incidentType == IncidentType.Loss
         then lostQuantity += newIncident.affectedQuantity

         // update an item list in an external file
         writeItemData( allItemsList, dataFilePath )

   /** Deactivate an item and hide it from the UI */
   def hideItem() =
      this.status = ItemStatus.Inactive

   /** Retrieve children items from all levels */
   def withAllChildren( allItemsList: Buffer[Item] = inventory ): Set[Item] =
      if this.children.isEmpty then Set[Item]( this )
      else
         val oneLevel = this.getChildrenDetails( allItemsList ) + this
         oneLevel ++ this
            .getChildrenDetails( allItemsList )
            .flatMap( c => c.withAllChildren( allItemsList ) )

   /** Get the earliest return date */
   def getEarliestReturnDate( bookingList:  Buffer[Booking] = bookings,
                              allItemsList: Buffer[Item] = inventory
   ): LocalDateTime =
      overlapBookingList( this.itemId,
                          LocalDateTime.now,
                          LocalDateTime.now.plusHours( 1 ),
                          bookingList,
                          allItemsList
      )
         .map( _.bookingEndTime )
         .min

   /** Get a list of incidents happened within a time interval <br>
     * - If start and end dates are null, return all incidents <br>
     * - If start date is null, return incidents up to end date <br>
     * - If end date is null, return incidents from start date <br>
     * - If both start and end dates are not null, return incidents between start and end dates <br>
     */
   def incidentsByDates( start: LocalDate, end: LocalDate ): Buffer[Incident] =
      if start == null && end == null then this.incidents
      else if start != null && end == null then
         this.incidents.filter( inc =>
            inc.incidentDate.isAfter( start ) || inc.incidentDate.isEqual( start )
         )
      else if start == null && end != null then
         this.incidents.filter( inc =>
            inc.incidentDate.isBefore( end ) || inc.incidentDate.isEqual( end )
         )
      else
         this.incidents.filter( inc =>
            ( inc.incidentDate.isAfter( start )
               || inc.incidentDate.isEqual( start ) )
               && ( inc.incidentDate.isBefore( end )
                  || inc.incidentDate.isEqual( end ) )
         )

   /** Calculate expense generated from incidents within a time interval */
   def totalExpense( targetType: IncidentType, start: LocalDate, end: LocalDate ): Double =
      this
         .incidentsByDates( start, end )
         .filter( _.incidentType == targetType )
         .map( _.repairCost )
         .sum

   /** Calculate income generated from an item within a time interval */
   def totalIncome( start:       LocalDate,
                    end:         LocalDate,
                    bookingList: Buffer[Booking] = bookings,
                    allItemsList: Buffer[Item] = inventory
   ): Double =
      bookingListByDates( start, end, bookingList )
         .map( b =>
            val bookingItems = b.items.filter( _.itemId == this.itemId )
            val duration = BookingDuration( b.bookingStartTime, b.bookingEndTime )
            calculateRentalCost( duration, bookingItems, allItemsList )
         )
         .sum

   /** Calculate demand for an item within a time interval */
   def totalDemand( start:       LocalDate,
                    end:         LocalDate,
                    bookingList: Buffer[Booking] = bookings
   ): Int =
      bookingListByDates( start, end, bookingList )
         .flatMap( _.items.filter( _.itemId == this.itemId ) )
         .map( _.rentQuantity )
         .sum
