package logic

import com.aventrix.jnanoid.jnanoid.NanoIdUtils.*
import logic.RentalStore.{ownedItems, updateItemsDataSource}
import upickle.default.*

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
                 private var lostQuantity:   Int = 0,
                 private var rentedQuantity: Int = 0,
                 var status:                 ItemStatus = ItemStatus.Active
) derives ReadWriter:

   def addChildren( childItemId: String ) =
      children += childItemId

   def deleteChildren( childItemId: String ) =
      children -= childItemId

   /** method to retrieve children items details */
   def getChildrenDetails( allItemsList: Buffer[Item] = ownedItems ): Set[Item] =
      this.children.flatMap( c => allItemsList.filter( _.itemId == c ) )

   def availableQuantity: Int =
      ownedQuantity - lostQuantity - rentedQuantity

   def hasEnoughQuantity( requestQuantity: Int ): Boolean =
      this.availableQuantity - requestQuantity >= 0

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
   def reportIncident( newIncident: Incident ) =
      this.incidents += newIncident
      if newIncident.incidentType == IncidentType.Loss
      then lostQuantity += 1

      // update items list in external file
      updateItemsDataSource()

   def hideItem() =
      this.status = ItemStatus.Inactive

   /** retrieve children items from all levels */
   def withAllChildren( allItemsList: Buffer[Item] = ownedItems ): Set[Item] =
      if this.children.isEmpty then Set[Item]( this )
      else
         val oneLevel = this.getChildrenDetails( allItemsList ) + this
         oneLevel ++ this
            .getChildrenDetails( allItemsList )
            .flatMap( c => c.withAllChildren( allItemsList ) )