package logic

import com.aventrix.jnanoid.jnanoid.NanoIdUtils.*
import logic.RentalStore.*
import logic.SavedItems.calculateRentalCost
import logic.enums.{IncidentType, ItemStatus}
import logic.util.{BookingDuration, MissingData}
import upickle.default.*

import java.time.{LocalDate, LocalDateTime}
import scala.collection.mutable
import scala.collection.mutable.{Buffer, SortedSet}

@upickle.implicits.serializeDefaults( true )
case class Item( name:                       String,
                 desc:                       String,
                 ownedQuantity:              Int,
                 category:                   String,
                 ratePerHour:                Double,
                 ratePerDay:                 Double,
                 ratePerWeek:                Double,
                 itemId:                     String = randomNanoId(),
                 var incidents:              Buffer[Incident] = Buffer(),
                 var children:               Set[String] = Set.empty,
                 var image:                  String = "",
                 comments:                   SortedSet[String] = SortedSet.empty,
                 var lostQuantity:           Int = 0,
                 private var rentedQuantity: Int = 0,
                 var status:                 ItemStatus = ItemStatus.Active
) derives ReadWriter:

   def addChildren( childItemId: String ) =
      children += childItemId

   def deleteChildren( childItemId: String ) =
      children -= childItemId

   /** retrieve children items details */
   def getChildrenDetails( allItemsList: Buffer[Item] = inventory ): Set[Item] =
      this.children.flatMap( c => allItemsList.filter( _.itemId == c ) )

   def availableQuantity: Int =
      ownedQuantity - lostQuantity
      
   def reserve( quantity: Int ) =
      rentedQuantity += quantity

   /** decrease rented quantity to increase available quantity */
   def free( quantity: Int ) =
      rentedQuantity -= quantity

   /** check if this item has enough quantity to be rented out */
   def hasEnoughQuantity( existingRentedQuantity: Int, requestQuantity: Int ): Boolean =
      this.ownedQuantity - this.lostQuantity - existingRentedQuantity - requestQuantity >= 0

   /** get item quantity that is rented out in a given time interval */
   def rentedQntByDateRange( start:       LocalDateTime,
                             end:         LocalDateTime,
                             bookingList: Buffer[Booking] = bookings
   ): Int =
      val overlapBookings =
         overlapBookingList( this.itemId, start, end )

      overlapBookings
         .flatMap( b => b.items.filter( si => si.itemId == this.itemId ) )
         .map( _.rentQuantity )
         .sum

   /** check if item has enough quantity to be rented out in a specific booking */
   def isItemAvailable( currentBooking :  Booking,
                        requestQuantity: Int,
                        bookingList    :     Buffer[Booking] = bookings
   ): Boolean =
      val reservedQuantity =
         this.rentedQntByDateRange(currentBooking.bookingStartTime, currentBooking.bookingEndTime)

      this.hasEnoughQuantity( reservedQuantity, requestQuantity )


   /** method to add a new comment to an item */
   def addComment( newCmt: String ) =
      this.comments += newCmt
      // update items list in external file
      updateItemsDataSource()

   /** method to add incident to an item */
   def reportIncident( newIncident: Incident, quantity: Int ) =
      if quantity <= 0 then throw MissingData( "Item quantity cannot be zero" )
      else
         this.incidents += newIncident
         if newIncident.incidentType == IncidentType.Loss
         then lostQuantity += quantity

         // update items list in external file
         updateItemsDataSource()

   def hideItem() =
      this.status = ItemStatus.Inactive

   /** retrieve children items from all levels */
   def withAllChildren( allItemsList: Buffer[Item] = inventory ): Set[Item] =
      if this.children.isEmpty then Set[Item]( this )
      else
         val oneLevel = this.getChildrenDetails( allItemsList ) + this
         oneLevel ++ this
            .getChildrenDetails( allItemsList )
            .flatMap( c => c.withAllChildren( allItemsList ) )

   /** get the earliest return date */
   def getEarliestReturnDate( bookingList: Buffer[Booking] = bookings ): LocalDateTime =
      val overlapBookings =
         overlapBookingList( this.itemId, LocalDateTime.now, LocalDateTime.now.plusHours( 1 ) )

      // get the minimum of return dates of the overlapping bookings
      overlapBookings.map( _.bookingEndTime ).min

   /** get a list of incidents happened within a time interval */
   def incidentsByDates( start: LocalDate, end: LocalDate ): Buffer[Incident] =
      if start == null || end == null then this.incidents
      else
         this.incidents.filter( inc =>
            ( inc.incidentDate.isAfter( start )
               || inc.incidentDate.isEqual( start ) )
               && ( inc.incidentDate.isBefore( end )
                  || inc.incidentDate.isEqual( end ) )
         )

   /** calculate expense generated from incidents within a time interval */
   def totalExpense( targetType: IncidentType, start: LocalDate, end: LocalDate ): Double =
      val targetIncidents =
         this.incidentsByDates( start, end ).filter( _.incidentType == targetType )
      if start == null || end == null then targetIncidents.map( _.repairCost ).sum
      else targetIncidents.map( _.repairCost ).sum

   /** calculate income generated from an item */
   def totalIncome( start: LocalDate, end: LocalDate ): Double =
      val filteredBookings = bookingListByDates( start, end )
      val includedBookings =
         filteredBookings.filter( _.items.map( _.itemId ).contains( this.itemId ) )

      includedBookings
         .map( b =>
            val targetItem = b.items.filter( _.itemId == this.itemId )
            val duration = BookingDuration( b.bookingStartTime, b.bookingEndTime )
            calculateRentalCost( duration, targetItem )
         )
         .sum

   /** calculate demand for an item within a time interval */
