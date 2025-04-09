package logic

import com.aventrix.jnanoid.jnanoid.NanoIdUtils.*
import logic.Booking.areDateRangeOverlapped
import logic.RentalStore.{bookings, inventory, updateItemsDataSource}
import upickle.default.*

import java.time.LocalDateTime
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
                 var incidents:              Set[Incident] = Set.empty,
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

   /** method to retrieve children items details */
   def getChildrenDetails( allItemsList: Buffer[Item] = inventory ): Set[Item] =
      this.children.flatMap( c => allItemsList.filter( _.itemId == c ) )

   def availableQuantity: Int =
      ownedQuantity - lostQuantity

   def hasEnoughQuantity( existingRentedQuantity: Int, requestQuantity: Int ): Boolean =
      this.ownedQuantity - this.lostQuantity - existingRentedQuantity - requestQuantity >= 0

   def reserve( quantity: Int ) =
      rentedQuantity += quantity

   /** decrease rented quantity to increase available quantity */
   def free( quantity: Int ) =
      rentedQuantity -= quantity

   /** method to add a new comment to an item */
   def addComment( newCmt: String ) =
      val newCmtFormatted = newCmt.trim
      // check that comment is not null
      if newCmtFormatted.nonEmpty then
         this.comments += newCmtFormatted

         // update items list in external file
         updateItemsDataSource()
      else throw MissingData( "Comment can't be empty" )

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

   /** get item availability at the current time */
   def rentedQuantityNow ( bookingList: Buffer[Booking] = bookings): Int =

      // get a list of existing currentbooking that contains the target item
      val relevantBookings: Buffer[Booking] =
         bookingList.filter( b => b.outstandingItems.map( _.itemId ).contains( this.itemId ) )

      // get a list of currentbooking that contains the target item and is happening at current time
      val overlapBookings: Buffer[Booking] = relevantBookings.filter( b =>
         areDateRangeOverlapped( b.bookingStartTime,
                                 b.bookingEndTime,
                                 LocalDateTime.now,
                                 LocalDateTime.now.plusHours(1),
         )
      )

      // get the total rented quantity of the target item from these bookings
      overlapBookings
         .flatMap( b => b.items.filter( si => si.itemId == this.itemId ) )
         .map( _.rentQuantity )
         .sum

   /** get the earliest return date */
   def getEarliestReturnDate(bookingList: Buffer[Booking] = bookings): LocalDateTime =
   
      // get a list of existing currentbooking that contains the target item
      val relevantBookings: Buffer[Booking] =
         bookingList.filter( b => b.outstandingItems.map( _.itemId ).contains( this.itemId ) )

      // get a list of currentbooking that contains the target item and is happening at current time
      val overlapBookings: Buffer[Booking] = relevantBookings.filter( b =>
         areDateRangeOverlapped( b.bookingStartTime,
                                 b.bookingEndTime,
                                 LocalDateTime.now,
                                 LocalDateTime.now.plusHours(1),
         )
      )
      
      // get the minimum of return dates of the overlapping bookings
      overlapBookings.map(_.bookingEndTime).min