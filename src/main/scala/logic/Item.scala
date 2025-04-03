package logic

import com.aventrix.jnanoid.jnanoid.NanoIdUtils.*
import logic.RentalStore.{getAllItems, updateItemsDataSource}
import upickle.default.*

import scala.collection.mutable.{Set, SortedSet}

@upickle.implicits.serializeDefaults( true )
case class Item( name:               String,
                 desc:               String,
                 ownedQuantity:      Int,
                 category:           String,
                 ratePerHour:        Double,
                 ratePerDay:         Double,
                 ratePerWeek:        Double,
                 itemId:             String = randomNanoId(),
                 incidents:          Set[Incident] = Set.empty,
                 children:           Set[String] = Set.empty,
                 var images:         String = "",
                 comments:           SortedSet[String] = SortedSet.empty,
                 var lostQuantity:   Int = 0,
                 var rentedQuantity: Int = 0
) derives ReadWriter:

   images = s"${this.name}.jpg"

   def addChildren( childItemId: String ) =
      children += childItemId

   def deleteChildren( childItemId: String ) =
      children -= childItemId

   /** method to retrieve children items details */
   def getChildrenDetails =
      this.children.flatMap( c => getAllItems.filter( _.itemId == c ) )

   def availableQuantity: Int =
      ownedQuantity - lostQuantity - rentedQuantity

   def hasEnoughQuantity( requestQuantity: Int ): Boolean =
      this.availableQuantity - requestQuantity >= 0

   def reserve( quantity: Int ) =
      rentedQuantity += quantity

   //   increase quantity when item is returned
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